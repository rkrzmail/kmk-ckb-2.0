package com.kmkbe.core.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtils<T> {

    private final RedisTemplate<String, T> redisTemplate;
    private final ValueOperations<String, T> valueOperations;
    private final ListOperations<String, T> listOperations;

    public void putValue(String key, T value) {
        valueOperations.set(key, value);
    }

    public T getValue(String key) {
        return valueOperations.get(key);
    }

    public void setExpire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    public List<T> getValueFromList(String key) {
        return listOperations.range(key, 0, -1);
    }

    public void addValueToList(String key, T value) {
        listOperations.rightPush(key, value);
    }
}
