package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InquiryAgreementByNoCwrRemoteDto;
import com.kmkbe.core.domain.dto.InquiryAgreementCwrDto;
import com.kmkbe.core.domain.dto.InquiryCwrRemoteDto;
import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.remote.request.*;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CwrRemoteService {
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> inquiryCwr(
            InquiryCwrRemoteRequest request
    ) throws JsonProcessingException {
        String jsonStr = "";
        String responseStr = null;
        int statusCode = 200;
        final String url = baseRemoteService.Mou_Generic_GetPagingObjectBySQL();
        try {
            PropCriteriaGenericTypeRequest propCriteria = PropCriteriaGenericTypeRequest.builder()
                    .propName(request.getName())
                    .value(
                            !StringUtil.isNullOrEmpty(request.getCwrNo())
                                    ? request.getCwrNo()
                                    : request.getCustNo()
                    )
                    .build();

            InquiryCwrCriteriaRemoteRequest criteriaRequest = InquiryCwrCriteriaRemoteRequest.builder()
                    .queryString(CriteriaGenericTypeRemoteRequest.QueryString.inquiryCwr())
                    .criteria(List.of(propCriteria))
                    .build();

            jsonStr = ObjectUtils.jsonToStr(criteriaRequest);
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    jsonStr,
                    headers
            );

            final ResponseEntity<BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            statusCode = response.getStatusCode().value();
            responseStr = ObjectUtils.jsonToStr(response.getBody());
            if (StringUtil.isNullOrEmpty(responseStr)) {
                responseStr = objectMapper.writeValueAsString(response.getBody());
            }

            return response.getBody();
        } catch (HttpStatusCodeException httpStatusCodeException) {
            String message = "Failed to inquiry CWR";
            statusCode = httpStatusCodeException.getStatusCode().value();
            responseStr = httpStatusCodeException.getResponseBodyAsString();

            Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
            if (errorObj != null) {
                message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
            }

            if (request.getName() == PropCriteriaGenericTypeRequest.CwrPropName.custNo) {
                message += " By CWR No";
            } else {
                message += " By Cust No";
            }

            throw new RuntimeException("Error while perform action to Confins. Detail:" + message);
        } catch (Exception e) {
            log.error("mstGenericInput: {}", e.getMessage());
            throw e;
        } finally {
            ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
                    .endpointUrl(url)
                    .contentType("application/json")
                    .requestPayload(jsonStr)
                    .responseJson(responseStr)
                    .responseStatus(String.valueOf(statusCode))
                    .build();
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> inquiryAgreementByNoAgreement(
            InquiryAgreementRemoteRequest request
    ) throws JsonProcessingException {
        String jsonStr = "";
        String responseStr = null;
        int statusCode = 200;

        final String url = baseRemoteService.Los_Generic_GetPagingObjectBySQL();
        try {
            PropCriteriaGenericTypeRequest propCriteria = PropCriteriaGenericTypeRequest.builder()
                    .propName(request.getName())
                    .value(request.getAgreementNo())
                    .build();

            InquiryAgreementCriteriaRemoteRequest criteriaRequest = InquiryAgreementCriteriaRemoteRequest.builder()
                    .queryString(CriteriaGenericTypeRemoteRequest.QueryString.inquiryAgreement())
                    .criteria(List.of(propCriteria))
                    .build();

            jsonStr = ObjectUtils.jsonToStr(criteriaRequest);
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    jsonStr,
                    headers
            );

            final ResponseEntity<BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            statusCode = response.getStatusCode().value();
            responseStr = ObjectUtils.jsonToStr(response.getBody());
            if (StringUtil.isNullOrEmpty(responseStr)) {
                responseStr = objectMapper.writeValueAsString(response.getBody());
            }

            return response.getBody();
        } catch (HttpStatusCodeException httpStatusCodeException) {
            String message = "Failed to inquiry Agreement";
            statusCode = httpStatusCodeException.getStatusCode().value();
            responseStr = httpStatusCodeException.getResponseBodyAsString();

            Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
            if (errorObj != null) {
                message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
            }

            throw new RuntimeException("Error while perform action to Confins. Detail:" + message);
        } catch (Exception e) {
            log.error("inquiryAgreement: {}", e.getMessage());
            throw e;
        } finally {
            ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
                    .endpointUrl(url)
                    .contentType("application/json")
                    .requestPayload(jsonStr)
                    .responseJson(responseStr)
                    .responseStatus(String.valueOf(statusCode))
                    .build();
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>> inquiryAgreementByNoCwr(
            String cwrNo
    ) throws JsonProcessingException {
        String jsonStr = "";
        String responseStr = null;
        int statusCode = 200;
        final String url = baseRemoteService.Los_Agreement_GetListAgreementDetailForCwrByCwrNo();
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("trxNo", cwrNo);
            request.put("RequestDateTime", new Date());
            jsonStr = ObjectUtils.jsonToStr(request);
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    jsonStr,
                    headers
            );

            final ResponseEntity<BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            statusCode = response.getStatusCode().value();
            responseStr = ObjectUtils.jsonToStr(response.getBody());
            if (StringUtil.isNullOrEmpty(responseStr)) {
                responseStr = objectMapper.writeValueAsString(response.getBody());
            }

            return response.getBody();
        } catch (HttpStatusCodeException httpStatusCodeException) {
            String message = "Failed to inquiry Agreement By No. CWR";
            statusCode = httpStatusCodeException.getStatusCode().value();
            responseStr = httpStatusCodeException.getResponseBodyAsString();

            Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
            if (errorObj != null) {
                message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
            }

            throw new RuntimeException("Error while perform action to Confins. Detail: " + message);
        } catch (Exception e) {
            log.error("inquiryAgreementByNoCwr: {}", e.getMessage());
            throw e;
        } finally {
            ApiIntegrationLog apiIntegrationLog = ApiIntegrationLog.builder()
                    .endpointUrl(url)
                    .contentType("application/json")
                    .requestPayload(jsonStr)
                    .responseJson(responseStr)
                    .responseStatus(String.valueOf(statusCode))
                    .build();
        }
    }
}
