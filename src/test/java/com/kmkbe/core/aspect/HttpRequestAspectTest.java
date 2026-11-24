package com.kmkbe.core.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.core.domain.repository.ApiIntegrationLogRepository;
import com.kmkbe.core.middleware.HttpServletRequestCopier;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpRequestAspectTest {

  @Mock ApiIntegrationLogRepository apiIntegrationLogRepository;
  @Mock ProceedingJoinPoint joinPoint;

  @Test
  void logsSuccessfulRestTemplateCall() throws Throwable {
    ObjectMapper mapper = new ObjectMapper();
    HttpRequestAspect aspect = new HttpRequestAspect(apiIntegrationLogRepository, mapper);

    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Test", "v");
    HttpEntity<String> entity = new HttpEntity<>("payload", headers);
    ResponseEntity<String> response = ResponseEntity.ok("resp");

    when(joinPoint.getArgs()).thenReturn(new Object[]{"http://example.com/api", entity});
    when(joinPoint.proceed()).thenReturn(response);
    when(apiIntegrationLogRepository.save(any(ApiIntegrationLog.class))).thenAnswer(inv -> inv.getArgument(0));

    Object result = aspect.logRestTemplateCalls(joinPoint);

    assertThat(result).isSameAs(response);
    ArgumentCaptor<ApiIntegrationLog> captor = ArgumentCaptor.forClass(ApiIntegrationLog.class);
    verify(apiIntegrationLogRepository).save(captor.capture());
    ApiIntegrationLog log = captor.getValue();
    assertThat(log.getEndpointUrl()).isEqualTo("http://example.com/api");
    assertThat(log.getResponseStatus()).isEqualTo("200");
    assertThat(log.getRequestPayload()).contains("payload");
    assertThat(log.getResponseJson()).contains("resp");
    assertThat(log.getContentType()).startsWith("application/json;").endsWith("ms");
  }

  @Test
  void logsHttpStatusCodeExceptionAndRethrowsIt() throws Throwable {
    ObjectMapper mapper = new ObjectMapper();
    HttpRequestAspect aspect = new HttpRequestAspect(apiIntegrationLogRepository, mapper);

    HttpStatusCodeException ex = mock(HttpStatusCodeException.class);
    when(ex.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
    when(ex.getResponseBodyAsString()).thenReturn("{\"message\":\"bad\"}");

    when(joinPoint.getArgs()).thenReturn(new Object[]{"http://example.com/fail"});
    when(joinPoint.proceed()).thenThrow(ex);
    when(apiIntegrationLogRepository.save(any(ApiIntegrationLog.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThatThrownBy(() -> aspect.logRestTemplateCalls(joinPoint)).isSameAs(ex);
    ArgumentCaptor<ApiIntegrationLog> captor = ArgumentCaptor.forClass(ApiIntegrationLog.class);
    verify(apiIntegrationLogRepository).save(captor.capture());
    assertThat(captor.getValue().getResponseStatus()).isEqualTo("400");
    assertThat(captor.getValue().getResponseJson()).contains("bad");
  }

  @Test
  void rethrowsGenericExceptionAfterLogging() throws Throwable {
    ObjectMapper mapper = new ObjectMapper();
    HttpRequestAspect aspect = new HttpRequestAspect(apiIntegrationLogRepository, mapper);

    when(joinPoint.getArgs()).thenReturn(new Object[]{"http://example.com/err"});
    when(joinPoint.proceed()).thenThrow(new RuntimeException("network"));
    when(apiIntegrationLogRepository.save(any(ApiIntegrationLog.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThatThrownBy(() -> aspect.logRestTemplateCalls(joinPoint))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("network");
    ArgumentCaptor<ApiIntegrationLog> captor = ArgumentCaptor.forClass(ApiIntegrationLog.class);
    verify(apiIntegrationLogRepository).save(captor.capture());
    assertThat(captor.getValue().getResponseStatus()).isEqualTo("500");
  }

  @Test
  void doesNotLogMailCredentialsOrJwtTokens() throws Throwable {
    HttpRequestAspect aspect = new HttpRequestAspect(apiIntegrationLogRepository, new ObjectMapper());
    when(joinPoint.getArgs()).thenReturn(
      new Object[]{"http://sisca/authconfig/mail/danasakti-mail"},
      new Object[]{"http://sisca/auth/req-jwt"}
    );
    when(joinPoint.proceed()).thenReturn(
      ResponseEntity.ok("{\"password\":\"smtp-secret\"}"),
      ResponseEntity.ok("{\"data\":\"jwt-secret\"}")
    );

    aspect.logRestTemplateCalls(joinPoint);
    aspect.logRestTemplateCalls(joinPoint);

    ArgumentCaptor<ApiIntegrationLog> captor = ArgumentCaptor.forClass(ApiIntegrationLog.class);
    verify(apiIntegrationLogRepository, times(2)).save(captor.capture());
    assertThat(captor.getAllValues()).allSatisfy(entry ->
      assertThat(entry.getResponseJson()).isEqualTo("[REDACTED]"));
  }

  @Test
  void logsApiCallbackSuccessWithServletAndRequestArgs() throws Throwable {
    ObjectMapper mapper = new ObjectMapper();
    HttpRequestAspect aspect = new HttpRequestAspect(apiIntegrationLogRepository, mapper);

    HttpServletRequestCopier copier = mock(HttpServletRequestCopier.class);
    when(copier.getContentAsByteArray()).thenReturn("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
    when(copier.getRequestURL()).thenReturn(new StringBuffer("http://host/callback"));

    FinancingInvoicePaidRequest req = new FinancingInvoicePaidRequest();
    req.setBouwheerCode("BW1");

    when(joinPoint.getArgs()).thenReturn(new Object[]{copier, req});
    when(joinPoint.proceed()).thenReturn("ok-response");
    when(apiIntegrationLogRepository.save(any(ApiIntegrationLog.class))).thenAnswer(inv -> inv.getArgument(0));

    Object result = aspect.logApiCallback(joinPoint);

    assertThat(result).isEqualTo("ok-response");
    ArgumentCaptor<ApiIntegrationLog> captor = ArgumentCaptor.forClass(ApiIntegrationLog.class);
    verify(apiIntegrationLogRepository).save(captor.capture());
    ApiIntegrationLog log = captor.getValue();
    assertThat(log.getEndpointUrl()).isEqualTo("http://host/callback");
    assertThat(log.getResponseStatus()).isEqualTo("0");
    assertThat(log.getRequestPayload()).isEqualTo("{\"a\":1}");
    assertThat(log.getResponseJson()).contains("ok-response");
  }

  @Test
  void logsApiCallbackExceptionAndReturnsMessage() throws Throwable {
    ObjectMapper mapper = new ObjectMapper();
    HttpRequestAspect aspect = new HttpRequestAspect(apiIntegrationLogRepository, mapper);

    when(joinPoint.getArgs()).thenReturn(new Object[]{});
    when(joinPoint.proceed()).thenThrow(new RuntimeException("boom"));
    when(apiIntegrationLogRepository.save(any(ApiIntegrationLog.class))).thenAnswer(inv -> inv.getArgument(0));

    Object result = aspect.logApiCallback(joinPoint);

    assertThat(result).isEqualTo("boom");
    ArgumentCaptor<ApiIntegrationLog> captor = ArgumentCaptor.forClass(ApiIntegrationLog.class);
    verify(apiIntegrationLogRepository).save(captor.capture());
    assertThat(captor.getValue().getResponseStatus()).isEqualTo("-1");
  }

  @Test
  void logAttackDelegatesToProceed() throws Throwable {
    HttpRequestAspect aspect = new HttpRequestAspect(apiIntegrationLogRepository, new ObjectMapper());
    when(joinPoint.proceed()).thenReturn("passed");

    assertThat(aspect.logAttack(joinPoint)).isEqualTo("passed");
    verify(apiIntegrationLogRepository, never()).save(any());
  }
}
