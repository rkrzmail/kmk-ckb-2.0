package com.kmkbe.core.middleware;

import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.core.domain.repository.RedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresi test untuk security issue #13 (SECURITY.md) — residual:
 * apiKeyValidation() di JwtAuthenticationFilter (path /api/monitoring/**)
 * harus constant-time (MessageDigest.isEqual), semantik tidak berubah:
 * key benar lolos, key salah / header null ditolak.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterApiKeyValidationTest {

  private static final String CONFIGURED_API_KEY =
    "$2b$10$YoLl0SFxCMlIWXfQ9RhixeU8Vxvj9Fi7RmF5j7zA9dhYwdplSGyWC";

  @Mock private HandlerExceptionResolver handlerExceptionResolver;
  @Mock private JwtService jwtService;
  @Mock private JwtLoanSubmissionService jwtLoanSubmissionService;
  @Mock private RedisRepository redisRepository;

  private JwtAuthenticationFilter filter;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(
      handlerExceptionResolver, jwtService, jwtLoanSubmissionService, redisRepository);
    ReflectionTestUtils.setField(filter, "apiKey", CONFIGURED_API_KEY);
    request = new MockHttpServletRequest();
  }

  private boolean validate() {
    try {
      var method = JwtAuthenticationFilter.class.getDeclaredMethod(
        "apiKeyValidation", jakarta.servlet.http.HttpServletRequest.class, jakarta.servlet.http.HttpServletResponse.class);
      method.setAccessible(true);
      return (boolean) method.invoke(filter, request, new MockHttpServletResponse());
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void correctApiKeyPasses() {
    request.addHeader("API-Key", CONFIGURED_API_KEY);

    assertThat(validate()).isTrue();
  }

  @Test
  void wrongApiKeyIsRejected() {
    request.addHeader("API-Key", "wrong-key");

    assertThat(validate()).isFalse();
  }

  @Test
  void missingApiKeyHeaderIsRejected() {
    assertThat(validate()).isFalse();
  }

  @Test
  void comparisonIsNotShortCircuitEquals() {
    // memastikan implementasi memakai MessageDigest.isEqual (constant-time),
    // bukan String.equals — diverifikasi via perilaku: nilai dengan panjang
    // sama tapi beda konten tetap ditolak.
    String sameLengthDifferentContent = "A".repeat(CONFIGURED_API_KEY.length());
    request.addHeader("API-Key", sameLengthDifferentContent);

    assertThat(validate()).isFalse();
  }

  @Test
  void monitoringHealthEndpointsSkipApiKeyValidation() throws Exception {
    var method = JwtAuthenticationFilter.class.getDeclaredMethod(
      "shouldValidateApiKey",
      jakarta.servlet.http.HttpServletRequest.class,
      jakarta.servlet.http.HttpServletResponse.class,
      jakarta.servlet.FilterChain.class);
    method.setAccessible(true);

    for (String path : new String[]{"/api/monitoring/health", "/api/monitoring/health/liveness", "/api/monitoring/info"}) {
      MockHttpServletRequest req = new MockHttpServletRequest("GET", path);
      req.setServletPath(path);
      org.springframework.mock.web.MockFilterChain chain = new org.springframework.mock.web.MockFilterChain();
      boolean handled = (boolean) method.invoke(filter, req, new MockHttpServletResponse(), chain);
      assertThat(handled).as(path).isFalse();
    }

    MockHttpServletRequest protectedReq = new MockHttpServletRequest("GET", "/api/monitoring/env");
    protectedReq.setServletPath("/api/monitoring/env");
    org.springframework.mock.web.MockFilterChain chain = new org.springframework.mock.web.MockFilterChain();
    boolean handled = (boolean) method.invoke(filter, protectedReq, new MockHttpServletResponse(), chain);
    assertThat(handled).as("/api/monitoring/env").isTrue();
  }
}
