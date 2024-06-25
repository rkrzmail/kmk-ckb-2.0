package com.kmkbe.modules.common.service;

import com.kmkbe.core.config.AppCacheConfig;
import com.kmkbe.modules.common.request.CsulLoginRequest;
import com.kmkbe.modules.common.response.BaseCsulResponse;
import com.kmkbe.modules.common.response.CsulMailResponse;
import com.kmkbe.modules.customer.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SignatureException;
import java.text.ParseException;
import java.time.LocalTime;


@Service
@Slf4j
public class InternalCsulService {
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
    private final AuthService authService;
    private final CacheManager cacheManager;

    public InternalCsulService(
            RestTemplate restTemplate,
            AuthService authService,
            CacheManager cacheManager
    ) {
        this.restTemplate = restTemplate;
        this.authService = authService;
        this.cacheManager = cacheManager;
    }

    public BaseCsulResponse<String> requestAuthJwt() throws SignatureException, ParseException {
        isAuthenticated();

        final Cache cache = cacheManager.getCache(AppCacheConfig.CSUL_CACHE_NAME);
        if (cache != null && cache.get(RESPONSE_TOKEN_CACHE_KEY) != null) {
            @SuppressWarnings("unchecked")
            BaseCsulResponse<String> loadedCache = (BaseCsulResponse<String>) cache.get(RESPONSE_TOKEN_CACHE_KEY).get();
            return loadedCache;
        }

        final String url = siscaUrlWhiteList + "/auth/req-jwt";
        final HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(authHeaderUsername, authHeaderPassword);

        final LocalTime time = LocalTime.now();
        long nowMillis = (time.getHour() * 60 * 60 * 1000) + time.getMinute() * 60 * 1000;

        final HttpEntity<CsulLoginRequest> request = new HttpEntity<>(
                new CsulLoginRequest(
                        authHeaderUsername,
                        nowMillis
                ),
                headers
        );

        final ResponseEntity<BaseCsulResponse<String>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        final BaseCsulResponse<String> result = response.getBody();
        if (result != null && result.getData() != null) {
            String token = result.getData();
            if (cache != null) {
                cache.put(RESPONSE_TOKEN_CACHE_KEY, result);
                cache.put(TOKEN_CACHE_KEY, token);
            }
        }

        return result;
    }

    public CsulMailResponse fetchEmailInfo() throws SignatureException, ParseException {
        isAuthenticated();

        final Cache cache = cacheManager.getCache(AppCacheConfig.CSUL_CACHE_NAME);
        String token;
        if (cache == null || cache.get(TOKEN_CACHE_KEY) == null || cache.get(TOKEN_CACHE_KEY).get() == null) {
            requestAuthJwt();
        }

        token = cache.get(TOKEN_CACHE_KEY).get().toString();

        final String url = siscaUrlWhiteList + "/authconfig/mail/appinfo";
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        final HttpEntity<Object> request = new HttpEntity<>(
                null,
                headers
        );

        final ResponseEntity<CsulMailResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        final CsulMailResponse result = response.getBody();
        cache.put(EMAIL_CACHE_KEY, result);

        return result;
    }

    private boolean isAuthenticated() throws SignatureException {
        Authentication authentication = authService.getAuthentication();
        if (authentication == null) {
            throw new SignatureException();
        }

        if (authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            throw new SignatureException();
        }

        return true;
    }
}
