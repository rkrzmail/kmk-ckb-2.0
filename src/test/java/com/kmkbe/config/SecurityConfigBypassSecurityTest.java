package com.kmkbe.config;

import com.kmkbe.core.callback.UnauthorizedEntryPoint;
import com.kmkbe.core.middleware.ApiKeyAuthenticationFilter;
import com.kmkbe.core.middleware.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigBypassSecurityTest {

    @Mock private AuthenticationProvider authenticationProvider;
    @Mock private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Mock private RsaKeyConfigProperties rsaKeyConfigProperties;
    @Mock private UserDetailsService userDetailsService;
    @Mock private UnauthorizedEntryPoint unauthorizedEntryPoint;

    @Test
    @DisplayName("WebSecurityCustomizer must only ignore static resources and not sensitive APIs")
    void webSecurityCustomizerDoesNotIgnoreSensitiveEndpoints() throws Exception {
        SecurityConfig config = new SecurityConfig(
                authenticationProvider,
                jwtAuthenticationFilter,
                rsaKeyConfigProperties,
                userDetailsService,
                unauthorizedEntryPoint
        );

        WebSecurityCustomizer customizer = config.webSecurityCustomizer();
        assertThat(customizer).isNotNull();

        WebSecurity webSecurity = mock(WebSecurity.class);
        WebSecurity.IgnoredRequestConfigurer ignoredConfigurer = mock(WebSecurity.IgnoredRequestConfigurer.class);
        when(webSecurity.ignoring()).thenReturn(ignoredConfigurer);

        customizer.customize(webSecurity);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(ignoredConfigurer).requestMatchers(captor.capture());

        List<String> ignoredPatterns = Arrays.asList(captor.getValue());

        // Memastikan /static/** di-ignore, tapi API sensitive dan backdoor wildcard TIDAK di-ignore
        assertThat(ignoredPatterns).contains("/static/**");
        assertThat(ignoredPatterns).doesNotContain(
                "/api/v*/auth/**",
                "/actuator/**",
                "/api-docs/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/public/storage/**",
                "/api/public/**"
        );

        // Uji pattern whitelist di JwtAuthenticationFilter: /api/v1/auth/sign-out TIDAK boleh masuk whitelist publik
        List<String> whitelist = Arrays.asList(JwtAuthenticationFilter.ENDPOINTS_WHITELIST);

        assertThat(whitelist).doesNotContain("/api/v1/auth/sign-out");
        assertThat(whitelist).doesNotContain("/api/v1/auth/**");
        assertThat(whitelist).doesNotContain("/api/public/**");
        assertThat(whitelist).doesNotContain("/public/storage/**");

        // Memastikan endpoint auth publik yang diizinkan hanya yang spesifik
        assertThat(whitelist).contains(
                "/api/v1/auth/sign-in",
                "/api/v1/auth/sign-up",
                "/api/v1/auth/forgot-pin",
                "/api/v1/auth/refresh-token"
        );
    }
}
