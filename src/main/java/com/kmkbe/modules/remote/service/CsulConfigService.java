package com.kmkbe.modules.remote.service;

import com.kmkbe.modules.remote.dto.BaseCsulDto;
import com.kmkbe.modules.remote.dto.CsulMailDto;
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
public class CsulConfigService {
    @Value("${csul.sisca.v1.internal}")
    private String siscaUrlInternal;

    @Value("${csul.sisca.v1.white-list}")
    private String siscaUrlWhiteList;

    private final RestTemplate restTemplate;
    private final CsulAuthService csulAuthService;

    public CsulConfigService(RestTemplate restTemplate, CsulAuthService csulAuthService) {
        this.restTemplate = restTemplate;
        //this.cacheManager = cacheManager;
        this.csulAuthService = csulAuthService;
    }

    public CsulMailDto fetchEmailInfo() {
        try {
            final BaseCsulDto<String> tokenResponse = csulAuthService.fetchAuthJwt();

            final String url = siscaUrlWhiteList + "/authconfig/mail/appinfo";
            final HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenResponse.getData());

            final HttpEntity<Object> request = new HttpEntity<>(
                    null,
                    headers
            );

            final ResponseEntity<CsulMailDto> response = restTemplate.exchange(
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
