package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.email.MailDataDto;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.LoanDisburseEmailPayload;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributionSubmissionAssignTest {
  @Mock FinancingHdrRepository headers;
  @Mock EmailService email;
  @Mock MstBranchRepository branches;
  @Mock BranchAreaMappingRepository mappings;
  @Mock ConfigRemoteService positions;
  @Mock CustomerRepository customers;
  @Mock CurrentUserService users;
  @Mock AuditTrailService audit;

  private DistributionSubmissionService service;
  private FinancingHdr financing;
  private MstBranch branch;

  @BeforeEach
  void setUp() throws Exception {
    service = new DistributionSubmissionService(headers, email, branches, mappings, positions,
      customers, users, audit);
    branch = MstBranch.builder().branchCode("JKT01").branchName("Jakarta 1")
      .businessUnit("CBU").isActive(true).build();
    financing = new FinancingHdr();
    financing.setFinancingHdrCode(UUID.randomUUID());
    financing.setFinancingStep("NEW");
    financing.setFinancingStatus("NEW");
    financing.setCustomer(Customer.builder().custName("Debitur").custEmail("debtor@example.com")
      .custMobilePhone("08123").custTypeCode("Personal").build());
    financing.setBouwheer(Bouwheer.builder().bouwheerName("Bouwheer").build());
    financing.setFinancingDtls(Set.of());
    financing.setDtmCrt(LocalDateTime.now());
    financing.setFinancingDueDate(LocalDateTime.now());
    financing.setAdminFeeAmt(0D);
    financing.setLegalFeeAmtNett(0D);
    financing.setInsuranceFeeAmt(0D);
    financing.setOthersFeeAmt(0D);
    financing.setProvisionFeeAmt(0D);
    financing.setSurveyFeeAmtNett(0D);
    financing.setRetention(0D);
    financing.setFinancingAmt(0D);
    financing.setTotalInvoiceAmt(0D);
    financing.setDisburseAmt(0D);
  }

  @Test
  void positionResponseCanBeReadFromRemoteJson() throws Exception {
    String json = "{\"data\":[{\"employeeCode\":\"AO2\",\"employeeName\":\"AO Dua\","
      + "\"email\":\"ao2@example.com\",\"branchCode\":\"JKT01\"}]}";
    MailPositionDto response = new ObjectMapper().readValue(json, MailPositionDto.class);
    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().getFirst().getEmployeeCode()).isEqualTo("AO2");
  }

  @Test
  void sendsOnlySelectedAoAndBmToAndAsmCc() throws Exception {
    stubAssignment();
    financing.setFinancingStep("ASSIGNMENT");
    financing.setAssignedAoEmployeeCode("AO1");
    financing.setAssignedAoName("AO Satu");
    financing.setAssignedAoEmail("ao1@example.com");
    when(positions.getEmailByPosition("", "JKT01", "AO/AM")).thenReturn(position(
      person("AO1", "AO Satu", "ao1@example.com", "JKT01"),
      person("AO2", "AO Dua", "ao2@example.com", "JKT01")));
    when(positions.getEmailByPosition("", "JKT01", "BM/BOH")).thenReturn(position(
      person("BM1", "BM", "bm@example.com", "JKT01")));
    when(positions.getEmailByPosition("", "JKT01", "ASM")).thenReturn(position(
      person("ASM1", "ASM", "asm@example.com", "JKT01")));
    when(headers.save(financing)).thenReturn(financing);

    service.assignSubmission(request("AO2"));

    assertThat(financing.getAssignedAoEmployeeCode()).isEqualTo("AO2");
    assertThat(financing.getAssignedAoName()).isEqualTo("AO Dua");
    assertThat(financing.getAssignedAoEmail()).isEqualTo("ao2@example.com");
    assertThat(financing.getFinancingStep()).isEqualTo("ASSIGNMENT");
    ArgumentCaptor<LoanDisburseEmailPayload> payload = ArgumentCaptor.forClass(LoanDisburseEmailPayload.class);
    verify(email).sendNotificationBranchAssign(any(), any(), any(), payload.capture());
    assertThat(payload.getValue().getToEmail().split(";"))
      .containsExactlyInAnyOrder("ao2@example.com", "bm@example.com");
    assertThat(payload.getValue().getCcEmail()).isEqualTo("asm@example.com");
  }

  @Test
  void rejectsAoFromAnotherBranchBeforeSaving() throws Exception {
    stubAssignment();
    when(positions.getEmailByPosition("", "JKT01", "AO/AM")).thenReturn(position(
      person("AO2", "AO Dua", "ao2@example.com", "SBY01")));

    assertThatThrownBy(() -> service.assignSubmission(request("AO2")))
      .hasMessageContaining("AO/AM tidak tersedia");
    verify(headers, never()).save(any());
    verifyNoInteractions(email);
  }

  @Test
  void doesNotSaveAssignmentWhenBmHasNoEmail() throws Exception {
    stubAssignment();
    when(positions.getEmailByPosition("", "JKT01", "AO/AM")).thenReturn(position(
      person("AO2", "AO Dua", "ao2@example.com", "JKT01")));
    when(positions.getEmailByPosition("", "JKT01", "BM/BOH")).thenReturn(position());

    assertThatThrownBy(() -> service.assignSubmission(request("AO2")))
      .hasMessageContaining("Email BM/BOH tidak tersedia");
    verify(headers, never()).save(any());
    verifyNoInteractions(email);
  }

  @Test
  void assignsWithoutCcWhenAsmHasNoEmail() throws Exception {
    stubAssignment();
    when(positions.getEmailByPosition("", "JKT01", "AO/AM")).thenReturn(position(
      person("AO2", "AO Dua", "ao2@example.com", "JKT01")));
    when(positions.getEmailByPosition("", "JKT01", "BM/BOH")).thenReturn(position(
      person("BM1", "BM", "bm@example.com", "JKT01")));
    when(positions.getEmailByPosition("", "JKT01", "ASM")).thenReturn(position());
    when(headers.save(financing)).thenReturn(financing);

    service.assignSubmission(request("AO2"));

    verify(headers).save(financing);
    ArgumentCaptor<LoanDisburseEmailPayload> payload = ArgumentCaptor.forClass(LoanDisburseEmailPayload.class);
    verify(email).sendNotificationBranchAssign(any(), any(), any(), payload.capture());
    assertThat(payload.getValue().getToEmail().split(";"))
      .containsExactlyInAnyOrder("ao2@example.com", "bm@example.com");
    assertThat(payload.getValue().getCcEmail()).isEmpty();
  }

  private AssignInvoiceToBranchRequest request(String employeeCode) {
    AssignInvoiceToBranchRequest request = new AssignInvoiceToBranchRequest();
    request.setFinancingHdrCode(financing.getFinancingHdrCode().toString());
    request.setBranchCode("JKT01");
    request.setEmployeeCode(employeeCode);
    return request;
  }

  private void stubAssignment() throws Exception {
    when(branches.findByBranchCode("JKT01")).thenReturn(Optional.of(branch));
    when(headers.findByFinancingHdrCode(financing.getFinancingHdrCode())).thenReturn(Optional.of(financing));
    MstUser user = new MstUser();
    user.setUsername("major");
    when(users.internalUser()).thenReturn(user);
  }

  private MailPositionDto position(MailDataDto... employees) {
    return MailPositionDto.builder().data(new ArrayList<>(List.of(employees))).build();
  }

  private MailDataDto person(String code, String name, String emailAddress, String branchCode) {
    return MailDataDto.builder().employeeCode(code).employeeName(name)
      .email(emailAddress).branchCode(branchCode).build();
  }
}
