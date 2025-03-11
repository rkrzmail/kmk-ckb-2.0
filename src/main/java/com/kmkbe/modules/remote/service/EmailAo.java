package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.EmailAoDto;
import com.kmkbe.core.domain.dto.UserInternalRemoteDto;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.LdapUrlService;
import com.kmkbe.core.utils.AESUtils;
import com.kmkbe.core.utils.ObjectUtils;
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
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailAo {
    @Lazy
    private final LdapUrlService ldapUrlService;

    @Lazy
    private final RestTemplate restTemplate;

    public List<Map<String, String>> getEmailByPosition(String branchCode, String type, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);

            EmailAoDto requestBody = new EmailAoDto("", branchCode, type);

            HttpEntity<EmailAoDto> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    ldapUrlService.email_ao(),
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getBody() != null && response.getBody().containsKey("data")) {
                return (List<Map<String, String>>) response.getBody().get("data");
            }
        } catch (Exception e) {
            log.error("Error while fetching emails by position: {}", e.getMessage());
        }

        return List.of();
    }
}