package com.kmkbe.feign.client;

import com.kmkbe.feign.config.ConfinsR3AuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.request.ZipCodeRequest;
import com.kmkbe.feign.model.response.ZipCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "confinsR3FouFeignClient",
  url = "${feign.confins.fou}",
  configuration = { ConfinsR3AuthInterceptor.class, FeignErrorDecoder.class }
)
public interface ConfinsR3FouFeignClient {

  @GetMapping(value = "/v1/RefZipcode/GetRefZipcodeByZipCode")
  ZipCodeResponse getZipcode(@RequestBody ZipCodeRequest request);
}
