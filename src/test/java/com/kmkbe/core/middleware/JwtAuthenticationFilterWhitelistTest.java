package com.kmkbe.core.middleware;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresi test untuk security issue #6 (SECURITY.md) — batch 1:
 * whitelist auth tidak boleh memuat ghost path (endpoint yang tidak
 * memiliki controller/handler sama sekali):
 * - /api/v1/testing/**
 * - /api/v1/error/**
 * - /api/v1/uploads/**
 * Juga menjaga path yang WAJIB tetap publik (flow login/OTP/internal auth).
 */
class JwtAuthenticationFilterWhitelistTest {

  @Test
  void whitelistDoesNotContainGhostPaths() {
    assertThat(Arrays.asList(JwtAuthenticationFilter.ENDPOINTS_WHITELIST))
      .doesNotContain("/api/v1/testing/**", "/api/v1/error/**", "/api/v1/uploads/**");
  }

  @Test
  void mandatoryPublicFlowsRemainWhitelisted() {
    List<String> whitelist = Arrays.asList(JwtAuthenticationFilter.ENDPOINTS_WHITELIST);

    assertThat(whitelist).contains(
      "/api/v1/auth/sign-in",
      "/api/v1/auth/sign-up",
      "/api/v1/auth/forgot-pin",
      "/api/v1/auth/refresh-token",
      "/api/v1/otp/**",
      "/api/v1/internal/auth/**",
      "/uploads/**"
    );
  }

  @Test
  void documentsDownloadIsNoLongerWhitelisted_issue3() {
    // jaga agar fix issue #3 tidak terbalikkan
    assertThat(Arrays.asList(JwtAuthenticationFilter.ENDPOINTS_WHITELIST))
      .doesNotContain("/api/v1/documents/download/**");
  }

  @Test
  void ghostTestingRequestIsNotBypassedByWhitelist() {
    // request ke ghost path tidak boleh lulus via whitelist —
    // harus jatuh ke alur auth (401 via entry point)
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/testing/something");
    request.setMethod("GET");

    boolean matched = Arrays.stream(JwtAuthenticationFilter.ENDPOINTS_WHITELIST)
      .anyMatch(endpoint -> new AntPathRequestMatcher(endpoint).matches(request));

    assertThat(matched).isFalse();
  }
}
