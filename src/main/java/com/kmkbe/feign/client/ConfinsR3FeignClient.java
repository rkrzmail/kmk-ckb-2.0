package com.kmkbe.feign.client;

import com.kmkbe.feign.config.ConfinsR3AuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.dto.*;
import com.kmkbe.feign.model.request.ConfinsR3GetCustomerNoRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetKeyValueActiveByCodeRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.CsulGetZipCodeRequest;
import com.kmkbe.feign.model.response.ConfinsR3ApiResponseWrapper;
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


  @PostMapping(value = "/api/mou/v1/Generic/GetPagingObjectBySQL")
  ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrCustomerDto> getCwrByCustomer(@RequestBody ConfinsR3GetPagingObjectBySQLRequest request);

  @PostMapping(value = "/api/fou/v1/RefZipcode/GetRefZipcodeByZipCode")
  ConfinsR3GetZipCodeDto getZipcode(@RequestBody CsulGetZipCodeRequest request);

  @PostMapping(value = "/api/fou/v2/Generic/GetPagingObjectBySQL")
  ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerDto> getByCustomer(@RequestBody ConfinsR3GetPagingObjectBySQLRequest request);

  @PostMapping(value = "/api/fou/v1/Cust/GetCustByCustNo")
  ConfinsR3GetCustomerNoDto getByCustomerNo(@RequestBody ConfinsR3GetCustomerNoRequest request);

  @PostMapping(value = "/api/fou/v1/RefMaster/GetListKeyValueActiveByCode")
  ConfinsR3GetKeyValueActiveByCodeDto getKyValueByCode(@RequestBody ConfinsR3GetKeyValueActiveByCodeRequest request);

  @PostMapping(value = "/api/fou/v1/Cust/GetCustCompanyForUpdateByCustNo")
  ConfinsR3GetCustomerNoCompanyDto getByCustomerNoCompany(@RequestBody ConfinsR3GetCustomerNoRequest request);

  @PostMapping(value = "/api/fou/v1/Cust/GetCustPersonalForUpdateByCustNo")
  ConfinsR3GetCustomerNoPersonalDto getByCustomerNoPersonal(@RequestBody ConfinsR3GetCustomerNoRequest request);

  @PostMapping(value = "/api/fou/v1/Generic/GetPagingObjectBySQL")
  ConfinsR3ApiResponseWrapper<ConfinsR3GetZipCodeDto> getAllZipcode(@RequestBody ConfinsR3GetPagingObjectBySQLRequest request);

}
