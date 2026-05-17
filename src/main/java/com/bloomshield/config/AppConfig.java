package com.bloomshield.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

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
}
