package com.kmkbe.core.aspect;

import com.kmkbe.core.exception.IllegalApiKeyException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Aspect
public class ApiKeyValidationAspect {
    private final HttpServletRequest request;

    @Value("${security.api.key}")
    private String apiKey;

    public ApiKeyValidationAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Pointcut("@annotation(com.kmkbe.core.annotation.ApiKeyValidation)")
    public void apiKeyValidationPointcut() {
    }

    @Before("apiKeyValidationPointcut()")
    public void validateApiKey() {
        String providedApiKey = request.getHeader("API-Key");

        if (providedApiKey == null || !MessageDigest.isEqual(
            providedApiKey.getBytes(StandardCharsets.UTF_8),
            apiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalApiKeyException();
        }
    }
}
