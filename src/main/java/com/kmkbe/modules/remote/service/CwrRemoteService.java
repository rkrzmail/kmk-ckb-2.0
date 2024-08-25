package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InquiryCwrRemoteDto;
import com.kmkbe.modules.remote.request.CriteriaGenericTypeRemoteRequest;
import com.kmkbe.modules.remote.request.InquiryCwrCriteriaRemoteRequest;
import com.kmkbe.modules.remote.request.InquiryCwrRemoteRequest;
import com.kmkbe.modules.remote.request.PropCriteriaGenericTypeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CwrRemoteService {
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;

    public BaseMstRemoteResponseDto<InquiryCwrRemoteDto> inquiryCwr(
            InquiryCwrRemoteRequest request
    ) throws JsonProcessingException {
        try {
            PropCriteriaGenericTypeRequest propCriteria = PropCriteriaGenericTypeRequest.builder()
                    .propName(request.getName())
                    .value(request.getCwrNo())
                    .build();

            InquiryCwrCriteriaRemoteRequest criteriaRequest = InquiryCwrCriteriaRemoteRequest.builder()
                    .queryString(CriteriaGenericTypeRemoteRequest.QueryString.inquiryCwr())
                    .criteria(List.of(propCriteria))
                    .build();

            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    ObjectUtils.jsonToStr(criteriaRequest),
                    headers
            );

            final ResponseEntity<BaseMstRemoteResponseDto<InquiryCwrRemoteDto>> response = restTemplate.exchange(
                    baseRemoteService.Fou_Generic_GetPagingObjectBySQL(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("mstGenericInput: {}", e.getMessage());
            throw e;
        }
    }
}
