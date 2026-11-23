package com.kmkbe.config;

import com.kmkbe.core.callback.UnauthorizedEntryPoint;
import com.kmkbe.core.middleware.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

  @Mock AuthenticationProvider authenticationProvider;
  @Mock JwtAuthenticationFilter jwtAuthenticationFilter;
  @Mock UserDetailsService userDetailsService;
  @Mock UnauthorizedEntryPoint unauthorizedEntryPoint;

  private SecurityConfig config;

  @BeforeEach
  void setUp() throws Exception {
    var kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    var kp = kpg.generateKeyPair();
    RsaKeyConfigProperties rsaProps = new RsaKeyConfigProperties((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate());
    config = new SecurityConfig(authenticationProvider, jwtAuthenticationFilter, rsaProps, userDetailsService, unauthorizedEntryPoint);
    ReflectionTestUtils.setField(config, "profileActives", "dev");
    ReflectionTestUtils.setField(config, "apiKey", "test-api-key");
  }

  @Test
  void webSecurityCustomizerIgnoresExpectedPaths() {
    WebSecurityCustomizer customizer = config.webSecurityCustomizer();
    var web = mock(org.springframework.security.config.annotation.web.builders.WebSecurity.class, RETURNS_DEEP_STUBS);
    customizer.customize(web);
    verify(web).ignoring();
  }

  @Test
  void customFilterRegistration() {
    var registration = config.customFilter();
    assertThat(registration.getFilter()).isInstanceOf(CustomFilter.class);
    assertThat(registration.getUrlPatterns()).containsExactly("/api/*");
    assertThat(registration.getOrder()).isEqualTo(1);
  }

  @Test
  void corsConfigurationSourceAllowsExpectedMethods() {
    CorsConfigurationSource source = config.corsConfigurationSource();
    var cors = source.getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest());
    assertThat(cors).isNotNull();
    assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    assertThat(cors.getAllowedOriginPatterns()).contains("*");
    assertThat(cors.getAllowCredentials()).isTrue();
  }

  @Test
  void jwtDecoderAndEncoderCreated() {
    assertThat(config.jwtDecoder()).isNotNull();
    assertThat(config.jwtEncoder()).isNotNull();
  }

  @Test
  void logoutHandlerCreated() {
    assertThat(config.logoutHandler()).isInstanceOf(SecurityContextLogoutHandler.class);
  }

}
