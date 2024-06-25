package com.kmkbe.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableCaching
public class AppCacheConfig {
    public static final String CSUL_CACHE_NAME = "csul_jwt_key";


    @Bean
    public CacheManager cacheManager() {
        List<CaffeineCache> caches = new ArrayList<>();
        caches.add(buildCache(CSUL_CACHE_NAME, Duration.of(60, ChronoUnit.MINUTES)));
        //caches.add(buildCache("", Duration.of(60, ChronoUnit.MINUTES)));

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);

        return cacheManager;
    }

    private CaffeineCache buildCache(String name, Duration expireAfterWriteDuration) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .recordStats()
                        .expireAfterWrite(expireAfterWriteDuration)
                        .build()
        );
    }
}
