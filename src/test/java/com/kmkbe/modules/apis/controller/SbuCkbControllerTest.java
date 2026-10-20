package com.kmkbe.modules.apis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.CreatedSimulationDto;
import com.kmkbe.core.domain.dto.EstimatedDisburseDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.apis.service.ApiSbuCkbService;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSubmissionRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SbuCkbControllerTest {

  private static final String API_KEY = "test-api-key";
  private static final String JWT_TOKEN = "test-jwt-token";

  @Mock
  private ApiSbuCkbService apiSbuCkbService;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private SbuCkbController controller;

  @BeforeEach
  void setUp() {
    controller = new SbuCkbController(objectMapper, apiSbuCkbService);
  }

  @Test
  @DisplayName("requestToken delegates to apiSbuCkbService.createToken")
  void requestTokenSuccess() {
    Map<String, Object> data = Map.of("token", "generated-jwt");
    CommonResult<Map<String, Object>> expected = new CommonResult<Map<String, Object>>().success(data);
    when(apiSbuCkbService.createToken(API_KEY)).thenReturn(expected);

    CommonResult<Map<String, Object>> actual = controller.requestToken(API_KEY);

    assertThat(actual).isSameAs(expected);
    verify(apiSbuCkbService).createToken(API_KEY);
  }

  @Test
  @DisplayName("getCalculateDisburse validates token and delegates to apiSbuCkbService.simulation")
  void getCalculateDisburseSuccess() throws Exception {
    CalculateSimulationRequest request = new CalculateSimulationRequest();
    request.setBouwheerCode("BOUW-001");
    EstimatedDisburseDto expectedDto = EstimatedDisburseDto.builder().build();
    CommonResult<EstimatedDisburseDto> expected = new CommonResult<EstimatedDisburseDto>().success(expectedDto);

    when(apiSbuCkbService.apiValidation(API_KEY, JWT_TOKEN)).thenReturn(new ValidationResponse());
    when(apiSbuCkbService.simulation(request)).thenReturn(expected);

    CommonResult<EstimatedDisburseDto> actual = controller.getCalculateDisburse(JWT_TOKEN, API_KEY, request);

    assertThat(actual).isSameAs(expected);
    verify(apiSbuCkbService).apiValidation(API_KEY, JWT_TOKEN);
    verify(apiSbuCkbService).simulation(request);
  }

  @Test
  @DisplayName("createSimulation validates token and delegates to apiSbuCkbService.submission")
  void createSimulationSuccess() throws Exception {
    CreateSubmissionRequest request = CreateSubmissionRequest.builder().build();
    CreatedSimulationDto expectedDto = CreatedSimulationDto.builder().build();
    CommonResult<CreatedSimulationDto> expected = new CommonResult<CreatedSimulationDto>().success(expectedDto);

    when(apiSbuCkbService.apiValidation(API_KEY, JWT_TOKEN)).thenReturn(new ValidationResponse());
    when(apiSbuCkbService.submission(request)).thenReturn(expected);

    CommonResult<CreatedSimulationDto> actual = controller.createSimulation(request, JWT_TOKEN, API_KEY);

    assertThat(actual).isSameAs(expected);
    verify(apiSbuCkbService).apiValidation(API_KEY, JWT_TOKEN);
    verify(apiSbuCkbService).submission(request);
  }

  @Test
  @DisplayName("getListPostedInvoice validates token and returns invoice data when vendorCode is valid")
  void getListPostedInvoiceSuccess() {
    Map<String, Object> body = Map.of("vendorCode", "VND-100");
    CsulInquiryInvoiceRemoteDto remoteDto = new CsulInquiryInvoiceRemoteDto();
    CommonResult<CsulInquiryInvoiceRemoteDto> expected = new CommonResult<CsulInquiryInvoiceRemoteDto>().success(remoteDto);

    when(apiSbuCkbService.apiValidation(API_KEY, JWT_TOKEN)).thenReturn(new ValidationResponse());
    when(apiSbuCkbService.inquiryListPostedInvoice("VND-100")).thenReturn(expected);

    CommonResult<CsulInquiryInvoiceRemoteDto> actual = controller.getListPostedInvoice(JWT_TOKEN, API_KEY, body);

    assertThat(actual).isSameAs(expected);
    verify(apiSbuCkbService).apiValidation(API_KEY, JWT_TOKEN);
    verify(apiSbuCkbService).inquiryListPostedInvoice("VND-100");
  }

  @Test
  @DisplayName("getListPostedInvoice throws when rawBody is null")
  void getListPostedInvoiceThrowsWhenNullBody() {
    when(apiSbuCkbService.apiValidation(API_KEY, JWT_TOKEN)).thenReturn(new ValidationResponse());

    assertThatThrownBy(() -> controller.getListPostedInvoice(JWT_TOKEN, API_KEY, null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Request body cannot be null");
  }

  @Test
  @DisplayName("getListPostedInvoice throws when vendorCode is empty or missing")
  void getListPostedInvoiceThrowsWhenVendorCodeMissing() {
    when(apiSbuCkbService.apiValidation(API_KEY, JWT_TOKEN)).thenReturn(new ValidationResponse());

    assertThatThrownBy(() -> controller.getListPostedInvoice(JWT_TOKEN, API_KEY, Collections.emptyMap()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("vendorCode is required and cannot be empty");

    Map<String, Object> emptyMap = new HashMap<>();
    emptyMap.put("vendorCode", "  ");
    assertThatThrownBy(() -> controller.getListPostedInvoice(JWT_TOKEN, API_KEY, emptyMap))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("vendorCode is required and cannot be empty");
  }

  @Test
  @DisplayName("updateFinancingStatus validates token and delegates to apiSbuCkbService.statusFinancing")
  void updateFinancingStatusSuccess() {
    String financingHdrCode = "HDR-001";
    StatusLabelDto statusLabelDto = StatusLabelDto.builder().status("INPROCESS").build();
    CommonResult<StatusLabelDto> expected = new CommonResult<StatusLabelDto>().success(statusLabelDto);

    when(apiSbuCkbService.apiValidation(API_KEY, JWT_TOKEN)).thenReturn(new ValidationResponse());
    when(apiSbuCkbService.statusFinancing(API_KEY, financingHdrCode)).thenReturn(expected);

    CommonResult<StatusLabelDto> actual = controller.updateFinancingStatus(JWT_TOKEN, API_KEY, financingHdrCode);

    assertThat(actual).isSameAs(expected);
    verify(apiSbuCkbService).apiValidation(API_KEY, JWT_TOKEN);
    verify(apiSbuCkbService).statusFinancing(API_KEY, financingHdrCode);
  }

  @Test
  @DisplayName("invoicePaid validates token and delegates to apiSbuCkbService.invoicePaid")
  void invoicePaidSuccess() throws Exception {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    BaseResponse expected = new BaseResponseBuilder<>(true, 200, "Success");

    when(apiSbuCkbService.apiValidation(API_KEY, JWT_TOKEN)).thenReturn(new ValidationResponse());
    when(apiSbuCkbService.invoicePaid(API_KEY, request)).thenReturn(expected);

    BaseResponse actual = controller.invoicePaid(JWT_TOKEN, API_KEY, request);

    assertThat(actual).isSameAs(expected);
    verify(apiSbuCkbService).apiValidation(API_KEY, JWT_TOKEN);
    verify(apiSbuCkbService).invoicePaid(API_KEY, request);
  }

  @Test
  @DisplayName("inquiryDisburse validates token and delegates to apiSbuCkbService.inquiryDisburse")
  void inquiryDisburseSuccess() {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    BaseResponse expected = new BaseResponseBuilder<>(true, 200, "Success");

    when(apiSbuCkbService.apiValidation(API_KEY, JWT_TOKEN)).thenReturn(new ValidationResponse());
    when(apiSbuCkbService.inquiryDisburse(API_KEY, request)).thenReturn(expected);

    BaseResponse actual = controller.inquiryDisburse(JWT_TOKEN, API_KEY, request);

    assertThat(actual).isSameAs(expected);
    verify(apiSbuCkbService).apiValidation(API_KEY, JWT_TOKEN);
    verify(apiSbuCkbService).inquiryDisburse(API_KEY, request);
  }
}
