package com.kmkbe.feign.client;

import com.kmkbe.feign.model.dto.CsulGetVendorDto;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.feign.model.response.CsulApiResponseWrapper;
import feign.Feign;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CsulVendorFeignClientTest {

  @Test
  void feignClientContract_getVendorDataAndGetListPostedInvoice() {
    HttpMessageConverters converters = new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
    AtomicReference<Request> capturedRequest = new AtomicReference<>();

    CsulVendorFeignClient client = Feign.builder()
      .contract(new SpringMvcContract())
      .encoder(new SpringEncoder(() -> converters))
      .decoder(new SpringDecoder(() -> converters))
      .client((request, options) -> {
        capturedRequest.set(request);
        return Response.builder()
          .request(request)
          .status(200)
          .reason("OK")
          .headers(Map.of("Content-Type", List.of("application/json")))
          .body("{\"status\":\"SUCCESS\",\"status_code\":200,\"data\":{}}", StandardCharsets.UTF_8)
          .build();
      })
      .target(CsulVendorFeignClient.class, "https://ckb.example");

    CsulApiResponseWrapper<CsulGetVendorDto> vendorResponse = client.getVendorData("VEND001");
    assertThat(vendorResponse).isNotNull();
    assertThat(vendorResponse.getStatus()).isEqualTo("SUCCESS");
    assertThat(capturedRequest.get().url()).contains("/api/v1/sbu/ckb/vendor?vendor_code=VEND001");
    assertThat(capturedRequest.get().httpMethod()).isEqualTo(Request.HttpMethod.GET);

    CsulApiResponseWrapper<CsulInquiryInvoiceRemoteDto> invoiceResponse = client.getListPostedInvoice("VEND002");
    assertThat(invoiceResponse).isNotNull();
    assertThat(invoiceResponse.getStatus()).isEqualTo("SUCCESS");
    assertThat(capturedRequest.get().url()).contains("/api/v1/sbu/ckb/listpostedinvoice?vendor_code=VEND002");
    assertThat(capturedRequest.get().httpMethod()).isEqualTo(Request.HttpMethod.GET);
  }
}
