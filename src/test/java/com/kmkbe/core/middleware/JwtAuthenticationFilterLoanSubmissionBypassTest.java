package com.kmkbe.core.middleware;

import com.kmkbe.core.domain.model.JwtSimulasiModel;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterLoanSubmissionBypassTest {

  @Mock private HandlerExceptionResolver handlerExceptionResolver;
  @Mock private JwtService jwtService;
  @Mock private JwtLoanSubmissionService jwtLoanSubmissionService;
  @Mock private UserDetailsService userDetailsService;
  @Mock private UserDetailsService internalUserDetailsService;
  @Mock private RedisRepository redisRepository;
  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    filter = new JwtAuthenticationFilter(
      handlerExceptionResolver,
      jwtService,
      jwtLoanSubmissionService,
      redisRepository
    );
    ReflectionTestUtils.setField(filter, "userDetailsService", userDetailsService);
    ReflectionTestUtils.setField(filter, "internalUserDetailsService", internalUserDetailsService);
    ReflectionTestUtils.setField(filter, "profileActives", "prod");
    ReflectionTestUtils.setField(filter, "apiKey", "test-api-key");
    ReflectionTestUtils.setField(filter, "envString", "prod");
  }

  @Test
  void magicTokenOneDoesNotBypassAuthentication() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/loan-submissions");
    request.setServletPath("/api/v1/loan-submissions");
    request.setParameter("token", "1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtLoanSubmissionService.extractToken("1")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

    // Karena token "1" diekstrak menghasilkan null, invalidBouwheer = true
    // HandlerExceptionResolver dipanggil dan filterChain TIDAK diloloskan
    verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(Exception.class));
    verify(filterChain, never()).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void magicTokensTwoAndThreeDoNotBypassAuthentication() throws ServletException, IOException {
    for (String tokenVal : new String[]{"2", "3"}) {
      reset(filterChain, handlerExceptionResolver, jwtLoanSubmissionService);
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/loan-submissions/invoices");
      request.setServletPath("/api/v1/loan-submissions/invoices");
      request.setParameter("token", tokenVal);
      MockHttpServletResponse response = new MockHttpServletResponse();

      when(jwtLoanSubmissionService.extractToken(tokenVal)).thenReturn(null);

      filter.doFilter(request, response, filterChain);

      verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(Exception.class));
      verify(filterChain, never()).doFilter(request, response);
      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
  }

  @Test
  void requestWithoutAuthorizationHeaderDoesNotBypassLoanSubmissionEndpoints() throws ServletException, IOException {
    String[] endpoints = {
      "/api/v1/loan-submissions",
      "/api/v1/loan-submissions/invoices",
      "/api/v1/loan-submissions/importance-notes",
      "/api/v1/loan-submissions/simulations/percentage",
      "/api/v1/loan-submissions/simulations/calculate"
    };

    for (String uri : endpoints) {
      reset(filterChain, handlerExceptionResolver);
      MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
      request.setServletPath(uri);
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilter(request, response, filterChain);

      // Request diteruskan ke filter chain berikutnya (setAuthenticate) tanpa otorisasi bypass
      verify(filterChain).doFilter(request, response);
      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
  }

  @Test
  void validExternalTokenExtractsBouwheerAndPasses() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/loan-submissions/simulations/calculate");
    request.setServletPath("/api/v1/loan-submissions/simulations/calculate");
    request.setParameter("token", "valid-signed-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    JwtSimulasiModel model = mock(JwtSimulasiModel.class);
    when(model.getBouwheerCode()).thenReturn("BOUW-001");
    when(jwtLoanSubmissionService.extractToken("valid-signed-token")).thenReturn(model);

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(handlerExceptionResolver, never()).resolveException(any(), any(), any(), any());
  }

  @Test
  void invalidExternalTokenIsRejectedViaResolver() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/loan-submissions");
    request.setServletPath("/api/v1/loan-submissions");
    request.setParameter("token", "invalid-external-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtLoanSubmissionService.extractToken("invalid-external-token")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

    verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(Exception.class));
    verify(filterChain, never()).doFilter(request, response);
  }
}
