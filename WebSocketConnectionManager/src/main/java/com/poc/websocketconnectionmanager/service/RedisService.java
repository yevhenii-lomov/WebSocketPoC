package com.poc.websocketconnectionmanager.service;

import com.poc.websocketconnectionmanager.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RedisService {

    private RedisRepository repository;
    private final String connectionsToVehiclesPrefix = "c";
    private final String vehiclesToConnectionPrefix = "v";
    private final String collectionPrefixFormat = "%s%s";

    @Autowired
    public RedisService(RedisRepository redisRepository) {
        this.repository = redisRepository;
    }

    public void AddUserSubscription(String connectionId, String[] vehicleIds) {

        // connectionIds to vehiclesIds
        repository.addMembers(
                String.format(collectionPrefixFormat,connectionsToVehiclesPrefix, connectionId), vehicleIds);

        // vehicleIds to connectionIds
        for (String vehicle : vehicleIds)
        {
            repository.addMembers(
                    String.format(collectionPrefixFormat,vehiclesToConnectionPrefix, vehicle), connectionId);
        }
    }

    public void RemoveUserSubscription(String connectionId) {
        Set<String> vehiclesToRemove = repository
                .getMembers(String.format(collectionPrefixFormat,connectionsToVehiclesPrefix, connectionId));

        for (String vehicle : vehiclesToRemove)
        {
            repository
                    .removeMember(String.format(collectionPrefixFormat,vehiclesToConnectionPrefix, vehicle), connectionId);
        }

        repository
                .removeCollection(String.format(collectionPrefixFormat,connectionsToVehiclesPrefix, connectionId));
    }
}
