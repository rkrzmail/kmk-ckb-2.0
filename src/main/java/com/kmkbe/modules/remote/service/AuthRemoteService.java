package com.kmkbe.modules.remote.service;

import com.kmkbe.core.service.LdapUrlService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.remote.dto.BaseRemoteResponseDto;
import com.kmkbe.modules.remote.request.CsulAuthRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    public BaseRemoteResponseDto<String> fetchAuthJwt() {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(ldapUrlService.authHeaderUsername, ldapUrlService.authHeaderPassword);

            final HttpEntity<CsulAuthRequest> request = new HttpEntity<>(
                    new CsulAuthRequest(
                            ldapUrlService.authHeaderUsername,
                            DateTimeUtils.nowMilliSeconds()
                    ),
                    headers
            );

            final ResponseEntity<BaseRemoteResponseDto<String>> response = restTemplate.exchange(
                    ldapUrlService.auth_requestJwt(),
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("fetchAuthJwt: {}", e.getMessage());
            throw e;
        }
    }
}
