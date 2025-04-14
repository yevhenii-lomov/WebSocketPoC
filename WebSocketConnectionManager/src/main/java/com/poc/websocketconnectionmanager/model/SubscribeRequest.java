package com.poc.websocketconnectionmanager.model;

public class SubscribeRequest extends AWSGatewayRequest {
    private String[] vehiclesToTrack;

    public String[] GetVehiclesToTrack()
    {
        if (vehiclesToTrack == null) {
            vehiclesToTrack = this.getPayload().split(",");
        }

        return vehiclesToTrack;
    }
}