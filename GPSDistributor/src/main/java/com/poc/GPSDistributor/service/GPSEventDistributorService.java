package com.poc.GPSDistributor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.GPSDistributor.model.GPSEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GetConnectionRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GoneException;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GPSEventDistributorService {

    private final RedisService connectionRedisService;

    /*
    The asynchronous web client is a crucial component of the system when used to perform HTTP requests involving I/O operations.

    Unlike synchronous HTTP clients, which block the calling thread until a response is received,
    an asynchronous web client allows the system to initiate non-blocking requests, freeing up threads and improving throughput,
    scalability, and responsiveness under high load.

    This makes the async client especially critical in high-concurrency environments,
    where efficient resource utilization and fast I/O are essential to system performance and stability.
    */
    private final ApiGatewayManagementApiAsyncClient awsWebClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public GPSEventDistributorService(RedisService connectionRedisService,
                                      ApiGatewayManagementApiAsyncClient awsWebClient) {
        this.connectionRedisService = connectionRedisService;
        this.awsWebClient = awsWebClient;
    }

    public CompletableFuture<Void> distributeGpsToClients(String vehicleId, GPSEvent event) throws JsonProcessingException {

        // TODO : Getting collection from Redis should be optimized using keyspace notification or by implementing pub-sub
        //  logic from Websocket Connection Manager to GPS Distributor regarding collection change.
        Set<String> connectionIds = connectionRedisService.GetAllConnectionIdsByVehicle(vehicleId);
        if (connectionIds.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        String responsePayload = objectMapper.writeValueAsString(event);

        StopWatch watch = new StopWatch();
        watch.start();

        List<CompletableFuture<PostToConnectionResponse>> futures = connectionIds.stream()
                .map(connId -> sendPost(connId, vehicleId, responsePayload,false))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((res, ex) -> {
                    watch.stop();
                    log.info("Execution time: {} ms, vehicleId: {}, success: {}, failed: {}",
                            watch.getTotalTimeMillis(), vehicleId);
                });
    }

    private CompletableFuture<PostToConnectionResponse> sendPost(
            String connectionId, String vehicleId, String payload, boolean skipGone) {

        PostToConnectionRequest request = PostToConnectionRequest.builder()
                .connectionId(connectionId)
                .data(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                .build();

        return awsWebClient.postToConnection(request)
                .thenApply(response -> response)
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause();
                    if (cause instanceof GoneException && !skipGone) {
                        retryPost(connectionId, vehicleId, payload);
                    } else {
                        if (!(cause instanceof GoneException)) {
                            log.error("Post failed: ", ex);
                        }
                    }
                    return null;
                });
    }

    /*
        - Transient fault handling, GoneException occurs often, e.g. user reconnection, at least one retry should happen

        TODO: Additional transient fault should be done in production version
        - LimitexceededException should be handled properly.
        https://repost.aws/questions/QUCxtPzI1JROGbvBUIVN63_g/unknown-reason-for-api-gateway-websocket-limitexceededexception
        It usually throws when the client can not receive the message due to the full buffer.
        The back-end should slow down the message rate to the client.
    */
    private void retryPost(String connectionId, String vehicleId, String payload) {
        GetConnectionRequest getStatusRequest = GetConnectionRequest.builder()
                .connectionId(connectionId)
                .build();

        // if GoneException occurs again, remove connectionId from the distribution list
        awsWebClient.getConnection(getStatusRequest)
                .thenAccept(response -> sendPost(connectionId, vehicleId, payload, true))
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof GoneException) {
                        connectionRedisService.removeMember(vehicleId, connectionId);
                    } else {
                        log.error("Retry failed: ", ex);
                    }
                    return null;
                });
    }
}