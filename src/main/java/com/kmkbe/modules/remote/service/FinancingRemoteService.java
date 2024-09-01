package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.modules.remote.request.FinancingSubmissionRequest;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinancingRemoteService {
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;


    public BaseSimpleRemoteResponseDto<ObjectUtils.Null> postedSubmission(
            FinancingSubmissionRequest request
    ) throws JsonProcessingException {
        try {
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    com.kmkbe.core.utils.ObjectUtils.jsonToStr(request),
                    baseRemoteService.apiKeyHeaders()
            );

            final ResponseEntity<BaseSimpleRemoteResponseDto<ObjectUtils.Null>> response = restTemplate.exchange(
                    BaseRemoteService.BASE_URL_MST + "/post/credit/submission",
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("postedSubmission, error {}", e.getMessage());
            throw e;
        }
    }

    public BaseSimpleRemoteResponseDto<ObjectUtils.Null> updateFinancingStatus(
            UpdateFinancingStatusRequest request
    ) throws JsonProcessingException {
        try {
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    com.kmkbe.core.utils.ObjectUtils.jsonToStr(request),
                    baseRemoteService.apiKeyHeaders()
            );

            final ResponseEntity<BaseSimpleRemoteResponseDto<ObjectUtils.Null>> response = restTemplate.exchange(
                    BaseRemoteService.BASE_URL_MST + "/post/credit/approval",
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("updateFinancingStatus, error {}", e.getMessage());
            throw e;
        }
    }



}
