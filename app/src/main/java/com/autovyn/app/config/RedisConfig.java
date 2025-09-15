package com.autovyn.app.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@ConditionalOnProperty(name = "spring.data.redis.repositories.enabled", havingValue = "true")
@EnableRedisRepositories
public class RedisConfig {
    // Redis configuration will only be enabled if explicitly enabled
}