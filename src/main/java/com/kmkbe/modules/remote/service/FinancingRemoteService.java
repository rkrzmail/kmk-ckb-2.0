package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.core.domain.repository.ApiIntegrationLogRepository;
import com.kmkbe.core.service.BaseRemoteService;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinancingRemoteService {
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;
    private final ApiIntegrationLogRepository apiIntegrationLogRepository;
    private final TransactionTemplate transactionTemplate;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BaseSimpleRemoteResponseDto<ObjectUtils.Null> postedSubmission(
            FinancingSubmissionRequest request
    ) throws Exception {
        String jsonStr = "";
        String responseStr = null;
        int statusCode = 200;
        try {
            jsonStr = com.kmkbe.core.utils.ObjectUtils.jsonToStr(request);
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    jsonStr,
                    baseRemoteService.apiKeyHeaders()
            );

            final ResponseEntity<BaseSimpleRemoteResponseDto<ObjectUtils.Null>> response = restTemplate.exchange(
                    BaseRemoteService.BASE_URL_MST + "/post/credit/submission",
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            statusCode = response.getStatusCode().value();
            responseStr = com.kmkbe.core.utils.ObjectUtils.jsonToStr(response.getBody());
            return response.getBody();
        } catch (HttpStatusCodeException httpStatusCodeException) {
            String message = "Posted Submission Failed";
            statusCode = httpStatusCodeException.getStatusCode().value();
            responseStr = httpStatusCodeException.getResponseBodyAsString();

            Map<String, Object> errorObj = com.kmkbe.core.utils.ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
            if (errorObj != null) {
                message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
            }

            throw new RuntimeException("Error while perform action to MST\n\nDetail:" + message);
        } catch (Exception e) {
            log.error("postedSubmission, error {}", e.getMessage());
            throw new RuntimeException("Posted Submission Failed");
        } finally {
            ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
                    .endpointUrl(BaseRemoteService.BASE_URL_MST + "/post/credit/submission")
                    .contentType("application/json")
                    .requestPayload(jsonStr)
                    .responseJson(responseStr)
                    .responseStatus(String.valueOf(statusCode))
                    .build();
            apiIntegrationLogRepository.save(apiIntegrationLog);
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BaseSimpleRemoteResponseDto<ObjectUtils.Null> updateFinancingStatus(
            UpdateFinancingStatusRequest request
    ) throws JsonProcessingException {
        String jsonStr = "";
        String responseStr = null;
        int statusCode = 200;
        Exception ex;
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

            statusCode = response.getStatusCode().value();
            responseStr = com.kmkbe.core.utils.ObjectUtils.jsonToStr(response.getBody());
            return response.getBody();
        } catch (HttpStatusCodeException httpStatusCodeException) {
            String message = "Posted Submission Failed";
            statusCode = httpStatusCodeException.getStatusCode().value();
            responseStr = httpStatusCodeException.getResponseBodyAsString();

            Map<String, Object> errorObj = com.kmkbe.core.utils.ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
            if (errorObj != null) {
                message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
            }

            throw new RuntimeException("Error while perform action to MST\n\nDetail:" + message);
        } catch (Exception e) {
            log.error("updateFinancingStatus, error {}", e.getMessage());
            throw e;
        } finally {
            ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
                    .endpointUrl(BaseRemoteService.BASE_URL_MST + "/post/credit/approval")
                    .contentType("application/json")
                    .requestPayload(jsonStr)
                    .responseJson(responseStr)
                    .responseStatus(String.valueOf(statusCode))
                    .build();
            apiIntegrationLogRepository.save(apiIntegrationLog);
        }
    }
}
