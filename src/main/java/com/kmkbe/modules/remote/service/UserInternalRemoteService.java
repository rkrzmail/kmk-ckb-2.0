package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.service.LdapUrlService;
import com.kmkbe.core.utils.AESUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.UserInternalRemoteDto;
import com.kmkbe.modules.remote.request.ActiveDirectoryRemoteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInternalRemoteService {
    @Lazy
    private final LdapUrlService ldapUrlService;

    @Lazy
    private final RestTemplate restTemplate;

    @Lazy
    private final AuthRemoteService authRemoteService;

    private final ObjectMapper objectMapper;

    /**
     * internal user login
     *
     * @return BaseInternalResponse<InternalUserDto>
     */
    public BaseLdapRemoteResponseDto<UserInternalRemoteDto> validateActiveDirectory(ActiveDirectoryRemoteRequest params) throws JsonProcessingException {
        try {
            final BaseLdapRemoteResponseDto<String> tokenResponse = authRemoteService.fetchAuthJwt();
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(tokenResponse.getData());

            final Map<String, String> obj = new HashMap<>();
            obj.put("Key", AESUtils.encrypt(ObjectUtils.jsonToStr(params)));

            final HttpEntity<Object> request = new HttpEntity<>(
                    obj,
                    headers
            );

            final ResponseEntity<BaseLdapRemoteResponseDto<UserInternalRemoteDto>> response = restTemplate.exchange(
                    ldapUrlService.activeDirectory_validation(),
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            return httpClientErrorException.getResponseBodyAs(new ParameterizedTypeReference<>() {
            });
        } catch (Exception e) {
            log.error("validateActiveDirectory: {}", e.getMessage());
            throw e;
        }
    }
}
