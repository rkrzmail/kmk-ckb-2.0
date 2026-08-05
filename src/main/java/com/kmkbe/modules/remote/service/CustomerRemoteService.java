package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.core.domain.dto.CustomerRemoteDto;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import com.kmkbe.modules.remote.request.ExistingCustomerRequest;
import com.kmkbe.modules.remote.request.InquiryRequest;
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

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerRemoteService {
  private final RestTemplate restTemplate;
  private final BaseRemoteService baseRemoteService;
  private final ObjectMapper objectMapper;
  // private final ApiSbuRepository apiSbuRepository;

  /**
   * <p>current usecase CustomerSignUp</p>
   * <p>Map CustNo to CustNo of @Customer</p>
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public CustomerRemoteDto validateExisting(ExistingCustomerRequest params) throws JsonProcessingException {
    String jsonStr = "";
    String responseStr = null;
    int statusCode = 200;
//        final String url = baseRemoteService.CustObj_GetListKeyValueActiveByCode();
    final String url = baseRemoteService.confinsFouCalculcate;
    try {
      jsonStr = ObjectUtils.jsonToStr(params);
      //params.setRandom(DateTimeUtils.now()+"");
      final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
      final HttpEntity<String> requestArgs = new HttpEntity<>(
        jsonStr,
        headers
      );
      log.info("ini Request Body: {}", jsonStr);
      log.info("ini Header: {}", headers);


      final ResponseEntity<CustomerRemoteDto> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        requestArgs,
        new ParameterizedTypeReference<>() {
        }
      );

      log.info("ini URL: {}", url);


      statusCode = response.getStatusCode().value();
      responseStr = ObjectUtils.jsonToStr(response.getBody());
      if (StringUtil.isNullOrEmpty(responseStr)) {
        responseStr = objectMapper.writeValueAsString(response.getBody());
      }
      log.info("Response: {}", responseStr);

      return response.getBody();
    } catch (HttpStatusCodeException httpStatusCodeException) {
      String message = "Failed to validate Existing Customer";
      statusCode = httpStatusCodeException.getStatusCode().value();
      responseStr = httpStatusCodeException.getResponseBodyAsString();

      Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
      if (errorObj != null) {
        message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
      }


      throw new RuntimeException("Error while perform action to Confins\nDetail:" + message);
    } catch (Exception e) {
      log.error("validateExistingCustomer, error {}", e.getMessage());
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
  public BaseSimpleRemoteResponseDto<InquiryVendorRemoteDto> inquiryVendorByBouwheer(
    LoanSubmissionService.VendorTokenExtractor vendorTokenExtractor
  ) throws JsonProcessingException {


    //perubahan di core BE utama
    String vendorCode = vendorTokenExtractor.getVendorCode();
    String bouwheerCode = vendorTokenExtractor.getBouwheerCode().toString();

        /*Optional<ApiSbu>  apiSbu = apiSbuRepository.findByBouwheerCode(vendorTokenExtractor.getBouwheerCode());
        if (apiSbu.isPresent()){
            ApiSbu sbu = apiSbu.get();



        }*/

    String jsonStr = "";
    String responseStr = null;
    int statusCode = 200;
    final String url = baseRemoteService.getBaseMst() + "/vendor/byVendorId";
    try {
      jsonStr = ObjectUtils.jsonToStr(InquiryRequest.builder()
        .vendorCode(vendorCode)
        .build());
      final HttpEntity<String> requestArgs = new HttpEntity<>(
        jsonStr,
        baseRemoteService.apiKeyHeaders()
      );

      final ResponseEntity<BaseSimpleRemoteResponseDto<InquiryVendorRemoteDto>> response = restTemplate.exchange(
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
      String message = "Failed inquiry vendor";
      statusCode = httpStatusCodeException.getStatusCode().value();
      responseStr = httpStatusCodeException.getResponseBodyAsString();

      Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
      if (errorObj != null) {
        message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
      }

      throw new RuntimeException("Error while perform action to MST\nDetail:" + message);
    } catch (Exception e) {
      log.error("inquiryVendor, error {}", e.getMessage());
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
  public BaseSimpleRemoteResponseDto<InquiryVendorRemoteDto> inquiryVendor(
    String vendorCode
  ) throws JsonProcessingException {


    String jsonStr = "";
    String responseStr = null;
    int statusCode = 200;
    final String url = baseRemoteService.getBaseMst() + "/vendor/byVendorId";
    try {
      jsonStr = ObjectUtils.jsonToStr(InquiryRequest.builder()
        .vendorCode(vendorCode)
        .build());
      final HttpEntity<String> requestArgs = new HttpEntity<>(
        jsonStr,
        baseRemoteService.apiKeyHeaders()
      );

      final ResponseEntity<BaseSimpleRemoteResponseDto<InquiryVendorRemoteDto>> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        requestArgs,
        new ParameterizedTypeReference<>() {
        }
      );

      statusCode = response.getStatusCode().value();
      responseStr = ObjectUtils.jsonToStr(response.getBody());
      log.error("Error API {} request {} response {} ",url,requestArgs, responseStr);
      if (StringUtil.isNullOrEmpty(responseStr)) {
        responseStr = objectMapper.writeValueAsString(response.getBody());
      }
      return response.getBody();
    } catch (HttpStatusCodeException httpStatusCodeException) {
      String message = "Failed inquiry vendor";
      statusCode = httpStatusCodeException.getStatusCode().value();
      responseStr = httpStatusCodeException.getResponseBodyAsString();

      Map<String, Object> errorObj = ObjectUtils.strToJson(httpStatusCodeException.getResponseBodyAsString());
      if (errorObj != null) {
        message = errorObj.get("message") != null ? (String) errorObj.get("message") : message;
      }

      throw new RuntimeException("Error while perform action to MST\nDetail:" + message);
    } catch (Exception e) {
      log.error("inquiryVendor, error {}", e.getMessage());
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
