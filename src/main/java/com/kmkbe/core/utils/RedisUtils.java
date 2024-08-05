package com.kmkbe.core.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    public void putValue(String key, T value, long timeout) {
        valueOperations.set(key, value, Duration.ofMillis(timeout));
    }

    @Nullable
    public T getValue(String key) {
        return valueOperations.get(key);
    }

    public T getValueAndExpire(String key, long timeout) {
        return valueOperations.getAndExpire(key, Duration.ofMillis(timeout));
    }

    public T getValueAndDelete(String key) {
        return valueOperations.getAndDelete(key);
    }

    public Boolean deleteKey(String key) {
        return redisTemplate.delete(key);
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

    public List<String> getAllKeys() {
        List<String> result = new ArrayList<>();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory != null) {
            Set<byte[]> keys = factory.getConnection().keyCommands().keys("*".getBytes());
            if (keys != null) {
                for (byte[] data : keys) {
                    result.add(new String(data, StandardCharsets.UTF_8));
                }
            }
        }

        return result;
    }

    public List<T> getAllEntirePairs() {
        List<T> result = new ArrayList<>();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory != null) {
            Set<byte[]> keys = factory.getConnection().keyCommands().keys("*".getBytes());
            if (keys != null) {
                for (byte[] data : keys) {
                    result.add(valueOperations.get(data));
                }
            }
        }

        return result;
    }
}
