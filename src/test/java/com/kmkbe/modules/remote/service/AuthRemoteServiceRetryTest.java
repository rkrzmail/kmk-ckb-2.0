package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.service.LdapUrlService;
import com.kmkbe.modules.remote.request.JwtAuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthRemoteServiceRetryTest {
    private RestTemplate restTemplate;
    private AuthRemoteService service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        LdapUrlService urlService = new LdapUrlService();
        urlService.siscaBaseUrlPrivate = "http://sisca";
        urlService.authHeaderUsername = "test-user";
        urlService.authHeaderPassword = "test-password";
        service = new AuthRemoteService(urlService, restTemplate, new ObjectMapper());
    }

    @Test
    void retriesOnceWithFreshTimestampWhenSiscaRejectsTimestamp() {
        when(restTemplate.exchange(eq("http://sisca/auth/req-jwt"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(httpError(HttpStatus.BAD_REQUEST,
                        "{\"header\":{\"success\":{\"isSuccess\":false,"
                                + "\"message\":\"Make sure the request timestamp is up to date.\"}},\"data\":\"\"}"))
                .thenReturn(ResponseEntity.ok("{\"data\":\"jwt-token\"}"));

        long before = Instant.now().getEpochSecond();
        assertThat(service.fetchAuthJwt().getData()).isEqualTo("jwt-token");
        long after = Instant.now().getEpochSecond();

        var requests = org.mockito.ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(2)).exchange(eq("http://sisca/auth/req-jwt"), eq(HttpMethod.POST),
                requests.capture(), eq(String.class));
        for (HttpEntity<?> request : requests.getAllValues()) {
            JwtAuthRequest body = (JwtAuthRequest) request.getBody();
            assertThat(body.requestTimestamp()).isBetween(before, after);
        }
    }

    @Test
    void doesNotRetryOtherAuthFailures() {
        when(restTemplate.exchange(eq("http://sisca/auth/req-jwt"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(httpError(HttpStatus.UNAUTHORIZED, "{\"message\":\"Unauthorized\"}"));

        assertThatThrownBy(() -> service.fetchAuthJwt())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("fetchAuthJwt failed")
                .hasCauseInstanceOf(HttpClientErrorException.class);
        verify(restTemplate, times(1)).exchange(eq("http://sisca/auth/req-jwt"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class));
    }

    @Test
    void rejectsSuccessfulResponseWithoutToken() {
        when(restTemplate.exchange(eq("http://sisca/auth/req-jwt"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"header\":{\"success\":{\"isSuccess\":false}},\"data\":\"\"}"));

        assertThatThrownBy(() -> service.fetchAuthJwt())
                .hasMessage("fetchAuthJwt failed")
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    private static HttpClientErrorException httpError(HttpStatus status, String body) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
