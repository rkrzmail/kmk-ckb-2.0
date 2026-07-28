package com.kmkbe.adapter;

import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.feign.model.request.GetCustomerNoRequest;
import com.kmkbe.feign.model.request.GetKeyValueActiveByCodeRequest;
import com.kmkbe.feign.model.response.*;
import com.kmkbe.feign.model.request.GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.GetZipCodeRequest;
import com.kmkbe.feign.utils.ConfinsR3ApiResponseWrapper;
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
  public ConfinsR3GetZipCodeResponse getZipcode(String zipCode) {
    return confinsR3FeignClient.getZipcode(GetZipCodeRequest.builder()
      .zipcode(zipCode.trim())
      .build());
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrRecordResponse> getCwrByCustomer(GetPagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getCwrByCustomer(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerResponse> getByCustomer(GetPagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getByCustomer(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetCustomerNoResponse getByCustomerNo(GetCustomerNoRequest request) {
    return confinsR3FeignClient.getByCustomerNo(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3GetKeyValueActiveByCodeResponse getKyValueByCode(GetKeyValueActiveByCodeRequest request) {
    return confinsR3FeignClient.getKyValueByCode(request);
  }
}
