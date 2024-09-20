package com.kmkbe.core.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.core.domain.repository.ApiIntegrationLogRepository;
import com.kmkbe.core.utils.ObjectUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class HttpRequestAspect {
    private final ApiIntegrationLogRepository apiIntegrationLogRepository;
    private final ObjectMapper objectMapper;

    @Around("execution(* org.springframework.web.client.RestTemplate.exchange(..))")
    public Object logRestTemplateCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object response;
        String requestStr = "", requestHeader = "", responseStr = "";
        String url = "";
        int statusCode = 200;
        try {
            response = joinPoint.proceed();
            if (response instanceof ResponseEntity<?> responseEntity) {
                statusCode = responseEntity.getStatusCode().value();
                responseStr = objectMapper.writeValueAsString(responseEntity.getBody());
                if (StringUtil.isNullOrEmpty(responseStr)) {
                    responseStr = objectMapper.writeValueAsString(responseEntity.getBody());
                }
            }
        } catch (HttpStatusCodeException httpStatusCodeException) {
            String message = "Unknown error";

            statusCode = httpStatusCodeException.getStatusCode().value();
            responseStr = httpStatusCodeException.getResponseBodyAsString();
            Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
            if (errorObj != null) {
                message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
            }

            throw new RuntimeException("Error while perform action. Detail:" + message);
        } catch (Exception e) {
            log.error("Error while executing RestTemplate call {}", e.getMessage());
            throw e;
        } finally {
            for (Object arg : args) {
                if (arg instanceof String argUrl && argUrl.toLowerCase().contains("http")) {
                    url = argUrl;
                }

                if (arg instanceof HttpEntity<?> requestEntity) {
                    requestHeader = objectMapper.writeValueAsString(requestEntity.getHeaders());
                    requestStr = objectMapper.writeValueAsString(requestEntity.getBody());
                }
            }

            ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
                    .endpointUrl(url)
                    .contentType("application/json")
                    .requestPayload(requestStr)
                    .responseJson(responseStr)
                    .responseStatus(String.valueOf(statusCode))
                    .build();

            apiIntegrationLogRepository.save(apiIntegrationLog);
        }

        return response;
    }
}
