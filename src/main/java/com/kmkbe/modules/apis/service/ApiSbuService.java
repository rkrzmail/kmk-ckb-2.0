package com.kmkbe.modules.apis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.model.ApiSbu;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.core.domain.repository.ApiSbuRepository;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.service.JwtValidatorService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.helpers.constant.ErrorConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Optional;

@Slf4j
@Service
public class ApiSbuService {

  private final ApiCsulAdapter apiCsulAdapter;
  private final ObjectMapper objectMapper;
  private final ApiSbuRepository apiSbuRepository;
  private final JwtValidatorService jwtValidatorService;

  public ApiSbuService(ApiCsulAdapter apiCsulAdapter, ObjectMapper objectMapper, ApiSbuRepository apiSbuRepository, JwtValidatorService jwtValidatorService) {
    this.apiCsulAdapter = apiCsulAdapter;
    this.objectMapper = objectMapper;
    this.apiSbuRepository = apiSbuRepository;
    this.jwtValidatorService = jwtValidatorService;
  }

  /**
   *
   * @param apiKey
   * @param jwtToken
   * @return
   * @throws IOException
   */
  public ValidationResponse apiValidation(String apiKey, String jwtToken) throws IOException {
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      throw new IllegalApiKeyException();
    }
    ValidationResponse validationResponse = jwtValidatorService.validate(apiKey, jwtToken, apiSbu.get());
    String bouwheerCode = apiSbu.get().getBouwheerCode().toString();
    if (!validationResponse.getBouwheer().equalsIgnoreCase(bouwheerCode)) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", bouwheerCode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80,"Invalid Bouwheer Code");
    }
    if (validationResponse.getExp() < (System.currentTimeMillis() / 1000)) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", bouwheerCode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80,"Expired Token");
    }
    return validationResponse;
  }

  /**
   *
   * @return
   * @throws Exception
   */
  public CommonResult<CsulInquiryInvoiceRemoteDto> inquiryListPostedInvoice(String vendorCode) {
    CsulInquiryInvoiceRemoteDto inquiryInvoice = apiCsulAdapter.findListPostedInvoice(vendorCode);
    return new CommonResult<CsulInquiryInvoiceRemoteDto>().success(
      inquiryInvoice,
      ""
    );
  }
}
