package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.modules.remote.request.InquiryAgreementRemoteRequest;
import com.kmkbe.modules.remote.request.InquiryCwrRemoteRequest;
import com.kmkbe.modules.remote.request.PropCriteriaGenericTypeRequest;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CwrRemoteServiceCoverageTest {
  @Mock ConfinsR3FeignClient client;
  CwrRemoteService service;

  @BeforeEach
  void setUp() {
    service = new CwrRemoteService(new ObjectMapper(), client);
    ReflectionTestUtils.setField(service, "confinsUrl", "http://confins");
  }

  @Test
  void agreementAttributeAndInquiryCwrBranches() throws Exception {
    BaseMstRemoteResponseDto response = mock(BaseMstRemoteResponseDto.class);
    when(client.getAgreementAttributeContent(any())).thenReturn(response);
    assertThat(service.agreementAttributeContent(1)).isSameAs(response);

    InquiryCwrRemoteRequest request = new InquiryCwrRemoteRequest();
    request.setCwrNo("CWR1");
    when(client.inquiryCwr(any())).thenReturn(response);
    assertThat(service.inquiryCwr(request)).isSameAs(response);

    request.setName(PropCriteriaGenericTypeRequest.CwrPropName.custNo);
    request.setCwrNo(null);
    request.setCustNo("CUST1");
    assertThat(service.inquiryCwr(request)).isSameAs(response);

    doThrow(feignError()).when(client).inquiryCwr(any());
    assertThatThrownBy(() -> service.inquiryCwr(request)).isInstanceOf(RuntimeException.class);
    request.setName(PropCriteriaGenericTypeRequest.CwrPropName.cwrNo);
    doThrow(feignError("{}")).when(client).inquiryCwr(any());
    assertThatThrownBy(() -> service.inquiryCwr(request)).isInstanceOf(RuntimeException.class);
    doThrow(feignError("not-json")).when(client).inquiryCwr(any());
    assertThatThrownBy(() -> service.inquiryCwr(request)).isInstanceOf(RuntimeException.class);

    doThrow(new IllegalStateException("boom")).when(client).inquiryCwr(any());
    assertThatThrownBy(() -> service.inquiryCwr(request)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void inquiryAgreementBranches() throws Exception {
    BaseMstRemoteResponseDto response = mock(BaseMstRemoteResponseDto.class);
    InquiryAgreementRemoteRequest request = new InquiryAgreementRemoteRequest();
    request.setAgreementNo("AGR1");
    when(client.inquiryAgreement(any())).thenReturn(response);
    assertThat(service.inquiryAgreementByNoAgreement(request)).isSameAs(response);

    doThrow(feignError()).when(client).inquiryAgreement(any());
    assertThatThrownBy(() -> service.inquiryAgreementByNoAgreement(request)).isInstanceOf(RuntimeException.class);
    doThrow(new IllegalStateException("boom")).when(client).inquiryAgreement(any());
    assertThatThrownBy(() -> service.inquiryAgreementByNoAgreement(request)).isInstanceOf(IllegalStateException.class);
    doThrow(feignError("{}")).when(client).inquiryAgreement(any());
    assertThatThrownBy(() -> service.inquiryAgreementByNoAgreement(request)).isInstanceOf(RuntimeException.class);
    doThrow(feignError("not-json")).when(client).inquiryAgreement(any());
    assertThatThrownBy(() -> service.inquiryAgreementByNoAgreement(request)).isInstanceOf(RuntimeException.class);

    doReturn(response).when(client).inquiryAgreementByCwr(any());
    assertThat(service.inquiryAgreementByNoCwr("CWR1")).isSameAs(response);
    doThrow(feignError()).when(client).inquiryAgreementByCwr(any());
    assertThatThrownBy(() -> service.inquiryAgreementByNoCwr("CWR1")).isInstanceOf(RuntimeException.class);
    doThrow(new IllegalStateException("boom")).when(client).inquiryAgreementByCwr(any());
    assertThatThrownBy(() -> service.inquiryAgreementByNoCwr("CWR1")).isInstanceOf(IllegalStateException.class);
    doThrow(feignError("{}")).when(client).inquiryAgreementByCwr(any());
    assertThatThrownBy(() -> service.inquiryAgreementByNoCwr("CWR1")).isInstanceOf(RuntimeException.class);
    doThrow(feignError("not-json")).when(client).inquiryAgreementByCwr(any());
    assertThatThrownBy(() -> service.inquiryAgreementByNoCwr("CWR1")).isInstanceOf(RuntimeException.class);
  }

  private static FeignException feignError() {
    return feignError("{\"message\":\"bad request\"}");
  }

  private static FeignException feignError(String body) {
    return new FeignException.BadRequest("bad request", Request.create(Request.HttpMethod.POST,
      "http://confins", Map.of(), null, StandardCharsets.UTF_8, null),
      body.getBytes(StandardCharsets.UTF_8), Map.of());
  }
}
