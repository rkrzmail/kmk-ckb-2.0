package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.email.MailDataDto;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.major_account.request.DistributionSubmissionListRequest;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributionSubmissionServiceCoverageTest {
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock EmailService emailService;
  @Mock MstBranchRepository mstBranchRepository;
  @Mock BranchAreaMappingRepository branchAreaMappingRepository;
  @Mock ConfigRemoteService configRemoteService;
  @Mock CustomerRepository customerRepository;
  @Mock CurrentUserService currentUserService;
  @Mock AuditTrailService auditTrailService;
  DistributionSubmissionService service;

  @BeforeEach
  void setUp() {
    service = new DistributionSubmissionService(financingHdrRepository, emailService,
      mstBranchRepository, branchAreaMappingRepository, configRemoteService,
      customerRepository, currentUserService, auditTrailService);
  }

  @Test
  void submissionDistributionBranches() {
    FinancingHdr company = companyFinancing();
    FinancingHdr personal = personalFinancing();
    FinancingHdr noBranch = financing("COMPLETED", "REFUND");
    noBranch.setMstBranch(null);
    when(financingHdrRepository.findAllForDistribution()).thenReturn(List.of(company, personal, noBranch));
    when(branchAreaMappingRepository.findByCityIgnoreCase(anyString()))
      .thenReturn(Optional.of(BranchAreaMapping.builder()
        .mstBranch(MstBranch.builder().branchCode("REC").branchName("Recommended").build()).build()));

    BasePaginationRequest request = new BasePaginationRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    assertThat(service.submissionDistribution(request).getList()).hasSize(3);

    DistributionSubmissionListRequest dated = new DistributionSubmissionListRequest();
    dated.setPageNo(1);
    dated.setPageSize(10);
    dated.setStartDate(LocalDate.of(2026, 11, 1));
    dated.setEndDate(LocalDate.of(2026, 11, 30));
    assertThat(service.submissionDistribution(dated).getList()).hasSize(3);

    when(financingHdrRepository.findAllForDistribution()).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.submissionDistribution(request)).hasMessage("db down");
  }

  @Test
  void safeHelpersHandleMissingReferences() {
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setCustomer(mock(Customer.class));
    hdr.setBouwheer(mock(Bouwheer.class));
    hdr.setMstBranch(mock(MstBranch.class));
    when(hdr.getCustomer().getCustName()).thenThrow(new EntityNotFoundException("customer"));
    when(hdr.getBouwheer().getBouwheerName()).thenThrow(new EntityNotFoundException("bouwheer"));
    when(hdr.getMstBranch().getBranchCode()).thenThrow(new EntityNotFoundException("branch"));
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "safeCustomer", hdr)).isNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "safeBouwheer", hdr)).isNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "safeMstBranch", hdr)).isNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "toAuditData", (Object) null)).isNull();
  }

  @Test
  void aoAmByBranchValidations() throws Exception {
    assertThatThrownBy(() -> service.aoAmByBranch("B1")).isInstanceOf(RuntimeException.class);
    when(mstBranchRepository.findByBranchCode("B1")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.aoAmByBranch("B1")).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Cabang tidak ditemukan");

    MstBranch inactive = MstBranch.builder().branchCode("B1").isActive(false).businessUnit("CBU").build();
    when(mstBranchRepository.findByBranchCode("B1")).thenReturn(Optional.of(inactive));
    assertThatThrownBy(() -> service.aoAmByBranch("B1")).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Cabang tidak aktif");

    MstBranch nonCbu = MstBranch.builder().branchCode("B1").isActive(true).businessUnit("OTHER").build();
    when(mstBranchRepository.findByBranchCode("B1")).thenReturn(Optional.of(nonCbu));
    assertThatThrownBy(() -> service.aoAmByBranch("B1")).isInstanceOf(RuntimeException.class);

    MstBranch active = MstBranch.builder().branchCode("B1").isActive(true).businessUnit("CBU").build();
    when(mstBranchRepository.findByBranchCode("B1")).thenReturn(Optional.of(active));
    when(configRemoteService.getEmailByPosition("", "B1", "AO/AM")).thenReturn(position(
      MailDataDto.builder().employeeCode("AO1").employeeName("AO").email("ao@example.com").branchCode("B1").build(),
      MailDataDto.builder().employeeCode("AO2").employeeName("").email("ao2@example.com").branchCode("B1").build()));
    assertThat(service.aoAmByBranch("B1")).hasSize(1);
  }

  @Test
  void assignSubmissionRejectsInvalidStates() {
    AssignInvoiceToBranchRequest request = new AssignInvoiceToBranchRequest();
    request.setFinancingHdrCode(UUID.randomUUID().toString());
    request.setBranchCode("B1");
    request.setEmployeeCode("AO1");
    when(mstBranchRepository.findByBranchCode("B1")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.assignSubmission(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Branch Not Found");

    when(mstBranchRepository.findByBranchCode("B1"))
      .thenReturn(Optional.of(MstBranch.builder().branchCode("B1").build()));
    when(financingHdrRepository.findByFinancingHdrCode(UUID.fromString(request.getFinancingHdrCode())))
      .thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.assignSubmission(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Financing Not Found");

    FinancingHdr paid = financing("PAID", "PAID");
    when(financingHdrRepository.findByFinancingHdrCode(UUID.fromString(request.getFinancingHdrCode())))
      .thenReturn(Optional.of(paid));
    assertThatThrownBy(() -> service.assignSubmission(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Financing step has been");

    FinancingHdr invalid = financing("NEW", "SIGNED");
    when(financingHdrRepository.findByFinancingHdrCode(UUID.fromString(request.getFinancingHdrCode())))
      .thenReturn(Optional.of(invalid));
    assertThatThrownBy(() -> service.assignSubmission(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Financing step has been");
  }

  private static MailPositionDto position(MailDataDto... data) {
    return MailPositionDto.builder().data(new ArrayList<>(List.of(data))).build();
  }

  private static FinancingHdr companyFinancing() {
    FinancingHdr hdr = financing("NEW", "NEW");
    Customer customer = hdr.getCustomer();
    customer.setCustTypeCode("Company");
    CustomerCompany company = new CustomerCompany();
    company.setCity("Jakarta");
    company.setCompanyAddress("Address");
    company.setPhone("021");
    customer.setCompany(company);
    return hdr;
  }

  private static FinancingHdr personalFinancing() {
    FinancingHdr hdr = financing("INPROCESS", "SIGNED");
    Customer customer = hdr.getCustomer();
    customer.setCustTypeCode("Personal");
    CustomerPersonal personal = new CustomerPersonal();
    personal.setCity("Bandung");
    personal.setLegalAddress("Address");
    customer.setPersonal(personal);
    hdr.setMstBranch(MstBranch.builder().branchCode("B2").branchName("Branch 2")
      .isActive(true).businessUnit("CBU")
      .employees(Set.of(MstEmployee.builder().employeeName("AO").build())).build());
    return hdr;
  }

  private static FinancingHdr financing(String status, String step) {
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setCustomer(Customer.builder().custCode(UUID.randomUUID()).custName("Customer")
      .custEmail("customer@example.com").custMobilePhone("0812")
      .custTypeCode("Company").existingCust("NEW").build());
    hdr.setBouwheer(Bouwheer.builder().bouwheerName("Bouwheer").build());
    hdr.setFinancingStatus(status);
    hdr.setFinancingStep(step);
    hdr.setDtmCrt(LocalDateTime.of(2026, 11, 24, 0, 0));
    hdr.setFinancingDueDate(LocalDateTime.of(2026, 12, 24, 0, 0));
    hdr.setFinancingAmt(1000.0);
    hdr.setTotalInvoiceAmt(1200.0);
    hdr.setInvoiceQty(1L);
    hdr.setFinancingDtls(Set.of());
    return hdr;
  }
}
