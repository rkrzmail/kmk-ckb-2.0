package com.kmkbe.modules.confinsr3.service;


import com.kmkbe.adapter.ApiConfinsR3Adapter;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.response.CwrRecordResponse;
import com.kmkbe.feign.model.request.PagingObjectBySQLRequest;
import com.kmkbe.feign.model.response.ZipCodeResponse;
import com.kmkbe.feign.utils.ConfinsR3ApiResponseWrapper;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.helpers.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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

  /**
   *
   * @param pageNo
   * @param pageSize
   * @param value
   * @return
   */
  public BaseResponseBuilder<List<CwrRecordResponse>> findCwrByCustomer(Integer pageNo,Integer pageSize,String value){
    ConfinsR3ApiResponseWrapper<CwrRecordResponse> response= apiConfinsR3Adapter.getCwrByCustomer(PagingObjectBySQLRequest.builder()
        .includeCount(true)
        .includeData(true)
        .isLoading(true)
        .queryString(PagingObjectBySQLRequest.QueryStringQueryDto.builder()
          .name("searhCwrInquiry")
          .whereQuery(Arrays.asList("FACTORING"))
          .build())
        .rowVersion("")
        .integrationObj(null)
        .joinType("INNER")
        .pageNo(pageNo)
        .rowPerPage(pageSize)
        .orderBy(null)
        .criteria(Arrays.asList(PagingObjectBySQLRequest.CriterionDto.builder()
            .low(0)
            .high(0)
            .dataType("text")
            .isCriteriaDataTable("false")
            .propName("COALESCE(G.CUST_NO,CC.CUST_NO)")
            .value(value)
            .restriction("Eq")
          .build()))
        .requestDateTime(CommonUtils.generateDate("DATE_FORMAT_YYYY_MM_DD"))
      .build());
    if(!response.getCode().equals("200")){
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", response.getMessage());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_01, ErrorConstant.ERROR_MESSAGE_81);
    }
    List<CwrRecordResponse> recordResponseList = response.getData().stream().toList();

    return new BaseResponseBuilder<>(true,AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, recordResponseList);
  }
}
