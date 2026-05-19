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
import com.bloomshield.filter.Filter;
import com.bloomshield.metrics.MetricsService;
import com.bloomshield.service.LookupService;

@RestController
@RequestMapping("/api/v2")
public class FilteredController {
    private final LookupService lookupService;
    private final RedisCache redisCache;
    private final Filter filter;

    @Autowired
    private MetricsService metricsService;

    public FilteredController(LookupService lookupService, RedisCache redisCache, Filter filter){
        this.lookupService = lookupService;
        this.redisCache = redisCache;
        this.filter = filter;
    }

    @GetMapping("/user/{userName}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable("userName") String userName) {
        long startTime = System.nanoTime();

        // before hitting cache and db, check with bloom filter
        boolean inBloomFilter = filter.mightContain(userName);

        if (!inBloomFilter) {
            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            metricsService.recordApiV2Latency(timeElapsed);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "user not found in bloom filter", "time_elapsed", timeElapsed));
        }
        
        String cachedValue = redisCache.get(userName);
        if (cachedValue != null) {
            metricsService.recordCacheHit();
            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            metricsService.recordApiV2Latency(timeElapsed);
            return ResponseEntity.ok(Map.of("status", "user found in cache", "time_elapsed", timeElapsed));
        }

        metricsService.recordCacheMiss();
        
        boolean success = lookupService.checkIfUserExits(userName);
        if (!success) {
            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            metricsService.recordApiV2Latency(timeElapsed);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "user not found in db", "time_elapsed", timeElapsed));
        }
        
        // Cache the successful DB lookup!
        redisCache.put(userName, "exists");
        
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        metricsService.recordApiV2Latency(timeElapsed);
        return ResponseEntity.ok(Map.of("status", "user found in db", "time_elapsed", timeElapsed));
    }
}
