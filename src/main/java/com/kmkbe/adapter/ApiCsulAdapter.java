package com.kmkbe.adapter;

import com.kmkbe.feign.client.CsulAuthFeignClient;
import com.kmkbe.feign.client.CsulVendorFeignClient;
import com.kmkbe.feign.model.dto.CsulGetVendorDto;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.feign.model.request.CsulPostLoginRequest;
import com.kmkbe.feign.model.dto.CsulPostLoginDto;
import com.kmkbe.feign.model.response.CsulApiResponseWrapper;
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
   *
   * @param vendorCode
   * @return
   */
  public CsulGetVendorDto findByCode(String vendorCode) {
    CsulApiResponseWrapper<CsulGetVendorDto> responseWrapper = csulVendorFeignClient.getVendorData(vendorCode);

    if (responseWrapper == null || responseWrapper.getData() == null) {
      return null;
    }

    return responseWrapper.getData();
  }

  /**
   *
   * @param vendorCode
   * @return
   */
  public CsulInquiryInvoiceRemoteDto findListPostedInvoice(String vendorCode) {
    CsulApiResponseWrapper<CsulInquiryInvoiceRemoteDto> responseWrapper = csulVendorFeignClient.getListPostedInvoice(vendorCode);
    if (responseWrapper == null || responseWrapper.getData() == null) {
      return null;
    }

    return responseWrapper.getData();
  }
}
