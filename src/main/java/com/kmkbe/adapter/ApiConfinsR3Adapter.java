package com.kmkbe.adapter;

import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.feign.model.dto.*;
import com.kmkbe.feign.model.request.GetCustomerNoRequest;
import com.kmkbe.feign.model.request.GetKeyValueActiveByCodeRequest;
import com.kmkbe.feign.model.request.GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.GetZipCodeRequest;
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
    return confinsR3FeignClient.getZipcode(GetZipCodeRequest.builder()
      .zipcode(zipCode.trim())
      .build());
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrRecordDto> getCwrByCustomer(GetPagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getCwrByCustomer(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerDto> getByCustomer(GetPagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getByCustomer(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetCustomerNoDto getByCustomerNo(GetCustomerNoRequest request) {
    return confinsR3FeignClient.getByCustomerNo(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetCustomerNoCompanyDto getByCustomerNoCompany(GetCustomerNoRequest request) {
    return confinsR3FeignClient.getByCustomerNoCompany(request);
  }


  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetCustomerNoPersonalDto getByCustomerNoPersonal(GetCustomerNoRequest request) {
    return confinsR3FeignClient.getByCustomerNoPersonal(request);
  }


  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetKeyValueActiveByCodeDto getKyValueByCode(GetKeyValueActiveByCodeRequest request) {
    return confinsR3FeignClient.getKyValueByCode(request);
  }
}
