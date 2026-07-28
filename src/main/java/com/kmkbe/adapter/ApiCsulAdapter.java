package com.kmkbe.adapter;

import com.kmkbe.feign.client.CsulAuthFeignClient;
import com.kmkbe.feign.client.CsulVendorFeignClient;
import com.kmkbe.feign.model.response.GetVendorResponse;
import com.kmkbe.feign.model.request.PostLoginRequest;
import com.kmkbe.feign.model.response.PostLoginResponse;
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
  public PostLoginResponse login(PostLoginRequest request) {
    CsulApiResponseWrapper<PostLoginResponse> responseWrapper = csulAuthFeignClient.login(request);

    if (responseWrapper == null || responseWrapper.getData() == null) {
      return null;
    }

    return responseWrapper.getData();
  }

  public GetVendorResponse findByCode(String vendorCode) {
    CsulApiResponseWrapper<GetVendorResponse> responseWrapper = csulVendorFeignClient.getVendorData(vendorCode);

    if (responseWrapper == null || responseWrapper.getData() == null) {
      return null;
    }

    return responseWrapper.getData();
  }
}
