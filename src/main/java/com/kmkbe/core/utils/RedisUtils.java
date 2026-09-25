package com.kmkbe.core.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtils<T> {
    private static final long SCAN_COUNT = 1_000L;

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
        return scanKeys("*");
    }

    public List<T> getAllEntirePairs() {
        List<T> result = new ArrayList<>();
        for (String key : scanKeys("*")) {
            result.add(valueOperations.get(key));
        }

        return result;
    }

    private List<String> scanKeys(String pattern) {
        List<String> result = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(SCAN_COUNT)
                .build();

        List<String> scannedKeys = redisTemplate.execute((RedisCallback<List<String>>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return result;
        });
        return scannedKeys != null ? scannedKeys : result;
    }
}
