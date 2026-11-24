package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.dto.CreatedSimulationDto;
import com.kmkbe.core.domain.dto.EstimatedDisburseDto;
import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.model.LoanDisburseEmailPayload;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.entity.SimulationHist;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.core.domain.model.JwtSimulasiModel;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.model.SimulationDisburseResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.SimulationHistRepository;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.repository.CustomerCompanyRepository;
import com.kmkbe.core.domain.repository.CustomerPersonalRepository;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.service.ExistingCustomerService;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateLoanApplicationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.SaveImportantNotesRequest;
import com.kmkbe.modules.product.model.entity.Product;
import com.kmkbe.modules.product.repository.ProductRepository;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanSubmissionServiceCoverageTest {
  @Mock ProductRepository productRepository;
  @Mock BouwheerRepository bouwheerRepository;
  @Mock BCryptPasswordEncoder bcryptEncoder;
  @Mock ConfigRemoteService configRemoteService;
  @Mock JwtLoanSubmissionService jwtLoanSubmissionService;
  @Mock SimulationHistRepository simulationHistRepository;
  @Mock InvoiceRepository invoiceRepository;
  @Mock CustomerRemoteService customerRemoteService;
  @Mock ExistingCustomerService existingCustomerService;
  @Mock CustomerCompanyRepository customerCompanyRepository;
  @Mock CustomerPersonalRepository customerPersonalRepository;
  @Mock InvoiceService invoiceService;
  @Mock FinancingHdrService financingHdrService;
  @Mock FinancingDtlService financingDtlService;
  @Mock MstFileTypeService mstFileTypeService;
  @Mock EmailService emailService;
  @Mock LegalFileRepository legalFileRepository;
  @Mock ImportantNotesService importantNotesService;
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock CustomerRepository customerRepository;
  @Mock AuditTrailService auditTrailService;
  @Mock FinancingDtlRepository financingDtlRepository;
  @Mock BranchAssignmentResolver branchAssignmentResolver;
  @Mock MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
  @Mock AgreementRepository agreementRepository;
  @Spy @InjectMocks LoanSubmissionService service;

  @BeforeEach
  void setUp() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
    request.setRequestURI("/api/v1/loan-submissions");
  }

  @Test
  void fetchActiveInvoiceValidationAndPagination() throws Exception {
    Customer customer = customer();
    customer.setCustCode(null);
    assertThatThrownBy(() -> service.fetchActiveInvoice(customer, "token"))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("Vendor code");

    customer.setCustCode(UUID.randomUUID());
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.fetchActiveInvoice(customer, "token"))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("Bouwheer code");

    Bouwheer bouwheer = bouwheer();
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer));
    when(invoiceRepository.findInvoicesByCustCode(anyString())).thenReturn(List.of(invoice()));
    List<PostedInvoiceDto> invoices = service.fetchActiveInvoice(customer, "token");
    assertThat(invoices).hasSize(1);

    BasePaginationRequest pageRequest = new BasePaginationRequest();
    pageRequest.setPageNo(1);
    pageRequest.setPageSize(10);
    pageRequest.setSearchBy("customerInvoiceNo");
    pageRequest.setSearchValue("INV");
    pageRequest.setSortBy("invoiceAmount");
    pageRequest.setSortType("desc");
    PaginationResult<PostedInvoiceDto> result = service.fetchActiveInvoice(customer, "token", pageRequest);
    assertThat(result.getList()).hasSize(1);
  }

  @Test
  void invoiceComparatorAndValueBranches() {
    PostedInvoiceDto dto = PostedInvoiceDto.builder().customerInvoiceNo("INV-1")
      .bouwheerInvoiceNo("B-1").poNumber("PO-1").invoiceDescription("desc")
      .bouwheerName("Bouwheer").invoiceAmount(BigDecimal.TEN)
      .invoiceDate(new Date()).invoiceDueDate(new Date()).postingDate(new Date()).build();
    for (String sort : List.of("customerInvoiceNo", "bouwheerInvoiceNo", "poNumber",
      "invoiceDescription", "bouwheerName", "invoiceDate", "invoiceDueDate", "postingDate",
      "invoiceAmount", "unknown")) {
      assertThatCode(() -> {
        Object comparator = ReflectionTestUtils.invokeMethod(service, "invoiceComparator", sort);
        if (comparator != null) {
          ((java.util.Comparator<PostedInvoiceDto>) comparator).compare(dto, dto);
        }
      }).doesNotThrowAnyException();
    }
    for (String field : List.of("customerInvoiceNo", "bouwheerInvoiceNo", "poNumber",
      "invoiceDescription", "bouwheerName", "invoiceDate", "invoiceDueDate", "postingDate",
      "invoiceAmount", "unknown")) {
      assertThat((String) ReflectionTestUtils.invokeMethod(service, "invoiceValue", dto, field)).isNotNull();
    }
  }

  @Test
  void fetchDisbursePercentageAndCalculateDisburseBranches() throws Exception {
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.empty(), Optional.of(bouwheer()));
    assertThat(service.fetchDisbursePercentage(null)).hasSize(10);
    assertThat(service.fetchDisbursePercentage(UUID.randomUUID().toString())).hasSize(10);

    CalculateSimulationRequest request = CalculateSimulationRequest.builder()
      .bouwheerCode(UUID.randomUUID().toString()).disbursePercentage(80.0)
      .totalInvoiceAmount(BigDecimal.valueOf(1000))
      .invoiceDueDate("30/11/2026").postingDate("01/11/2026").build();
    when(bouwheerRepository.findByBouwheerCode(UUID.fromString(request.getBouwheerCode())))
      .thenReturn(Optional.empty(), Optional.of(bouwheer()), Optional.of(discountBouwheer()));
    assertThatThrownBy(() -> service.calculateDisburse(null, request))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("Bouwheer Not Found");

    request.setDisbursePercentage(20.0);
    assertThatThrownBy(() -> service.calculateDisburse(null, request))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("Persentase");
    request.setDisbursePercentage(80.0);

    when(productRepository.findFirstByAmountInRangeAndBouwheerCode(anyDouble(), any()))
      .thenReturn(Optional.empty(), Optional.of(product()), Optional.of(product()));
    assertThatThrownBy(() -> service.calculateDisburse(null, request))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("Nominal belum terdaftar");

    Customer newCustomer = customer();
    newCustomer.setExistingCust("NEW");
    assertThat(service.calculateDisburse(newCustomer, request).getEstimatedDisburseAmount()).isNotNull();

    Customer existing = customer();
    existing.setExistingCust("Existing Customer");
    Cwr cwr = mock(Cwr.class);
    when(cwr.getPlafondAmt()).thenReturn(3000.0);
    when(existingCustomerService.inquiryAndDecideExistingCustomer(any(), anyString(), any(), any()))
      .thenReturn(cwr);
    assertThat(service.calculateDisburse(existing, request).getProvisionFeeAmount()).isEqualByComparingTo(BigDecimal.ZERO);

    assertThat(service.calculateDisburse(null, request).getEstimatedDisburseAmount()).isNotNull();
  }

  @Test
  void viewAndRecalculateDisburse() {
    FinancingHdr hdr = financingHdr();
    hdr.setEffectiveRate(0.05);
    UUID code = hdr.getFinancingHdrCode();
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr), Optional.of(hdr), Optional.empty());
    assertThat(service.viewCulateDisburse(code.toString(), null).getEffectiveRate()).isEqualTo(5.0);

    hdr.setEffectiveRate(120.0);
    SimulationHist hist = SimulationHist.builder().simulationHistCode(UUID.randomUUID())
      .effectiveRate(8.0).retention(10.0).build();
    when(simulationHistRepository.findTopBySimulationHistCode(hist.getSimulationHistCode())).thenReturn(Optional.of(hist));
    assertThat(service.viewCulateDisburse(code.toString(), hist.getSimulationHistCode().toString())
      .getSimulationHist()).isNotNull();
    assertThat(service.viewCulateDisburse(code.toString(), "invalid")).isNull();

    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    MockHttpServletRequest recalc = requestWithParams("80", "12", "1000", code.toString());
    when(productRepository.findFirstByAmountInRangeAndBouwheerCode(anyDouble(), any()))
      .thenReturn(Optional.of(product()));
    assertThat(service.recalculateDisburse(recalc).getEstimatedDisburseAmount()).isNotNull();

    MockHttpServletRequest missing = requestWithParams("80", "12", "1000", UUID.randomUUID().toString());
    assertThatThrownBy(() -> service.recalculateDisburse(missing)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void createSimulationBranches() throws Exception {
    Customer customer = customer();
    CreateSimulationRequest request = CreateSimulationRequest.builder()
      .vendorCode("V1").productId(1L).disbursePercentage(80.0)
      .invoices(List.of(posted())).build();
    when(customerRepository.findFirstByCustExternalCode("V1")).thenReturn(Optional.empty(), Optional.of(customer));
    assertThatThrownBy(() -> service.createSimulation(null, request)).isInstanceOf(RuntimeException.class);

    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer()));
    EstimatedDisburseDto estimated = EstimatedDisburseDto.builder()
      .financingAmount(BigDecimal.valueOf(100_000_000))
      .estimatedDisburseAmount(BigDecimal.valueOf(95_000_000))
      .totalInvoiceAmount(BigDecimal.valueOf(1000))
      .interestFeeAmount(BigDecimal.TEN).provisionFeeAmount(BigDecimal.ONE)
      .adminFeeAmount(BigDecimal.ONE).othersFeeAmount(BigDecimal.ONE)
      .legalFeeAmount(BigDecimal.ONE).surveyFeeAmount(BigDecimal.ONE)
      .adminRate(1.0).effectiveRate(1.0).provisionRate(1.0)
      .top(3L).tenor(10L).retention(80.0).build();
    doReturn(estimated).when(service).calculateDisburse(any(), any());
    FinancingHdr hdr = financingHdr();
    doReturn(hdr).when(service).resolveFinancingHdr(any(), any(), any(), any(), anyDouble());
    when(invoiceService.createBulk(any(), any(), any())).thenReturn(List.of());

    CreatedSimulationDto result = service.createSimulation(null, request);
    assertThat(result.getFinancingHdrCode()).isEqualTo(hdr.getFinancingHdrCode());

    EstimatedDisburseDto negative = EstimatedDisburseDto.builder()
      .financingAmount(BigDecimal.valueOf(100_000_000)).estimatedDisburseAmount(BigDecimal.valueOf(-1)).build();
    doReturn(negative).when(service).calculateDisburse(any(), any());
    assertThatThrownBy(() -> service.createSimulation(customer, request)).isInstanceOf(RuntimeException.class);

    EstimatedDisburseDto small = EstimatedDisburseDto.builder()
      .financingAmount(BigDecimal.ONE).estimatedDisburseAmount(BigDecimal.ONE).build();
    doReturn(small).when(service).calculateDisburse(any(), any());
    assertThatThrownBy(() -> service.createSimulation(customer, request)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void updateSimulationBranches() throws Exception {
    FinancingHdr hdr = financingHdr();
    SimulationHist hist = SimulationHist.builder().simulationHistCode(UUID.randomUUID())
      .estDisbust(90.0).retention(10.0).adminAmt(5.0).interestAmt(4.0)
      .effectiveRate(0.08).financingAmt(100.0).build();
    MockHttpServletRequest request = requestWithParams("80", "12", "1000", hdr.getFinancingHdrCode().toString());
    request.setParameter("histCode", hist.getSimulationHistCode().toString());
    when(simulationHistRepository.findTopBySimulationHistCode(hist.getSimulationHistCode())).thenReturn(Optional.of(hist));
    when(financingHdrRepository.findByFinancingHdrCode(hdr.getFinancingHdrCode())).thenReturn(Optional.of(hdr));
    when(financingHdrRepository.save(hdr)).thenReturn(hdr);
    when(financingHdrService.dtoFromEntity(hdr)).thenReturn(new FinancingHdrDto());
    assertThatCode(() -> service.updateSimulation(request)).doesNotThrowAnyException();

    request.setParameter("histCode", (String) null);
    EstimatedDisburseDto estimated = EstimatedDisburseDto.builder()
      .adminFeeAmount(BigDecimal.ONE).financingAmount(BigDecimal.valueOf(100))
      .estimatedDisburseAmount(BigDecimal.valueOf(90)).interestFeeAmount(BigDecimal.TEN).build();
    doReturn(estimated).when(service).recalculateDisburse(any());
    when(simulationHistRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    assertThatCode(() -> service.updateSimulation(request)).doesNotThrowAnyException();

    request.setParameter("histCodeUpdate", hist.getSimulationHistCode().toString());
    assertThatCode(() -> service.updateSimulation(request)).doesNotThrowAnyException();
  }

  @Test
  void createLoanSubmissionBranches() {
    Customer customer = customer();
    customer.setCustPin("123456");
    CreateLoanApplicationRequest request = new CreateLoanApplicationRequest();
    request.setFinancingHdrCode(UUID.randomUUID());
    request.setPin("123456");

    assertThatThrownBy(() -> service.createLoanSubmission(null, request)).isInstanceOf(RuntimeException.class);
    when(bcryptEncoder.matches("123456", "123456")).thenReturn(false, true);
    assertThatThrownBy(() -> service.createLoanSubmission(customer, request)).isInstanceOf(RuntimeException.class);

    when(legalFileRepository.findAllByCustCode(customer)).thenReturn(List.of());
    when(mstFileTypeService.getAllMandatory()).thenReturn(List.of());
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.empty(), Optional.of(customer));
    assertThatThrownBy(() -> service.createLoanSubmission(customer, request)).isInstanceOf(RuntimeException.class);

    FinancingHdr hdr = financingHdr();
    hdr.setMstBranch(branch());
    FinancingDtl detail = FinancingDtl.builder().invoice(invoice()).build();
    hdr.setFinancingDtls(Set.of(detail));
    when(financingHdrService.getByCode(request.getFinancingHdrCode())).thenReturn(hdr);
    FinancingHdr prior = financingHdr();
    prior.setMstBranch(branch());
    when(financingHdrRepository.findLatestValidFinancingHdr(customer)).thenReturn(Optional.of(prior));
    when(configRemoteService.getEmailByPosition(anyString(), anyString(), anyString()))
      .thenReturn(mock(com.kmkbe.core.domain.dto.email.MailPositionDto.class));
    FinancingHdrDto dto = financingDto();
    dto.setCustomer(customer);
    when(financingHdrService.dtoFromEntity(hdr)).thenReturn(dto);
    when(customerCompanyRepository.findByCustomer(customer)).thenReturn(Optional.of(company()));

    assertThatCode(() -> service.createLoanSubmission(customer, request)).doesNotThrowAnyException();
    verify(emailService).sendNotificationBranchAssign(any(), any(), any(), any(), any());
    verify(emailService).sendNotificationLoanSubmited(any(), any());

    when(financingHdrRepository.findLatestValidFinancingHdr(customer)).thenReturn(Optional.empty());
    when(branchAssignmentResolver.resolve(customer)).thenReturn(Optional.of(branch()));
    when(mstAppRoleFormUserRepository.findActiveMajorAccountEmails()).thenReturn(List.of("major@example.com"));
    assertThatCode(() -> service.createLoanSubmission(customer, request)).doesNotThrowAnyException();
    verify(emailService).sendNotificationMajorAccount(any(), any(), any(), any());
  }

  @Test
  void helperAndExternalIntegrationBranches() throws Exception {
    when(productRepository.findFirstByAmountInRangeAndBouwheerCode(anyDouble(), any()))
      .thenReturn(Optional.of(product()), Optional.empty());
    FinancingHdr hdr = financingHdr();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "toFinancingAuditData", hdr)).isNotNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "toFinancingAuditData", (Object) null)).isNull();
    SimulationHist hist = SimulationHist.builder().simulationHistCode(UUID.randomUUID()).financingHdr(hdr).build();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "toSimulationAuditData", hist)).isNotNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "toSimulationAuditData", (Object) null)).isNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "findProductByNtfRangeAndBouwheer", 100.0,
      UUID.randomUUID())).isNotNull();
    assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "findProductByNtfRangeAndBouwheer",
      101.0, UUID.randomUUID())).isInstanceOf(RuntimeException.class);
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "parseBouwheerCode", "")).isNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "parseBouwheerCode", UUID.randomUUID().toString()))
      .isInstanceOf(UUID.class);

    Customer customer = customer();
    customer.setCustTypeCode("Company");
    customer.setCustIdNo("12345678901");
    when(customerCompanyRepository.findByCustomer(customer)).thenReturn(Optional.of(company()));
    assertThat(service.resolveDebtorPhone(customer)).isEqualTo("021");
    customer.setCustTypeCode("Personal");
    when(customerPersonalRepository.findByCustomer(customer)).thenReturn(Optional.of(personal("0812")));
    assertThat(service.resolveDebtorPhone(customer)).isEqualTo("0812");
    when(customerPersonalRepository.findByCustomer(customer)).thenReturn(Optional.empty());
    customer.setCustMobilePhone("12345678901");
    assertThat((String) service.resolveDebtorPhone(customer)).isEmpty();

    LoanSubmissionService.applySubmissionMetadata(hdr, "user", LocalDateTime.now());
    assertThat(hdr.getFinancingStatus()).isEqualTo("NEW");

    JwtSimulasiModel jwt = mock(JwtSimulasiModel.class);
    when(jwt.getBouwheerCode()).thenReturn(UUID.randomUUID().toString());
    when(jwt.getVendorCode()).thenReturn("V1");
    when(jwtLoanSubmissionService.extractToken("token")).thenReturn(jwt);
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer()));
    when(importantNotesService.findExternalIntegrationByBouwheerCode(anyString(), anyString()))
      .thenReturn(Optional.empty());
    BaseSimpleRemoteResponseDto<com.kmkbe.core.domain.dto.InquiryVendorRemoteDto> remote = new BaseSimpleRemoteResponseDto<>();
    remote.setData(com.kmkbe.core.domain.dto.InquiryVendorRemoteDto.builder().vendorName("Vendor").build());
    when(customerRemoteService.inquiryVendorByBouwheer(any())).thenReturn(remote);
    when(importantNotesService.generateVendor(any(), anyString(), anyString()))
      .thenReturn(com.kmkbe.core.domain.dto.ExternalIntegrationLoanSimulationDto.VendorDto.builder().vendorCode("V1").build());
    assertThat(service.externalIntegrationSimulation(customer, "token").getVendor()).isNotBlank();
    assertThat(service.externalIntegrationSimulation(customer, "")).isNull();

    when(customerRemoteService.inquiryVendor("V1")).thenReturn(remote);
    when(importantNotesService.create(any(), anyString(), anyString()))
      .thenReturn(com.kmkbe.core.domain.dto.ExternalIntegrationLoanSimulationDto.builder().build());
    SaveImportantNotesRequest notesRequest = new SaveImportantNotesRequest();
    notesRequest.setToken("token");
    assertThat(service.saveImportantNotes(notesRequest)).isNotNull();
    assertThat(service.importanceNotes()).isNull();
  }

  @Test
  void resolveFinancingHdrBranches() {
    Customer customer = customer();
    Bouwheer bouwheer = bouwheer();
    CreateSimulationRequest request = CreateSimulationRequest.builder().invoices(List.of(posted())).build();
    SimulationDisburseResult simulation = simulationResult();

    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
      any(), any(), any(), any())).thenReturn(Optional.empty());
    FinancingHdr created = financingHdr();
    when(financingHdrService.create(any(), any(), any(), any())).thenReturn(created);
    assertThat(service.resolveFinancingHdr(customer, bouwheer, request, simulation, 1000.0)).isSameAs(created);

    Invoice invoice = invoice();
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
      any(), any(), any(), any())).thenReturn(Optional.of(invoice));
    FinancingHdr existing = financingHdr();
    existing.setFinancingStatus("");
    FinancingDtl detail = FinancingDtl.builder().financingHdr(existing).bouwheerInvNo("B-INV-1").build();
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(List.of(invoice.getInvoiceCode())))
      .thenReturn(List.of(detail));
    when(financingHdrRepository.save(existing)).thenReturn(existing);
    assertThat(service.resolveFinancingHdr(customer, bouwheer, request, simulation, 1000.0)).isSameAs(existing);

    existing.setFinancingStatus("INPROCESS");
    assertThatThrownBy(() -> service.resolveFinancingHdr(customer, bouwheer, request, simulation, 1000.0))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("sedang di proses");
  }

  private static MockHttpServletRequest requestWithParams(String schema, String interest,
                                                          String adminFee, String hdrCode) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("schemaRate", schema);
    request.setParameter("intestRate", interest);
    request.setParameter("adminFee", adminFee);
    request.setParameter("financingHdrCode", hdrCode);
    request.setParameter("histCodeUpdate", "");
    return request;
  }

  private static Customer customer() {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID()).custName("Customer").custEmail("customer@example.com")
      .custExternalCode("V1").custMobilePhone("0812").custTypeCode("Company")
      .custIdTypeCode("npwp").custIdNo("12345678901").custPin("123456")
      .bouwheer(UUID.randomUUID().toString()).existingCust("NEW").build();
    return customer;
  }

  private static Bouwheer bouwheer() {
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerCode(UUID.randomUUID());
    bouwheer.setBouwheerName("Bouwheer");
    bouwheer.setGracePeriod(3L);
    bouwheer.setMinRetention(5.0f);
    bouwheer.setInterestScheme("S");
    return bouwheer;
  }

  private static Bouwheer discountBouwheer() {
    Bouwheer bouwheer = bouwheer();
    bouwheer.setInterestScheme("D");
    return bouwheer;
  }

  private static Product product() {
    return Product.builder().productId(1L).effectiveRate(5.0).provisionRate(1.0)
      .adminRate(1.0).surveyFee(1000.0).legalFee(1000.0).othersFee(1000.0).build();
  }

  private static Invoice invoice() {
    Invoice invoice = Invoice.builder().invoiceCode(UUID.randomUUID())
      .custInvNo("INV-1").bouwheerInvNo("B-INV-1").poNumber("PO-1").invoiceAmt(1000.0)
      .invoiceDate(LocalDateTime.of(2026, 11, 1, 0, 0))
      .invoiceDueDate(LocalDateTime.of(2026, 11, 30, 0, 0))
      .postingDate(new Date()).invoiceDescription("desc").build();
    return invoice;
  }

  private static com.kmkbe.core.domain.model.PostedInvoicePayload posted() {
    com.kmkbe.core.domain.model.PostedInvoicePayload posted = new com.kmkbe.core.domain.model.PostedInvoicePayload();
    posted.setBouwheerInvoiceNo("B-INV-1");
    posted.setCustomerInvoiceNo("INV-1");
    posted.setBouwheerCode(UUID.randomUUID().toString());
    posted.setInvoiceAmount(BigDecimal.valueOf(1000));
    posted.setInvoiceDate(new Date());
    posted.setInvoiceDueDate(new Date());
    posted.setPostingDate(new Date());
    return posted;
  }

  private static FinancingHdr financingHdr() {
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setCustomer(customer());
    hdr.setBouwheer(bouwheer());
    hdr.setTenor(12L);
    hdr.setFinancingDate(LocalDateTime.of(2026, 11, 1, 0, 0));
    hdr.setFinancingDueDate(LocalDateTime.of(2026, 12, 1, 0, 0));
    hdr.setFinancingStatus("");
    hdr.setFinancingStep("");
    hdr.setAdminFeeAmt(0.0);
    hdr.setLegalFeeAmtNett(0.0);
    hdr.setInsuranceFeeAmt(0.0);
    hdr.setOthersFeeAmt(0.0);
    hdr.setProvisionFeeAmt(0.0);
    hdr.setSurveyFeeAmtNett(0.0);
    hdr.setTotalInvoiceAmt(1000.0);
    hdr.setFinancingAmt(900.0);
    hdr.setDisburseAmt(850.0);
    hdr.setRetention(80.0);
    hdr.setInterestAmt(10.0);
    hdr.setEffectiveRate(5.0);
    hdr.setFinancingDtls(new java.util.HashSet<>());
    return hdr;
  }

  private static MstBranch branch() {
    MstBranch branch = new MstBranch();
    branch.setBranchCode("BR-1");
    branch.setBranchName("Branch");
    return branch;
  }

  private static CustomerCompany company() {
    CustomerCompany company = new CustomerCompany();
    company.setPhone("021");
    return company;
  }

  private static CustomerPersonal personal(String phone) {
    CustomerPersonal personal = new CustomerPersonal();
    personal.setPhone(phone);
    return personal;
  }

  private static FinancingHdrDto financingDto() {
    FinancingHdrDto dto = new FinancingHdrDto();
    dto.setFinancingHdrCode(UUID.randomUUID());
    dto.setCustomer(customer());
    dto.setBouwheer(bouwheer());
    dto.setDetails(List.of());
    dto.setAdminFeeAmt(0.0);
    dto.setLegalFeeAmtNett(0.0);
    dto.setInsuranceFeeAmt(0.0);
    dto.setOthersFeeAmt(0.0);
    dto.setProvisionFeeAmt(0.0);
    dto.setSurveyFeeAmtNett(0.0);
    dto.setTenor(12L);
    dto.setTotalInvoiceAmt(1000.0);
    dto.setFinancingAmt(900.0);
    dto.setDisburseAmt(850.0);
    dto.setRetention(80.0);
    dto.setDtmCrt(LocalDateTime.of(2026, 11, 1, 0, 0));
    dto.setFinancingDueDate(LocalDateTime.of(2026, 12, 1, 0, 0));
    return dto;
  }

  private static SimulationDisburseResult simulationResult() {
    return SimulationDisburseResult.builder()
      .tenor(12L).top(3L).retention(10.0).totalInvoiceAmount(BigDecimal.valueOf(1000))
      .financingAmount(BigDecimal.valueOf(900)).estimatedDisburseAmount(BigDecimal.valueOf(850))
      .interestFeeAmount(BigDecimal.TEN).maxInvoiceDate(new Date())
      .provisionFeeAmount(BigDecimal.ONE).provisionRate(1.0).adminRate(1.0)
      .effectiveRate(5.0).surveyFeeAmount(BigDecimal.ONE).legalFeeAmount(BigDecimal.ONE)
      .othersFeeAmount(BigDecimal.ONE).adminFeeAmount(BigDecimal.ONE).build();
  }
}
