package com.poc.websocketconnectionmanager.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RedisRepository {

    private RedisTemplate<String, String> redisTemplate;
    private SetOperations<String, String> setOperations;

    @Autowired
    public RedisRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        setOperations = redisTemplate.opsForSet();
    }

    public void addMembers(String key, String value) {
        setOperations.add(key, value);
    }

    public void addMembers(String key, String[] values) {
        setOperations.add(key, values);
    }

    public Set<String> getMembers(String key) {
        return setOperations.members(key);
    }

    public void removeMember(String key, String value) {
        setOperations.remove(key, value);
    }

    public void removeCollection(String key) {
        redisTemplate.delete(key);
    }
}
