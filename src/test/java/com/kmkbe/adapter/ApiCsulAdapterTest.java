package com.kmkbe.adapter;

import com.kmkbe.feign.client.CsulAuthFeignClient;
import com.kmkbe.feign.client.CsulVendorFeignClient;
import com.kmkbe.feign.model.dto.CsulGetVendorDto;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.feign.model.dto.CsulPostLoginDto;
import com.kmkbe.feign.model.request.CsulPostLoginRequest;
import com.kmkbe.feign.model.response.CsulApiResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiCsulAdapterTest {

  @Mock
  private CsulVendorFeignClient csulVendorFeignClient;

  @Mock
  private CsulAuthFeignClient csulAuthFeignClient;

  private ApiCsulAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ApiCsulAdapter(csulVendorFeignClient, csulAuthFeignClient);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(csulVendorFeignClient, csulAuthFeignClient);
  }

  @Test
  void loginReturnsDataWhenFeignResponseHasData() {
    CsulPostLoginRequest request = mock(CsulPostLoginRequest.class);
    CsulPostLoginDto expected = mock(CsulPostLoginDto.class);
    CsulApiResponseWrapper<CsulPostLoginDto> responseWrapper = responseWrapperWithData(expected);
    when(csulAuthFeignClient.login(request)).thenReturn(responseWrapper);

    CsulPostLoginDto actual = adapter.login(request);

    assertThat(actual).isSameAs(expected);
    verify(csulAuthFeignClient).login(request);
  }

  @Test
  void loginReturnsNullWhenFeignResponseIsNull() {
    CsulPostLoginRequest request = mock(CsulPostLoginRequest.class);
    when(csulAuthFeignClient.login(request)).thenReturn(null);

    CsulPostLoginDto actual = adapter.login(request);

    assertThat(actual).isNull();
    verify(csulAuthFeignClient).login(request);
  }

  @Test
  void loginReturnsNullWhenFeignResponseDataIsNull() {
    CsulPostLoginRequest request = mock(CsulPostLoginRequest.class);
    when(csulAuthFeignClient.login(request)).thenReturn(new CsulApiResponseWrapper<>());

    CsulPostLoginDto actual = adapter.login(request);

    assertThat(actual).isNull();
    verify(csulAuthFeignClient).login(request);
  }

  @Test
  void findByCodeReturnsDataWhenFeignResponseHasData() {
    String vendorCode = "VENDOR-001";
    CsulGetVendorDto expected = mock(CsulGetVendorDto.class);
    CsulApiResponseWrapper<CsulGetVendorDto> responseWrapper = responseWrapperWithData(expected);
    when(csulVendorFeignClient.getVendorData(vendorCode)).thenReturn(responseWrapper);

    CsulGetVendorDto actual = adapter.findByCode(vendorCode);

    assertThat(actual).isSameAs(expected);
    verify(csulVendorFeignClient).getVendorData(vendorCode);
  }

  @Test
  void findByCodeReturnsNullWhenFeignResponseIsNull() {
    String vendorCode = "VENDOR-001";
    when(csulVendorFeignClient.getVendorData(vendorCode)).thenReturn(null);

    CsulGetVendorDto actual = adapter.findByCode(vendorCode);

    assertThat(actual).isNull();
    verify(csulVendorFeignClient).getVendorData(vendorCode);
  }

  @Test
  void findByCodeReturnsNullWhenFeignResponseDataIsNull() {
    String vendorCode = "VENDOR-001";
    when(csulVendorFeignClient.getVendorData(vendorCode)).thenReturn(new CsulApiResponseWrapper<>());

    CsulGetVendorDto actual = adapter.findByCode(vendorCode);

    assertThat(actual).isNull();
    verify(csulVendorFeignClient).getVendorData(vendorCode);
  }

  @Test
  void findListPostedInvoiceReturnsDataWhenFeignResponseHasData() {
    String vendorCode = "VENDOR-001";
    CsulInquiryInvoiceRemoteDto expected = mock(CsulInquiryInvoiceRemoteDto.class);
    CsulApiResponseWrapper<CsulInquiryInvoiceRemoteDto> responseWrapper = responseWrapperWithData(expected);
    when(csulVendorFeignClient.getListPostedInvoice(vendorCode)).thenReturn(responseWrapper);

    CsulInquiryInvoiceRemoteDto actual = adapter.findListPostedInvoice(vendorCode);

    assertThat(actual).isSameAs(expected);
    verify(csulVendorFeignClient).getListPostedInvoice(vendorCode);
  }

  @Test
  void findListPostedInvoiceReturnsNullWhenFeignResponseIsNull() {
    String vendorCode = "VENDOR-001";
    when(csulVendorFeignClient.getListPostedInvoice(vendorCode)).thenReturn(null);

    CsulInquiryInvoiceRemoteDto actual = adapter.findListPostedInvoice(vendorCode);

    assertThat(actual).isNull();
    verify(csulVendorFeignClient).getListPostedInvoice(vendorCode);
  }

  @Test
  void findListPostedInvoiceReturnsNullWhenFeignResponseDataIsNull() {
    String vendorCode = "VENDOR-001";
    when(csulVendorFeignClient.getListPostedInvoice(vendorCode)).thenReturn(new CsulApiResponseWrapper<>());

    CsulInquiryInvoiceRemoteDto actual = adapter.findListPostedInvoice(vendorCode);

    assertThat(actual).isNull();
    verify(csulVendorFeignClient).getListPostedInvoice(vendorCode);
  }

  private static <T> CsulApiResponseWrapper<T> responseWrapperWithData(T data) {
    CsulApiResponseWrapper<T> responseWrapper = new CsulApiResponseWrapper<>();
    responseWrapper.setData(data);
    return responseWrapper;
  }
}
