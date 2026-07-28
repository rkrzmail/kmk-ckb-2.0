package com.kmkbe.feign.client;

import com.kmkbe.feign.config.CsulVdcAuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.response.GetVendorResponse;
import com.kmkbe.feign.utils.CsulApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
  name = "csulVendorFeignClient",
  url = "${feign.csul.ckb.url}",
  configuration = { CsulVdcAuthInterceptor.class, FeignErrorDecoder.class }
)
public interface CsulVendorFeignClient {

  @GetMapping(value = "/api/v1/sbu/ckb/vendor", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  CsulApiResponseWrapper<GetVendorResponse> getVendorData(@RequestParam("vendor_code") String vendorCode);

  @GetMapping(value = "/api/v1/sbu/ckb/listpostedinvoice", headers = "Content-Type=application/json")
  String getListPostedInvoice(@RequestParam("vendor_code") String vendorCode);
}
