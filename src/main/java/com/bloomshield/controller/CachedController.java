package com.bloomshield.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloomshield.cache.RedisCache;
import com.bloomshield.metrics.MetricsService;
import com.bloomshield.service.LookupService;

@RestController
@RequestMapping("/api/v1")
public class CachedController {
    private final LookupService lookupService;
    private final RedisCache redisCache;

    @Autowired
    private MetricsService metricsService;


    public CachedController(LookupService lookupService, RedisCache redisCache){
        this.lookupService = lookupService;
        this.redisCache = redisCache;
    }

    @GetMapping("/user/{userName}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable("userName") String userName) {
        long startTime = System.nanoTime();
        
        String cachedValue = redisCache.get(userName);
        if (cachedValue != null) {
            // cache hit
            metricsService.recordCacheHit();

            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            metricsService.recordApiV1Latency(timeElapsed);
            metricsService.recordValidRequest();
            return ResponseEntity.ok(Map.of("status", "user found in cache", "time_elapsed", timeElapsed));
        }

        metricsService.recordCacheMiss();
        
        boolean success = lookupService.checkIfUserExits(userName);
        metricsService.recordDbHit();
        if (!success) {
            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            metricsService.recordApiV1Latency(timeElapsed);
            metricsService.recordInvalidRequest();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "user not found in db", "time_elapsed", timeElapsed));
        }
        
        // Cache the successful DB lookup!
        redisCache.put(userName, "exists");
        
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        metricsService.recordApiV1Latency(timeElapsed);
        metricsService.recordValidRequest();
        return ResponseEntity.ok(Map.of("status", "user found in db", "time_elapsed", timeElapsed));
    }
}
