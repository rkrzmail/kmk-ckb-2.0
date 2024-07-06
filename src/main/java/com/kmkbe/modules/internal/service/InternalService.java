package com.kmkbe.modules.internal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.utils.AESUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.JsonUtils;
import com.kmkbe.modules.internal.dto.InternalUserDto;
import com.kmkbe.modules.internal.request.ActiveDirectoryRequest;
import com.kmkbe.modules.internal.request.InternalAuthRequest;
import com.kmkbe.modules.internal.dto.BaseInternalResponse;
import com.kmkbe.modules.internal.dto.InternalMailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SignatureException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class InternalService {
    private static final String RESPONSE_TOKEN_CACHE_KEY = "response_token_cache_key";
    private static final String TOKEN_CACHE_KEY = "token_cache_key";
    private static final String EMAIL_CACHE_KEY = "email_cache_key";

    @Value("${csul.sisca.v1.internal}")
    private String siscaUrlInternal;

    @Value("${csul.sisca.v1.white-list}")
    private String siscaUrlWhiteList;

    @Value("${csul.sisca.v1.auth.username}")
    private String authHeaderUsername;

    @Value("${csul.sisca.v1.auth.password}")
    private String authHeaderPassword;

    private final RestTemplate restTemplate;
    //private final CacheManager cacheManager;

    public InternalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        //this.cacheManager = cacheManager;
    }

    public BaseInternalResponse<String> fetchAuthJwt() throws SignatureException {
        //isAuthenticated();

        final String url = siscaUrlWhiteList + "/auth/req-jwt";
        final HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(authHeaderUsername, authHeaderPassword);

        final HttpEntity<InternalAuthRequest> request = new HttpEntity<>(
                new InternalAuthRequest(
                        authHeaderUsername,
                        DateTimeUtils.nowMilliSeconds()
                ),
                headers
        );

        final ResponseEntity<BaseInternalResponse<String>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody();
    }

    public InternalMailDto fetchEmailInfo() throws SignatureException {
        //isAuthenticated();

        final BaseInternalResponse<String> tokenResponse = fetchAuthJwt();

        final String url = siscaUrlWhiteList + "/authconfig/mail/appinfo";
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenResponse.getData());

        final HttpEntity<Object> request = new HttpEntity<>(
                null,
                headers
        );

        final ResponseEntity<InternalMailDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody();
    }

    /**
     * internal user login
     *
     * @return BaseInternalResponse<InternalUserDto>
     * @throws SignatureException kmk jwt validation
     * @throws ParseException     parse exception
     */
    public BaseInternalResponse<InternalUserDto> validateActiveDirectory(ActiveDirectoryRequest params) throws SignatureException, JsonProcessingException {
        isAuthenticated();

        final BaseInternalResponse<String> tokenResponse = fetchAuthJwt();

        final String url = siscaUrlWhiteList + "/activedirectory/validation";
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(tokenResponse.getData());

        final Map<String, String> obj = new HashMap<>();
        obj.put("Key", AESUtils.encrypt(JsonUtils.jsonToStr(params)));

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
                response.getBody() instanceof BaseInternalResponse<?> data
                        && data.getData() instanceof InternalUserDto user
        ) {
            return new BaseInternalResponse<>(
                    data.getHeader(),
                    user
            );
        }

        throw new IllegalStateException("Failed to login, try again");
    }

    private void isAuthenticated() throws SignatureException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new SignatureException();
        }

        if (authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            throw new SignatureException();
        }
    }
}
