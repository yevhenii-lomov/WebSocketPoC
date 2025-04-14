package com.poc.GPSKafkaSimulator.model;

import lombok.Data;

@Data
public class GPSVehicleCycleEvents {
    private Integer startVehicleIndex;
    private Integer endVehicleIndex;
    private Integer generationIntervalInSeconds;
    private Integer maxCycles;
}
