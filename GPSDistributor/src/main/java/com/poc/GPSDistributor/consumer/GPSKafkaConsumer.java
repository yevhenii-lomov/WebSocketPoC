package com.poc.GPSDistributor.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.poc.GPSDistributor.model.GPSEvent;
import com.poc.GPSDistributor.service.GPSEventDistributorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GPSKafkaConsumer {

    private GPSEventDistributorService distributor;

    public GPSKafkaConsumer(GPSEventDistributorService distributor) {
        this.distributor = distributor;
    }

    /*
        10 separate concurrent kafka consumers are used. Which listen to 10 partitions simultaneously.
        Manual acknowledgment is used to control backpresuare from Kafka, without it the consumers will be overloaded
    */
    @KafkaListener(topics = "#{systemEnvironment['kafka.topic']}", groupId = "#{systemEnvironment['kafka.group']}")
    public void listensToGpsEvents(GPSEvent event,  Acknowledgment ack) {
        CompletableFuture.runAsync(() -> {
                    try {
                        processMessage(event);
                    } catch (Exception ex) {
                        log.error("error during processing " + ex.getMessage());
                    }
                })
                .thenRun(ack::acknowledge)
                .exceptionally(ex -> {
                    log.error("Failed to acknowledge Kafka message. Error: {}", ex.getMessage(), ex);
                    return null;
                });
    }

    private void processMessage(GPSEvent event) throws JsonProcessingException {
        distributor.distributeGpsToClients(event.getVehicleId(), event);
    }
}
