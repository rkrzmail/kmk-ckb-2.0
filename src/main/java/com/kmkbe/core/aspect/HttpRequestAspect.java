package com.kmkbe.core.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.core.domain.repository.ApiIntegrationLogRepository;
import com.kmkbe.core.middleware.HttpServletRequestCopier;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.nikita.data.Nson;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.thymeleaf.util.StringUtils;

import java.time.Instant;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class HttpRequestAspect {
    private final ApiIntegrationLogRepository apiIntegrationLogRepository;
    private final ObjectMapper objectMapper;

    @Around("execution(* org.springframework.web.client.RestTemplate.exchange(..))")
    //@Around("execution(* com.kmkbe.modules.loan_submission.controller.FinancingController.invoicePaid(..))")
    //@Around("execution(* org.springframework.web.client.RestTemplate.exchange(..))||execution(* com.kmkbe.modules.loan_submission.controller.FinancingController.invoicePaid(..))")

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Object logRestTemplateCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        Object response;
        String requestStr = "empty", requestHeader = "", responseStr = "";
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

            response = new ResponseEntity<String>(responseStr, HttpStatusCode.valueOf(statusCode));
            //throw new RuntimeException("Error while perform action. Detail:" + message);
        } catch (Exception e) {
            log.error("Error while executing RestTemplate call {}", e.getMessage());
            response = null;
            //throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            for (Object arg : args) {
                if (arg instanceof String argUrl && argUrl.toLowerCase().contains("http")) {
                    url = argUrl;
                }
                if (arg instanceof HttpEntity<?> requestEntity) {
                    requestHeader = objectMapper.writeValueAsString(requestEntity.getHeaders());

                    if (requestEntity.getBody() != null) {
                        requestStr = objectMapper.writeValueAsString(requestEntity.getBody());
                    }
                }
            }

            ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
                    .endpointUrl(url)
                    .contentType("application/json;"+executionTime + "ms")
                    .requestPayload(StringUtils.isEmpty(requestStr) ? "empty" : requestStr)
                    .responseJson(responseStr)
                    .responseStatus(String.valueOf(statusCode))
                    .dtmUpd(DateTimeUtils.now())
                    .build();

            apiIntegrationLogRepository.save(apiIntegrationLog);
        }



        return response;
    }


    @Around("execution(* com.kmkbe.modules.loan_submission.controller.FinancingController.invoicePaid(..))")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Object logApiCallback(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        Object response;
        String requestStr = "empty", requestHeader = "", responseStr = "";
        String url = "";
        int statusCode = 200;


        try {
            response = joinPoint.proceed();

            url = "callback://api/v1/financing/invoice-paid";

            if (response !=null ) {
                statusCode = 0;
                responseStr = objectMapper.writeValueAsString(response);
            }

        } catch (Exception e) {
            statusCode = -1;
            log.error("Error while executing RestTemplate call {}", e.getMessage());
            response = e.getMessage();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            for (Object arg : args) {
                if (arg instanceof HttpServletRequestCopier) {
                    HttpServletRequestCopier request = (HttpServletRequestCopier)arg;
                    requestStr = new String(request.getContentAsByteArray());
                    url = request.getRequestURL().toString();
                }
                if (arg instanceof FinancingInvoicePaidRequest) {
                    FinancingInvoicePaidRequest request = (FinancingInvoicePaidRequest) arg;

                    requestHeader = objectMapper.writeValueAsString(request);


                }
            }

            ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
                    .endpointUrl(url)
                    .contentType("application/json;"+executionTime + "ms")
                    .requestPayload(StringUtils.isEmpty(requestStr) ? "empty" : requestStr)
                    .responseJson(responseStr)
                    .responseStatus(String.valueOf(statusCode))
                    .dtmUpd(DateTimeUtils.now())
                    .build();

            apiIntegrationLogRepository.save(apiIntegrationLog);
        }



        return response;
    }
}
