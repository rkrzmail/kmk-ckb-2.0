package com.kmkbe.core.aspect;

import com.kmkbe.core.exception.IllegalApiKeyException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

        if (providedApiKey == null || !providedApiKey.equals(apiKey)) {
            throw new IllegalApiKeyException();
        }
    }
}
