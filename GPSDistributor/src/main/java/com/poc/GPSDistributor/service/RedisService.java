package com.poc.GPSDistributor.service;

import com.poc.GPSDistributor.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RedisService {

    /*
        TODO: Redis collection should have TTL, unexpired collection should be avoided.
    */
    private RedisRepository redisRepository;
    private final String vehiclesToConnectionPrefix = "v";
    private final String collectionPrefixFormat = "%s%s";

    @Autowired
    public RedisService(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    public Set<String> GetAllConnectionIdsByVehicle(String vehicleId) {
        return redisRepository
                .getMembers(String.format(collectionPrefixFormat,vehiclesToConnectionPrefix, vehicleId));
    }

    public void removeMember(String vehicleId, String connectionId) {
        redisRepository
                .removeMember(String.format(collectionPrefixFormat,vehiclesToConnectionPrefix, vehicleId), connectionId);
    }
}
