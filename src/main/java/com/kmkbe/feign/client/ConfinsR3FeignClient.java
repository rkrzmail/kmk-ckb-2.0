package com.kmkbe.feign.client;

import com.kmkbe.feign.config.ConfinsR3AuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.request.PagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.ZipCodeRequest;
import com.kmkbe.feign.model.response.ConfinsR3CustomerResponse;
import com.kmkbe.feign.model.response.ConfinsR3CwrRecordResponse;
import com.kmkbe.feign.model.response.ConfinsR3ZipCodeResponse;
import com.kmkbe.feign.utils.ConfinsR3ApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "confinsR3FouFeignClient",
  url = "${feign.confins.url}",
  configuration = { ConfinsR3AuthInterceptor.class, FeignErrorDecoder.class }
)
public interface ConfinsR3FeignClient {


  @GetMapping(value = "/api/mou/v1/Generic/GetPagingObjectBySQL")
  ConfinsR3ApiResponseWrapper<ConfinsR3CwrRecordResponse> getCwrByCustomer(@RequestBody PagingObjectBySQLRequest request);

  @GetMapping(value = "/api/fou/v1/RefZipcode/GetRefZipcodeByZipCode")
  ConfinsR3ZipCodeResponse getZipcode(@RequestBody ZipCodeRequest request);

  @GetMapping(value = "/api/fou/v2/Generic/GetPagingObjectBySQL")
  ConfinsR3ApiResponseWrapper<ConfinsR3CustomerResponse> getByCustomer(@RequestBody PagingObjectBySQLRequest request);

}
