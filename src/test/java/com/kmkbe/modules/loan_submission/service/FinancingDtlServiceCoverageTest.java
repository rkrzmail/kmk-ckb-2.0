package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.InquiryDataAgreementDto;
import com.kmkbe.core.domain.dto.InquiryDataAgreementDtoAgrmntObj;
import com.kmkbe.core.domain.dto.InquiryNewInfoAgreementDto;
import com.kmkbe.core.domain.dto.InquiryOutstandingBillDetailtDto;
import com.kmkbe.core.domain.dto.InquiryReceiveAmountRequestDto;
import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.dto.SubmitPaymentReceiveRequestDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.entity.PaymentReceiveHistory;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.PaymentReceiveHistoryRepository;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancingDtlServiceCoverageTest {
  @Mock FinancingDtlRepository financingDtlRepository;
  @Mock PaymentReceiveHistoryRepository historyRepository;
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock BaseRemoteService baseRemoteService;
  @Mock ObjectMapper objectMapper;
  @Mock RestTemplate restTemplate;
  @Mock ApplicationEventPublisher eventPublisher;
  @Mock Environment environment;
  FinancingDtlService service;

  @BeforeEach
  void setUp() {
    service = spy(new FinancingDtlService(financingDtlRepository, historyRepository,
      financingHdrRepository, baseRemoteService, objectMapper, restTemplate, eventPublisher, environment));
  }

  @Test
  void createBulkAndDelete() {
    Customer customer = customer();
    Bouwheer bouwheer = bouwheer();
    FinancingHdr hdr = financingHdr();
    InvoiceDto invoiceDto = invoiceDto("INV-1");
    PostedInvoicePayload posted = posted("INV-1");
    when(financingHdrRepository.findById(hdr.getFinancingHdrCode())).thenReturn(Optional.empty());
    when(financingHdrRepository.saveAndFlush(hdr)).thenReturn(hdr);
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(List.of(invoiceDto.getInvoiceCode())))
      .thenReturn(List.of());
    when(financingDtlRepository.saveAll(any())).thenAnswer(call -> call.getArgument(0));

    assertThat(service.createBulk(customer, bouwheer, hdr, List.of(posted), List.of(invoiceDto))).hasSize(1);
    assertThat(service.createBulk(customer, bouwheer, hdr, List.of(), List.of())).isEmpty();

    Invoice existingInvoice = new Invoice();
    existingInvoice.setInvoiceCode(invoiceDto.getInvoiceCode());
    FinancingDtl existing = FinancingDtl.builder().invoice(existingInvoice).build();
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(List.of(invoiceDto.getInvoiceCode())))
      .thenReturn(List.of(existing));
    assertThat(service.createBulk(customer, bouwheer, hdr, List.of(posted), List.of(invoiceDto)))
      .containsExactly(existing);

    service.delete(existing);
    verify(financingDtlRepository).delete(existing);
    doThrow(new RuntimeException("db down")).when(financingDtlRepository).delete(existing);
    assertThatThrownBy(() -> service.delete(existing)).hasMessage("db down");
  }

  @Test
  void updatePaidValidationsAndSuccess() {
    FinancingHdr hdr = financingHdr();
    hdr.setTotalInvoiceSubmittedAmt(1000.0);
    FinancingDtl detail = FinancingDtl.builder()
      .invoice(Invoice.builder().custInvNo("INV-1").invoiceAmt(1000.0).build())
      .build();
    when(financingDtlRepository.findAllByFinancingHdrForUpdate(hdr))
      .thenReturn(List.of(), List.of(detail));
    service.updatePaid(paidRequest("INV-1", "900"), hdr);

    assertThatThrownBy(() -> service.updatePaid(paidRequest("INV-2", "1000"), hdr))
      .isInstanceOf(RuntimeException.class);
    FinancingInvoicePaidRequest emptyRequest = new FinancingInvoicePaidRequest();
    emptyRequest.setFinancingCode("F1");
    emptyRequest.setInvoicePaid(List.of());
    assertThatThrownBy(() -> service.updatePaid(emptyRequest, hdr))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("belum masuk list");
    assertThatThrownBy(() -> service.updatePaid(paidRequest("INV-1", "1100"), hdr))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("Kelebihan Bayar");

    service.updatePaid(paidRequest("INV-1", "1000"), hdr);
    assertThat(detail.getInvoice().getStatus()).isEqualTo("PAID");
    verify(eventPublisher).publishEvent(any(InvoicePaidEvent.class));
  }

  @Test
  void paymentReceiveCreatesAndUpdatesHistory() throws Exception {
    FinancingHdr hdr = financingHdr();
    doReturn(dataAgreement()).when(service).inquiryDataAgreement(any());
    doReturn(newInfo()).when(service).inquiryNewInfoAgreement(any());
    doReturn(outstanding()).when(service).inquiryOutstandingBillDetailt(any());
    doReturn(receiveAmount()).when(service).inquiryReceiveAmountRequest(any());
    doReturn(submitResponse()).when(service).submitPaymentReceiveRequest(any());
    when(historyRepository.findTopByAgreementCode("AGR-1"))
      .thenReturn(Optional.empty(), Optional.of(new PaymentReceiveHistory()));
    when(financingHdrRepository.findByFinancingHdrCode(hdr.getFinancingHdrCode()))
      .thenReturn(Optional.of(hdr));

    service.paymentReceive(paidRequest("INV-1", "1000"), hdr);
    service.paymentReceive(paidRequest("INV-1", "1000"), hdr);
    verify(historyRepository, org.mockito.Mockito.times(4)).save(any());
  }

  @Test
  void paymentReceiveRejectsMissingAgreementAndBadStatus() throws Exception {
    FinancingHdr noAgreement = financingHdr();
    noAgreement.setAgreement(Set.of());
    assertThatThrownBy(() -> service.paymentReceive(paidRequest("INV-1", "1000"), noAgreement))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("persetujuan internal");

    FinancingHdr hdr = financingHdr();
    InquiryDataAgreementDto bad = dataAgreement();
    bad.statusCode = "500";
    doReturn(bad).when(service).inquiryDataAgreement(any());
    assertThatThrownBy(() -> service.paymentReceive(paidRequest("INV-1", "1000"), hdr))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("proses persetujuan");
  }

  @Test
  void remoteWrappersPostRemoteAndGetBillingAmount() throws Exception {
    when(baseRemoteService.adInsKeyHeaders()).thenReturn(new HttpHeaders());
    when(baseRemoteService.Agrmnt_GetAgrmntByAgrmntNo()).thenReturn("http://example.com");
    when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
      .thenReturn(ResponseEntity.ok(""));
    assertThatThrownBy(() -> service.inquiryDataAgreement(null)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("respons yang valid");

    when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
      .thenThrow(new RuntimeException("remote down"));
    assertThatThrownBy(() -> service.inquiryDataAgreement(null)).hasMessage("remote down");

    ArrayList<InquiryOutstandingBillDetailtDto.ListBillingDetail> bills = new ArrayList<>(List.of(
      new InquiryOutstandingBillDetailtDto.ListBillingDetail("LC Installment Amount", 100.0),
      new InquiryOutstandingBillDetailtDto.ListBillingDetail("Retention Amount", 50.0)));
    assertThat(service.getBillingAmount(bills, "LC Installment Amount")).isEqualTo(100.0);
    assertThat(service.getBillingAmount(bills, "Other")).isZero();
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustName("Customer");
    return customer;
  }

  private static Bouwheer bouwheer() {
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerName("Bouwheer");
    return bouwheer;
  }

  private static FinancingHdr financingHdr() {
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setFinancingStatus("INPROCESS");
    hdr.setFinancingStep("SIGNED");
    hdr.setTotalInvoiceAmt(1000.0);
    hdr.setTotalInvoiceSubmittedAmt(1000.0);
    hdr.setCurrencyCode("IDR");
    hdr.setBouwheer(bouwheer());
    hdr.setCustomer(customer());
    hdr.setUsrCrt("maker");
    hdr.setRetention(0.0);
    Agreement agreement = new Agreement();
    agreement.setAgreementCode("AGR-1");
    hdr.setAgreement(Set.of(agreement));
    return hdr;
  }

  private static InvoiceDto invoiceDto(String no) {
    InvoiceDto dto = new InvoiceDto();
    dto.setInvoiceCode(UUID.randomUUID());
    dto.setBouwheerInvNo(no);
    dto.setCustInvNo(no);
    return dto;
  }

  private static PostedInvoicePayload posted(String no) {
    PostedInvoicePayload posted = new PostedInvoicePayload();
    posted.setBouwheerInvoiceNo(no);
    posted.setCustomerInvoiceNo(no);
    posted.setInvoiceDate(new Date());
    posted.setInvoiceDueDate(new Date());
    return posted;
  }

  private static FinancingInvoicePaidRequest paidRequest(String invoiceNo, String amount) {
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    request.setFinancingCode("F1");
    FinancingInvoicePaidRequest.InvoicePaid paid = new FinancingInvoicePaidRequest.InvoicePaid();
    paid.setInvoiceNo(invoiceNo);
    paid.setInvoiceAmount(new BigDecimal(amount));
    paid.setClearingDate(new Date());
    request.setInvoicePaid(List.of(paid));
    return request;
  }

  private static InquiryDataAgreementDto dataAgreement() {
    InquiryDataAgreementDtoAgrmntObj obj = InquiryDataAgreementDtoAgrmntObj.builder()
      .currCode("IDR").goLiveDt(LocalDateTime.of(2026, 11, 1, 0, 0))
      .effectiveDt(LocalDateTime.of(2026, 11, 1, 0, 0))
      .nextInstDueDt(LocalDateTime.of(2026, 12, 1, 0, 0))
      .ntfAmt(1000).lcInstRatePrml(1).build();
    return InquiryDataAgreementDto.builder().statusCode("200").agrmntObj(obj).build();
  }

  private static InquiryNewInfoAgreementDto newInfo() {
    InquiryNewInfoAgreementDto dto = new InquiryNewInfoAgreementDto();
    dto.interestAmt = 10.0;
    return dto;
  }

  private static InquiryOutstandingBillDetailtDto outstanding() {
    InquiryOutstandingBillDetailtDto dto = new InquiryOutstandingBillDetailtDto();
    dto.listBillingDetail = new ArrayList<>(List.of(
      new InquiryOutstandingBillDetailtDto.ListBillingDetail("LC Installment Amount", 10),
      new InquiryOutstandingBillDetailtDto.ListBillingDetail("Retention Amount", 20),
      new InquiryOutstandingBillDetailtDto.ListBillingDetail("Amount To Be Paid", 1000)));
    return dto;
  }

  private static InquiryReceiveAmountRequestDto receiveAmount() {
    InquiryReceiveAmountRequestDto.AllocList alloc = new InquiryReceiveAmountRequestDto.AllocList();
    alloc.paymentAllocCode = "ALLOC";
    alloc.rcvAmt = 1000;
    InquiryReceiveAmountRequestDto.AllocMapList map = new InquiryReceiveAmountRequestDto.AllocMapList();
    map.allocList = new ArrayList<>(List.of(alloc));
    InquiryReceiveAmountRequestDto dto = new InquiryReceiveAmountRequestDto();
    dto.allocMapList = new ArrayList<>(List.of(map));
    return dto;
  }

  private static SubmitPaymentReceiveRequestDto submitResponse() {
    SubmitPaymentReceiveRequestDto dto = new SubmitPaymentReceiveRequestDto();
    dto.setStatusCode("200");
    dto.setPayRcvNo("PR-1");
    return dto;
  }
}
