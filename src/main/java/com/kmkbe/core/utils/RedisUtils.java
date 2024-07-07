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
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOperations;
    private final ListOperations<String, Object> listOperations;

    public void putValue(String key, Object value) {
        valueOperations.set(key, value);
    }

    public Object getValue(String key) {
        return valueOperations.get(key);
    }

    public void setExpire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    public List<Object> getValueFromList(String key) {
        return listOperations.range(key, 0, -1);
    }

    public void addValueToList(String key, Object value) {
        listOperations.rightPush(key, value);
    }
}
