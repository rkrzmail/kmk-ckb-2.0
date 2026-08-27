package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.dto.DisbursePercentageDto;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.service.ExistingCustomerService;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.product.repository.ProductRepository;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.remote.service.CurrencyRemoteService;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
      financingDtlRepository
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
}
