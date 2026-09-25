package com.kmkbe.modules.apis.service;

import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.core.domain.dto.CreatedSimulationDto;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.service.JwtGeneratorService;
import com.kmkbe.core.service.JwtValidatorService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.service.CustomerCompanyService;
import com.kmkbe.modules.customer.service.CustomerService;
import com.kmkbe.modules.loan_submission.service.*;
import com.kmkbe.modules.loan_submission.request.CreateSubmissionRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiSbuCkbServiceTest {

  private static final String API_KEY = "api-key";
  private static final String JWT_TOKEN = "jwt-token";
  private static final UUID BOUWHEER_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock
  private ApiCsulAdapter apiCsulAdapter;

  @Mock
  private ApiSbuRepository apiSbuRepository;

  @Mock
  private JwtValidatorService jwtValidatorService;
  @Mock
  private ApiSbuCkbService service;
  @Mock
  private CustomerRepository customerRepository;
  @Mock
  private LoanSubmissionService loanSubmissionService;
  @Mock
  private BouwheerRepository bouwheerRepository;
  @Mock
  private JwtGeneratorService jwtGeneratorService;
  @Mock
  private FinancingService financingService;
  @Mock
  private FinancingHdrService financingHdrService;
  @Mock
  private FinancingDtlService financingDtlService;
  @Mock
  private CustomerCompanyService customerCompanyService;
  @Mock
  private DocumentService documentService;
  @Mock
  private CustomerService customerService;

  @BeforeEach
  void setUp() {
    service = new ApiSbuCkbService(apiCsulAdapter,
      apiSbuRepository,
      jwtValidatorService,
      customerRepository,
      loanSubmissionService,
      bouwheerRepository,
      jwtGeneratorService,
      financingService,
      financingHdrService,
      financingDtlService,
      customerCompanyService);
  }

  @Test
  void apiValidationReturnsValidationResponseWhenApiKeyBouwheerAndTokenAreValid() throws IOException {
    ApiSbu apiSbu = apiSbu();
    ValidationResponse expected = validationResponse(BOUWHEER_CODE.toString(), futureEpochSecond());
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(jwtValidatorService.validate(API_KEY, JWT_TOKEN, apiSbu)).thenReturn(expected);

    ValidationResponse actual = service.apiValidation(API_KEY, JWT_TOKEN);

    assertThat(actual).isSameAs(expected);
    verify(apiSbuRepository).findByAppKey(API_KEY);
    verify(jwtValidatorService).validate(API_KEY, JWT_TOKEN, apiSbu);
  }

  @Test
  void apiValidationThrowsIllegalApiKeyExceptionWhenApiKeyIsUnknown() {
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.apiValidation(API_KEY, JWT_TOKEN))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Invalid Bouwheer API-KEY");
    verify(apiSbuRepository).findByAppKey(API_KEY);
    verifyNoInteractions(jwtValidatorService);
  }

  @Test
  void apiValidationThrowsBusinessExceptionWhenBouwheerDoesNotMatch() {
    ApiSbu apiSbu = apiSbu();
    ValidationResponse validationResponse = validationResponse(UUID.randomUUID().toString(), futureEpochSecond());
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(jwtValidatorService.validate(API_KEY, JWT_TOKEN, apiSbu)).thenReturn(validationResponse);

    assertThatThrownBy(() -> service.apiValidation(API_KEY, JWT_TOKEN))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Invalid Bouwheer Code");

    verify(apiSbuRepository).findByAppKey(API_KEY);
    verify(jwtValidatorService).validate(API_KEY, JWT_TOKEN, apiSbu);
  }

  @Test
  void apiValidationThrowsBusinessExceptionWhenTokenIsExpired() throws IOException {
    ApiSbu apiSbu = apiSbu();
    ValidationResponse validationResponse = validationResponse(BOUWHEER_CODE.toString(), pastEpochSecond());
    when(apiSbuRepository.findByAppKey(API_KEY)).thenReturn(Optional.of(apiSbu));
    when(jwtValidatorService.validate(API_KEY, JWT_TOKEN, apiSbu)).thenReturn(validationResponse);

    assertThatThrownBy(() -> service.apiValidation(API_KEY, JWT_TOKEN))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Expired Token");
    verify(apiSbuRepository).findByAppKey(API_KEY);
    verify(jwtValidatorService).validate(API_KEY, JWT_TOKEN, apiSbu);
  }

  @Test
  void inquiryListPostedInvoiceReturnsSuccessResultFromCsulAdapter() {
    String vendorCode = "VENDOR-001";
    CsulInquiryInvoiceRemoteDto expected = mock(CsulInquiryInvoiceRemoteDto.class);
    when(apiCsulAdapter.findListPostedInvoice(vendorCode)).thenReturn(expected);

    CommonResult<CsulInquiryInvoiceRemoteDto> result = service.inquiryListPostedInvoice(vendorCode);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getCode()).isEqualTo(200);
    assertThat(result.getMessage()).isEqualTo("Success");
    assertThat(result.getData()).isSameAs(expected);
    verify(apiCsulAdapter).findListPostedInvoice(vendorCode);
  }

  @Test
  void submissionDoesNotStoreVendorCodeAsCustomerPhone() throws Exception {
    String vendorCode = "800001618";
    CreateSubmissionRequest request = CreateSubmissionRequest.builder()
      .vendorCode(vendorCode)
      .bouwheerCode(BOUWHEER_CODE.toString())
      .build();
    Bouwheer bouwheer = Bouwheer.builder()
      .bouwheerCode(BOUWHEER_CODE)
      .bouwheerName("PT Cipta Krida Bahari")
      .build();
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.of(bouwheer));
    when(customerRepository.findByBouwheerAndCustExternalCode(BOUWHEER_CODE.toString(), vendorCode))
      .thenReturn(Optional.empty());
    when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
    CreatedSimulationDto simulation = CreatedSimulationDto.builder().build();
    when(loanSubmissionService.createSimulation(any(Customer.class), any(CreateSimulationRequest.class)))
      .thenReturn(simulation);

    CommonResult<CreatedSimulationDto> result = service.submission(request);

    org.mockito.ArgumentCaptor<Customer> customerCaptor = org.mockito.ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository).save(customerCaptor.capture());
    assertThat(customerCaptor.getValue().getCustMobilePhone()).isEmpty();
    assertThat(customerCaptor.getValue().getCustIdNo()).isEqualTo(vendorCode);
    assertThat(result.getData()).isSameAs(simulation);
  }

  private static ApiSbu apiSbu() {
    return ApiSbu.builder()
        .bouwheerCode(BOUWHEER_CODE)
        .appSecret("secret")
        .build();
  }

  private static ValidationResponse validationResponse(String bouwheerCode, long exp) {
    ValidationResponse response = new ValidationResponse();
    response.setBouwheer(bouwheerCode);
    response.setExp(exp);
    response.setApiKey(API_KEY);
    return response;
  }

  private static long futureEpochSecond() {
    return (System.currentTimeMillis() / 1000) + 60;
  }

  private static long pastEpochSecond() {
    return (System.currentTimeMillis() / 1000) - 60;
  }
}
