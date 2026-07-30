package com.kmkbe.modules.confinsr3.service;


import com.kmkbe.adapter.ApiConfinsR3Adapter;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.*;
import com.kmkbe.feign.model.request.ConfinsR3GetCustomerNoRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetKeyValueActiveByCodeRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.response.ConfinsR3ApiResponseWrapper;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.helpers.utils.CommonUtils;
import com.kmkbe.modules.confinsr3.model.response.*;
import com.kmkbe.modules.master.request.AreaPageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
  public BaseResponseBuilder<GetZipCodeResponse> findZipcode(String zipcode) {
    ConfinsR3GetZipCodeDto response = apiConfinsR3Adapter.getZipcode(zipcode.trim());
    if (response == null) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", zipcode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, GetZipCodeResponse.builder()
      .refZipcodeId(response.getRefZipcodeId())
      .areaCode1(response.getAreaCode1())
      .areaCode2(response.getAreaCode2())
      .city(response.getCity())
      .zipcode(response.getZipcode())
      .refProvDistrictId(response.getRefProvDistrictId())
      .provDistrictName(response.getProvDistrictName())
      .subZipcode(response.getSubZipcode())
      .phoneArea(response.getPhoneArea())
      .build());
  }

  public BaseResponseBuilder<List<GetZipCodeResponse>> allZipcode(Integer pageNo, Integer pageSize) {
    ConfinsR3ApiResponseWrapper<ConfinsR3GetZipCodeDto> response = apiConfinsR3Adapter.getAllZipcode(ConfinsR3GetPagingObjectBySQLRequest.builder()
      .includeCount(true)
      .includeData(true)
      .isLoading(true)
      .queryString(ConfinsR3GetPagingObjectBySQLRequest.QueryStringQueryDto.builder()
        .name("lookupZipcode")
        .build())
      .rowVersion("")
      .integrationObj(null)
      .joinType("INNER")
      .pageNo(pageNo)
      .rowPerPage(pageSize)
      .orderBy(null)
      .criteria(List.of())
      .requestDateTime(CommonUtils.generateDate(AppConstants.DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ))
      .build());

    if (!response.getCode().equals("200")) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", response.getMessage());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    List<GetZipCodeResponse> recordResponseList = response.getData().stream().map(item -> {
      GetZipCodeResponse cwrRecordResponse = new GetZipCodeResponse();
      BeanUtils.copyProperties(item, cwrRecordResponse);
      return cwrRecordResponse;
    }).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, recordResponseList);
  }

  public BaseResponseBuilder<List<GetZipCodeResponse>> pageZipcode(AreaPageRequest request) {

    log.info("Gate zipcode by criteria {} ",request.getCriteria().stream().toList());
    List<ConfinsR3GetPagingObjectBySQLRequest.CriterionDto> criterionDtos = new ArrayList<>();
    request.getCriteria().stream().toList().forEach(item -> {
      ConfinsR3GetPagingObjectBySQLRequest.CriterionDto type = new ConfinsR3GetPagingObjectBySQLRequest.CriterionDto();
      type.setLow(0);
      type.setHigh(0);
      type.setDataType("");
      type.setIsCriteriaDataTable(false);
      type.setRestriction("Eq");
      type.setPropName(item.getPropName());
      type.setValue(item.getValue());

      criterionDtos.add(type);
    });

    ConfinsR3ApiResponseWrapper<ConfinsR3GetZipCodeDto> response = apiConfinsR3Adapter.getAllZipcode(ConfinsR3GetPagingObjectBySQLRequest.builder()
      .includeCount(true)
      .includeData(true)
      .isLoading(true)
      .queryString(ConfinsR3GetPagingObjectBySQLRequest.QueryStringQueryDto.builder()
        .name("lookupZipcode")
        .build())
      .rowVersion("")
      .integrationObj(null)
      .joinType("INNER")
      .pageNo(request.getPageNo())
      .rowPerPage(request.getPageSize())
      .orderBy(null)
      .criteria(criterionDtos)
      .requestDateTime(CommonUtils.generateDate(AppConstants.DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ))
      .build());

    if (!response.getCode().equals("200")) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", response.getMessage());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    List<GetZipCodeResponse> recordResponseList = response.getData().stream().map(item -> {
      GetZipCodeResponse cwrRecordResponse = new GetZipCodeResponse();
      BeanUtils.copyProperties(item, cwrRecordResponse);
      return cwrRecordResponse;
    }).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, recordResponseList);
  }

  /**
   *
   * @param pageNo
   * @param pageSize
   * @param value
   * @return
   */
  public BaseResponseBuilder<List<GetCwrRecordResponse>> findCwrByCustomer(Integer pageNo, Integer pageSize, String value) {
    ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrCustomerDto> response = apiConfinsR3Adapter.getCwrByCustomer(ConfinsR3GetPagingObjectBySQLRequest.builder()
      .includeCount(true)
      .includeData(true)
      .isLoading(true)
      .queryString(ConfinsR3GetPagingObjectBySQLRequest.QueryStringQueryDto.builder()
        .name("searhCwrInquiry")
        .whereQuery(Arrays.asList("FACTORING"))
        .build())
      .rowVersion("")
      .integrationObj(null)
      .joinType("INNER")
      .pageNo(pageNo)
      .rowPerPage(pageSize)
      .orderBy(null)
      .criteria(Arrays.asList(ConfinsR3GetPagingObjectBySQLRequest.CriterionDto.builder()
        .low(0)
        .high(0)
        .dataType("text")
        .isCriteriaDataTable(false)
        .propName("COALESCE(G.CUST_NO,CC.CUST_NO)")
        .value(value)
        .restriction("Eq")
        .build()))
      .requestDateTime(CommonUtils.generateDate(AppConstants.DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ))
      .build());
    if (!response.getCode().equals("200")) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", response.getMessage());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    List<GetCwrRecordResponse> recordResponseList = response.getData().stream().map(item -> {
      GetCwrRecordResponse cwrRecordResponse = new GetCwrRecordResponse();
      BeanUtils.copyProperties(item, cwrRecordResponse);
      return cwrRecordResponse;
    }).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, recordResponseList);
  }

  /**
   *
   * @param pageNo
   * @param pageSize
   * @param value
   * @return
   */
  public BaseResponseBuilder<List<GetCustomerResponse>> findByCustomer(Integer pageNo, Integer pageSize, String value) {
    ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerDto> response = apiConfinsR3Adapter.getByCustomer(ConfinsR3GetPagingObjectBySQLRequest.builder()
      .includeCount(true)
      .includeData(true)
      .isLoading(true)
      .queryString(ConfinsR3GetPagingObjectBySQLRequest.QueryStringQueryDto.builder()
        .name("searchCustomerV2X")
        .build())
      .rowVersion("")
      .integrationObj(null)
      .joinType("INNER")
      .pageNo(pageNo)
      .rowPerPage(pageSize)
      .orderBy(null)
      .criteria(List.of( // Or new ArrayList<>()
          ConfinsR3GetPagingObjectBySQLRequest.CriterionDto.builder()
            .low(0)
            .high(0)
            .dataType("text")
            .isCriteriaDataTable(false)
            .propName("C.CUST_NAME")
            .value("%".concat(value).concat("%"))
            .restriction("Like")
            .build(),
        ConfinsR3GetPagingObjectBySQLRequest.CriterionDto.builder()
            .low(0)
            .high(0)
            .dataType("text")
            .isCriteriaDataTable(false)
            .propName("C.CUST_NAME")
            .value("%".concat(value).concat("%"))
            .restriction("Like")
            .build()
        )
      )
      .requestDateTime(CommonUtils.generateDate(AppConstants.DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ))
      .build());
    if (!response.getCode().equals("200")) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", response.getMessage());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    List<GetCustomerResponse> recordResponseList = response.getData().stream().map(item -> {
      GetCustomerResponse cwrRecordResponse = new GetCustomerResponse();
      BeanUtils.copyProperties(item, cwrRecordResponse);
      return cwrRecordResponse;
    }).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, recordResponseList);
  }

  /**
   * General
   * @param custNo
   * @return
   */
  public BaseResponseBuilder<GetCustomerNoResponse> findByCustomerNo(String custNo) {
    ConfinsR3GetCustomerNoDto response = apiConfinsR3Adapter.getByCustomerNo(ConfinsR3GetCustomerNoRequest.builder()
      .requestDateTime(CommonUtils.generateDate(AppConstants.DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ))
      .rowVersion("")
      .custNo(custNo)
      .build());
    if (response == null) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", custNo);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    GetCustomerNoResponse getCustomerNoResponse = new GetCustomerNoResponse();
    BeanUtils.copyProperties(response,getCustomerNoResponse);
    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, getCustomerNoResponse);
  }

  /**
   *
   * @param typeCode
   * @return
   */
  public BaseResponseBuilder<List<GetKeyValueActiveByCodeResponse>> findKeyValueByCode(String typeCode) {
    ConfinsR3GetKeyValueActiveByCodeDto response = apiConfinsR3Adapter.getKyValueByCode(ConfinsR3GetKeyValueActiveByCodeRequest.builder()
      .requestDateTime(CommonUtils.generateDate(AppConstants.DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ))
      .refMasterTypeCode(typeCode)
      .build());
    if (response == null) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", typeCode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    List<GetKeyValueActiveByCodeResponse> responseLit = new ArrayList<>();
    response.getReturnObject().forEach(keyType -> {
      GetKeyValueActiveByCodeResponse type = new GetKeyValueActiveByCodeResponse();
      type.setKey(keyType.getKey());
      type.setValue(keyType.getValue());
      responseLit.add(type);
    });

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, responseLit);
  }

  /**
   * Company
   * @param custNo
   * @return
   */
  public BaseResponseBuilder<GetCustomerNoCompanyResponse> findByCustomerNoCompany(String custNo) {
    ConfinsR3GetCustomerNoCompanyDto response = apiConfinsR3Adapter.getByCustomerNoCompany(ConfinsR3GetCustomerNoRequest.builder()
      .requestDateTime(CommonUtils.generateDate(AppConstants.DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ))
      .rowVersion("")
      .custNo(custNo)
      .build());
    if (response == null) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", custNo);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    GetCustomerResponse customerResponse = new GetCustomerResponse();
    BeanUtils.copyProperties(response.getCustomer(),customerResponse);

    GetCustomerCompanyInfoResponse customerCompanyInfoResponse = new GetCustomerCompanyInfoResponse();
    BeanUtils.copyProperties(response.getCustomerCampany(),customerCompanyInfoResponse);
    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, GetCustomerNoCompanyResponse.builder()
      .customer(customerResponse)
      .customerCampany(customerCompanyInfoResponse)
      .build());
  }

  /**
   * Personal
   * @param custNo
   * @return
   */
  public BaseResponseBuilder<GetCustomerNoPersonalResponse> findByCustomerNoPersonal(String custNo) {
    ConfinsR3GetCustomerNoPersonalDto response = apiConfinsR3Adapter.getByCustomerNoPersonal(ConfinsR3GetCustomerNoRequest.builder()
      .requestDateTime(CommonUtils.generateDate(AppConstants.DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ))
      .rowVersion("")
      .custNo(custNo)
      .build());
    if (response == null) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", custNo);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    GetCustomerResponse customerResponse = new GetCustomerResponse();
    BeanUtils.copyProperties(response.getCustomer(),customerResponse);

    GetCustomerPersonalInfoResponse customerPersonalInfoResponse = new GetCustomerPersonalInfoResponse();
    BeanUtils.copyProperties(response.getCustomerPersonal(),customerPersonalInfoResponse);
    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, GetCustomerNoPersonalResponse.builder()
      .customer(customerResponse)
      .customerPersonal(customerPersonalInfoResponse)
      .build());
  }
}
