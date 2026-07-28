package com.kmkbe.adapter;

import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.feign.model.response.ConfinsR3CwrRecordResponse;
import com.kmkbe.feign.model.request.PagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.ZipCodeRequest;
import com.kmkbe.feign.model.response.ConfinsR3ZipCodeResponse;
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
  public ConfinsR3ZipCodeResponse getZipcode(String zipCode) {
    return confinsR3FeignClient.getZipcode(ZipCodeRequest.builder()
      .zipcode(zipCode.trim())
      .build());
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3CwrRecordResponse> getCwrByCustomer(PagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getCwrByCustomer(request);
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<ConfinsR3CwrRecordResponse> getByCustomer(PagingObjectBySQLRequest request) {
    return confinsR3FeignClient.getCwrByCustomer(request);
  }
}
