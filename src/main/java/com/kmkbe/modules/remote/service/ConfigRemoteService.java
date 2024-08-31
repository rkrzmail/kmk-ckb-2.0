package com.kmkbe.modules.remote.service;

import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.MailRemoteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class ConfigRemoteService {
    @Value("${csul.sisca.v1.internal}")
    private String siscaUrlInternal;

    @Value("${csul.sisca.v1.white-list}")
    private String siscaUrlWhiteList;

    private final RestTemplate restTemplate;
    private final AuthRemoteService authRemoteService;

    public ConfigRemoteService(RestTemplate restTemplate, AuthRemoteService authRemoteService) {
        this.restTemplate = restTemplate;
        //this.cacheManager = cacheManager;
        this.authRemoteService = authRemoteService;
    }

    public MailRemoteDto fetchEmailInfo() {
        try {
            final BaseLdapRemoteResponseDto<String> tokenResponse = authRemoteService.fetchAuthJwt();

            final String url = siscaUrlWhiteList + "/authconfig/mail/office365";
            final HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenResponse.getData());

            final HttpEntity<Object> request = new HttpEntity<>(
                    null,
                    headers
            );

            final ResponseEntity<MailRemoteDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("fetchEmailInfo: {}", e.getMessage());
            throw e;
        }
    }
}
