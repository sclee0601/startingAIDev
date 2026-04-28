package com.app.stock.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AiSummaryService {

    private final RedisTemplate<String, Object> redisTemplate;

    public AiSummaryService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getSummary(String cacheKey, String portfolioSummary) {
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof String) {
            return (String) cached;
        }

        String generated = "AI Summary: " + portfolioSummary;
        redisTemplate.opsForValue().set(cacheKey, generated, Duration.ofMinutes(10));
        return generated;
    }
}
