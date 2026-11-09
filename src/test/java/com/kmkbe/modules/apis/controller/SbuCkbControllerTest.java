package com.kmkbe.modules.apis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.CreatedSimulationDto;
import com.kmkbe.core.domain.dto.EstimatedDisburseDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.apis.service.ApiSbuCkbService;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSubmissionRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test SbuCkbController (modules/apis) - memastikan tiap endpoint
 * melakukan apiValidation(apiKey, jwtToken) lalu mendelegasi ke service.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SbuCkbControllerTest {

  private static final String API_KEY = "app-key-1";
  private static final String JWT = "header.payload.signature";

  @Mock private ApiSbuCkbService apiSbuCkbService;

  private SbuCkbController controller;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    controller = new SbuCkbController(objectMapper, apiSbuCkbService);
  }

  @Test
  @DisplayName("GET /request/token -> delegasi createToken(apiKey)")
  void requestTokenDelegates() {
    CommonResult<Map<String, Object>> expected = new CommonResult<Map<String, Object>>()
      .success(Map.of("token", "t1"));
    when(apiSbuCkbService.createToken(API_KEY)).thenReturn(expected);

    CommonResult<Map<String, Object>> result = controller.requestToken(API_KEY);

    assertThat(result).isSameAs(expected);
    verify(apiSbuCkbService).createToken(API_KEY);
  }

  @Test
  @DisplayName("POST /simulations/calculate/{jwt} -> apiValidation lalu simulation")
  void calculateDelegatesAfterValidation() throws Exception {
    CalculateSimulationRequest request = new CalculateSimulationRequest();
    CommonResult<EstimatedDisburseDto> expected = new CommonResult<EstimatedDisburseDto>()
      .success(EstimatedDisburseDto.builder().build());

    when(apiSbuCkbService.simulation(request)).thenReturn(expected);

    CommonResult<EstimatedDisburseDto> result =
      controller.getCalculateDisburse(JWT, API_KEY, request);

    verify(apiSbuCkbService).apiValidation(API_KEY, JWT);
    assertThat(result).isSameAs(expected);
  }

  @Test
  @DisplayName("POST /submissions/{jwt} -> apiValidation lalu submission")
  void submissionsDelegateAfterValidation() throws Exception {
    CreateSubmissionRequest request = CreateSubmissionRequest.builder().build();
    CommonResult<CreatedSimulationDto> expected = new CommonResult<CreatedSimulationDto>()
      .success(CreatedSimulationDto.builder().build());

    when(apiSbuCkbService.submission(request)).thenReturn(expected);

    CommonResult<CreatedSimulationDto> result = controller.createSimulation(request, JWT, API_KEY);

    verify(apiSbuCkbService).apiValidation(API_KEY, JWT);
    assertThat(result).isSameAs(expected);
  }

  @Test
  @DisplayName("POST /listpostedinvoice/{jwt} -> vendorCode diekstrak dari body")
  void listPostedInvoiceExtractsVendorCode() {
    Map<String, Object> rawBody = Map.of("vendorCode", "VND-77");
    CommonResult<CsulInquiryInvoiceRemoteDto> expected = new CommonResult<CsulInquiryInvoiceRemoteDto>()
      .success(new CsulInquiryInvoiceRemoteDto());

    when(apiSbuCkbService.inquiryListPostedInvoice("VND-77")).thenReturn(expected);

    CommonResult<CsulInquiryInvoiceRemoteDto> result =
      controller.getListPostedInvoice(JWT, API_KEY, rawBody);

    verify(apiSbuCkbService).apiValidation(API_KEY, JWT);
    assertThat(result).isSameAs(expected);
  }

  @Test
  @DisplayName("GET /financing/status/{jwt} -> statusFinancing(apiKey, financingHdrCode)")
  void financingStatusDelegates() {
    CommonResult<StatusLabelDto> expected = new CommonResult<StatusLabelDto>()
      .success(StatusLabelDto.builder().color("#808080").build());

    when(apiSbuCkbService.statusFinancing(API_KEY, "hdr-9")).thenReturn(expected);

    CommonResult<StatusLabelDto> result = controller.updateFinancingStatus(JWT, API_KEY, "hdr-9");

    verify(apiSbuCkbService).apiValidation(API_KEY, JWT);
    assertThat(result).isSameAs(expected);
  }

  @Test
  @DisplayName("POST /invoice-paid/{jwt} -> apiValidation lalu invoicePaid")
  void invoicePaidDelegates() throws Exception {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    BaseResponseBuilder<?> expected = new BaseResponseBuilder<>(true, 200, "Success Submitted");

    when(apiSbuCkbService.invoicePaid(API_KEY, request)).thenReturn(expected);

    BaseResponseBuilder<?> result = (BaseResponseBuilder<?>) controller.invoicePaid(JWT, API_KEY, request);

    verify(apiSbuCkbService).apiValidation(API_KEY, JWT);
    assertThat(result).isSameAs(expected);
  }

  @Test
  @DisplayName("POST /inquiry/disburse/{jwt} -> apiValidation lalu inquiryDisburse")
  void inquiryDisburseDelegates() {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    BaseResponseBuilder<?> expected = new BaseResponseBuilder<>(true, 200, "Success Submitted");

    when(apiSbuCkbService.inquiryDisburse(API_KEY, request)).thenReturn(expected);

    BaseResponseBuilder<?> result = (BaseResponseBuilder<?>) controller.inquiryDisburse(JWT, API_KEY, request);

    verify(apiSbuCkbService).apiValidation(API_KEY, JWT);
    assertThat(result).isSameAs(expected);
  }

  @Test
  @DisplayName("apiValidation gagal -> BusinessException menyebar dari controller")
  void apiValidationFailurePropagates() throws Exception {
    when(apiSbuCkbService.apiValidation(API_KEY, JWT))
      .thenThrow(new BusinessException(HttpStatus.CONFLICT, 80, "Invalid Bouwheer API-KEY"));

    CalculateSimulationRequest request = new CalculateSimulationRequest();

    assertThatThrownBy(() -> controller.getCalculateDisburse(JWT, API_KEY, request))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("Invalid Bouwheer API-KEY");
  }
}
