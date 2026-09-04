package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.dto.DisbursePercentageDto;
import com.kmkbe.core.domain.dto.FinancingDtlDto;
import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.LoanDisburseEmailPayload;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.service.ExistingCustomerService;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.product.repository.ProductRepository;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.remote.service.CurrencyRemoteService;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.modules.user.entity.MstBranch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanSubmissionServiceIssueRegressionTest {

  @Mock private ProductRepository productRepository;
  @Mock private BouwheerRepository bouwheerRepository;
  @Mock private BCryptPasswordEncoder bcryptEncoder;
  @Mock private JdbcTemplate jdbcTemplate;
  @Mock private ConfigRemoteService configRemoteService;
  @Mock private JwtLoanSubmissionService jwtLoanSubmissionService;
  @Mock private SimulationHistRepository simulationHistRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private CustomerRemoteService customerRemoteService;
  @Mock private CurrencyRemoteService currencyRemoteService;
  @Mock private ExistingCustomerService existingCustomerService;
  @Mock private CustomerCompanyRepository customerCompanyRepository;
  @Mock private CustomerPersonalRepository customerPersonalRepository;
  @Mock private AgreementRepository agreementRepository;
  @Mock private InvoiceService invoiceService;
  @Mock private FinancingHdrService financingHdrService;
  @Mock private FinancingDtlService financingDtlService;
  @Mock private MstFileTypeService mstFileTypeService;
  @Mock private EmailService emailService;
  @Mock private LegalFileRepository legalFileRepository;
  @Mock private ImportantNotesService importantNotesService;
  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private MstBranchRepository mstBranchRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private ApiCsulAdapter apiCsulAdapter;
  @Mock private AuditTrailService auditTrailService;
  @Mock private FinancingDtlRepository financingDtlRepository;
  @Mock private BranchAssignmentResolver branchAssignmentResolver;

  private LoanSubmissionService service;

  @BeforeEach
  void setUp() {
    service = new LoanSubmissionService(
      productRepository,
      bouwheerRepository,
      bcryptEncoder,
      jdbcTemplate,
      configRemoteService,
      jwtLoanSubmissionService,
      simulationHistRepository,
      invoiceRepository,
      customerRemoteService,
      currencyRemoteService,
      existingCustomerService,
      customerCompanyRepository,
      customerPersonalRepository,
      agreementRepository,
      invoiceService,
      financingHdrService,
      financingDtlService,
      mstFileTypeService,
      emailService,
      legalFileRepository,
      importantNotesService,
      financingHdrRepository,
      mstBranchRepository,
      customerRepository,
      apiCsulAdapter,
      auditTrailService,
      financingDtlRepository,
      branchAssignmentResolver
    );
  }

  @Test
  void fetchDisbursePercentageIncludesNinetyFivePercent() {
    List<Double> percentages = service.fetchDisbursePercentage().stream()
      .map(DisbursePercentageDto::getDisbursePercentage)
      .toList();

    assertThat(percentages).startsWith(50.0);
    assertThat(percentages).endsWith(95.0);
    assertThat(percentages).contains(90.0, 95.0);
  }

  @Test
  void calculateDisburseRejectsPercentageAboveNinetyFive() {
    CalculateSimulationRequest request = CalculateSimulationRequest.builder()
      .bouwheerCode(UUID.randomUUID().toString())
      .totalInvoiceAmount(BigDecimal.valueOf(1_000_000))
      .disbursePercentage(96.0)
      .build();

    assertThatThrownBy(() -> service.calculateDisburse(null, request))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Persentase pencairan harus di antara 50% sampai 95%");
  }

  @Test
  void assignMappedBranchSetsBranchForFirstSubmission() {
    Customer customer = Customer.builder().custCode(UUID.randomUUID()).build();
    MstBranch branch = MstBranch.builder().branchCode("JKT01").build();
    FinancingHdr financing = new FinancingHdr();
    financing.setCustomer(customer);
    when(branchAssignmentResolver.resolve(customer)).thenReturn(Optional.of(branch));

    service.assignMappedBranch(financing);

    assertThat(financing.getMstBranch()).isSameAs(branch);
  }

  @Test
  void assignMappedBranchLeavesBranchEmptyWhenNoMappingMatches() {
    Customer customer = Customer.builder().custCode(UUID.randomUUID()).build();
    FinancingHdr financing = new FinancingHdr();
    financing.setCustomer(customer);
    when(branchAssignmentResolver.resolve(customer)).thenReturn(Optional.empty());

    service.assignMappedBranch(financing);

    assertThat(financing.getMstBranch()).isNull();
  }

  @Test
  void simulationAdjustmentEmailUsesInvoiceDataDebtorPhoneAndTwoDecimalAmounts() {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("PT Debitur")
      .custTypeCode("Company")
      .custIdNo("80.000.161.8")
      .custExternalCode("800001618")
      .custMobilePhone("800001618")
      .build();
    when(customerCompanyRepository.findByCustomer(customer)).thenReturn(Optional.of(
      CustomerCompany.builder().customer(customer).phone(" 081234567890 ").build()
    ));

    InvoiceDto invoice = new InvoiceDto();
    invoice.setCustInvNo("INV-001");
    invoice.setInvoiceDescription("Jasa pengangkutan CKB");
    invoice.setInvoiceAmt(BigDecimal.valueOf(6_000_000_000D));
    invoice.setInvoiceDate(LocalDateTime.of(2026, 8, 1, 0, 0));
    invoice.setInvoiceDueDate(LocalDateTime.of(2026, 9, 1, 0, 0));
    FinancingDtlDto detail = new FinancingDtlDto();
    detail.setInvoice(invoice);

    FinancingHdrDto financing = FinancingHdrDto.builder()
      .financingHdrCode(UUID.randomUUID())
      .bouwheer(Bouwheer.builder().bouwheerName("PT Cipta Krida Bahari").build())
      .details(List.of(detail))
      .disburseDate(LocalDateTime.of(2026, 8, 2, 0, 0))
      .financingDueDate(LocalDateTime.of(2026, 9, 2, 0, 0))
      .tenor(30L)
      .retention(20D)
      .financingAmt(4_800_000_000D)
      .totalInvoiceAmt(6_000_000_000D)
      .disburseAmt(4_700_000_000D)
      .adminFeeAmt(10_000D)
      .legalFeeAmtNett(20_000D)
      .insuranceFeeAmt(30_000D)
      .othersFeeAmt(40_000D)
      .provisionFeeAmt(50_000D)
      .surveyFeeAmtNett(60_000D)
      .build();

    LoanDisburseEmailPayload payload = service.buildSimulationAdjustmentEmailPayload(financing, customer);

    assertThat(payload.getPhoneNumber()).isEqualTo("081234567890");
    assertThat(payload.getInvoiceAmt()).isEqualTo("6,000,000,000.00");
    assertThat(payload.getFinancingAmt()).isEqualTo("4,800,000,000.00");
    assertThat(payload.getDisburseAmt()).isEqualTo("4,700,000,000.00");
    assertThat(payload.getTotalFeeAmt()).isEqualTo("210,000.00");
    assertThat(payload.getRetention()).isEqualTo("20.00");
    assertThat(payload.getInvoices()).singleElement().satisfies(emailInvoice -> {
      assertThat(emailInvoice.getDescription()).isEqualTo("Jasa pengangkutan CKB");
      assertThat(emailInvoice.getInvoiceAmt()).isEqualTo("6,000,000,000.00");
    });
  }

  @Test
  void debtorPhoneDoesNotReturnNpwpOrVendorCodeStoredAsPhone() {
    Customer customer = Customer.builder()
      .custTypeCode("Company")
      .custIdNo("80.000.161.8")
      .custExternalCode("800001618")
      .custMobilePhone("800001618")
      .build();
    when(customerCompanyRepository.findByCustomer(customer)).thenReturn(Optional.empty());

    assertThat(service.resolveDebtorPhone(customer)).isEmpty();
  }
}
