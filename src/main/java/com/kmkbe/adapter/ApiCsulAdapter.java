package com.kmkbe.adapter;

import com.kmkbe.feign.client.CsulVendorFeignClient;
import com.kmkbe.feign.model.dto.CsulGetVendorDto;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.feign.model.response.CsulApiResponseWrapper;
import org.springframework.stereotype.Component;

@Component
public class ApiCsulAdapter {
  private final CsulVendorFeignClient csulVendorFeignClient;

  public ApiCsulAdapter(CsulVendorFeignClient csulVendorFeignClient) {
    this.csulVendorFeignClient = csulVendorFeignClient;
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
