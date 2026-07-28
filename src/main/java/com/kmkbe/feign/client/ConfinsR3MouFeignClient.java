package com.kmkbe.feign.client;

import com.kmkbe.feign.config.ConfinsR3AuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.response.CwrRecordResponse;
import com.kmkbe.feign.model.request.PagingObjectBySQLRequest;
import com.kmkbe.feign.utils.ConfinsR3ApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "confinsR3mouFeignClient",
  url = "${feign.confins.mou}",
  configuration = { ConfinsR3AuthInterceptor.class, FeignErrorDecoder.class }
)
public interface ConfinsR3MouFeignClient {

  @GetMapping(value = "/v1/Generic/GetPagingObjectBySQL")
  ConfinsR3ApiResponseWrapper<CwrRecordResponse> getCwrByCustomer(@RequestBody PagingObjectBySQLRequest request);
}
