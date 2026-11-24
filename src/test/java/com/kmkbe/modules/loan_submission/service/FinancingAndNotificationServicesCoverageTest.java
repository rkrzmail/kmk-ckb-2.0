package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.FinancingDtlDto;
import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.dto.InquiryAgreementCwrDto;
import com.kmkbe.core.domain.dto.InquiryCwrRemoteDto;
import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.domain.repository.DisbursementLogRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.response.InquiryDisburseDatum;
import com.kmkbe.core.domain.response.InquiryDisburseResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.service.ExternalApiService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.common.service.EmailDeliveryService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancingAndNotificationServicesCoverageTest {
  @Mock AgreementRepository agreementRepository;
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock DisbursementLogRepository disbursementLogRepository;
  @Mock CwrRemoteService cwrRemoteService;
  @Mock CwrRepository cwrRepository;
  @Mock CurrentUserService currentUserService;
  @Mock InquiryDisburseService inquiryDisburseService;
  @Mock EmailService emailService;
  @Mock FinancingHdrService financingHdrService;
  @Mock BaseRemoteService baseRemoteService;
  @Mock RestTemplate restTemplate;
  @Mock FinancingRemoteService financingRemoteService;
  @Mock ExternalApiService externalApiService;
  @Mock FinancingDtlRepository financingDtlRepository;
  @Mock MstAppRoleFormUserRepository roleRepository;
  @Mock EmailDeliveryService emailDeliveryService;

  FinancingService financingService;
  InquiryDisburseService inquiryService;
  InvoicePaidFinanceNotificationService notificationService;

  @BeforeEach
  void setUp() {
    financingService = new FinancingService(agreementRepository, financingHdrRepository,
      inquiryDisburseService, disbursementLogRepository, cwrRemoteService, cwrRepository, currentUserService);
    inquiryService = spy(new InquiryDisburseService(financingHdrRepository, emailService,
      financingHdrService, baseRemoteService, restTemplate, disbursementLogRepository,
      financingRemoteService, externalApiService));
    notificationService = new InvoicePaidFinanceNotificationService(financingHdrRepository,
      financingDtlRepository, agreementRepository, roleRepository, emailService, emailDeliveryService);
  }

  @Test
  void financingServiceRecallAndUpdateBranches() throws Exception {
    when(agreementRepository.viewApprovalStatusNoPending()).thenReturn(null, List.of());
    financingService.recallApprovalStatus();
    financingService.recallApprovalStatus();

    Agreement agreement = new Agreement();
    agreement.setAgreementCode("AGR-1");
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setFinancingStatus("INPROCESS");
    hdr.setFinancingStep("SIGNED");
    hdr.setCurrencyCode("IDR");
    agreement.setFinancingHdr(hdr);
    when(agreementRepository.viewApprovalStatusNoPending()).thenReturn(List.of(agreement));
    when(cwrRemoteService.inquiryAgreementByNoAgreement(any())).thenReturn(agreementResponse());
    when(currentUserService.usernameOrDefault(any())).thenReturn("confins");
    when(disbursementLogRepository.findAllByAgreement(agreement)).thenReturn(List.of());
    financingService.recallApprovalStatus();
    assertThat(agreement.getStatus()).isEqualTo("Live");
    verify(agreementRepository).save(agreement);

    when(agreementRepository.viewApprovalStatusNoPending()).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(financingService::recallApprovalStatus).hasMessage("db down");

    FinancingHdr otherHdr = new FinancingHdr();
    otherHdr.setFinancingStatus("LIVE");
    otherHdr.setFinancingStep("PAID");
    agreement.setFinancingHdr(otherHdr);
    when(financingHdrRepository.findByFinancingHdrCode(otherHdr.getFinancingHdrCode()))
      .thenReturn(Optional.empty(), Optional.of(otherHdr));
    agreement.setStatus("Live");
    financingService.updateFinStatusLiveIfGoLive(agreement);
    financingService.updateFinStatusLiveIfGoLive(agreement);
    assertThat(otherHdr.getFinancingStatus()).isEqualTo("LIVE");
  }

  @Test
  void financingServiceRecallCwr() throws Exception {
    when(cwrRepository.findAllByStatusIsIn(any())).thenReturn(null, List.of());
    financingService.recallCWRStatus();
    financingService.recallCWRStatus();

    Cwr cwr = new Cwr();
    cwr.setCwrCode("CWR-1");
    when(cwrRepository.findAllByStatusIsIn(any())).thenReturn(List.of(cwr));
    BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> response = new BaseMstRemoteResponseDto<>();
    InquiryCwrRemoteDto cwrRemote = mock(InquiryCwrRemoteDto.class);
    lenient().when(cwrRemote.getStartDt()).thenReturn("2026-11-01T00:00:00");
    lenient().when(cwrRemote.getEndDt()).thenReturn("2026-12-01T00:00:00");
    lenient().when(cwrRemote.getRealisationAmt()).thenReturn(100.0);
    lenient().when(cwrRemote.getPlafondAmt()).thenReturn(200.0);
    lenient().when(cwrRemote.getCurrency()).thenReturn("IDR");
    lenient().when(cwrRemote.getCwrStatDescr()).thenReturn("ACTIVE");
    response.setData(List.of(cwrRemote));
    when(cwrRemoteService.inquiryCwr(any())).thenReturn(response);
    when(currentUserService.usernameOrDefault(any())).thenReturn("confins");
    financingService.recallCWRStatus();
    assertThat(cwr.getStatus()).isEqualTo("ACTIVE");

    response.setData(List.of());
    financingService.recallCWRStatus();
  }

  @Test
  void inquiryDisburseParsesResponseAndPropagatesError() throws Exception {
    when(baseRemoteService.adInsKeyHeaders()).thenReturn(new HttpHeaders());
    when(baseRemoteService.inquiry_Disburse()).thenReturn("http://example.com");
    String json = "{\"StatusCode\":\"200\",\"Data\":[],\"Count\":0}";
    when(restTemplate.exchange(any(String.class), any(), any(), any(ParameterizedTypeReference.class)))
      .thenReturn(ResponseEntity.ok(json));
    InquiryDisburseResult result = inquiryService.inquiryDisburse(null);
    assertThat(result.getStatusCode()).isEqualTo("200");

    when(baseRemoteService.adInsKeyHeaders()).thenThrow(new RuntimeException("remote down"));
    assertThatThrownBy(() -> inquiryService.inquiryDisburse(null)).hasMessage("remote down");
  }

  @Test
  void inquiryDisburseAutoCoversNoDataAndPaidBranches() throws Exception {
    Agreement agreement = agreement();
    doReturn(result("200", List.of())).when(inquiryService).inquiryDisburse(any());
    assertThat(inquiryService.inquiryDisburseAuto(agreement)).isFalse();

    InquiryDisburseDatum datum = datum();
    doReturn(result("500", List.of(datum))).when(inquiryService).inquiryDisburse(any());
    assertThat(inquiryService.inquiryDisburseAuto(agreement)).isFalse();

    doReturn(result("200", List.of(datum))).when(inquiryService).inquiryDisburse(any());
    when(disbursementLogRepository.findAllByAgreement(agreement)).thenReturn(List.of());
    var account = new com.kmkbe.core.domain.dto.DisbursementAccountResponse.DisbInfo();
    account.setNominal(BigDecimal.TEN);
    account.setBankName("Bank");
    account.setAccNo("123");
    when(externalApiService.getDebtorDisbursementAccount("AGR-1")).thenReturn(account);
    when(externalApiService.getDisbursementExecutionDate("AP-1", "BR-1", "IDR"))
      .thenReturn(LocalDateTime.of(2026, 11, 24, 0, 0));
    when(financingHdrRepository.findByCodeWithDetails(agreement.getFinancingHdr().getFinancingHdrCode()))
      .thenReturn(Optional.of(agreement.getFinancingHdr()));
    when(financingHdrService.dtoFromEntity(agreement.getFinancingHdr())).thenReturn(financingDto());

    assertThat(inquiryService.inquiryDisburseAuto(agreement)).isFalse();
    verify(emailService).sendNotificationPencairan(any(), any(), any(), any(), any());
    verify(disbursementLogRepository).save(any());

    when(financingHdrRepository.findByCodeWithDetails(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> inquiryService.inquiryDisburseAuto(agreement))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void invoicePaidNotificationBranches() {
    UUID code = UUID.randomUUID();
    notificationService.onInvoicePaid(new InvoicePaidEvent(code));
    verify(emailDeliveryService, org.mockito.Mockito.never()).recordFinanceInvoicePaid(any(), any(), any());

    FinancingHdr financing = financing();
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(financing));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(code)).thenReturn(List.of());
    notificationService.onInvoicePaid(new InvoicePaidEvent(code));

    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(code)).thenReturn(List.of(agreement()));
    when(roleRepository.findActiveFinanceEmails()).thenReturn(List.of());
    notificationService.onInvoicePaid(new InvoicePaidEvent(code));

    when(roleRepository.findActiveFinanceEmails()).thenReturn(List.of("finance@x.com"));
    when(financingDtlRepository.findAllByFinancingHdr(financing)).thenReturn(Optional.of(List.of()));
    notificationService.onInvoicePaid(new InvoicePaidEvent(code));

    FinancingDtl paid = FinancingDtl.builder().invoice(invoice("PAID")).build();
    when(financingDtlRepository.findAllByFinancingHdr(financing)).thenReturn(Optional.of(List.of(paid)));
    when(emailService.sendFinanceInvoicePaidNotification(any(), any(), any()))
      .thenReturn(new EmailService.DeliveryResult(true, null),
        new EmailService.DeliveryResult(false, "smtp down"));
    notificationService.onInvoicePaid(new InvoicePaidEvent(code));
    notificationService.onInvoicePaid(new InvoicePaidEvent(code));
    verify(emailDeliveryService, org.mockito.Mockito.times(5))
      .recordFinanceInvoicePaid(any(), any(), any());
  }

  private static BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> agreementResponse() {
    InquiryAgreementCwrDto dto = InquiryAgreementCwrDto.builder()
      .status("Live").currency("IDR").ntfAmt(1000.0).productOffering("PO").facility("FAC").build();
    BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = new BaseMstRemoteResponseDto<>();
    response.setData(List.of(dto));
    return response;
  }

  private static InquiryDisburseResult result(String status, List<InquiryDisburseDatum> data) {
    return InquiryDisburseResult.builder().statusCode(status).data(new ArrayList<>(data)).build();
  }

  private static InquiryDisburseDatum datum() {
    return InquiryDisburseDatum.builder().agreementNo("AGR-1").aPStatCode("P")
      .aPNo("AP-1").officeCode("BR-1").aPDescr("desc").currCode("IDR")
      .apAmt(1000).aPPaidAmt(1000).aPAmtInProces(0).unpaidAmt(0)
      .aPTypeCode("TYPE").aPTypeName("TypeName").aPDueDt(LocalDateTime.now())
      .apPaidLocCode("LOC").build();
  }

  private static FinancingHdrDto financingDto() {
    InvoiceDto invoice = new InvoiceDto();
    invoice.setCustInvNo("INV-1");
    invoice.setInvoiceAmt(BigDecimal.TEN);
    invoice.setInvoiceDate(LocalDateTime.of(2026, 11, 1, 0, 0));
    invoice.setInvoiceDueDate(LocalDateTime.of(2026, 12, 1, 0, 0));
    invoice.setInvoiceDescription("desc");
    FinancingDtlDto detail = new FinancingDtlDto();
    detail.setInvoice(invoice);
    FinancingHdrDto result = new FinancingHdrDto();
    result.setDetails(List.of(detail));
    return result;
  }

  private static Agreement agreement() {
    Agreement agreement = new Agreement();
    agreement.setAgreementCode("AGR-1");
    agreement.setFinancingHdr(financing());
    return agreement;
  }

  private static FinancingHdr financing() {
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setFinancingStatus("INPROCESS");
    hdr.setFinancingStep("SIGNED");
    hdr.setCurrencyCode("IDR");
    hdr.setDtmCrt(LocalDateTime.of(2026, 11, 1, 0, 0));
    hdr.setFinancingDate(LocalDateTime.of(2026, 11, 1, 0, 0));
    hdr.setFinancingDueDate(LocalDateTime.of(2026, 12, 1, 0, 0));
    hdr.setTenor(12L);
    hdr.setRetention(0.0);
    hdr.setAdminFeeAmt(0.0);
    hdr.setLegalFeeAmtNett(0.0);
    hdr.setInsuranceFeeAmt(0.0);
    hdr.setOthersFeeAmt(0.0);
    hdr.setProvisionFeeAmt(0.0);
    hdr.setSurveyFeeAmtNett(0.0);
    hdr.setFinancingAmt(1000.0);
    hdr.setTotalInvoiceAmt(1200.0);
    hdr.setAdminFeeAmt(0.0);
    hdr.setLegalFeeAmtNett(0.0);
    hdr.setInsuranceFeeAmt(0.0);
    hdr.setOthersFeeAmt(0.0);
    hdr.setProvisionFeeAmt(0.0);
    hdr.setSurveyFeeAmtNett(0.0);
    hdr.setBouwheer(Bouwheer.builder().bouwheerName("Bouwheer").picEmail("pic@x.com").build());
    hdr.setMstBranch(branch());
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    customer.setCustName("Customer");
    customer.setCustEmail("customer@example.com");
    customer.setCustMobilePhone("0812");
    customer.setCustTypeCode("Company");
    var company = new com.kmkbe.core.domain.entity.CustomerCompany();
    company.setPhone("021");
    customer.setCompany(company);
    hdr.setCustomer(customer);
    return hdr;
  }

  private static MstBranch branch() {
    MstBranch branch = new MstBranch();
    branch.setBranchName("Branch");
    return branch;
  }

  private static Invoice invoice(String status) {
    return Invoice.builder().custInvNo("INV-1").status(status).invoiceDescription("desc")
      .invoiceAmt(10.0)
      .invoiceDate(LocalDateTime.of(2026, 11, 1, 0, 0))
      .invoiceDueDate(LocalDateTime.of(2026, 12, 1, 0, 0)).build();
  }
}
