package com.kmkbe.adapter;

import com.kmkbe.feign.client.ConfinsR3FouFeignClient;
import com.kmkbe.feign.client.ConfinsR3MouFeignClient;
import com.kmkbe.feign.model.response.CwrRecordResponse;
import com.kmkbe.feign.model.request.PagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.ZipCodeRequest;
import com.kmkbe.feign.model.response.ZipCodeResponse;
import com.kmkbe.feign.utils.ConfinsR3ApiResponseWrapper;
import org.springframework.stereotype.Component;

@Component
public class ApiConfinsR3Adapter {
  private final ConfinsR3FouFeignClient confinsR3FouFeignClient;
  private final ConfinsR3MouFeignClient confinsR3MouFeignClient;

  public ApiConfinsR3Adapter(ConfinsR3FouFeignClient confinsR3FouFeignClient, ConfinsR3MouFeignClient confinsR3MouFeignClient) {
    this.confinsR3FouFeignClient = confinsR3FouFeignClient;
    this.confinsR3MouFeignClient = confinsR3MouFeignClient;
  }

  /**
   * Get Confins R3 Zipcode
   * @param zipCode
   * @return
   */
  public ZipCodeResponse getZipcode(String zipCode) {
    return confinsR3FouFeignClient.getZipcode(ZipCodeRequest.builder()
      .zipcode(zipCode.trim())
      .build());
  }

  /**
   *
   * @param request
   * @return
   */
  public ConfinsR3ApiResponseWrapper<CwrRecordResponse> getCwrByCustomer(PagingObjectBySQLRequest request) {
    return confinsR3MouFeignClient.getCwrByCustomer(request);
  }
}
