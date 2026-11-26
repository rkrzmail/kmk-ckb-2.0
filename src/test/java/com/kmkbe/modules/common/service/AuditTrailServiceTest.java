package com.kmkbe.modules.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.constant.AuditActorType;
import com.kmkbe.core.domain.event.AuditTrailEvent;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditTrailServiceTest {
  @Mock ApplicationEventPublisher eventPublisher;

  AuditTrailService service;

  @BeforeEach
  void setUp() {
    service = new AuditTrailService(eventPublisher, new ObjectMapper());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void recordInternalUserWithRequestMasksSensitiveFields() {
    UUID userCode = UUID.randomUUID();
    MstUser user = new MstUser();
    user.setUserCode(userCode);
    user.setUsername("admin412");
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("USER"))));
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/agreement");
    request.setRemoteAddr("10.0.0.9");
    request.addHeader("X-Request-ID", "trace-1");
    request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");
    request.addHeader("User-Agent", "JUnit");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    service.recordEvent("AGREEMENT", AuditAction.CREATE, "Agreement", 42L,
      Map.of("password", "secret", "note", "before"), Map.of("api_key", "secret", "note", "after"));

    AuditTrailEvent event = capture();
    assertThat(event.actorType()).isEqualTo(AuditActorType.INTERNAL);
    assertThat(event.actorUsername()).isEqualTo("admin412");
    assertThat(event.actorId()).isEqualTo(userCode.toString());
    assertThat(event.traceId()).isEqualTo("trace-1");
    assertThat(event.sourceIp()).isEqualTo("1.2.3.4");
    assertThat(event.userAgent()).isEqualTo("JUnit");
    assertThat(event.entityId()).isEqualTo("42");
    assertThat(event.beforeData().get("password").asText()).isEqualTo("******");
    assertThat(event.afterData().get("api_key").asText()).isEqualTo("******");
    assertThat(event.requestPath()).isEqualTo("/api/v1/agreement");
    assertThat(event.httpMethod()).isEqualTo("POST");
    assertThat(event.responseStatus()).isEqualTo(200);
    assertThat(event.success()).isTrue();
  }

  @Test
  void recordCustomerWithoutRequestUsesUnknownTraceAndNoHttpContext() {
    UUID customerCode = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setCustCode(customerCode);
    customer.setCustEmail("customer@example.com");
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(customer, null, List.of(new SimpleGrantedAuthority("CUSTOMER"))));

    service.recordEvent("CUSTOMER", AuditAction.UPDATE, "Customer", null, null, null);

    AuditTrailEvent event = capture();
    assertThat(event.actorType()).isEqualTo(AuditActorType.CUSTOMER);
    assertThat(event.actorUsername()).isEqualTo("customer@example.com");
    assertThat(event.actorId()).isEqualTo(customerCode.toString());
    assertThat(event.traceId()).isNotBlank();
    assertThat(event.sourceIp()).isNull();
    assertThat(event.entityId()).isNull();
    assertThat(event.beforeData()).isNull();
    assertThat(event.afterData()).isNull();
    assertThat(event.requestPath()).isNull();
    assertThat(event.httpMethod()).isNull();
  }

  @Test
  void recordAnonymousAndUnauthenticatedActorsAreUnknown() {
    SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
      "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    service.recordEvent("A", AuditAction.VIEW, "E", "1", null, null);
    assertThat(capture().actorType()).isEqualTo(AuditActorType.UNKNOWN);
    assertThat(capture().actorUsername()).isEqualTo("UNKNOWN");

    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "pass"));
    service.recordEvent("A", AuditAction.VIEW, "E", "1", null, null);
    assertThat(capture().actorType()).isEqualTo(AuditActorType.UNKNOWN);

    SecurityContextHolder.clearContext();
    service.recordEvent("A", AuditAction.VIEW, "E", "1", null, null);
    assertThat(capture().actorType()).isEqualTo(AuditActorType.UNKNOWN);
  }

  @Test
  void recordGenericPrincipalWithBlankNameFallsBackToUnknown() {
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken("", null, List.of(new SimpleGrantedAuthority("API"))));

    service.recordEvent("API", AuditAction.DOWNLOAD, "Document", "D1", null, null);

    AuditTrailEvent event = capture();
    assertThat(event.actorType()).isEqualTo(AuditActorType.UNKNOWN);
    assertThat(event.actorUsername()).isEqualTo("UNKNOWN");
  }

  @Test
  void recordAuthenticationSuccessUsesCorrelationIdAndRemoteAddress() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/login");
    request.setRemoteAddr("192.168.1.10");
    request.addHeader("X-Correlation-ID", "corr-1");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    service.recordAuthentication("AUTH", null, "  ", null, true, null);

    AuditTrailEvent event = capture();
    assertThat(event.actorType()).isEqualTo(AuditActorType.UNKNOWN);
    assertThat(event.actorUsername()).isEqualTo("UNKNOWN");
    assertThat(event.traceId()).isEqualTo("corr-1");
    assertThat(event.sourceIp()).isEqualTo("192.168.1.10");
    assertThat(event.action()).isEqualTo(AuditAction.LOGIN);
    assertThat(event.entityName()).isEqualTo("Authentication");
    assertThat(event.responseStatus()).isEqualTo(200);
    assertThat(event.success()).isTrue();
    assertThat(event.afterData().get("success").asBoolean()).isTrue();
  }

  @Test
  void recordAuthenticationFailureUsesRandomTraceIdWhenHeadersBlank() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/login");
    request.addHeader("X-Request-ID", "  ");
    request.addHeader("X-Correlation-ID", "");
    request.addHeader("X-Forwarded-For", "9.9.9.9, 8.8.8.8");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    service.recordAuthentication("AUTH", AuditActorType.CUSTOMER, "customer", 7L, false, "bad credentials");

    AuditTrailEvent event = capture();
    assertThat(event.actorType()).isEqualTo(AuditActorType.CUSTOMER);
    assertThat(event.actorUsername()).isEqualTo("customer");
    assertThat(event.actorId()).isEqualTo("7");
    assertThat(event.traceId()).isNotBlank().isNotEqualTo("  ");
    assertThat(event.sourceIp()).isEqualTo("9.9.9.9");
    assertThat(event.responseStatus()).isEqualTo(401);
    assertThat(event.success()).isFalse();
    assertThat(event.errorMessage()).isEqualTo("bad credentials");
  }

  @Test
  void publisherFailuresAreSwallowed() {
    doThrow(new RuntimeException("publisher down")).when(eventPublisher).publishEvent(any(AuditTrailEvent.class));

    assertThatCode(() -> service.recordEvent("A", AuditAction.CREATE, "E", "1", null, null)).doesNotThrowAnyException();
    assertThatCode(() -> service.recordAuthentication("AUTH", AuditActorType.SYSTEM, "sys", null, false, "x"))
      .doesNotThrowAnyException();
  }

  private AuditTrailEvent capture() {
    ArgumentCaptor<AuditTrailEvent> captor = ArgumentCaptor.forClass(AuditTrailEvent.class);
    verify(eventPublisher, atLeastOnce()).publishEvent(captor.capture());
    return captor.getValue();
  }
}
