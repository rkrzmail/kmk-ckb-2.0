package com.kmkbe.adapter;

import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.feign.client.AuthFeignClient;
import com.kmkbe.feign.client.VendorFeignClient;
import com.kmkbe.feign.model.dto.VendorDataPayload;
import com.kmkbe.feign.model.request.LoginRequest;
import com.kmkbe.feign.model.response.LoginResponse;
import com.kmkbe.feign.utils.ApiResponseWrapper;
import org.springframework.stereotype.Component;

@Component
public class ApiConfinsAdapter {
  private final VendorFeignClient vendorFeignClient;
  private final AuthFeignClient authFeignClient;

  public ApiConfinsAdapter(VendorFeignClient vendorFeignClient, AuthFeignClient authFeignClient) {
    this.vendorFeignClient = vendorFeignClient;
    this.authFeignClient = authFeignClient;
  }


  public LoginResponse login(LoginRequest request) {
    ApiResponseWrapper<LoginResponse> responseWrapper = authFeignClient.login(request);

    if (responseWrapper == null || responseWrapper.getData() == null) {
      return null;
    }

    return responseWrapper.getData();
  }

  public VendorDataPayload findByCode(String vendorCode) {
    ApiResponseWrapper<VendorDataPayload> responseWrapper = vendorFeignClient.getVendorData(vendorCode);

    if (responseWrapper == null || responseWrapper.getData() == null) {
      return null;
    }

    return responseWrapper.getData();
  }
}
