package com.quality.platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public void setValue(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void hset(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public String hget(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return value == null ? null : value.toString();
    }
}
