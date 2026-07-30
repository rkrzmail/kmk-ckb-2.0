package com.kmkbe.adapter;

import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.feign.model.dto.*;
import com.kmkbe.feign.model.request.ConfinsR3GetCustomerNoRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetKeyValueActiveByCodeRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.CsulGetZipCodeRequest;
import com.kmkbe.feign.model.response.ConfinsR3ApiResponseWrapper;
import org.springframework.stereotype.Component;

@Component
public class ApiConfinsR3Adapter {
  private final ConfinsR3FeignClient confinsR3FeignClient;

  public ApiConfinsR3Adapter(ConfinsR3FeignClient confinsR3FeignClient) {
    this.confinsR3FeignClient = confinsR3FeignClient;
  }

  /**
   * Get Confins R3 Zipcode
   * @param zipCode
   * @return
   */
  public ConfinsR3GetZipCodeDto getZipcode(String zipCode) {
    return confinsR3FeignClient.getZipcode(CsulGetZipCodeRequest.builder()
      .zipcode(zipCode.trim())
      .build());
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3GetZipCodeDto> getAllZipcode(ConfinsR3GetPagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getAllZipcode(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrCustomerDto> getCwrByCustomer(ConfinsR3GetPagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getCwrByCustomer(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerDto> getByCustomer(ConfinsR3GetPagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getByCustomer(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetCustomerNoDto getByCustomerNo(ConfinsR3GetCustomerNoRequest request) {
    return confinsR3FeignClient.getByCustomerNo(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetCustomerNoCompanyDto getByCustomerNoCompany(ConfinsR3GetCustomerNoRequest request) {
    return confinsR3FeignClient.getByCustomerNoCompany(request);
  }


  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetCustomerNoPersonalDto getByCustomerNoPersonal(ConfinsR3GetCustomerNoRequest request) {
    return confinsR3FeignClient.getByCustomerNoPersonal(request);
  }


  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetKeyValueActiveByCodeDto getKyValueByCode(ConfinsR3GetKeyValueActiveByCodeRequest request) {
    return confinsR3FeignClient.getKyValueByCode(request);
  }
}
