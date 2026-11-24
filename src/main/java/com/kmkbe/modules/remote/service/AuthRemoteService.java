package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.service.LdapUrlService;
import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.modules.remote.request.JwtAuthRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthRemoteService {
    @Lazy
    private final LdapUrlService ldapUrlService;

    @Lazy
    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public BaseLdapRemoteResponseDto<String> fetchAuthJwt() {
        try {
            try {
                return requestAuthJwt();
            } catch (HttpStatusCodeException e) {
                if (!isTimestampRejected(e)) {
                    throw e;
                }
                log.warn("SISCA rejected JWT request timestamp; retrying once with a fresh timestamp");
                return requestAuthJwt();
            }
        } catch (Exception e) {
            log.error("fetchAuthJwt: {}", e.getMessage(), e);
            throw new RuntimeException("fetchAuthJwt failed", e);
        }
    }

    private BaseLdapRemoteResponseDto<String> requestAuthJwt() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(ldapUrlService.authHeaderUsername, ldapUrlService.authHeaderPassword);
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<JwtAuthRequest> request = new HttpEntity<>(
                new JwtAuthRequest(
                        ldapUrlService.authHeaderUsername,
                        Instant.now().getEpochSecond()
                ),
                headers
        );

        final ResponseEntity<String> response = restTemplate.exchange(
                ldapUrlService.auth_requestJwt(),
                HttpMethod.POST,
                request,
                String.class
        );

        String body = response.getBody();
        if (body == null || body.isBlank()) {
            throw new IllegalStateException("Empty JWT response");
        }
        body = body.trim();
        if (body.startsWith("{")) {
            BaseLdapRemoteResponseDto<String> dto = objectMapper.readValue(
                    body, new TypeReference<BaseLdapRemoteResponseDto<String>>() {});
            if (dto.getData() == null || dto.getData().isBlank()
                    || (dto.getHeader() != null && dto.getHeader().getSuccess() != null
                    && Boolean.FALSE.equals(dto.getHeader().getSuccess().getIsSuccess()))) {
                throw new IllegalStateException("SISCA JWT response did not contain a valid token");
            }
            return dto;
        }

        String token = body;
        if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
            token = token.substring(1, token.length() - 1);
        }
        if (token.isBlank()) {
            throw new IllegalStateException("Empty JWT token");
        }
        BaseLdapRemoteResponseDto<String> dto = new BaseLdapRemoteResponseDto<>();
        dto.setData(token);
        return dto;
    }

    private boolean isTimestampRejected(HttpStatusCodeException e) {
        if (e.getStatusCode().value() != 400) {
            return false;
        }
        try {
            JsonNode response = objectMapper.readTree(e.getResponseBodyAsString());
            return "Make sure the request timestamp is up to date."
                    .equalsIgnoreCase(response.at("/header/success/message").asText());
        } catch (Exception ignored) {
            return false;
        }
    }
}
