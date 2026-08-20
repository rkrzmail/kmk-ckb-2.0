package com.kmkbe.modules.remote.service;

import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.MailRemoteDto;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


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

            final String url = siscaUrlWhiteList + "/authconfig/mail/hrs-config";
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
                    MailRemoteDto.class
            );

            System.out.println("Successfully get email config");

            return response.getBody();
        } catch (Exception e) {
            log.error("fetchEmailInfo: {}", e.getMessage());
            throw e;
        }
    }
    public MailPositionDto getEmailByPosition(String employeeCode, String branchCode, String type  ) {
        try {
            final BaseLdapRemoteResponseDto<String> tokenResponse = authRemoteService.fetchAuthJwt();
            final String url = siscaUrlWhiteList + "/user-management/get-ao-email";
            final HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenResponse.getData());
            headers.setContentType(MediaType.APPLICATION_JSON);
            final Map<String, String> body  = new HashMap<>();
            body.put("employeeCode", employeeCode);
            body.put("branchCode", branchCode);
            body.put("type", type);

            final HttpEntity<Object> request = new HttpEntity<>(
                    body,
                    headers
            );

            final ResponseEntity<MailPositionDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            System.out.println("Successfully getEmailByPosition");

            return response.getBody();
        } catch (Exception e) {
            log.error("getEmailByPosition: {}", e.getMessage());
            throw e;
        }
    }
}
