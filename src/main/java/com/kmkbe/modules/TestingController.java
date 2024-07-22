package com.kmkbe.modules;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.customer.repository.OtpRepository;
import com.kmkbe.modules.customer.service.CustomerSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/testing")
public class TestingController {
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ListOperations<String, Object> listOperations;
    private final OtpRepository otpRepository;
    private final FileStorageService fileStorageService;
    private final CustomerSeederService seederService;

    @GetMapping("/get")
    public CommonResult<Object> get() {
        Cache cache = cacheManager.getCache("refreshToken");
        if (cache != null) {
            String res = cache.get("khesatoken", Object.class).toString();
            return new CommonResult<>().success(res, "ok");
        }
        return new CommonResult<>().success(null, "ok");
    }

    @GetMapping("/getAll")
    public CommonResult<Object> getAll() {
        var b = cacheManager.getCacheNames()
                .stream()
                .parallel()
                .map((cacheName) -> {
                    Cache cache = cacheManager.getCache(cacheName);

                    return cache.get("khesatoken", Object.class).toString();
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return new CommonResult<>().success(null, "ok");
    }

    @GetMapping("/add")
    public CommonResult<Object> add() {
        seederService.seed();
        return new CommonResult<>().success(null, "failed, cache null");
    }

    @GetMapping("/load-img")
    public CommonResult<Object> testingCount() {
        return new CommonResult<>().success(fileStorageService.load("Banner Jawa Tengah.jpg"));
    }
}
