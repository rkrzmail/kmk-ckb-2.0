package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.dto.SimulationHistDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.SimulationHist;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.SimulationHistRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstAppRoleForm;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstApplicationRole;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstRole;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentSubmissionServiceTest {

  private static final UUID USER_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID FINANCING_HDR_CODE = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CUSTOMER_CODE = UUID.fromString("33333333-3333-3333-3333-333333333333");

  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private MstUserRepository mstUserRepository;
  @Mock private AgreementRepository agreementRepository;
  @Mock private AgreementFileRepository agreementFileRepository;
  @Mock private MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
  @Mock private SimulationHistRepository simulationHistRepository;
  @Mock private CurrentUserService currentUserService;

  private AssignmentSubmissionService service;
  private HttpServletRequest httpServletRequest;

  @BeforeEach
  void setUp() {
    service = new AssignmentSubmissionService(
        financingHdrRepository,
        mstUserRepository,
        agreementRepository,
        agreementFileRepository,
        mstAppRoleFormUserRepository,
        simulationHistRepository,
        currentUserService
    );
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8443);
    request.setContextPath("");
    httpServletRequest = request;
  }

  @Test
  void assignmentListReturnsAccountOfficerAssignmentsWithAgreementDocument() throws Exception {
    FinancingHdr financingHdr = financingHdr("INPROCESS", "ASSIGNMENT");
    stubUserAndRole("account_officer");
    when(financingHdrRepository.findAllAssignmentFinancingRaw(eq("JKT"), eq(null), eq(null), eq(null), any()))
        .thenReturn(pageOf(financingHdr));
    when(financingHdrRepository.countByCustomerAndFinancingStatus(financingHdr.getCustomer(), "PAID")).thenReturn(0L);
    Agreement agreement = Agreement.builder().agreementCode("AGR001").build();
    when(agreementRepository.findTopByFinancingHdr(financingHdr)).thenReturn(Optional.of(agreement));
    when(agreementFileRepository.findTopByAgreementOrderByAgreementFileId(agreement))
        .thenReturn(Optional.of(AgreementFile.builder().agreementFileId(9L).build()));

    PaginationResult<AssignmentDto> result = service.assignmentList(httpServletRequest, new PaginationRequest());

    assertThat(result.getCurrentPage()).isEqualTo(1);
    assertThat(result.getList()).hasSize(1);
    AssignmentDto dto = result.getList().get(0);
    assertThat(dto.getFinancingHdrCode()).isEqualTo(FINANCING_HDR_CODE);
    assertThat(dto.getAgreementCode()).isEqualTo("AGR001");
    assertThat(dto.getCustCode()).isEqualTo(CUSTOMER_CODE);
    assertThat(dto.getCustName()).isEqualTo("Customer");
    assertThat(dto.getBouwheerName()).isEqualTo("PT BOUWHEER");
    assertThat(dto.getCustStatus()).isEqualTo("New Customer");
    assertThat(dto.getStatus()).isEqualTo("NEW");
    assertThat(dto.getAgreementDoc()).contains("/api/v1/documents/download/agreement/9");
  }

  @Test
  void assignmentListReturnsBranchAdminAssignmentAndExistingCustomerWithoutAgreement() throws Exception {
    FinancingHdr financingHdr = financingHdr("INPROCESS", "INPROCESS");
    stubUserAndRole("branch_admin");
    PaginationRequest request = pagination(null, null, 1, 5);
    when(financingHdrRepository.findAllAssignmentFinancingRaw(eq("JKT"), eq(null), eq(null), eq(null), any()))
        .thenReturn(pageOf(financingHdr));
    when(financingHdrRepository.countByCustomerAndFinancingStatus(financingHdr.getCustomer(), "PAID")).thenReturn(1L);
    when(agreementRepository.findTopByFinancingHdr(financingHdr)).thenReturn(Optional.empty());

    PaginationResult<AssignmentDto> result = service.assignmentList(httpServletRequest, request);

    assertThat(result.getList()).hasSize(1);
    assertThat(result.getList().get(0).getStatus()).isEqualTo("PREPARATION");
    assertThat(result.getList().get(0).getCustStatus()).isEqualTo("Existing Customer");
    assertThat(result.getList().get(0).getAgreementDoc()).isNull();
  }

  @Test
  void assignmentListFiltersRepositoryByStatusDebtorBouwheerAndCabang() throws Exception {
    for (String searchBy : List.of("status", "namaDebitur", "pemberiKerja", "cabang")) {
      stubUserAndRole("account_officer");
      PaginationRequest request = pagination(searchBy, "needle", 1, 5);
      when(financingHdrRepository.findAllAssignmentFinancingRaw(any(), any(), any(), any(), any()))
          .thenReturn(new PageImpl<>(List.of()));

      service.assignmentList(httpServletRequest, request);
    }
  }

  @Test
  void assignmentListSpecSearchCoversFinancingHdrCustomerBouwheerAndNoMatch() throws Exception {
    FinancingHdr financingHdr = financingHdr("INPROCESS", "INPROCESS");
    stubUserAndRole("account_officer");
    when(financingHdrRepository.findAllAssignmentFinancingRaw(any(), any(), any(), any(), any())).thenReturn(pageOf(financingHdr));
    when(financingHdrRepository.countByCustomerAndFinancingStatus(financingHdr.getCustomer(), "PAID")).thenReturn(0L);
    when(agreementRepository.findTopByFinancingHdr(financingHdr)).thenReturn(Optional.empty());

    assertThat(service.assignmentList(httpServletRequest, pagination("financingHdrCode", FINANCING_HDR_CODE.toString(), 1, 5)).getList()).hasSize(1);
    assertThat(service.assignmentList(httpServletRequest, pagination("custName", "cust", 1, 5)).getList()).hasSize(1);
    assertThat(service.assignmentList(httpServletRequest, pagination("bouwheerName", "bouwheer", 1, 5)).getList()).hasSize(1);
    assertThat(service.assignmentList(httpServletRequest, pagination("unknown", "none", 1, 5)).getList()).isEmpty();
  }

  @Test
  void assignmentListSkipsRowsWithMissingCustomerOrBouwheerAndBranchAdminNewStatus() throws Exception {
    FinancingHdr missingCustomer = financingHdr("INPROCESS", "INPROCESS");
    missingCustomer.setCustomer(null);
    FinancingHdr branchAdminNew = financingHdr("INPROCESS", "ASSIGNMENT");
    stubUserAndRole("branch_admin");
    when(financingHdrRepository.findAllAssignmentFinancingRaw(any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(missingCustomer, branchAdminNew)));

    PaginationResult<AssignmentDto> result = service.assignmentList(httpServletRequest, new PaginationRequest());

    assertThat(result.getList()).isEmpty();
  }

  @Test
  void assignmentListRethrowsWhenAuthenticationFails() throws Exception {
    when(currentUserService.internalUser()).thenThrow(new SignatureException("not authenticated"));

    assertThatThrownBy(() -> service.assignmentList(httpServletRequest, new PaginationRequest()))
        .isInstanceOf(SignatureException.class)
        .hasMessage("not authenticated");
  }

  @Test
  void assignmentListThrowsWhenPermissionMissingRoleChain() throws Exception {
    MstUser user = user("account_officer");
    when(currentUserService.internalUser()).thenReturn(user);
    when(mstUserRepository.findById(USER_CODE)).thenReturn(Optional.of(user));
    when(mstAppRoleFormUserRepository.findTopByUserOrderByAppRoleFormUserId(user)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.assignmentList(httpServletRequest, new PaginationRequest()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void tocListReturnsSimulationHistoryAndAssignsRowNumbers() {
    FinancingHdr financingHdr = financingHdr("INPROCESS", "INPROCESS");
    SimulationHist first = simulationHist(1_000D, 10D);
    SimulationHist second = simulationHist(2_000D, 20D);
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
    when(simulationHistRepository.findAllByFinancingHdr(financingHdr)).thenReturn(Optional.of(List.of(first, second)));

    PaginationResult<SimulationHistDto> result = service.tocList(FINANCING_HDR_CODE.toString(), new PaginationRequest());

    assertThat(result.getList()).hasSize(2);
    assertThat(result.getList().get(0).getNo()).isEqualTo(1);
    assertThat(result.getList().get(0).getFinancingAmt()).isEqualTo(1_000D);
    assertThat(result.getList().get(0).getSchema()).isEqualTo(90D);
    assertThat(result.getList().get(1).getNo()).isEqualTo(2);
  }

  @Test
  void tocListSearchesByFinancingAmountSchemaAndReturnsEmptyForNoMatch() {
    FinancingHdr financingHdr = financingHdr("INPROCESS", "INPROCESS");
    SimulationHist simulationHist = simulationHist(1_000D, 10D);
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
    when(simulationHistRepository.findAllByFinancingHdr(financingHdr)).thenReturn(Optional.of(List.of(simulationHist)));

    assertThat(service.tocList(FINANCING_HDR_CODE.toString(), pagination("financingAmt", "1000", 1, 10)).getList()).hasSize(1);
    assertThat(service.tocList(FINANCING_HDR_CODE.toString(), pagination("schema", "90", 1, 10)).getList()).hasSize(1);
    assertThat(service.tocList(FINANCING_HDR_CODE.toString(), pagination("schema", "99", 1, 10)).getList()).isEmpty();
  }

  @Test
  void tocListRethrowsWhenFinancingHdrMissing() {
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.tocList(FINANCING_HDR_CODE.toString(), new PaginationRequest()))
        .isInstanceOf(NoSuchElementException.class);
  }

  private void stubUserAndRole(String roleCode) throws SignatureException {
    MstUser user = user(roleCode);
    when(currentUserService.internalUser()).thenReturn(user);
    when(mstUserRepository.findById(USER_CODE)).thenReturn(Optional.of(user));
    when(mstAppRoleFormUserRepository.findTopByUserOrderByAppRoleFormUserId(user))
        .thenReturn(Optional.of(permission(roleCode, user)));
  }

  private static MstUser user(String roleCode) {
    MstBranch branch = MstBranch.builder().branchCode("JKT").build();
    MstEmployee employee = MstEmployee.builder().branch(branch).build();
    return MstUser.builder()
        .userCode(USER_CODE)
        .username(roleCode + ".user")
        .employee(employee)
        .build();
  }

  private static MstAppRoleFormUser permission(String roleCode, MstUser user) {
    MstRole role = MstRole.builder().roleCode(roleCode).build();
    MstApplicationRole applicationRole = MstApplicationRole.builder().roleCode(role).build();
    MstAppRoleForm appRoleForm = MstAppRoleForm.builder().applicationRole(applicationRole).build();
    return MstAppRoleFormUser.builder()
        .user(user)
        .appRoleForm(appRoleForm)
        .build();
  }

  private static FinancingHdr financingHdr(String status, String step) {
    Customer customer = new Customer();
    customer.setCustCode(CUSTOMER_CODE);
    customer.setCustName("Customer");
    Bouwheer bouwheer = Bouwheer.builder().bouwheerName("PT BOUWHEER").build();
    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(FINANCING_HDR_CODE);
    financingHdr.setCustomer(customer);
    financingHdr.setBouwheer(bouwheer);
    financingHdr.setFinancingStatus(status);
    financingHdr.setFinancingStep(step);
    financingHdr.setFinancingDueDate(LocalDateTime.of(2026, 9, 12, 10, 0));
    financingHdr.setFinancingAmt(1_000D);
    return financingHdr;
  }

  private static Page<FinancingHdr> pageOf(FinancingHdr financingHdr) {
    return new PageImpl<>(List.of(financingHdr));
  }

  private static PaginationRequest pagination(String searchBy, String searchValue, Integer pageNo, Integer pageSize) {
    PaginationRequest request = new PaginationRequest();
    request.setSearchBy(searchBy);
    request.setSearchValue(searchValue);
    request.setPageNo(pageNo);
    request.setPageSize(pageSize);
    return request;
  }

  private static SimulationHist simulationHist(Double financingAmt, Double retention) {
    return SimulationHist.builder()
        .simulationHistCode(UUID.randomUUID())
        .adminAmt(10D)
        .financingAmt(financingAmt)
        .effectiveRate(1.5D)
        .estDisbust(900D)
        .retention(retention)
        .build();
  }
}
