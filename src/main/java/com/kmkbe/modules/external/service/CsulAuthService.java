package com.kmkbe.modules.external.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.utils.AESUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.external.dto.BaseCsulDto;
import com.kmkbe.modules.external.dto.CsulUserDto;
import com.kmkbe.modules.external.request.ActiveDirectoryRequest;
import com.kmkbe.modules.external.request.CsulAuthRequest;
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
public class CsulAuthService {

    @Value("${csul.sisca.v1.white-list}")
    private String siscaUrlWhiteList;

    @Value("${csul.sisca.v1.auth.username}")
    private String authHeaderUsername;

    @Value("${csul.sisca.v1.auth.password}")
    private String authHeaderPassword;

    private final RestTemplate restTemplate;

    public CsulAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BaseCsulDto<String> fetchAuthJwt() {
        try {
            final String url = siscaUrlWhiteList + "/auth/req-jwt";
            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(authHeaderUsername, authHeaderPassword);

            final HttpEntity<CsulAuthRequest> request = new HttpEntity<>(
                    new CsulAuthRequest(
                            authHeaderUsername,
                            DateTimeUtils.nowMilliSeconds()
                    ),
                    headers
            );

            final ResponseEntity<BaseCsulDto<String>> response = restTemplate.exchange(
                    url,
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

    /**
     * internal user login
     *
     * @return BaseInternalResponse<InternalUserDto>
     */
    public BaseCsulDto<CsulUserDto> validateActiveDirectory(ActiveDirectoryRequest params) throws JsonProcessingException {
        try {
            final BaseCsulDto<String> tokenResponse = fetchAuthJwt();
            final String url = siscaUrlWhiteList + "/activedirectory/validation";
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(tokenResponse.getData());

            final Map<String, String> obj = new HashMap<>();
            obj.put("Key", AESUtils.encrypt(ObjectUtils.jsonToStr(params)));

            final HttpEntity<Object> request = new HttpEntity<>(
                    obj,
                    headers
            );

            final ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (
                    response.getBody() instanceof BaseCsulDto<?> data
                            && data.getData() instanceof CsulUserDto user
            ) {
                return new BaseCsulDto<>(
                        data.getHeader(),
                        user
                );
            }

            throw new IllegalStateException("Failed to login, try again");
        } catch (Exception e) {
            log.error("validateActiveDirectory: {}", e.getMessage());
            throw e;
        }
    }
}
