package com.poc.GPSKafkaSimulator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.GPSKafkaSimulator.model.GPSVehicleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GPSEmulationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PAYLOAD = "12345678".repeat(100);

    public GPSEmulationService(Environment environment, KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = environment.getProperty("kafka.topic");
    }

    public void publishGPSEvent(GPSVehicleEvent gpsEvent) throws JsonProcessingException {
        kafkaTemplate.send(topicName, objectMapper.writeValueAsString(gpsEvent));
    }

    public void publishGPSEventsForVehicles(int startVehicleIndex, int endVehicleIndex, int generationInterval, int maxCycles) {
        log.info("Begin GPS event generation: startVehicleIndex={}, endVehicleIndex={}, interval={}s",
                startVehicleIndex, endVehicleIndex, generationInterval);

        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < maxCycles; i++) {
                    StopWatch watch = new StopWatch();
                    watch.start();

                    for (int j = startVehicleIndex; j < endVehicleIndex; j++) {
                        GPSVehicleEvent newEvent = new GPSVehicleEvent();
                        newEvent.setVehicleId(String.valueOf(j));
                        newEvent.setPayload(PAYLOAD);
                        newEvent.setTimeStamp(Instant.now().toString());

                        String message = objectMapper.writeValueAsString(newEvent);
                        kafkaTemplate.send(topicName, String.valueOf(j), message);
                    }

                    watch.stop();
                    log.info("Cycle {} completed in {} ms", i, watch.getTotalTimeMillis());
                    Thread.sleep(generationInterval * 1000L);
                }
            } catch (Exception e) {
                log.error("Error during GPS event generation: ", e);
            } finally {
                log.info("GPS event generation finished.");
            }
        }).exceptionally(ex -> {
            log.error("Async error during GPS event generation: {}", ex.getLocalizedMessage());
            return null;
        });
    }
}