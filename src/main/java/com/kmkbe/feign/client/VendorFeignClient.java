package com.kmkbe.feign.client;

import com.kmkbe.feign.config.AuthInterceptor;
import com.kmkbe.feign.config.FeignErrorDecoder;
import com.kmkbe.feign.model.dto.VendorDataPayload;
import com.kmkbe.feign.utils.ApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
  name = "vendorFeignClient",
  url = "${feign.csul.ckb.url}",
  configuration = { AuthInterceptor.class, FeignErrorDecoder.class }
)
public interface VendorFeignClient {

  @GetMapping(value = "/api/v1/sbu/ckb/vendor", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  ApiResponseWrapper<VendorDataPayload> getVendorData(@RequestParam("vendor_code") String vendorCode);

  @GetMapping(value = "/api/v1/sbu/ckb/listpostedinvoice", headers = "Content-Type=application/json")
  String getListPostedInvoice(@RequestParam("vendor_code") String vendorCode);
}
