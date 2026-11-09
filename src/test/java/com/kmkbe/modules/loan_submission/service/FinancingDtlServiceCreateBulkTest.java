package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.PaymentReceiveHistoryRepository;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regresi test bug simulasi/create: 1 invoice harus memiliki tepat 1
 * financing detail (relasi Invoice -> FinancingDtl adalah @OneToOne).
 * Lookup existing harus GLOBAL by invoice, bukan per financing header.
 */
@ExtendWith(MockitoExtension.class)
class FinancingDtlServiceCreateBulkTest {

  @Mock FinancingDtlRepository financingDtlRepository;
  @Mock PaymentReceiveHistoryRepository paymentReceiveHistoryRepository;
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock BaseRemoteService baseRemoteService;
  @Mock RestTemplate restTemplate;
  @Mock ApplicationEventPublisher eventPublisher;

  private FinancingDtlService service;
  private Customer customer;
  private Bouwheer bouwheer;
  private FinancingHdr financingHdr;

  @BeforeEach
  void setUp() {
    service = new FinancingDtlService(
      financingDtlRepository,
      paymentReceiveHistoryRepository,
      financingHdrRepository,
      baseRemoteService,
      new ObjectMapper(),
      restTemplate,
      eventPublisher
    );
    customer = Customer.builder().custName("PT Debitur").build();
    bouwheer = Bouwheer.builder().bouwheerName("PT Bouwheer").build();
    financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(UUID.randomUUID());

    lenient().when(financingHdrRepository.findById(any())).thenReturn(Optional.of(financingHdr));
    lenient().when(financingDtlRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
  }

  private PostedInvoicePayload posted(String bouwheerInvNo, String custInvNo) {
    PostedInvoicePayload payload = new PostedInvoicePayload();
    payload.setBouwheerInvoiceNo(bouwheerInvNo);
    payload.setCustomerInvoiceNo(custInvNo);
    payload.setInvoiceAmount(new BigDecimal("1000000"));
    payload.setInvoiceDate(new Date());
    payload.setInvoiceDueDate(new Date());
    return payload;
  }

  private InvoiceDto invoiceDto(UUID code, String bouwheerInvNo, String custInvNo) {
    InvoiceDto dto = new InvoiceDto();
    dto.setInvoiceCode(code);
    dto.setBouwheerInvNo(bouwheerInvNo);
    dto.setCustInvNo(custInvNo);
    dto.setInvoiceAmt(new BigDecimal("1000000"));
    return dto;
  }

  @Test
  @DisplayName("Invoice baru -> INSERT tepat 1 financing detail")
  void newInvoiceCreatesSingleDetail() {
    UUID invoiceCode = UUID.randomUUID();
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of());

    List<FinancingDtl> result = service.createBulk(
      customer, bouwheer, financingHdr,
      List.of(posted("INV-B-1", "INV-C-1")),
      List.of(invoiceDto(invoiceCode, "INV-B-1", "INV-C-1"))
    );

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getFinancingDtlCode()).isNotNull();
    assertThat(result.getFirst().getFinancingHdr()).isEqualTo(financingHdr);
    assertThat(result.getFirst().getBouwheerInvNo()).isEqualTo("INV-B-1");
  }

  @Test
  @DisplayName("Invoice sudah punya financing detail (header BERBEDA) -> UPDATE, tidak INSERT duplikat")
  void existingDetailOnDifferentHeaderIsUpdatedNotDuplicated() {
    UUID invoiceCode = UUID.randomUUID();
    FinancingHdr otherHdr = new FinancingHdr();
    otherHdr.setFinancingHdrCode(UUID.randomUUID());

    FinancingDtl existingDetail = new FinancingDtl();
    existingDetail.setFinancingDtlCode(UUID.randomUUID());
    existingDetail.setFinancingHdr(otherHdr);
    existingDetail.setInvoice(Invoice.builder().invoiceCode(invoiceCode).build());
    existingDetail.setBouwheerInvNo("INV-B-1");

    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of(existingDetail));

    List<FinancingDtl> result = service.createBulk(
      customer, bouwheer, financingHdr,
      List.of(posted("INV-B-1", "INV-C-1")),
      List.of(invoiceDto(invoiceCode, "INV-B-1", "INV-C-1"))
    );

    assertThat(result).hasSize(1);
    // detail yang sama (bukan record baru) di-rebind ke header aktif
    assertThat(result.getFirst().getFinancingDtlCode()).isEqualTo(existingDetail.getFinancingDtlCode());
    assertThat(result.getFirst().getFinancingHdr()).isEqualTo(financingHdr);
    assertThat(result.getFirst().getUsrUpd()).isEqualTo("PT Debitur");
  }

  @Test
  @DisplayName("Invoice duplikat dalam 1 request -> tetap 1 financing detail")
  void duplicateInvoiceInRequestYieldsSingleDetail() {
    UUID invoiceCode = UUID.randomUUID();
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of());

    List<FinancingDtl> result = service.createBulk(
      customer, bouwheer, financingHdr,
      List.of(posted("INV-B-1", "INV-C-1"), posted("INV-B-1", "INV-C-1")),
      List.of(invoiceDto(invoiceCode, "INV-B-1", "INV-C-1"))
    );

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Dua invoice berbeda -> 2 financing detail")
  void twoDistinctInvoicesCreateTwoDetails() {
    UUID code1 = UUID.randomUUID();
    UUID code2 = UUID.randomUUID();
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of());

    List<FinancingDtl> result = service.createBulk(
      customer, bouwheer, financingHdr,
      List.of(posted("INV-B-1", "INV-C-1"), posted("INV-B-2", "INV-C-2")),
      List.of(invoiceDto(code1, "INV-B-1", "INV-C-1"), invoiceDto(code2, "INV-B-2", "INV-C-2"))
    );

    assertThat(result).hasSize(2);
    assertThat(result).extracting(FinancingDtl::getBouwheerInvNo).containsExactly("INV-B-1", "INV-B-2");
  }

  @Test
  @DisplayName("bouwheerInvNo sama tapi custInvNo beda -> 2 invoice & 2 detail (tidak tertukar)")
  void sameBouwheerInvNoDifferentCustInvNoAreSeparate() {
    UUID code1 = UUID.randomUUID();
    UUID code2 = UUID.randomUUID();
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of());

    List<FinancingDtl> result = service.createBulk(
      customer, bouwheer, financingHdr,
      List.of(posted("INV-B-1", "INV-C-1"), posted("INV-B-1", "INV-C-2")),
      List.of(invoiceDto(code1, "INV-B-1", "INV-C-1"), invoiceDto(code2, "INV-B-1", "INV-C-2"))
    );

    assertThat(result).hasSize(2);
    assertThat(result).extracting(dtl -> dtl.getInvoice().getInvoiceCode())
      .containsExactly(code1, code2);
  }

  @Test
  @DisplayName("postedInvoices kosong -> empty list, repository save tidak dipanggil")
  void emptyPostedInvoicesReturnsEmpty() {
    assertThat(service.createBulk(customer, bouwheer, financingHdr, List.of(), List.of())).isEmpty();
    verify(financingDtlRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("Invoice di payload tidak punya data invoice -> IllegalStateException")
  void missingInvoiceDataThrows() {
    assertThatThrownBy(() -> service.createBulk(
      customer, bouwheer, financingHdr,
      List.of(posted("INV-B-X", "INV-C-X")),
      List.of()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Invoice data missing");
  }

  @Test
  @DisplayName("postedInvoices tanpa bouwheerInvoiceNo -> dilewati (tidak error)")
  void postedInvoiceWithoutNumberIsSkipped() {
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of());

    List<FinancingDtl> result = service.createBulk(
      customer, bouwheer, financingHdr,
      List.of(posted(null, "INV-C-1")),
      List.of(invoiceDto(UUID.randomUUID(), null, "INV-C-1"))
    );

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Batch fetch existing memakai invoiceCode (global), bukan filter financing header")
  void existingLookupUsesInvoiceCodesGlobally() {
    UUID invoiceCode = UUID.randomUUID();
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of());

    service.createBulk(
      customer, bouwheer, financingHdr,
      List.of(posted("INV-B-1", "INV-C-1")),
      List.of(invoiceDto(invoiceCode, "INV-B-1", "INV-C-1"))
    );

    ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
    verify(financingDtlRepository).findByInvoice_InvoiceCodeIn(captor.capture());
    assertThat(captor.getValue()).containsExactly(invoiceCode);
    // lookup lama per-header tidak boleh dipakai lagi
    verify(financingDtlRepository, never()).findByfinancingHdrAndBouwheerInvNoIn(any(), anyList());
  }
}
