package com.kmkbe.core.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.core.domain.repository.ApiIntegrationLogRepository;
import com.kmkbe.core.middleware.HttpServletRequestCopier;
import com.kmkbe.core.utils.AuditMaskingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import io.netty.util.internal.StringUtil;
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

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class HttpRequestAspect {
  private final ApiIntegrationLogRepository apiIntegrationLogRepository;
  private final ObjectMapper objectMapper;

  @Around("execution(* org.springframework.web.client.RestTemplate.exchange(..))")
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
        Object body = responseEntity.getBody();
        if (body instanceof String strBody) {
          responseStr = AuditMaskingUtils.maskJsonString(strBody);
        } else {
          responseStr = objectMapper.writeValueAsString(AuditMaskingUtils.maskObject(body));
        }
      }
    } catch (HttpStatusCodeException httpStatusCodeException) {
      String message = "Unknown error";

      statusCode = httpStatusCodeException.getStatusCode().value();
      responseStr = AuditMaskingUtils.maskJsonString(httpStatusCodeException.getResponseBodyAsString());
      Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
      if (errorObj != null) {
        message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
      }

      response = new ResponseEntity<String>(responseStr, HttpStatusCode.valueOf(statusCode));
    } catch (Exception e) {
      log.error("Error while executing RestTemplate call {}", e.getMessage());
      response = null;
    } finally {
      long executionTime = System.currentTimeMillis() - startTime;

      for (Object arg : args) {
        if (arg instanceof String argUrl && argUrl.toLowerCase().contains("http")) {
          url = argUrl;
        }
        if (arg instanceof HttpEntity<?> requestEntity) {
          requestHeader = objectMapper.writeValueAsString(AuditMaskingUtils.maskHeaders(requestEntity.getHeaders()));

          if (requestEntity.getBody() != null) {
            Object body = requestEntity.getBody();
            if (body instanceof String strBody) {
              requestStr = AuditMaskingUtils.maskJsonString(strBody);
            } else {
              requestStr = objectMapper.writeValueAsString(AuditMaskingUtils.maskObject(body));
            }
          }
        }
      }

      String sanitizedRequest = StringUtils.isEmpty(requestStr) ? "empty" : AuditMaskingUtils.maskJsonString(requestStr);
      String sanitizedResponse = AuditMaskingUtils.maskJsonString(responseStr);

      ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
        .endpointUrl(url)
        .contentType("application/json;" + executionTime + "ms")
        .requestPayload(sanitizedRequest)
        .responseJson(sanitizedResponse)
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

      if (response != null) {
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
        if (arg instanceof HttpServletRequestCopier request) {
          requestStr = AuditMaskingUtils.maskJsonString(new String(request.getContentAsByteArray(), StandardCharsets.UTF_8));
          url = request.getRequestURL().toString();
        }
        if (arg instanceof FinancingInvoicePaidRequest request) {
          requestHeader = objectMapper.writeValueAsString(AuditMaskingUtils.maskObject(request));
        }
      }

      String sanitizedRequest = StringUtils.isEmpty(requestStr) ? "empty" : AuditMaskingUtils.maskJsonString(requestStr);
      String sanitizedResponse = AuditMaskingUtils.maskJsonString(responseStr);

      ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
        .endpointUrl(url)
        .contentType("application/json;" + executionTime + "ms")
        .requestPayload(sanitizedRequest)
        .responseJson(sanitizedResponse)
        .responseStatus(String.valueOf(statusCode))
        .dtmUpd(DateTimeUtils.now())
        .build();

      apiIntegrationLogRepository.save(apiIntegrationLog);
    }

    return response;
  }

  @Around("execution(* com.kmkbe.modules.customer.service.AuthService.signIn(..))")
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public Object logAttack(ProceedingJoinPoint joinPoint) throws Throwable {
    return joinPoint.proceed();
  }
}
