package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.request.CreateSubmissionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regresi test bug simulasi/create: 1 invoice = 1 record, unik berdasarkan
 * (customer, bouwheerInvNo, custInvNo). Jika sudah ada -> UPDATE, jika belum -> INSERT.
 */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceCreateBulkTest {

  @Mock InvoiceRepository invoiceRepository;
  @Mock FinancingDtlRepository financingDtlRepository;
  @InjectMocks InvoiceService invoiceService;

  private Customer customer;
  private Bouwheer bouwheer;

  @BeforeEach
  void setUp() {
    customer = Customer.builder().custName("PT Debitur").build();
    bouwheer = Bouwheer.builder().bouwheerName("PT Bouwheer").build();
  }

  private PostedInvoicePayload payload(String bouwheerInvNo, String custInvNo, String amount) {
    PostedInvoicePayload posted = new PostedInvoicePayload();
    posted.setBouwheerInvoiceNo(bouwheerInvNo);
    posted.setCustomerInvoiceNo(custInvNo);
    posted.setInvoiceAmount(amount == null ? null : new BigDecimal(amount));
    posted.setInvoiceDate(new Date());
    posted.setInvoiceDueDate(new Date());
    posted.setPoNumber("PO-1");
    return posted;
  }

  private CreateSubmissionRequest request(List<PostedInvoicePayload> invoices) {
    return CreateSubmissionRequest.builder().invoices(invoices).build();
  }

  @Test
  @DisplayName("Invoice baru (belum ada di DB) -> INSERT 1 record")
  void newInvoiceIsCreated() {
    PostedInvoicePayload posted = payload("INV-B-1", "INV-C-1", "1000000");
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-1"))
      .thenReturn(Optional.empty());
    when(invoiceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    List<InvoiceDto> result = invoiceService.createBulk(customer, bouwheer, request(List.of(posted)));

    assertThat(result).hasSize(1);
    ArgumentCaptor<List<Invoice>> captor = ArgumentCaptor.forClass(List.class);
    verify(invoiceRepository).saveAll(captor.capture());
    Invoice saved = captor.getValue().getFirst();
    assertThat(saved.getBouwheerInvNo()).isEqualTo("INV-B-1");
    assertThat(saved.getCustInvNo()).isEqualTo("INV-C-1");
    assertThat(saved.getInvoiceAmt()).isEqualTo(1_000_000d);
    assertThat(saved.getUsrCrt()).isEqualTo("PT Bouwheer");
  }

  @Test
  @DisplayName("Invoice sudah ada (customer+bouwheerInvNo+custInvNo sama) -> UPDATE, tidak INSERT baru")
  void existingInvoiceIsUpdatedNotDuplicated() {
    PostedInvoicePayload posted = payload("INV-B-1", "INV-C-1", "2000000");
    Invoice existing = Invoice.builder()
      .invoiceCode(UUID.randomUUID())
      .customer(customer)
      .bouwheer(bouwheer)
      .bouwheerInvNo("INV-B-1")
      .custInvNo("INV-C-1")
      .invoiceAmt(1_000_000d)
      .build();

    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-1"))
      .thenReturn(Optional.of(existing));
    when(invoiceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    List<InvoiceDto> result = invoiceService.createBulk(customer, bouwheer, request(List.of(posted)));

    assertThat(result).hasSize(1);
    ArgumentCaptor<List<Invoice>> captor = ArgumentCaptor.forClass(List.class);
    verify(invoiceRepository).saveAll(captor.capture());
    List<Invoice> saved = captor.getValue();
    assertThat(saved).hasSize(1);
    assertThat(saved.getFirst().getInvoiceCode()).isEqualTo(existing.getInvoiceCode());
    assertThat(saved.getFirst().getInvoiceAmt()).isEqualTo(2_000_000d);
    assertThat(saved.getFirst().getUsrUpd()).isEqualTo("PT Debitur");
    assertThat(saved.getFirst().getDtmUpd()).isNotNull();
  }

  @Test
  @DisplayName("Invoice duplikat dalam 1 request -> hanya diproses sekali")
  void duplicateInSameRequestIsDeduplicated() {
    PostedInvoicePayload posted = payload("INV-B-1", "INV-C-1", "1000000");
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-1"))
      .thenReturn(Optional.empty());
    when(invoiceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    List<InvoiceDto> result = invoiceService.createBulk(
      customer, bouwheer, request(List.of(posted, payload("INV-B-1", "INV-C-1", "1000000"))));

    assertThat(result).hasSize(1);
    verify(invoiceRepository, times(1))
      .findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-1");
  }

  @Test
  @DisplayName("bouwheerInvNo sama tapi custInvNo beda -> 2 invoice terpisah")
  void sameBouwheerInvNoDifferentCustInvNoAreSeparate() {
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-1"))
      .thenReturn(Optional.empty());
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-2"))
      .thenReturn(Optional.empty());
    when(invoiceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    List<InvoiceDto> result = invoiceService.createBulk(customer, bouwheer,
      request(List.of(payload("INV-B-1", "INV-C-1", "100"), payload("INV-B-1", "INV-C-2", "200"))));

    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("Daftar invoice kosong -> tidak menyentuh repository")
  void emptyInvoicesReturnsEmpty() {
    assertThat(invoiceService.createBulk(customer, bouwheer, request(List.of()))).isEmpty();
    verify(invoiceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("invoiceAmount null -> tidak error (tidak NPE)")
  void nullAmountDoesNotThrow() {
    PostedInvoicePayload posted = payload("INV-B-9", "INV-C-9", null);
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-9", "INV-C-9"))
      .thenReturn(Optional.empty());
    when(invoiceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    List<InvoiceDto> result = invoiceService.createBulk(customer, bouwheer, request(List.of(posted)));

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Deskripsi kosong -> default 'Invoice By <bouwheer>'")
  void blankDescriptionUsesDefault() {
    PostedInvoicePayload posted = payload("INV-B-2", "INV-C-2", "100");
    posted.setInvoiceDescription("");
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-2", "INV-C-2"))
      .thenReturn(Optional.empty());
    when(invoiceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    invoiceService.createBulk(customer, bouwheer, request(List.of(posted)));

    ArgumentCaptor<List<Invoice>> captor = ArgumentCaptor.forClass(List.class);
    verify(invoiceRepository).saveAll(captor.capture());
    assertThat(captor.getValue().getFirst().getInvoiceDescription()).isEqualTo("Invoice By PT Bouwheer");
  }

  @Test
  @DisplayName("Invoice sama tapi BOUWHEER berbeda -> dianggap invoice baru (lookup pakai 4 kunci)")
  void sameInvoiceDifferentBouwheerIsNewInvoice() {
    PostedInvoicePayload posted = payload("INV-B-1", "INV-C-1", "1000000");
    Bouwheer otherBouwheer = Bouwheer.builder().bouwheerName("PT Bouwheer Lain").build();

    // lookup dengan bouwheer lain -> tidak ditemukan
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
      customer, otherBouwheer, "INV-B-1", "INV-C-1")).thenReturn(Optional.empty());
    when(invoiceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    List<InvoiceDto> result = invoiceService.createBulk(customer, otherBouwheer, request(List.of(posted)));

    assertThat(result).hasSize(1);
    ArgumentCaptor<List<Invoice>> captor = ArgumentCaptor.forClass(List.class);
    verify(invoiceRepository).saveAll(captor.capture());
    assertThat(captor.getValue().getFirst().getBouwheer()).isEqualTo(otherBouwheer);
    assertThat(captor.getValue().getFirst().getUsrCrt()).isEqualTo("PT Bouwheer Lain");
  }

  @Test
  @DisplayName("Lookup selalu memakai customer + bouwheer + bouwheerInvNo + custInvNo")
  void lookupUsesAllFourKeys() {
    PostedInvoicePayload posted = payload("INV-B-1", "INV-C-1", "1000000");
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
      customer, bouwheer, "INV-B-1", "INV-C-1")).thenReturn(Optional.empty());
    when(invoiceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    invoiceService.createBulk(customer, bouwheer, request(List.of(posted)));

    verify(invoiceRepository).findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
      customer, bouwheer, "INV-B-1", "INV-C-1");
    // signature lama (tanpa bouwheer) tidak boleh dipakai lagi
    verify(invoiceRepository, never())
      .findFirstByCustomerAndBouwheerInvNoAndCustInvNo(any(), any(), any());
  }
}
