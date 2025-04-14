package com.poc.GPSKafkaSimulator.model;

import lombok.Data;

@Data
public class GPSVehicleEvent {
    private String vehicleId;
    private String payload;
    private String timeStamp;
}
