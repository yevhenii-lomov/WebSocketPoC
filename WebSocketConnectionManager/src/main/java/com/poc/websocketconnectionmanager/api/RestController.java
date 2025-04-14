package com.poc.websocketconnectionmanager.api;
import com.poc.websocketconnectionmanager.model.AWSGatewayRequest;
import com.poc.websocketconnectionmanager.model.ResponseStub;
import com.poc.websocketconnectionmanager.model.SubscribeRequest;
import com.poc.websocketconnectionmanager.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@org.springframework.web.bind.annotation.RestController
public class RestController {

    private RedisService connectionRedisService;

    public RestController(
            RedisService connectionRedisService) {
        this.connectionRedisService = connectionRedisService;
    }

    // Health endpoint is required if the Service(instead task) is used for ECS Fargate
    @GetMapping("/health")
    String health() {

        return "OK";
    }

    // It is called when a new user is connected to WebSockets
    @ResponseBody
    @PostMapping("/connect")
    void connect(@RequestBody (required=false) AWSGatewayRequest request) {
        /* authentication/authorization could be added here.
        connection counts successful if method return 200 OK
        if method returns 4**, 5** the WebSocket connection won't be established
        */
    }

    // It is called when the user disconnected from WebSockets
    @ResponseBody
    @PostMapping("/disconnect")
    void disconnect(@RequestBody (required=false) AWSGatewayRequest model) {
        connectionRedisService.RemoveUserSubscription(model.getConnectionId());
    }

    // It is called when message route can not be found
    @ResponseBody
    @PostMapping("/default")
    ResponseStub defaultMap(@RequestBody (required=false) AWSGatewayRequest model) {
        return new ResponseStub("default routes is used");
    }

    // It is called when user provide the following message {"message":"subscribe", "vehicles" : "1"}
    @ResponseBody
    @PostMapping("/subscribe")
    void subscribe(@RequestBody (required=false) SubscribeRequest request) {
        connectionRedisService.AddUserSubscription(request.getConnectionId(), request.GetVehiclesToTrack());
    }
}
