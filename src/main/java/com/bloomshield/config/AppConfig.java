package com.bloomshield.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.bloomshield.filter.Filter;
import com.bloomshield.filter.BloomFilter;
import com.bloomshield.filter.CountingBloomFilter;

@Configuration
public class AppConfig {
    
    @Bean
    LettuceConnectionFactory connectionFactory(){
        return new LettuceConnectionFactory();
    }

    @Bean
    public StringRedisTemplate redisTemplate(){
        StringRedisTemplate t = new StringRedisTemplate();
        t.setConnectionFactory(connectionFactory());
        return t;
    }

    @Bean
    public Filter bloomFilter(
            @Value("${bloomshield.filter.type:standard}") String type,
            @Value("${bloomshield.filter.bit-size:100000}") int bitSize,
            @Value("${bloomshield.filter.hash-count:3}") int hashCount) {
        
        if ("counting".equalsIgnoreCase(type)) {
            return new CountingBloomFilter(bitSize, hashCount);
        }
        return new BloomFilter(bitSize, hashCount);
    }
}
