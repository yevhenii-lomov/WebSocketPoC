package com.poc.websocketconnectionmanager.model;
import lombok.Data;

@Data
public class AWSGatewayRequest {

    private String connectionId;
    private String extendedRequestId;
    private String routeKey;
    private String eventType;
    private String connectedAt;

    private String payload;
}