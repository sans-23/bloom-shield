package com.bloomshield.cache;

import java.time.Duration;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.bloomshield.metrics.MetricsService;

@Component
public class RedisCache {
    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    @Autowired
    private MetricsService metricsService;

    public RedisCache(StringRedisTemplate redisTemplate, @Value("${bloomshield.cache.ttl-seconds}") int ttlseconds){
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(ttlseconds);
    }

    public String get(String key){
        long startTime = System.nanoTime();
        String value = redisTemplate.opsForValue().get(key);
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        metricsService.recordRedisGetLatency(timeElapsed);
        return value;
    }

    public void put(String key, String value){
        long startTime = System.nanoTime();
        redisTemplate.opsForValue().set(key, value, ttl);
        long endTime = System.nanoTime();
        long timeElapsed = endTime-startTime;
        metricsService.recordRedisPutLatency(timeElapsed);
    }

    public void delete(String key){
        redisTemplate.delete(key);
    }

    public void flush(){
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
    }
}
