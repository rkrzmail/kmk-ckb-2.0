package com.kmkbe.feign.client;

import com.kmkbe.feign.config.ConfinsR3AuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.request.GetCustomerNoRequest;
import com.kmkbe.feign.model.request.GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.GetZipCodeRequest;
import com.kmkbe.feign.model.response.ConfinsR3GetCustomerNoResponse;
import com.kmkbe.feign.model.response.ConfinsR3GetCustomerResponse;
import com.kmkbe.feign.model.response.ConfinsR3GetCwrRecordResponse;
import com.kmkbe.feign.model.response.ConfinsR3GetZipCodeResponse;
import com.kmkbe.feign.utils.ConfinsR3ApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "confinsR3FouFeignClient",
  url = "${feign.confins.url}",
  configuration = { ConfinsR3AuthInterceptor.class, FeignErrorDecoder.class }
)
public interface ConfinsR3FeignClient {


  @GetMapping(value = "/api/mou/v1/Generic/GetPagingObjectBySQL")
  ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrRecordResponse> getCwrByCustomer(@RequestBody GetPagingObjectBySQLRequest request);

  @GetMapping(value = "/api/fou/v1/RefZipcode/GetRefZipcodeByZipCode")
  ConfinsR3GetZipCodeResponse getZipcode(@RequestBody GetZipCodeRequest request);

  @GetMapping(value = "/api/fou/v2/Generic/GetPagingObjectBySQL")
  ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerResponse> getByCustomer(@RequestBody GetPagingObjectBySQLRequest request);

  @GetMapping(value = "/api/fou/v1/Cust/GetCustByCustNo")
  ConfinsR3GetCustomerNoResponse getByCustomerNo(@RequestBody GetCustomerNoRequest request);

}
