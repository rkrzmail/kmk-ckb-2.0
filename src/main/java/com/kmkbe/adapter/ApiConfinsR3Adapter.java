package com.kmkbe.adapter;

import com.kmkbe.feign.client.ConfinsR3FouFeignClient;
import com.kmkbe.feign.model.request.ZipCodeRequest;
import com.kmkbe.feign.model.response.ZipCodeResponse;
import org.springframework.stereotype.Component;

@Component
public class ApiConfinsR3Adapter {
  private final ConfinsR3FouFeignClient confinsR3FouFeignClient;

  public ApiConfinsR3Adapter(ConfinsR3FouFeignClient confinsR3FouFeignClient) {
    this.confinsR3FouFeignClient = confinsR3FouFeignClient;
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
}
