package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.major_account.request.DistributionSubmissionListRequest;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributionSubmissionServiceIssueRegressionTest {

  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private EmailService emailService;
  @Mock private MstBranchRepository mstBranchRepository;
  @Mock private BranchAreaMappingRepository branchAreaMappingRepository;
  @Mock private ConfigRemoteService configRemoteService;
  @Mock private CustomerRepository customerRepository;
  @Mock private CurrentUserService currentUserService;
  @Mock private AuditTrailService auditTrailService;

  private DistributionSubmissionService service;

  @BeforeEach
  void setUp() {
    service = new DistributionSubmissionService(
      financingHdrRepository,
      emailService,
      mstBranchRepository,
      branchAreaMappingRepository,
      configRemoteService,
      customerRepository,
      currentUserService,
      auditTrailService
    );
    lenient().when(financingHdrRepository.findDistributionReferenceIssues(null, null)).thenReturn(List.of());
  }

  @Test
  void sortsBeforePaginationAndPreservesExistingFilter() {
    var a = row("A", "Vendor A", 10D, LocalDateTime.of(2026, 9, 10, 8, 0));
    var b = row("B", "Vendor B", 2D, LocalDateTime.of(2026, 9, 11, 8, 0));
    var c = row("C", "Other", 100D, LocalDateTime.of(2026, 9, 12, 8, 0));
    when(financingHdrRepository.findAllForDistribution()).thenReturn(List.of(c, a, b));
    var request = new PaginationRequest();
    request.setSortBy("financingAmount");
    request.setSortType("asc");
    request.setPageNo(2);
    request.setPageSize(1);
    request.setSearchBy("NamaDebitur");
    request.setSearchValue("Vendor");
    var baseRequest = new com.kmkbe.helpers.base.BasePaginationRequest(1, 2, "financingAmount", "asc", "NamaDebitur", "Vendor");
    var result = service.submissionDistribution(baseRequest);
    assertThat(result.getList()).extracting(DistributionSubmissionDto::getCustName).containsExactly("Vendor A");
    assertThat(result.getTotalData()).isEqualTo(2);
    request.setSortType("desc");
    assertThat(service.submissionDistribution(request).getList())
      .extracting(DistributionSubmissionDto::getCustName).containsExactly("Vendor B");
    request.setSearchBy(null);
    request.setSearchValue(null);
    request.setPageNo(1);
    request.setPageSize(10);
    request.setSortBy("dtmCrt");
    assertThat(service.submissionDistribution(request).getList())
      .extracting(DistributionSubmissionDto::getCustName).containsExactly("Other", "Vendor B", "Vendor A");
  }

  @Test
  void noSortingKeepsRepositoryOrder() {
    when(financingHdrRepository.findAllForDistribution()).thenReturn(List.of(
      row("B", "Second", 2D, null), row("A", "First", 1D, null)));
    assertThat(service.submissionDistribution(new PaginationRequest()).getList())
      .extracting(DistributionSubmissionDto::getCustName).containsExactly("Second", "First");
  }

  @Test
  void filtersSubmissionsUsingDashboardDateRangeBeforePagination() {
    when(financingHdrRepository.findDistributionReferenceIssues("2026-08-01", "2026-08-31"))
      .thenReturn(List.of());
    when(financingHdrRepository.findAllForDistribution()).thenReturn(List.of(
      row("before", "Before", 1D, LocalDateTime.of(2026, 7, 31, 23, 59)),
      row("inside", "Inside", 2D, LocalDateTime.of(2026, 8, 15, 8, 0)),
      row("after", "After", 3D, LocalDateTime.of(2026, 9, 1, 0, 0))
    ));
    var request = new DistributionSubmissionListRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    request.setStartDate(LocalDate.of(2026, 8, 1));
    request.setEndDate(LocalDate.of(2026, 8, 31));

    var result = service.submissionDistribution(request);

    assertThat(result.getList())
      .extracting(DistributionSubmissionDto::getCustName)
      .containsExactly("Inside");
    assertThat(result.getTotalData()).isEqualTo(1);
  }

  @Test
  void failsWithClearMessageWhenDistributionReferenceDataIsIncomplete() {
    var issue = mock(FinancingHdrRepository.DistributionReferenceIssue.class);
    when(issue.getFinancingHdrCode()).thenReturn("lead-001");
    when(issue.getCustomerMissing()).thenReturn(true);
    when(issue.getBouwheerMissing()).thenReturn(false);
    when(issue.getBranchMissing()).thenReturn(true);
    when(financingHdrRepository.findDistributionReferenceIssues("2026-08-01", "2026-08-31"))
      .thenReturn(List.of(issue));

    var request = new DistributionSubmissionListRequest();
    request.setStartDate(LocalDate.of(2026, 8, 1));
    request.setEndDate(LocalDate.of(2026, 8, 31));

    assertThatThrownBy(() -> service.submissionDistribution(request))
      .isInstanceOf(com.kmkbe.exception.BusinessException.class)
      .hasMessageContaining("Data pengajuan tidak lengkap")
      .hasMessageContaining("customer, branch")
      .hasMessageContaining("lead-001");
    verify(financingHdrRepository, never()).findAllForDistribution();
  }

  @Test
  void invalidSortingFailsBeforeQuery() {
    var request = new PaginationRequest();
    request.setSortBy("unsupported");
    assertThatThrownBy(() -> service.submissionDistribution(request))
      .isInstanceOf(com.kmkbe.exception.BusinessException.class).hasMessageContaining("sortBy tidak didukung");
    verifyNoInteractions(financingHdrRepository);
  }

  private FinancingHdr row(String key, String name, double amount, LocalDateTime date) {
    var customer = Customer.builder().custName(name).custTypeCode("Company").build();
    var header = new FinancingHdr();
    header.setFinancingHdrCode(UUID.nameUUIDFromBytes(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    header.setCustomer(customer);
    header.setBouwheer(Bouwheer.builder().bouwheerName("Bouwheer").build());
    header.setFinancingStatus("NEW");
    header.setFinancingStep("NEW");
    header.setFinancingDueDate(LocalDateTime.of(2026, 10, 1, 0, 0));
    header.setFinancingAmt(amount);
    header.setDtmCrt(date);
    return header;
  }

  @Test
  void submissionDistributionUsesDebtorCompanyAddressInsteadOfBouwheerLegalAddress() {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custTypeCode("Company")
      .npwp("123")
      .build();
    CustomerCompany company = CustomerCompany.builder()
      .customer(customer)
      .companyAddress("Alamat Debitur")
      .city("Jakarta")
      .build();
    customer.setCompany(company);
    Bouwheer bouwheer = Bouwheer.builder()
      .bouwheerCode(UUID.randomUUID())
      .bouwheerName("Bouwheer")
      .legalAddress("Alamat Bouwheer")
      .build();
    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(UUID.randomUUID());
    financingHdr.setCustomer(customer);
    financingHdr.setBouwheer(bouwheer);
    financingHdr.setFinancingStatus("NEW");
    financingHdr.setFinancingStep("NEW");
    financingHdr.setFinancingDueDate(LocalDateTime.now());
    financingHdr.setFinancingAmt(1_000_000D);
    financingHdr.setDtmCrt(LocalDateTime.now());

    when(financingHdrRepository.findAllForDistribution()).thenReturn(List.of(financingHdr));

    PaginationResult<DistributionSubmissionDto> result = service.submissionDistribution(new PaginationRequest());

    assertThat(result.getList()).hasSize(1);
    assertThat(result.getList().getFirst().getAddress()).isEqualTo("Alamat Debitur");
    assertThat(result.getList().getFirst().getAddress()).isNotEqualTo("Alamat Bouwheer");
  }
}
