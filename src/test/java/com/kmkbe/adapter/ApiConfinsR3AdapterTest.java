package com.kmkbe.adapter;

import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.feign.model.dto.ConfinsR3GetCustomerDto;
import com.kmkbe.feign.model.dto.ConfinsR3GetCustomerNoCompanyDto;
import com.kmkbe.feign.model.dto.ConfinsR3GetCustomerNoDto;
import com.kmkbe.feign.model.dto.ConfinsR3GetCustomerNoPersonalDto;
import com.kmkbe.feign.model.dto.ConfinsR3GetCwrCustomerDto;
import com.kmkbe.feign.model.dto.ConfinsR3GetKeyValueActiveByCodeDto;
import com.kmkbe.feign.model.dto.ConfinsR3GetZipCodeDto;
import com.kmkbe.feign.model.request.ConfinsR3GetCustomerNoRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetKeyValueActiveByCodeRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.CsulGetZipCodeRequest;
import com.kmkbe.feign.model.response.ConfinsR3ApiResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiConfinsR3AdapterTest {

  @Mock
  private ConfinsR3FeignClient confinsR3FeignClient;

  private ApiConfinsR3Adapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ApiConfinsR3Adapter(confinsR3FeignClient);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(confinsR3FeignClient);
  }

  @Test
  void getZipcodeTrimsZipcodeAndDelegatesToFeignClient() {
    ConfinsR3GetZipCodeDto expected = mock(ConfinsR3GetZipCodeDto.class);
    ArgumentCaptor<CsulGetZipCodeRequest> requestCaptor = ArgumentCaptor.forClass(CsulGetZipCodeRequest.class);
    when(confinsR3FeignClient.getZipcode(any(CsulGetZipCodeRequest.class))).thenReturn(expected);

    ConfinsR3GetZipCodeDto actual = adapter.getZipcode("  12345  ");

    assertThat(actual).isSameAs(expected);
    verify(confinsR3FeignClient).getZipcode(requestCaptor.capture());
    assertThat(requestCaptor.getValue().getZipcode()).isEqualTo("12345");
  }

  @Test
  void getAllZipcodeDelegatesSameRequestToFeignClient() {
    ConfinsR3GetPagingObjectBySQLRequest request = mock(ConfinsR3GetPagingObjectBySQLRequest.class);
    ConfinsR3ApiResponseWrapper<ConfinsR3GetZipCodeDto> expected = mockResponseWrapper();
    when(confinsR3FeignClient.getAllZipcode(request)).thenReturn(expected);

    ConfinsR3ApiResponseWrapper<ConfinsR3GetZipCodeDto> actual = adapter.getAllZipcode(request);

    assertThat(actual).isSameAs(expected);
    verify(confinsR3FeignClient).getAllZipcode(request);
  }

  @Test
  void getCwrByCustomerDelegatesSameRequestToFeignClient() {
    ConfinsR3GetPagingObjectBySQLRequest request = mock(ConfinsR3GetPagingObjectBySQLRequest.class);
    ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrCustomerDto> expected = mockResponseWrapper();
    when(confinsR3FeignClient.getCwrByCustomer(request)).thenReturn(expected);

    ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrCustomerDto> actual = adapter.getCwrByCustomer(request);

    assertThat(actual).isSameAs(expected);
    verify(confinsR3FeignClient).getCwrByCustomer(request);
  }

  @Test
  void getByCustomerDelegatesSameRequestToFeignClient() {
    ConfinsR3GetPagingObjectBySQLRequest request = mock(ConfinsR3GetPagingObjectBySQLRequest.class);
    ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerDto> expected = mockResponseWrapper();
    when(confinsR3FeignClient.getByCustomer(request)).thenReturn(expected);

    ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerDto> actual = adapter.getByCustomer(request);

    assertThat(actual).isSameAs(expected);
    verify(confinsR3FeignClient).getByCustomer(request);
  }

  @Test
  void getByCustomerNoDelegatesSameRequestToFeignClient() {
    ConfinsR3GetCustomerNoRequest request = mock(ConfinsR3GetCustomerNoRequest.class);
    ConfinsR3GetCustomerNoDto expected = mock(ConfinsR3GetCustomerNoDto.class);
    when(confinsR3FeignClient.getByCustomerNo(request)).thenReturn(expected);

    ConfinsR3GetCustomerNoDto actual = adapter.getByCustomerNo(request);

    assertThat(actual).isSameAs(expected);
    verify(confinsR3FeignClient).getByCustomerNo(request);
  }

  @Test
  void getByCustomerNoCompanyDelegatesSameRequestToFeignClient() {
    ConfinsR3GetCustomerNoRequest request = mock(ConfinsR3GetCustomerNoRequest.class);
    ConfinsR3GetCustomerNoCompanyDto expected = mock(ConfinsR3GetCustomerNoCompanyDto.class);
    when(confinsR3FeignClient.getByCustomerNoCompany(request)).thenReturn(expected);

    ConfinsR3GetCustomerNoCompanyDto actual = adapter.getByCustomerNoCompany(request);

    assertThat(actual).isSameAs(expected);
    verify(confinsR3FeignClient).getByCustomerNoCompany(request);
  }

  @Test
  void getByCustomerNoPersonalDelegatesSameRequestToFeignClient() {
    ConfinsR3GetCustomerNoRequest request = mock(ConfinsR3GetCustomerNoRequest.class);
    ConfinsR3GetCustomerNoPersonalDto expected = mock(ConfinsR3GetCustomerNoPersonalDto.class);
    when(confinsR3FeignClient.getByCustomerNoPersonal(request)).thenReturn(expected);

    ConfinsR3GetCustomerNoPersonalDto actual = adapter.getByCustomerNoPersonal(request);

    assertThat(actual).isSameAs(expected);
    verify(confinsR3FeignClient).getByCustomerNoPersonal(request);
  }

  @Test
  void getKyValueByCodeDelegatesSameRequestToFeignClient() {
    ConfinsR3GetKeyValueActiveByCodeRequest request = mock(ConfinsR3GetKeyValueActiveByCodeRequest.class);
    ConfinsR3GetKeyValueActiveByCodeDto expected = mock(ConfinsR3GetKeyValueActiveByCodeDto.class);
    when(confinsR3FeignClient.getKyValueByCode(request)).thenReturn(expected);

    ConfinsR3GetKeyValueActiveByCodeDto actual = adapter.getKyValueByCode(request);

    assertThat(actual).isSameAs(expected);
    verify(confinsR3FeignClient).getKyValueByCode(request);
  }

  @SuppressWarnings("unchecked")
  private static <T> ConfinsR3ApiResponseWrapper<T> mockResponseWrapper() {
    return mock(ConfinsR3ApiResponseWrapper.class);
  }
}
