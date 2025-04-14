package com.poc.GPSDistributor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;
import java.net.URI;
import java.time.Duration;

@Configuration
public class AWSWebClientConfiguration {

    private Environment environment;

    public AWSWebClientConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public ApiGatewayManagementApiAsyncClient apiGatewayManagementApiClient() {

       NettyNioAsyncHttpClient.Builder nettyClient = NettyNioAsyncHttpClient
                .builder()
                .connectionAcquisitionTimeout(
                        Duration.ofSeconds(Long.valueOf(environment.getProperty("CONNECTION_ACQUISITION_TIMEOUT_SECONDS"))))
                .maxPendingConnectionAcquires(
                        Integer.valueOf(environment.getProperty("MAX_PENDING_CONNECTION_ACQUIRES")))
                .connectionTimeout(
                        Duration.ofSeconds(Long.valueOf(environment.getProperty("CONNECTION_TIMEOUT_SECONDS"))))
                .maxConcurrency(Integer.valueOf(environment.getProperty("MAX_CONCURRENCY")));

        ApiGatewayManagementApiAsyncClient client =
                ApiGatewayManagementApiAsyncClient
                        .builder()
                        .httpClientBuilder(nettyClient)
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(environment.getProperty("gateway.url")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        return client;
    }
}