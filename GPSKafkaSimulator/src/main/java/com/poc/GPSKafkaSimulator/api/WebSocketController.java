package com.poc.GPSKafkaSimulator.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.poc.GPSKafkaSimulator.model.GPSVehicleCycleEvents;
import com.poc.GPSKafkaSimulator.model.GPSVehicleEvent;
import com.poc.GPSKafkaSimulator.service.GPSEmulationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
public class WebSocketController {

    private final GPSEmulationService emulationService;

    /**
     * Endpoint to publish a single GPS Event
     * @param event The GPSVehicleEvent object
     * @return ResponseEntity with appropriate http status code.
     */
    @PostMapping("one")
    public ResponseEntity<String> single(@RequestBody(required=false) GPSVehicleEvent event) {
        try {
            emulationService.publishGPSEvent(event);
            return ResponseEntity.ok().body("GPS Event published successfully");
        } catch (JsonProcessingException ex) {
            log.error("Error while processing GPSVehicleEvent: {}", event, ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Endpoint to publish multiple GPS Events for Vehicles
     * Messages start StartVehicleIndex to EndVehicleIndex
     * will be generated MaxCycles times with interval GenerationIntervalInSeconds
     * e.g.
     * {
     *     "startVehicleIndex" : 1,
     *     "endVehicleIndex" : 11,
     *     "generationIntervalInSeconds" : 10,
     *     "maxCycles" : 8
     * }
     * Generate 11 messages(1-11 vehicle ids) every 10 seconds, 8 times
     * @param event The GPSVehicleCycleEvents object
     * @return ResponseEntity with appropriate http status code.
     */
    @PostMapping("simulation")
    public ResponseEntity<String> busses(@RequestBody GPSVehicleCycleEvents event) {
        emulationService.publishGPSEventsForVehicles(
                event.getStartVehicleIndex(),
                event.getEndVehicleIndex(),
                event.getGenerationIntervalInSeconds(),
                event.getMaxCycles());
        return ResponseEntity.ok().body("GPS Events for Vehicles published successfully");
    }
}