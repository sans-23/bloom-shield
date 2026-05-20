package com.bloomshield.metrics;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class MetricsService {

        private final MeterRegistry meterRegistry;

        //counters
        private final Counter cacheHitsCounter;
        private final Counter cacheMissesCounter;
        private final Counter dbHitsCounter;
        private final Counter bloomFilterFalsePositiveCounter;
        private final Counter validRequestCounter;
        private final Counter invalidRequestCounter;

        //timers
        private final Timer apiLatencyTimer; // (baseline)
        private final Timer apiV1LatencyTimer; // with cache
        private final Timer apiV2LatencyTimer; // with bloom filter
        private final Timer redisGetLatencyTimer; 
        private final Timer redisPutLatencyTimer;
        private final Timer dbQueryLatencyTimer;

        public MetricsService(MeterRegistry meterRegistry){
            this.meterRegistry = meterRegistry;

            this.cacheHitsCounter = Counter.builder("cache.hits.total")
                .description("Total number of cache hits")
                .register(meterRegistry);

            this.cacheMissesCounter = Counter.builder("cache.misses.total")
                .description("Total number of cache misses")
                .register(meterRegistry);

            this.dbHitsCounter = Counter.builder("db.hits.total")
                .description("Total number of queries hit the db")
                .register(meterRegistry);

            this.bloomFilterFalsePositiveCounter = Counter.builder("bloom.filter.false_positives.total")
                .description("Total false positives from Bloom filter (invalid user passed filter but missing from DB)")
                .register(meterRegistry);
                
            this.validRequestCounter =  Counter.builder("request.valid.total")
                .description("Total requests with valid users")
                .register(meterRegistry);   
            
            this.invalidRequestCounter = Counter.builder("request.invalid.total")
                .description("Total requests with invalid users")
                .register(meterRegistry);
                
            this.apiLatencyTimer = Timer.builder("request.latency")
                .description("Request latencynfor api - baseline")
                .tag("controller", "lookup")
                .register(meterRegistry);

            this.apiV1LatencyTimer = Timer.builder("request.latency")
                .description("Request latency for api/v1 - with cache")
                .tag("controller", "cached")
                .register(meterRegistry);

            this.apiV2LatencyTimer = Timer.builder("request.latency")
                .description("Request latency for /api/v2 - with cache + Bloom filter")
                .tag("controller", "filtered")
                .register(meterRegistry);

            this.redisGetLatencyTimer = Timer.builder("redis.get.latency")
                .description("Redis get latency")
                .register(meterRegistry);

            this.redisPutLatencyTimer = Timer.builder("redis.put.latency")
                .description("Redis put latency")
                .register(meterRegistry);

            this.dbQueryLatencyTimer = Timer.builder("db.query.latency")
            .description("DB Query latency")
            .register(meterRegistry);
            
            Gauge.builder("cache.hit.rate", this, value -> calculateCacheHitRate())
                .description("Cache hit rate")
                    .register(meterRegistry);

        }

        public void recordCacheHit(){
            cacheHitsCounter.increment();
        }

        public void recordCacheMiss(){
            cacheMissesCounter.increment();
        }

        public double getCacheHitCount(){
            return cacheHitsCounter.count();
        }

        public double getCacheMissCount(){
            return cacheMissesCounter.count();
        }

        public double calculateCacheHitRate(){
            double hits = getCacheHitCount();
            double miss = getCacheMissCount();
            double total = hits+miss;

            if(total==0.0) return 0.0;

            return (hits/total)*100.0;
        }

        public void recordDbHit(){
            dbHitsCounter.increment();
        }

        public double getDbHitCount(){
            return dbHitsCounter.count();
        }
        
        public void recordBloomFilterFalsePositive(){
            bloomFilterFalsePositiveCounter.increment();
        }

        public void recordValidRequest(){
            validRequestCounter.increment();
        }

        public void recordInvalidRequest(){
            invalidRequestCounter.increment();
        }

        public double getBloomFilterFalsePositiveCount(){
            return bloomFilterFalsePositiveCounter.count();
        }

        public double getValidRequestCount(){
            return validRequestCounter.count();
        }

        public double getInvalidRequestCount(){
            return invalidRequestCounter.count();
        }

        public void recordApiLatency(long nanoSeconds){
            apiLatencyTimer.record(java.time.Duration.ofNanos(nanoSeconds));
        }

        public void recordApiV1Latency(long nanoSeconds) {
            apiV1LatencyTimer.record(java.time.Duration.ofNanos(nanoSeconds));
        }

        public void recordApiV2Latency(long nanoSeconds) {
            apiV2LatencyTimer.record(java.time.Duration.ofNanos(nanoSeconds));
        }

        public void recordRedisGetLatency(long nanoSeconds){
            redisGetLatencyTimer.record(java.time.Duration.ofNanos(nanoSeconds));
        }

        public void recordRedisPutLatency(long nanoSeconds){
            redisPutLatencyTimer.record(java.time.Duration.ofNanos(nanoSeconds));
        }

        public void recordDbQueryLatency(long nanoSeconds){
            dbQueryLatencyTimer.record(java.time.Duration.ofNanos(nanoSeconds));
        }
}
