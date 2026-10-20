package com.kmkbe.modules.apis.service;

import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.dto.CreatedSimulationDto;
import com.kmkbe.core.domain.dto.EstimatedDisburseDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.service.JwtGeneratorService;
import com.kmkbe.core.service.JwtValidatorService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
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

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiSbuCkbServiceComprehensiveTest {

  private static final String API_KEY = "test-api-key";
  private static final String JWT_TOKEN = "test-jwt-token";
  private static final UUID BOUWHEER_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");

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
      apiCsulAdapter,
      apiSbuRepository,
      jwtValidatorService,
      customerRepository,
      loanSubmissionService,
      bouwheerRepository,
      jwtGeneratorService,
      financingService,
      financingHdrService,
      financingDtlService,
      customerCompanyService
    );
  }

  // ---------- 1. createToken ----------

  @Test
  @DisplayName("createToken throws BusinessException when apiKey not found in DB")
  void createTokenThrowsWhenApiKeyNotFound() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.createToken(API_KEY))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Invalid Bouwheer API-KEY");
  }

  @Test
  @DisplayName("createToken returns success map when token generated normally")
  void createTokenSuccess() {
    ApiSbu apiSbu = ApiSbu.builder()
      .appSecret("my-secret")
      .bouwheerCode(BOUWHEER_CODE)
      .build();
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(jwtGeneratorService.generateToken(eq("my-secret"), eq(BOUWHEER_CODE.toString()), any(Date.class)))
      .thenReturn("generated-jwt");

    CommonResult<Map<String, Object>> result = service.createToken(API_KEY);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().get("token")).isEqualTo("generated-jwt");
    assertThat(result.getData().get("bouwheer")).isEqualTo(BOUWHEER_CODE.toString());
    assertThat(result.getData().get("expired_at")).isNotNull();
  }

  @Test
  @DisplayName("createToken handles IllegalArgumentException from jwtGeneratorService and returns 401")
  void createTokenCatchesIllegalArgumentException() {
    ApiSbu apiSbu = ApiSbu.builder()
      .appSecret("my-secret")
      .bouwheerCode(BOUWHEER_CODE)
      .build();
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(jwtGeneratorService.generateToken(anyString(), anyString(), any(Date.class)))
      .thenThrow(new IllegalArgumentException("Secret key format invalid"));

    CommonResult<Map<String, Object>> result = service.createToken(API_KEY);

    assertThat(result.getCode()).isEqualTo(401);
    assertThat(result.getMessage()).isEqualTo("Secret key format invalid");
  }

  // ---------- 2. submission (with existing customer & missing bouwheer) ----------

  @Test
  @DisplayName("submission throws BusinessException when Bouwheer is not found")
  void submissionThrowsWhenBouwheerNotFound() {
    CreateSubmissionRequest request = CreateSubmissionRequest.builder()
      .bouwheerCode(BOUWHEER_CODE.toString())
      .vendorCode("VND-001")
      .build();
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.submission(request))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Bouwheer not found");
  }

  @Test
  @DisplayName("submission succeeds with existing customer without creating new customer")
  void submissionSucceedsWithExistingCustomer() throws Exception {
    CreateSubmissionRequest request = CreateSubmissionRequest.builder()
      .bouwheerCode(BOUWHEER_CODE.toString())
      .vendorCode("VND-001")
      .productId(10L)
      .disbursePercentage(80.0)
      .totalInvoiceAmount(500000.0)
      .build();

    Bouwheer bouwheer = Bouwheer.builder().bouwheerCode(BOUWHEER_CODE).bouwheerName("PT CKB").build();
    Customer existingCustomer = Customer.builder().custCode(UUID.randomUUID()).custName("PT Vendor").build();
    CreatedSimulationDto simulationDto = CreatedSimulationDto.builder().build();

    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.of(bouwheer));
    when(customerRepository.findByBouwheerAndCustExternalCode(BOUWHEER_CODE.toString(), "VND-001"))
      .thenReturn(Optional.of(existingCustomer));
    when(loanSubmissionService.createSimulation(eq(existingCustomer), any(CreateSimulationRequest.class)))
      .thenReturn(simulationDto);

    CommonResult<CreatedSimulationDto> result = service.submission(request);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isSameAs(simulationDto);
    verify(customerRepository, never()).save(any());
    verify(customerCompanyService, never()).create(any(), any());
  }

  // ---------- 3. simulation (calculate disburse) ----------

  @Test
  @DisplayName("simulation throws BusinessException when Customer Bouwheer not found")
  void simulationThrowsWhenCustomerBouwheerNotFound() {
    CalculateSimulationRequest request = new CalculateSimulationRequest();
    request.setBouwheerCode(BOUWHEER_CODE.toString());

    when(customerRepository.findFirstByBouwheer(BOUWHEER_CODE.toString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.simulation(request))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Customer Bouwheer not found");
  }

  @Test
  @DisplayName("simulation returns EstimatedDisburseDto when customer exists")
  void simulationSuccess() throws Exception {
    CalculateSimulationRequest request = new CalculateSimulationRequest();
    request.setBouwheerCode(BOUWHEER_CODE.toString());

    Customer customer = Customer.builder().custCode(UUID.randomUUID()).build();
    EstimatedDisburseDto expectedDto = EstimatedDisburseDto.builder().build();

    when(customerRepository.findFirstByBouwheer(BOUWHEER_CODE.toString())).thenReturn(Optional.of(customer));
    when(loanSubmissionService.calculateDisburse(customer, request)).thenReturn(expectedDto);

    CommonResult<EstimatedDisburseDto> result = service.simulation(request);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isSameAs(expectedDto);
  }

  // ---------- 4. approval ----------

  @Test
  @DisplayName("approval throws BusinessException when ApiSbu not found")
  void approvalThrowsWhenApiKeyNotFound() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.approval(API_KEY))
      .isInstanceOf(BusinessException.class)
      .hasMessage("API SBU not found");
  }

  @Test
  @DisplayName("approval recalls approval status and returns success BaseResponse")
  void approvalSuccess() {
    ApiSbu apiSbu = ApiSbu.builder().build();
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));

    BaseResponse response = service.approval(API_KEY);

    assertThat(response).isInstanceOf(BaseResponseBuilder.class);
    BaseResponseBuilder<?> builder = (BaseResponseBuilder<?>) response;
    assertThat(builder.isSuccess()).isTrue();
    assertThat(builder.getMessage()).isEqualTo("Success Submitted");
    verify(financingService).recallApprovalStatus();
  }

  // ---------- 5. invoicePaid ----------

  @Test
  @DisplayName("invoicePaid throws BusinessException when ApiSbu not found")
  void invoicePaidThrowsWhenApiKeyNotFound() {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.invoicePaid(API_KEY, request))
      .isInstanceOf(BusinessException.class)
      .hasMessage("API SBU not found");
  }

  @Test
  @DisplayName("invoicePaid updates paid and paymentReceive then returns BaseResponse")
  void invoicePaidSuccess() throws Exception {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    request.setFinancingCode("FIN-001");
    ApiSbu apiSbu = ApiSbu.builder().build();
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingStep("PAID");
    hdr.setFinancingStatus("PAID");

    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(financingHdrService.paidFinancing(request)).thenReturn(hdr);

    BaseResponse response = service.invoicePaid(API_KEY, request);

    assertThat(response).isInstanceOf(BaseResponseBuilder.class);
    BaseResponseBuilder<?> builder = (BaseResponseBuilder<?>) response;
    assertThat(builder.isSuccess()).isTrue();
    verify(financingDtlService).updatePaid(request, hdr);
    verify(financingDtlService).paymentReceive(request, hdr);
  }

  // ---------- 6. inquiryDisburse ----------

  @Test
  @DisplayName("inquiryDisburse throws IllegalApiKeyException when apiKey not found")
  void inquiryDisburseThrowsWhenApiKeyNotFound() {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.inquiryDisburse(API_KEY, request))
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  @DisplayName("inquiryDisburse calls paidFinancing and returns BaseResponse")
  void inquiryDisburseSuccess() {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    ApiSbu apiSbu = ApiSbu.builder().build();
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));

    BaseResponse response = service.inquiryDisburse(API_KEY, request);

    assertThat(response).isInstanceOf(BaseResponseBuilder.class);
    BaseResponseBuilder<?> builder = (BaseResponseBuilder<?>) response;
    assertThat(builder.isSuccess()).isTrue();
    verify(financingHdrService).paidFinancing(request);
  }

  // ---------- 7. statusFinancing (all status color branches) ----------

  @Test
  @DisplayName("statusFinancing throws IllegalApiKeyException when apiKey not found")
  void statusFinancingThrowsWhenApiKeyNotFound() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.statusFinancing(API_KEY, "HDR-001"))
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  @DisplayName("statusFinancing sets gray color for status NEW")
  void statusFinancingColorNew() {
    ApiSbu apiSbu = ApiSbu.builder().build();
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingStatus("NEW");
    hdr.setFinancingStep("NEW");

    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(financingHdrService.findByCode("HDR-001")).thenReturn(hdr);

    CommonResult<StatusLabelDto> result = service.statusFinancing(API_KEY, "HDR-001");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData().getColor()).isEqualTo("#808080");
  }

  @Test
  @DisplayName("statusFinancing sets green color for INPROCESS, SIGNED, PAID statuses")
  void statusFinancingColorGreen() {
    ApiSbu apiSbu = ApiSbu.builder().build();
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingStatus("INPROCESS");
    hdr.setFinancingStep("INPROCESS");

    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(financingHdrService.findByCode("HDR-002")).thenReturn(hdr);

    CommonResult<StatusLabelDto> result = service.statusFinancing(API_KEY, "HDR-002");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData().getColor()).isEqualTo("#ccffcc");
  }

  @Test
  @DisplayName("statusFinancing sets red color for REJECTED or unknown status")
  void statusFinancingColorRed() {
    ApiSbu apiSbu = ApiSbu.builder().build();
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingStatus("REJECTED");
    hdr.setFinancingStep("REJECTED");

    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(financingHdrService.findByCode("HDR-003")).thenReturn(hdr);

    CommonResult<StatusLabelDto> result = service.statusFinancing(API_KEY, "HDR-003");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData().getColor()).isEqualTo("#FF5C5C");
  }
}
