package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.DisbursementAccountResponse;
import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.PencarianPayload;
import com.kmkbe.core.domain.repository.DisbursementLogRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.response.InquiryDisburseDatum;
import com.kmkbe.core.domain.response.InquiryDisburseResult;
import com.kmkbe.core.domain.request.InquiryDisburseRequest;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.service.ExternalApiService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.user.entity.MstBranch;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InquiryDisburseEmailTest {
  private final FinancingHdrRepository headers = mock(FinancingHdrRepository.class);
  private final EmailService emails = mock(EmailService.class);
  private final FinancingHdrService headerService = mock(FinancingHdrService.class);
  private final DisbursementLogRepository logs = mock(DisbursementLogRepository.class);
  private final ExternalApiService confins = mock(ExternalApiService.class);
  private final InquiryDisburseService service = spy(new InquiryDisburseService(
    headers, emails, headerService, mock(BaseRemoteService.class), mock(RestTemplate.class),
    logs, mock(FinancingRemoteService.class), confins));

  @Test
  void confirmedDisbursementUsesConfinsAccountAndKeepsExistingRecipients() throws Exception {
    Agreement agreement = confirmedAgreement();
    FinancingHdr header = agreement.getFinancingHdr();
    when(headers.findByFinancingHdrCode(header.getFinancingHdrCode())).thenReturn(Optional.of(header));
    when(headerService.dtoFromEntity(header)).thenReturn(FinancingHdrDto.builder().details(List.of()).build());
    when(logs.findAllByAgreement(agreement)).thenReturn(List.of());
    DisbursementAccountResponse.DisbInfo account = new DisbursementAccountResponse.DisbInfo();
    account.setDisbTo("DEBTOR");
    account.setBankName("BANK CENTRAL ASIA");
    account.setAccNo("3245678000");
    account.setNominal(new BigDecimal("483367500"));
    when(confins.getDebtorDisbursementAccount("41250270017")).thenReturn(account);

    service.inquiryDisburseAuto(agreement);

    ArgumentCaptor<PencarianPayload> payload = ArgumentCaptor.forClass(PencarianPayload.class);
    verify(emails).sendNotificationPencairan(eq("debtor@example.com"), eq("pic@example.com"),
      eq("Pemberi Kerja"), eq("Jakarta"), payload.capture());
    assertThat(payload.getValue().getAgreementCode()).isEqualTo("41250270017");
    assertThat(payload.getValue().getBankName()).isEqualTo("BANK CENTRAL ASIA");
    assertThat(payload.getValue().getAccountNo()).isEqualTo("3245678000");
    assertThat(payload.getValue().getDisburseAmt()).isEqualTo("483,367,500");
    assertThat(payload.getValue().getDisbursementDate()).isNotBlank();
  }

  @Test
  void missingConfinsAccountDoesNotMarkNotificationAsProcessed() throws Exception {
    Agreement agreement = confirmedAgreement();
    when(logs.findAllByAgreement(agreement)).thenReturn(List.of());
    when(confins.getDebtorDisbursementAccount("41250270017"))
      .thenThrow(new IllegalStateException("Rekening DEBTOR tidak tersedia"));

    assertThatThrownBy(() -> service.inquiryDisburseAuto(agreement))
      .isInstanceOf(IllegalStateException.class);

    verify(emails, never()).sendNotificationPencairan(any(), any(), any(), any(), any());
    verify(logs, never()).save(any());
  }

  private Agreement confirmedAgreement() throws Exception {
    Customer customer = new Customer();
    customer.setCustEmail("debtor@example.com");
    customer.setCustName("Debitur");
    customer.setCustMobilePhone("08123456789");
    customer.setCustTypeCode("Personal");
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerName("Pemberi Kerja");
    bouwheer.setPicEmail("pic@example.com");
    MstBranch branch = new MstBranch();
    branch.setBranchName("Jakarta");
    FinancingHdr header = new FinancingHdr();
    header.setFinancingHdrCode(UUID.randomUUID());
    header.setFinancingStatus("LIVE");
    header.setFinancingStep("GOLIVE");
    header.setCustomer(customer);
    header.setBouwheer(bouwheer);
    header.setMstBranch(branch);
    header.setFinancingDate(LocalDateTime.of(2026, 11, 1, 0, 0));
    header.setFinancingDueDate(LocalDateTime.of(2026, 12, 1, 0, 0));
    header.setAdminFeeAmt(0.0);
    header.setLegalFeeAmtNett(0.0);
    header.setInsuranceFeeAmt(0.0);
    header.setOthersFeeAmt(0.0);
    header.setProvisionFeeAmt(0.0);
    header.setSurveyFeeAmtNett(0.0);
    header.setRetention(0.0);
    header.setFinancingAmt(500000000.0);
    header.setTotalInvoiceAmt(500000000.0);
    Agreement agreement = new Agreement();
    agreement.setAgreementCode("41250270017");
    agreement.setFinancingHdr(header);
    InquiryDisburseDatum row = new InquiryDisburseDatum();
    row.setAgreementNo(agreement.getAgreementCode());
    row.setAPStatCode("P");
    InquiryDisburseResult result = new InquiryDisburseResult();
    result.setStatusCode("200");
    result.setData(new ArrayList<>(List.of(row)));
    doReturn(result).when(service).inquiryDisburse(any(InquiryDisburseRequest.class));
    return agreement;
  }
}
