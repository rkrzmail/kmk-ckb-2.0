package com.kmkbe.modules.apis.service;

import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.dto.CreatedSimulationDto;
import com.kmkbe.core.domain.dto.EstimatedDisburseDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import com.kmkbe.core.service.JwtGeneratorService;
import com.kmkbe.core.service.JwtValidatorService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.service.CustomerCompanyService;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSubmissionRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.service.FinancingDtlService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test untuk modules/apis (ApiSbuCkbService) - target coverage > 90%.
 * Mencakup: createToken, apiValidation, inquiryListPostedInvoice, submission,
 * simulation, approval, invoicePaid, inquiryDisburse, statusFinancing.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApiSbuCkbServiceTest {

  private static final String API_KEY = "app-key-1";
  private static final String APP_SECRET = "secret-1";
  private static final UUID BOUWHEER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock private ApiCsulAdapter apiCsulAdapter;
  @Mock private ApiSbuRepository apiSbuRepository;
  @Mock private JwtValidatorService jwtValidatorService;
  @Mock private CustomerRepository customerRepository;
  @Mock private LoanSubmissionService loanSubmissionService;
  @Mock private BouwheerRepository bouwheerRepository;
  @Mock private JwtGeneratorService jwtGeneratorService;
  @Mock private FinancingService financingService;
  @Mock private FinancingHdrService financingHdrService;
  @Mock private FinancingDtlService financingDtlService;
  @Mock private CustomerCompanyService customerCompanyService;

  private ApiSbuCkbService service;

  @BeforeEach
  void setUp() {
    service = new ApiSbuCkbService(
      apiCsulAdapter, apiSbuRepository, jwtValidatorService, customerRepository,
      loanSubmissionService, bouwheerRepository, jwtGeneratorService,
      financingService, financingHdrService, financingDtlService, customerCompanyService
    );
  }

  private ApiSbu apiSbu() {
    return ApiSbu.builder()
      .appKey(API_KEY)
      .appSecret(APP_SECRET)
      .bouwheerCode(BOUWHEER_UUID)
      .sesStatus("ACTIVE")
      .build();
  }

  private ValidationResponse validationResponse(String bouwheer, Long exp) {
    ValidationResponse response = new ValidationResponse();
    response.setBouwheer(bouwheer);
    response.setExp(exp);
    return response;
  }

  // =========================================================================
  // createToken
  // =========================================================================

  @Test
  @DisplayName("createToken: ApiKey tidak dikenal -> BusinessException 'Invalid Bouwheer API-KEY'")
  void createTokenWithUnknownApiKeyThrows() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.createToken(API_KEY))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("Invalid Bouwheer API-KEY");
  }

  @Test
  @DisplayName("createToken: valid -> token, bouwheer, expired_at tersedia")
  void createTokenSuccess() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));
    when(jwtGeneratorService.generateToken(eq(APP_SECRET), eq(BOUWHEER_UUID.toString()), any(Date.class)))
      .thenReturn("jwt-token-1");

    CommonResult<Map<String, Object>> result = service.createToken(API_KEY);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getCode()).isEqualTo(200);
    assertThat(result.getData()).containsKeys("token", "bouwheer", "expired_at");
    assertThat(result.getData().get("token")).isEqualTo("jwt-token-1");
    assertThat(result.getData().get("bouwheer")).isEqualTo(BOUWHEER_UUID.toString());
  }

  @Test
  @DisplayName("createToken: generator gagal (IllegalArgumentException) -> fail 401")
  void createTokenGeneratorFailureReturnsFail() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));
    when(jwtGeneratorService.generateToken(anyString(), anyString(), any(Date.class)))
      .thenThrow(new IllegalArgumentException("bad secret"));

    CommonResult<Map<String, Object>> result = service.createToken(API_KEY);

    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getCode()).isEqualTo(401);
    assertThat(result.getMessage()).contains("bad secret");
  }

  // =========================================================================
  // apiValidation
  // =========================================================================

  @Test
  @DisplayName("apiValidation: ApiKey tidak dikenal -> BusinessException")
  void apiValidationUnknownApiKeyThrows() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.apiValidation(API_KEY, "jwt"))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("Invalid Bouwheer API-KEY");
  }

  @Test
  @DisplayName("apiValidation: token valid, bouwheer cocok, belum expired -> sukses")
  void apiValidationSuccess() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));
    long future = System.currentTimeMillis() / 1000 + 3600;
    when(jwtValidatorService.validate(eq(API_KEY), eq("jwt"), any(ApiSbu.class)))
      .thenReturn(validationResponse(BOUWHEER_UUID.toString(), future));

    ValidationResponse response = service.apiValidation(API_KEY, "jwt");

    assertThat(response.getBouwheer()).isEqualTo(BOUWHEER_UUID.toString());
    assertThat(response.getExp()).isEqualTo(future);
  }

  @Test
  @DisplayName("apiValidation: bouwheer di token tidak cocok -> BusinessException")
  void apiValidationBouwheerMismatchThrows() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));
    long future = System.currentTimeMillis() / 1000 + 3600;
    when(jwtValidatorService.validate(eq(API_KEY), eq("jwt"), any(ApiSbu.class)))
      .thenReturn(validationResponse("bouwheer-lain", future));

    assertThatThrownBy(() -> service.apiValidation(API_KEY, "jwt"))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("Invalid Bouwheer Code");
  }

  @Test
  @DisplayName("apiValidation: token expired -> BusinessException 'Expired Token'")
  void apiValidationExpiredTokenThrows() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));
    long past = System.currentTimeMillis() / 1000 - 10;
    when(jwtValidatorService.validate(eq(API_KEY), eq("jwt"), any(ApiSbu.class)))
      .thenReturn(validationResponse(BOUWHEER_UUID.toString(), past));

    assertThatThrownBy(() -> service.apiValidation(API_KEY, "jwt"))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("Expired Token");
  }

  // =========================================================================
  // inquiryListPostedInvoice
  // =========================================================================

  @Test
  @DisplayName("inquiryListPostedInvoice: delegasi ke adapter dan bungkus success")
  void inquiryListPostedInvoiceSuccess() {
    CsulInquiryInvoiceRemoteDto dto = new CsulInquiryInvoiceRemoteDto();
    when(apiCsulAdapter.findListPostedInvoice("VND-1")).thenReturn(dto);

    CommonResult<CsulInquiryInvoiceRemoteDto> result = service.inquiryListPostedInvoice("VND-1");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isSameAs(dto);
  }

  // =========================================================================
  // submission
  // =========================================================================

  private CreateSubmissionRequest submissionRequest() {
    return CreateSubmissionRequest.builder()
      .bouwheerCode(BOUWHEER_UUID.toString())
      .vendorCode("VND-1")
      .build();
  }

  @Test
  @DisplayName("submission: bouwheer tidak ditemukan -> BusinessException")
  void submissionBouwheerNotFoundThrows() throws Exception {
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_UUID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.submission(submissionRequest()))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("Bouwheer not found");
  }

  @Test
  @DisplayName("submission: customer sudah ada -> langsung createSimulation")
  void submissionWithExistingCustomer() throws Exception {
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_UUID))
      .thenReturn(Optional.of(Bouwheer.builder().bouwheerName("PT Bouwheer").build()));
    Customer existing = Customer.builder().custName("PT Debitur").build();
    when(customerRepository.findByBouwheerAndCustExternalCode(BOUWHEER_UUID.toString(), "VND-1"))
      .thenReturn(Optional.of(existing));

    CreatedSimulationDto created = CreatedSimulationDto.builder().build();
    when(loanSubmissionService.createSimulation(eq(existing), any(CreateSimulationRequest.class)))
      .thenReturn(created);

    CommonResult<CreatedSimulationDto> result = service.submission(submissionRequest());

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isSameAs(created);
    verify(customerRepository, org.mockito.Mockito.never()).save(any(Customer.class));
  }

  @Test
  @DisplayName("submission: customer belum ada -> buat customer + company, lalu createSimulation")
  void submissionCreatesNewCustomerWhenAbsent() throws Exception {
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_UUID))
      .thenReturn(Optional.of(Bouwheer.builder().bouwheerName("PT Bouwheer").build()));
    when(customerRepository.findByBouwheerAndCustExternalCode(BOUWHEER_UUID.toString(), "VND-1"))
      .thenReturn(Optional.empty());
    when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

    CreatedSimulationDto created = CreatedSimulationDto.builder().build();
    when(loanSubmissionService.createSimulation(any(Customer.class), any(CreateSimulationRequest.class)))
      .thenReturn(created);

    CommonResult<CreatedSimulationDto> result = service.submission(submissionRequest());

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isSameAs(created);

    verify(customerRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
      "tmp.VND-1danasakti.com".equals(saved.getCustEmail())
        && "Customer - PT Bouwheer".equals(saved.getCustName())));
    verify(customerCompanyService).create(any(Customer.class), any());
  }

  // =========================================================================
  // simulation (calculate)
  // =========================================================================

  @Test
  @DisplayName("simulation: customer tidak ditemukan -> BusinessException")
  void simulationCustomerNotFoundThrows() throws Exception {
    CalculateSimulationRequest request = new CalculateSimulationRequest();
    request.setBouwheerCode(BOUWHEER_UUID.toString());
    when(customerRepository.findFirstByBouwheer(BOUWHEER_UUID.toString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.simulation(request))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("Customer Bouwheer not found");
  }

  @Test
  @DisplayName("simulation: customer ditemukan -> calculateDisburse dibungkus success")
  void simulationSuccess() throws Exception {
    CalculateSimulationRequest request = new CalculateSimulationRequest();
    request.setBouwheerCode(BOUWHEER_UUID.toString());
    Customer customer = Customer.builder().custName("PT Debitur").build();
    when(customerRepository.findFirstByBouwheer(BOUWHEER_UUID.toString())).thenReturn(Optional.of(customer));

    EstimatedDisburseDto estimate = EstimatedDisburseDto.builder().build();
    when(loanSubmissionService.calculateDisburse(eq(customer), eq(request))).thenReturn(estimate);

    CommonResult<EstimatedDisburseDto> result = service.simulation(request);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isSameAs(estimate);
  }

  // =========================================================================
  // approval
  // =========================================================================

  @Test
  @DisplayName("approval: ApiKey tidak dikenal -> BusinessException")
  void approvalUnknownApiKeyThrows() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.approval(API_KEY))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("API SBU not found");
  }

  @Test
  @DisplayName("approval: ok -> recallApprovalStatus dipanggil, response sukses")
  void approvalSuccess() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));

    BaseResponseBuilder<?> response = (BaseResponseBuilder<?>) service.approval(API_KEY);

    verify(financingService).recallApprovalStatus();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).isEqualTo("Success Submitted");
  }

  // =========================================================================
  // invoicePaid
  // =========================================================================

  @Test
  @DisplayName("invoicePaid: ApiKey tidak dikenal -> BusinessException")
  void invoicePaidUnknownApiKeyThrows() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.invoicePaid(API_KEY, new FinancingInvoicePaidRequest()))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("API SBU not found");
  }

  @Test
  @DisplayName("invoicePaid: ok -> paidFinancing, updatePaid, paymentReceive dipanggil")
  void invoicePaidSuccess() throws Exception {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));

    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingStatus("");
    financingHdr.setFinancingStep("");
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    when(financingHdrService.paidFinancing(request)).thenReturn(financingHdr);

    BaseResponseBuilder<?> response = (BaseResponseBuilder<?>) service.invoicePaid(API_KEY, request);

    verify(financingHdrService).paidFinancing(request);
    verify(financingDtlService).updatePaid(request, financingHdr);
    verify(financingDtlService).paymentReceive(request, financingHdr);
    assertThat(response.isSuccess()).isTrue();
  }

  // =========================================================================
  // inquiryDisburse
  // =========================================================================

  @Test
  @DisplayName("inquiryDisburse: ApiKey tidak dikenal -> IllegalApiKeyException")
  void inquiryDisburseUnknownApiKeyThrows() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.inquiryDisburse(API_KEY, new FinancingInvoicePaidRequest()))
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  @DisplayName("inquiryDisburse: ok -> paidFinancing dipanggil, response sukses")
  void inquiryDisburseSuccess() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    when(financingHdrService.paidFinancing(request)).thenReturn(new FinancingHdr());

    BaseResponseBuilder<?> response = (BaseResponseBuilder<?>) service.inquiryDisburse(API_KEY, request);

    verify(financingHdrService).paidFinancing(request);
    assertThat(response.isSuccess()).isTrue();
  }

  // =========================================================================
  // statusFinancing
  // =========================================================================

  private FinancingHdr hdrWithStatus(String status) {
    FinancingHdr hdr = org.mockito.Mockito.mock(FinancingHdr.class);
    when(hdr.getFinancingStatus()).thenReturn(status);
    when(hdr.getFinancingStep()).thenReturn("");
    return hdr;
  }

  @Test
  @DisplayName("statusFinancing: ApiKey tidak dikenal -> IllegalApiKeyException")
  void statusFinancingUnknownApiKeyThrows() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.statusFinancing(API_KEY, "abc"))
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  @DisplayName("statusFinancing: status 'new' -> warna abu-abu")
  void statusFinancingNewColor() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));
    FinancingHdr hdrNew = hdrWithStatus("new");
    when(financingHdrService.findByCode("hdr-1")).thenReturn(hdrNew);

    CommonResult<StatusLabelDto> result = service.statusFinancing(API_KEY, "hdr-1");

    assertThat(result.getData().getColor()).isEqualTo("#808080");
    assertThat(result.getData().getStatusLabel()).isEqualTo("New");
  }

  @Test
  @DisplayName("statusFinancing: status proses (inprocess/signing/signed/live/golive/paid) -> warna hijau muda")
  void statusFinancingProcessColors() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));

    for (String status : List.of("inprocess", "SIGNING", "Signed", "live", "GoLive", "paid")) {
      FinancingHdr hdrProcess = hdrWithStatus(status);
      when(financingHdrService.findByCode("hdr-" + status)).thenReturn(hdrProcess);

      CommonResult<StatusLabelDto> result = service.statusFinancing(API_KEY, "hdr-" + status);

      assertThat(result.getData().getColor())
        .as("status %s harus hijau muda", status)
        .isEqualTo("#ccffcc");
    }
  }

  @Test
  @DisplayName("statusFinancing: status tidak dikenal -> warna merah")
  void statusFinancingUnknownColor() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu()));
    FinancingHdr hdrRejected = hdrWithStatus("REJECTED");
    when(financingHdrService.findByCode("hdr-1")).thenReturn(hdrRejected);

    CommonResult<StatusLabelDto> result = service.statusFinancing(API_KEY, "hdr-1");

    assertThat(result.getData().getColor()).isEqualTo("#FF5C5C");
  }

}
