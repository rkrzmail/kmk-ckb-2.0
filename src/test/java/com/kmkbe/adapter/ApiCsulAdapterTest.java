package com.kmkbe.adapter;

import com.kmkbe.feign.client.CsulVendorFeignClient;
import com.kmkbe.feign.model.dto.CsulGetVendorDto;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.feign.model.response.CsulApiResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test untuk ApiCsulAdapter (adapter CSUL vendor portal).
 * Menguji delegasi ke CsulVendorFeignClient, unwrapping wrapper
 * CsulApiResponseWrapper, dan branch null-safety.
 */
@ExtendWith(MockitoExtension.class)
class ApiCsulAdapterTest {

  @Mock
  private CsulVendorFeignClient csulVendorFeignClient;

  private ApiCsulAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ApiCsulAdapter(csulVendorFeignClient);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(csulVendorFeignClient);
  }

  // ------------------------------------------------------------------
  // findByCode
  // ------------------------------------------------------------------

  @Test
  void findByCodeDelegatesToFeignClientWithVendorCode() {
    ArgumentCaptor<String> vendorCodeCaptor = ArgumentCaptor.forClass(String.class);
    CsulGetVendorDto expected = CsulGetVendorDto.builder()
      .vendorId("V-001")
      .vendorName("Vendor Satu")
      .build();

    when(csulVendorFeignClient.getVendorData("V-001")).thenReturn(wrap(expected));

    CsulGetVendorDto actual = adapter.findByCode("V-001");

    assertThat(actual).isSameAs(expected);
    verify(csulVendorFeignClient).getVendorData(vendorCodeCaptor.capture());
    assertThat(vendorCodeCaptor.getValue()).isEqualTo("V-001");
  }

  @Test
  void findByCodeUnwrapsWrapperData() {
    CsulGetVendorDto data = CsulGetVendorDto.builder()
      .vendorId("V-002")
      .sapCode("SAP-9")
      .npwp("123456789")
      .build();
    when(csulVendorFeignClient.getVendorData(anyString())).thenReturn(wrap(data));

    CsulGetVendorDto actual = adapter.findByCode("V-002");

    assertThat(actual).isSameAs(data);
    assertThat(actual.getVendorId()).isEqualTo("V-002");
    assertThat(actual.getSapCode()).isEqualTo("SAP-9");
    assertThat(actual.getNpwp()).isEqualTo("123456789");
  }

  @Test
  void findByCodeReturnsNullWhenWrapperIsNull() {
    when(csulVendorFeignClient.getVendorData(anyString())).thenReturn(null);

    assertThat(adapter.findByCode("missing")).isNull();
  }

  @Test
  void findByCodeReturnsNullWhenWrapperDataIsNull() {
    when(csulVendorFeignClient.getVendorData(anyString())).thenReturn(wrap(null));

    assertThat(adapter.findByCode("missing")).isNull();
  }

  // ------------------------------------------------------------------
  // findListPostedInvoice
  // ------------------------------------------------------------------

  @Test
  void findListPostedInvoiceDelegatesToFeignClientWithVendorCode() {
    ArgumentCaptor<String> vendorCodeCaptor = ArgumentCaptor.forClass(String.class);
    CsulInquiryInvoiceRemoteDto expected = new CsulInquiryInvoiceRemoteDto();
    expected.setCount(3);

    when(csulVendorFeignClient.getListPostedInvoice("V-003")).thenReturn(wrap(expected));

    CsulInquiryInvoiceRemoteDto actual = adapter.findListPostedInvoice("V-003");

    assertThat(actual).isSameAs(expected);
    verify(csulVendorFeignClient).getListPostedInvoice(vendorCodeCaptor.capture());
    assertThat(vendorCodeCaptor.getValue()).isEqualTo("V-003");
  }

  @Test
  void findListPostedInvoiceUnwrapsWrapperData() {
    CsulInquiryInvoiceRemoteDto data = new CsulInquiryInvoiceRemoteDto();
    data.setCount(5);
    data.setDocumentStatus("02");
    when(csulVendorFeignClient.getListPostedInvoice(anyString())).thenReturn(wrap(data));

    CsulInquiryInvoiceRemoteDto actual = adapter.findListPostedInvoice("V-004");

    assertThat(actual).isSameAs(data);
    assertThat(actual.getCount()).isEqualTo(5);
    assertThat(actual.getDocumentStatus()).isEqualTo("02");
  }

  @Test
  void findListPostedInvoiceReturnsNullWhenWrapperIsNull() {
    when(csulVendorFeignClient.getListPostedInvoice(anyString())).thenReturn(null);

    assertThat(adapter.findListPostedInvoice("missing")).isNull();
  }

  @Test
  void findListPostedInvoiceReturnsNullWhenWrapperDataIsNull() {
    when(csulVendorFeignClient.getListPostedInvoice(anyString())).thenReturn(wrap(null));

    assertThat(adapter.findListPostedInvoice("missing")).isNull();
  }

  // ------------------------------------------------------------------
  // wrapper metadata (status / statusCode tetap terbaca dari response)
  // ------------------------------------------------------------------

  @Test
  void wrapperCarriesStatusMetadataAlongsideData() {
    CsulApiResponseWrapper<CsulGetVendorDto> wrapper = wrap(
      CsulGetVendorDto.builder().vendorId("V-005").build());
    wrapper.setStatus("SUCCESS");
    wrapper.setStatusCode(200);

    when(csulVendorFeignClient.getVendorData(anyString())).thenReturn(wrapper);

    CsulGetVendorDto actual = adapter.findByCode("V-005");

    assertThat(actual).isNotNull();
    assertThat(actual.getVendorId()).isEqualTo("V-005");
    assertThat(wrapper.getStatus()).isEqualTo("SUCCESS");
    assertThat(wrapper.getStatusCode()).isEqualTo(200);
  }

  private <T> CsulApiResponseWrapper<T> wrap(T data) {
    CsulApiResponseWrapper<T> wrapper = new CsulApiResponseWrapper<>();
    wrapper.setData(data);
    return wrapper;
  }
}
