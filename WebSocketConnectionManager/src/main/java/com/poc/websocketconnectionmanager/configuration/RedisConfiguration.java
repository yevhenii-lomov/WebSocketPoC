package com.poc.websocketconnectionmanager.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    private Environment environment;

    public RedisConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisConnectionFactory factory =
                new LettuceConnectionFactory(
                        environment.getProperty("redis.host"),
                        Integer.parseInt(environment.getProperty("redis.port")));
        return factory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}

