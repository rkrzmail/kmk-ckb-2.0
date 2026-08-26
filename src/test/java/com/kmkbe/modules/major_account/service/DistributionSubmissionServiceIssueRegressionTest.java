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
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    when(financingHdrRepository.findAllByRaw()).thenReturn(List.of(financingHdr));
    when(financingHdrRepository.countByCustomerAndFinancingStatus(customer, "PAID")).thenReturn(0L);

    PaginationResult<DistributionSubmissionDto> result = service.submissionDistribution(new PaginationRequest());

    assertThat(result.getList()).hasSize(1);
    assertThat(result.getList().getFirst().getAddress()).isEqualTo("Alamat Debitur");
    assertThat(result.getList().getFirst().getAddress()).isNotEqualTo("Alamat Bouwheer");
  }
}
