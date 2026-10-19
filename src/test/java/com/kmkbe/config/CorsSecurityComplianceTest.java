package com.kmkbe.config;

import com.kmkbe.core.callback.UnauthorizedEntryPoint;
import com.kmkbe.core.middleware.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CorsSecurityComplianceTest {

    @Mock private AuthenticationProvider authenticationProvider;
    @Mock private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Mock private RsaKeyConfigProperties rsaKeyConfigProperties;
    @Mock private UserDetailsService userDetailsService;
    @Mock private UnauthorizedEntryPoint unauthorizedEntryPoint;

    private SecurityConfig createSecurityConfig() {
        return new SecurityConfig(
                authenticationProvider,
                jwtAuthenticationFilter,
                rsaKeyConfigProperties,
                userDetailsService,
                unauthorizedEntryPoint
        );
    }

    @Test
    @DisplayName("C8 Compliance: Default configuration must use explicit domain whitelist and forbid wildcard origin pattern")
    void defaultConfigurationUsesExplicitWhitelistWithoutWildcardOriginPattern() {
        SecurityConfig config = createSecurityConfig();
        // Uses default @Value annotations initialized through ReflectionTestUtils or defaults
        ReflectionTestUtils.setField(config, "corsAllowedOrigins", "https://dev1-danasakti.csulfinance.com,https://danasakti.csulfinance.com");
        ReflectionTestUtils.setField(config, "corsAllowedMethods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        ReflectionTestUtils.setField(config, "corsAllowedHeaders", "*");
        ReflectionTestUtils.setField(config, "corsExposedHeaders", "Referrer-Policy,Authorization,rtoken,ApiKey");
        ReflectionTestUtils.setField(config, "corsAllowCredentials", true);
        ReflectionTestUtils.setField(config, "corsMaxAge", 3600L);

        CorsConfigurationSource source = config.corsConfigurationSource();
        assertThat(source).isNotNull();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/sign-in");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertThat(corsConfig).isNotNull();

        // 1. Allowed origin patterns must NOT contain "*" wildcard
        List<String> allowedOriginPatterns = corsConfig.getAllowedOriginPatterns();
        if (allowedOriginPatterns != null) {
            assertThat(allowedOriginPatterns).doesNotContain("*");
        }

        // 2. Explicit whitelist domains must be present
        List<String> allowedOrigins = corsConfig.getAllowedOrigins();
        assertThat(allowedOrigins).isNotNull();
        assertThat(allowedOrigins).containsExactlyInAnyOrder(
                "https://dev1-danasakti.csulfinance.com",
                "https://danasakti.csulfinance.com"
        );
        assertThat(allowedOrigins).doesNotContain("*");

        // 3. Allow credentials can be true ONLY with explicit origins
        assertThat(corsConfig.getAllowCredentials()).isTrue();

        // 4. Exposed headers must expose essential API tokens/headers
        assertThat(corsConfig.getExposedHeaders()).contains("Referrer-Policy", "Authorization", "rtoken", "ApiKey");

        // 5. Allowed methods must include standard HTTP methods
        assertThat(corsConfig.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    @DisplayName("C8 Compliance: If wildcard * is explicitly set in allowed-origins, allowCredentials must automatically be forced to FALSE")
    void wildcardOriginForcesAllowCredentialsToFalse() {
        SecurityConfig config = createSecurityConfig();
        ReflectionTestUtils.setField(config, "corsAllowedOrigins", "*");
        ReflectionTestUtils.setField(config, "corsAllowedMethods", "GET,POST");
        ReflectionTestUtils.setField(config, "corsAllowedHeaders", "*");
        ReflectionTestUtils.setField(config, "corsExposedHeaders", "");
        ReflectionTestUtils.setField(config, "corsAllowCredentials", true); // Even if set to true, must be disabled
        ReflectionTestUtils.setField(config, "corsMaxAge", 1800L);

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertThat(corsConfig).isNotNull();
        // Wildcard must force allowCredentials = false per CORS security standard
        assertThat(corsConfig.getAllowCredentials()).isFalse();
        assertThat(corsConfig.getAllowedOriginPatterns()).contains("*");
    }

    @Test
    @DisplayName("C8 Compliance: Local environment origins (localhost) are parsed and accepted safely")
    void localEnvironmentOriginsParsedSafely() {
        SecurityConfig config = createSecurityConfig();
        ReflectionTestUtils.setField(config, "corsAllowedOrigins", "http://localhost:3000, http://localhost:4200, http://localhost:5173, http://localhost:8080");
        ReflectionTestUtils.setField(config, "corsAllowedMethods", "GET,POST,PUT,DELETE");
        ReflectionTestUtils.setField(config, "corsAllowedHeaders", "Authorization,Content-Type");
        ReflectionTestUtils.setField(config, "corsExposedHeaders", "Referrer-Policy");
        ReflectionTestUtils.setField(config, "corsAllowCredentials", true);
        ReflectionTestUtils.setField(config, "corsMaxAge", 3600L);

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/ping");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).containsExactlyInAnyOrder(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173",
                "http://localhost:8080"
        );
        assertThat(corsConfig.getAllowCredentials()).isTrue();
    }
}
