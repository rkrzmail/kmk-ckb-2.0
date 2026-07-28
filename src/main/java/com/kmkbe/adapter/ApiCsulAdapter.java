package com.kmkbe.adapter;

import com.kmkbe.feign.client.CsulAuthFeignClient;
import com.kmkbe.feign.client.CsulVendorFeignClient;
import com.kmkbe.feign.model.response.VendorResponse;
import com.kmkbe.feign.model.request.LoginRequest;
import com.kmkbe.feign.model.response.LoginResponse;
import com.kmkbe.feign.utils.CsulApiResponseWrapper;
import org.springframework.stereotype.Component;

@Component
public class ApiCsulAdapter {
  private final CsulVendorFeignClient csulVendorFeignClient;
  private final CsulAuthFeignClient csulAuthFeignClient;

  public ApiCsulAdapter(CsulVendorFeignClient csulVendorFeignClient, CsulAuthFeignClient csulAuthFeignClient) {
    this.csulVendorFeignClient = csulVendorFeignClient;
    this.csulAuthFeignClient = csulAuthFeignClient;
  }

  /**
   * Login Confins
   * @param request
   * @return
   */
  public LoginResponse login(LoginRequest request) {
    CsulApiResponseWrapper<LoginResponse> responseWrapper = csulAuthFeignClient.login(request);

    if (responseWrapper == null || responseWrapper.getData() == null) {
      return null;
    }

    return responseWrapper.getData();
  }

  public VendorResponse findByCode(String vendorCode) {
    CsulApiResponseWrapper<VendorResponse> responseWrapper = csulVendorFeignClient.getVendorData(vendorCode);

    if (responseWrapper == null || responseWrapper.getData() == null) {
      return null;
    }

    return responseWrapper.getData();
  }
}
