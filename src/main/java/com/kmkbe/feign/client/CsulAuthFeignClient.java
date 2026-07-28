package com.kmkbe.feign.client;

import com.kmkbe.feign.model.request.LoginRequest;
import com.kmkbe.feign.model.response.LoginResponse;
import com.kmkbe.feign.utils.CsulApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "csulAuthFeignClient",
  url = "${feign.csul.ckb.url}"
)
public interface CsulAuthFeignClient {

  @GetMapping(value = "/api/v1/webhook/token",
      headers = {
    "Content-Type=application/json"
  })
  CsulApiResponseWrapper<LoginResponse> login(@RequestBody LoginRequest request);
}

