package com.bloomshield.cache;

import java.time.Duration;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCache {
    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public RedisCache(StringRedisTemplate redisTemplate, @Value("${bloomshield.cache.ttl-seconds}") int ttlseconds){
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(ttlseconds);
    }

    public String get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public void put(String key, String value){
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public void delete(String key){
        redisTemplate.delete(key);
    }

    public void flush(){
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
    }
}
