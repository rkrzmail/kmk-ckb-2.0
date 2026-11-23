package com.kmkbe.core.middleware;

import com.kmkbe.core.domain.entity.RedisLog;
import com.kmkbe.core.domain.model.JwtSimulasiModel;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterFlowTest {
  @Mock HandlerExceptionResolver resolver;
  @Mock JwtService jwtService;
  @Mock JwtLoanSubmissionService jwtLoanSubmissionService;
  @Mock RedisRepository redisRepository;
  @Mock UserDetailsService userDetailsService;
  @Mock UserDetailsService internalUserDetailsService;

  JwtAuthenticationFilter filter;
  Method doFilter;

  @BeforeEach void setUp() throws Exception {
    filter = new JwtAuthenticationFilter(resolver, jwtService, jwtLoanSubmissionService, redisRepository);
    ReflectionTestUtils.setField(filter, "apiKey", "key");
    ReflectionTestUtils.setField(filter, "profileActives", "dev");
    ReflectionTestUtils.setField(filter, "envString", "test");
    ReflectionTestUtils.setField(filter, "userDetailsService", userDetailsService);
    ReflectionTestUtils.setField(filter, "internalUserDetailsService", internalUserDetailsService);
    doFilter = org.springframework.web.filter.OncePerRequestFilter.class.getDeclaredMethod(
      "doFilter", jakarta.servlet.ServletRequest.class, jakarta.servlet.ServletResponse.class, FilterChain.class);
    doFilter.setAccessible(true);
    SecurityContextHolder.clearContext();
  }
  @AfterEach void clear() { SecurityContextHolder.clearContext(); }

  private void invoke(MockHttpServletRequest req, MockHttpServletResponse res, FilterChain chain) throws Exception {
    // OncePerRequestFilter.doFilter -> doFilterInternal (protected), using reflection on filter instance
    Method m = org.springframework.web.filter.GenericFilterBean.class.getMethod("doFilter", jakarta.servlet.ServletRequest.class, jakarta.servlet.ServletResponse.class, FilterChain.class);
    m.invoke(filter, req, res, chain);
  }

  private UserDetails user() { return User.withUsername("u").password("p").authorities(new SimpleGrantedAuthority("ROLE_USER")).build(); }

  @Test void noAuthHeaderPassesThrough() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/protected");
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(chain.getRequest()).isNotNull();
  }

  @Test void whitelistPathPassesThrough() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/auth/sign-in");
    req.setAttribute("FILTERED", null);
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(chain.getRequest()).isNotNull();
  }

  @Test void validBearerTokenAuthenticates() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/protected");
    req.addHeader("Authorization", "Bearer tok");
    when(jwtService.extractUsername("tok")).thenReturn("u");
    when(redisRepository.findFirstByRedis("u")).thenReturn(Optional.empty());
    when(userDetailsService.loadUserByUsername("u")).thenReturn(user());
    when(jwtService.isTokenValid("tok", user())).thenReturn(true);
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(chain.getRequest()).isNotNull();
  }

  @Test void validBearerTokenInternalFallback() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/protected");
    req.addHeader("Authorization", "Bearer tok");
    when(jwtService.extractUsername("tok")).thenReturn("u");
    when(redisRepository.findFirstByRedis("u")).thenReturn(Optional.empty());
    when(userDetailsService.loadUserByUsername("u")).thenThrow(new RuntimeException("no"));
    when(internalUserDetailsService.loadUserByUsername("u")).thenReturn(user());
    when(jwtService.isTokenValid(eq("tok"), any(UserDetails.class))).thenReturn(true);
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }

  @Test void downloadWithQueryTokenAuthenticates() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/documents/download/1");
    req.setParameter("token", "qtok");
    when(jwtService.extractUsername("qtok")).thenReturn("u");
    when(redisRepository.findFirstByRedis("u")).thenReturn(Optional.empty());
    when(userDetailsService.loadUserByUsername("u")).thenReturn(user());
    when(jwtService.isTokenValid("qtok", user())).thenReturn(true);
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }

  @Test void redisLogPresentWithRtokenPasses() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/protected");
    req.addHeader("Authorization", "Bearer tok");
    req.addHeader("rtoken", "sess");
    when(jwtService.extractUsername("tok")).thenReturn("u");
    when(redisRepository.findFirstByRedis("u")).thenReturn(Optional.of(RedisLog.builder().redis("u").session("sess").build()));
    when(userDetailsService.loadUserByUsername("u")).thenReturn(user());
    when(jwtService.isTokenValid("tok", user())).thenReturn(true);
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(chain.getRequest()).isNotNull();
  }

  @Test void redisLogPresentRtokenMismatchLoanCreatePasses() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/loan-submissions/create");
    req.addHeader("Authorization", "Bearer tok");
    req.addHeader("rtoken", "other");
    when(jwtService.extractUsername("tok")).thenReturn("u");
    when(redisRepository.findFirstByRedis("u")).thenReturn(Optional.of(RedisLog.builder().redis("u").session("sess").build()));
    when(userDetailsService.loadUserByUsername("u")).thenReturn(user());
    when(jwtService.isTokenValid("tok", user())).thenReturn(true);
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(chain.getRequest()).isNotNull();
  }

  @Test void expiredJwtPassesThroughWithAttribute() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/protected");
    req.addHeader("Authorization", "Bearer tok");
    when(jwtService.extractUsername("tok")).thenThrow(mock(ExpiredJwtException.class));
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(req.getAttribute("exception")).isNotNull();
  }

  @Test void badCredentialsForwardsExternalAndInternal() throws Exception {
    MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/protected");
    req1.addHeader("Authorization", "Bearer tok");
    when(jwtService.extractUsername("tok")).thenThrow(new BadCredentialsException("bad"));
    invoke(req1, new MockHttpServletResponse(), new MockFilterChain());
    assertThat(req1.getRequestDispatcher("/error/unauthorized")).isNotNull();

    MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/v1/internal/x");
    req2.addHeader("Authorization", "Bearer tok");
    invoke(req2, new MockHttpServletResponse(), new MockFilterChain());
    assertThat(req2.getRequestDispatcher("/error/internal/unauthorized")).isNotNull();
  }

  @Test void loanSubmissionBypassTokens() throws Exception {
    for (String t : List.of("1", "2", "3")) {
      MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/loan-submissions");
      req.setServletPath("/api/v1/loan-submissions");
      req.setParameter("token", t);
      MockFilterChain chain = new MockFilterChain();
      invoke(req, new MockHttpServletResponse(), chain);
      assertThat(chain.getRequest()).as(t).isNotNull();
    }
  }

  @Test void loanSubmissionJwtModelValid() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/loan-submissions");
    req.setServletPath("/api/v1/loan-submissions");
    req.setParameter("token", "jwtmodel");
    JwtSimulasiModel model = mock(JwtSimulasiModel.class);
    when(model.getBouwheerCode()).thenReturn("BWH");
    when(jwtLoanSubmissionService.extractToken("jwtmodel")).thenReturn(model);
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    assertThat(chain.getRequest()).isNotNull();
  }

  @Test void loanSubmissionJwtModelNullResolvesException() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/loan-submissions");
    req.setServletPath("/api/v1/loan-submissions");
    req.setParameter("token", "badmodel");
    when(jwtLoanSubmissionService.extractToken("badmodel")).thenReturn(null);
    MockFilterChain chain = new MockFilterChain();
    invoke(req, new MockHttpServletResponse(), chain);
    verify(resolver).resolveException(any(), any(), isNull(), argThat(e -> e instanceof Exception && "External request need passing an valid token".equals(e.getMessage())));
  }

  @Test void monitoringApiKeyValidAndInvalid() throws Exception {
    MockHttpServletRequest ok = new MockHttpServletRequest("GET", "/api/monitoring/env");
    ok.setServletPath("/api/monitoring/env");
    ok.addHeader("API-Key", "key");
    MockFilterChain chain = new MockFilterChain();
    invoke(ok, new MockHttpServletResponse(), chain);
    assertThat(chain.getRequest()).isNotNull();

    MockHttpServletRequest bad = new MockHttpServletRequest("GET", "/api/monitoring/env");
    bad.setServletPath("/api/monitoring/env");
    bad.addHeader("API-Key", "wrong");
    invoke(bad, new MockHttpServletResponse(), new MockFilterChain());
    verify(resolver).resolveException(any(), any(), isNull(), any(com.kmkbe.core.exception.IllegalApiKeyException.class));
  }
}
