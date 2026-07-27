package com.kmkbe.feign.client;

import com.kmkbe.feign.config.AuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.dto.VendorDataPayload;
import com.kmkbe.feign.model.request.ZipCodeRequest;
import com.kmkbe.feign.utils.ApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
  name = "confinsR3FouFeignClient",
  url = "${feign.confins.fou}",
  configuration = { ConfinsR3FouFeignClient.class, FeignErrorDecoder.class }
)
public interface ConfinsR3FouFeignClient {

  @GetMapping(value = "/v1/RefZipcode/GetRefZipcodeByZipCode", headers = "Content-Type=application/json")
  String getListPostedInvoice(@RequestBody ZipCodeRequest request);
}
