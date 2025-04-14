package com.poc.GPSDistributor.model;

import lombok.Data;

@Data
public class GPSEvent {
    private String vehicleId;
    private String payload;
    private String timeStamp;
}
