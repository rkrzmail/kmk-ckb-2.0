package com.kmkbe.feign.client;

import com.kmkbe.feign.config.CsulVdcAuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.request.CsulPostLoginRequest;
import com.kmkbe.feign.model.dto.CsulPostLoginDto;
import com.kmkbe.feign.model.response.CsulApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "csulAuthFeignClient",
  url = "${feign.csul.ckb.url}",
  configuration = { CsulVdcAuthInterceptor.class, FeignErrorDecoder.class}
)
public interface CsulAuthFeignClient {
  @GetMapping(value = "/api/v1/webhook/token",
      headers = {
    "Content-Type=application/json"
  })
  CsulApiResponseWrapper<CsulPostLoginDto> login(@RequestBody CsulPostLoginRequest request);
}

