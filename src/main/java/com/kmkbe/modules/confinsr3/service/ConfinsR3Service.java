package com.kmkbe.modules.confinsr3.service;


import com.kmkbe.adapter.ApiConfinsR3Adapter;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.response.ZipCodeResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConfinsR3Service {

  private final ApiConfinsR3Adapter apiConfinsR3Adapter;

  public ConfinsR3Service(ApiConfinsR3Adapter apiConfinsR3Adapter) {
    this.apiConfinsR3Adapter = apiConfinsR3Adapter;
  }

  /**
   * @param zipcode
   * @return
   */
  public BaseResponseBuilder<ZipCodeResponse> findZipcode(String zipcode){
    ZipCodeResponse response = apiConfinsR3Adapter.getZipcode(zipcode.trim());
    if(response ==null){
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", zipcode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_01, ErrorConstant.ERROR_MESSAGE_81);
    }

    return new BaseResponseBuilder<>(true,AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, response);
  }
}
