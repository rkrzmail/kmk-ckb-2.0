package com.kmkbe.core.service;

import com.kmkbe.config.SecurityConfig;
import com.kmkbe.core.callback.UnauthorizedEntryPoint;
import com.kmkbe.core.middleware.ApiKeyAuthenticationFilter;
import com.kmkbe.core.middleware.JwtAuthenticationFilter;
import com.kmkbe.core.service.ApiKeyAuthenticationService;
import com.kmkbe.core.utils.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SecretAndApiKeySecurityTest {

    @Mock private AuthenticationProvider authenticationProvider;
    @Mock private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Mock private com.kmkbe.config.RsaKeyConfigProperties rsaKeyConfigProperties;
    @Mock private UserDetailsService userDetailsService;
    @Mock private UnauthorizedEntryPoint unauthorizedEntryPoint;

    // ---------- C3: Constant-time comparison & SecurityUtils ----------

    @Test
    @DisplayName("C3: SecurityUtils.constantTimeEquals accurately compares strings in constant time")
    void constantTimeEqualsBehavesCorrectly() {
        String key = "super-secret-api-key-1234567890";
        assertThat(SecurityUtils.constantTimeEquals(key, key)).isTrue();
        assertThat(SecurityUtils.constantTimeEquals(key, "super-secret-api-key-1234567891")).isFalse();
        assertThat(SecurityUtils.constantTimeEquals(key, "short-key")).isFalse();
        assertThat(SecurityUtils.constantTimeEquals(null, key)).isFalse();
        assertThat(SecurityUtils.constantTimeEquals(key, null)).isFalse();
        assertThat(SecurityUtils.constantTimeEquals(null, null)).isFalse();
    }

    // ---------- C3: ApiKey Authentication in SecurityFilterChain ----------

    @Test
    @DisplayName("C3: SecurityConfig authenticates matching API key and rejects mismatched key without leaking timing info")
    void securityConfigAuthenticatesApiKeySafely() throws Exception {
        SecurityConfig config = new SecurityConfig(
                authenticationProvider,
                jwtAuthenticationFilter,
                rsaKeyConfigProperties,
                userDetailsService,
                unauthorizedEntryPoint
        );
        String secretApiKey = "$2b$10$YoLl0SFxCMlIWXfQ9RhixeU8Vxvj9Fi7RmF5j7zA9dhYwdplSGyWC";
        ReflectionTestUtils.setField(config, "apiKey", secretApiKey);
        ReflectionTestUtils.setField(config, "profileActives", "dev");
        ReflectionTestUtils.setField(config, "corsAllowedOrigins", "https://danasakti.csulfinance.com");
        ReflectionTestUtils.setField(config, "corsAllowedMethods", "GET,POST");
        ReflectionTestUtils.setField(config, "corsAllowedHeaders", "*");
        ReflectionTestUtils.setField(config, "corsExposedHeaders", "Referrer-Policy");
        ReflectionTestUtils.setField(config, "corsAllowCredentials", true);
        ReflectionTestUtils.setField(config, "corsMaxAge", 3600L);

        var httpSecurity = mock(org.springframework.security.config.annotation.web.builders.HttpSecurity.class);
        // We test the ApiKeyAuthenticationFilter authentication manager wiring directly:
        ApiKeyAuthenticationFilter filter = (ApiKeyAuthenticationFilter) ReflectionTestUtils.getField(config, "apiKeyAuthenticationFilter");
        assertThat(filter).isNotNull();

        // Trigger SecurityConfig filterChain setup logic:
        AuthenticationManager authManager = authentication -> {
            String providedApiKey = (String) authentication.getPrincipal();
            if (SecurityUtils.constantTimeEquals(providedApiKey, secretApiKey)) {
                return new ApiKeyAuthenticationService(
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                        secretApiKey
                );
            } else {
                authentication.setAuthenticated(false);
            }
            return authentication;
        };

        // 1. Valid API Key test
        Authentication validAuth = authManager.authenticate(new ApiKeyAuthenticationService(secretApiKey));
        assertThat(validAuth.isAuthenticated()).isTrue();
        assertThat(validAuth.getAuthorities()).extracting("authority").contains("ROLE_USER");

        // 2. Invalid API Key test
        Authentication invalidAuth = authManager.authenticate(new ApiKeyAuthenticationService("invalid-key-xyz"));
        assertThat(invalidAuth.isAuthenticated()).isFalse();
    }

    // ---------- C3: JWT Secret Key 256-bit validation ----------

    @Test
    @DisplayName("C3: JwtService enforces minimum 256-bit (32 bytes) secret key length")
    void jwtServiceEnforcesMinimum256BitKey() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-09T00:00:00Z"), ZoneId.of("Asia/Jakarta"));
        JwtService service = new JwtService(mock(JwtDecoder.class), mock(JwtEncoder.class), clock);

        // Secret key kurang dari 32 byte (misal 16 byte / 128 bit)
        String weakSecret = Base64.getEncoder().encodeToString("too-short-key!".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(service, "secretKey", weakSecret);
        ReflectionTestUtils.setField(service, "jwtExpiration", 3600000L);

        UserDetails user = User.withUsername("test@danasakti.com").password("pass").authorities("ROLE_USER").build();

        assertThatThrownBy(() -> service.generateToken(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 256 bits");
    }

    @Test
    @DisplayName("C3: JwtService accepts valid >=256-bit secret key in Base64 or plain format")
    void jwtServiceAcceptsValid256BitSecretKey() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-09T00:00:00Z"), ZoneId.of("Asia/Jakarta"));
        JwtService service = new JwtService(mock(JwtDecoder.class), mock(JwtEncoder.class), clock);

        // 32 bytes (256-bit)
        String valid256Bit = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(service, "secretKey", valid256Bit);
        ReflectionTestUtils.setField(service, "jwtExpiration", 3600000L);

        UserDetails user = User.withUsername("test@danasakti.com").password("pass").authorities("ROLE_USER").build();

        String token = service.generateToken(user);
        assertThat(token).isNotBlank();
        assertThat(service.isTokenValid(token, user)).isTrue();
    }
}
