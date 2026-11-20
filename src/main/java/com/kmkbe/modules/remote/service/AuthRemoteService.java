package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.service.LdapUrlService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.modules.remote.request.JwtAuthRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(ldapUrlService.authHeaderUsername, ldapUrlService.authHeaderPassword);
            headers.setContentType(MediaType.APPLICATION_JSON);

            final HttpEntity<JwtAuthRequest> request = new HttpEntity<>(
                    new JwtAuthRequest(
                            ldapUrlService.authHeaderUsername,
                            DateTimeUtils.plusWIB()
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
                return objectMapper.readValue(body, new TypeReference<BaseLdapRemoteResponseDto<String>>() {});
            }

            String token = body;
            if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
            BaseLdapRemoteResponseDto<String> dto = new BaseLdapRemoteResponseDto<>();
            dto.setData(token);
            return dto;
        } catch (Exception e) {
            log.error("fetchAuthJwt: {}", e.getMessage(), e);
            throw new RuntimeException("fetchAuthJwt failed", e);
        }
    }
}
