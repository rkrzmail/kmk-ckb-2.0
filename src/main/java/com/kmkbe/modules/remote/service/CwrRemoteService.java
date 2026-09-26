package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InquiryAgreementByNoCwrRemoteDto;
import com.kmkbe.core.domain.dto.InquiryAgreementCwrDto;
import com.kmkbe.core.domain.dto.InquiryCwrRemoteDto;
import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.remote.request.*;
import com.kmkbe.helpers.utils.Utils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import feign.FeignException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CwrRemoteService {
  @org.springframework.beans.factory.annotation.Value("${feign.confins.url}")
  private String confinsUrl;
  private final ObjectMapper objectMapper;
  private final ConfinsR3FeignClient confinsR3FeignClient;

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> inquiryCwr(
    InquiryCwrRemoteRequest request
  ) throws JsonProcessingException {
    String jsonStr = "";
    String responseStr = null;
    int statusCode = 200;
    final String url = confinsUrl + "/api/mou/v1/Generic/GetPagingObjectBySQL";
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
      final BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> response = confinsR3FeignClient.inquiryCwr(criteriaRequest);

      responseStr = ObjectUtils.jsonToStr(response);
      if (StringUtil.isNullOrEmpty(responseStr)) {
        responseStr = objectMapper.writeValueAsString(response);
      }

      return response;
    } catch (FeignException httpStatusCodeException) {
      String message = "Failed to inquiry CWR";
      statusCode = httpStatusCodeException.status();
      responseStr = httpStatusCodeException.contentUTF8();

      Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.contentUTF8());
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

    final String url = confinsUrl + "/api/los/v1/Generic/GetPagingObjectBySQL";
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
      final BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = confinsR3FeignClient.inquiryAgreement(criteriaRequest);

      responseStr = ObjectUtils.jsonToStr(response);
      if (StringUtil.isNullOrEmpty(responseStr)) {
        responseStr = objectMapper.writeValueAsString(response);
      }

      return response;
    } catch (FeignException httpStatusCodeException) {
      String message = "Failed to inquiry Agreement";
      statusCode = httpStatusCodeException.status();
      responseStr = httpStatusCodeException.contentUTF8();

      Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.contentUTF8());
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
    final String url = confinsUrl + "/api/los/v1/Agrmnt/GetListAgrmntDetailForCwrByCwrNo";
    try {
      Map<String, Object> request = new HashMap<>();
      request.put("trxNo", cwrNo);
      request.put("RequestDateTime", Utils.NowDate());
      jsonStr = ObjectUtils.jsonToStr(request);
      final BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>> response = confinsR3FeignClient.inquiryAgreementByCwr(request);

      responseStr = ObjectUtils.jsonToStr(response);
      if (StringUtil.isNullOrEmpty(responseStr)) {
        responseStr = objectMapper.writeValueAsString(response);
      }

      return response;
    } catch (FeignException httpStatusCodeException) {
      String message = "Failed to inquiry Agreement By No. CWR";
      statusCode = httpStatusCodeException.status();
      responseStr = httpStatusCodeException.contentUTF8();

      Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.contentUTF8());
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
