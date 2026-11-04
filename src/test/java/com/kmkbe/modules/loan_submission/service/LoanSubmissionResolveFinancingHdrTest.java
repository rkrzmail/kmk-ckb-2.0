package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.model.SimulationDisburseResult;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regresi test bug simulasi/create: financing_hdr TIDAK boleh selalu dibuat baru
 * ketika invoice & financing detail-nya sudah ada sebelumnya.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanSubmissionResolveFinancingHdrTest {

  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private FinancingDtlRepository financingDtlRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private FinancingHdrService financingHdrService;

  private LoanSubmissionService service;
  private Customer customer;
  private Bouwheer bouwheer;

  @BeforeEach
  void setUp() {
    service = new LoanSubmissionService(
      null, null, null, null, null, null,
      invoiceRepository,
      null, null, null, null,
      null,
      financingHdrService,
      null, null, null, null, null,
      financingHdrRepository,
      null, null,
      financingDtlRepository,
      null, null
    );
    customer = Customer.builder().custName("PT Debitur").build();
    bouwheer = Bouwheer.builder().bouwheerName("PT Bouwheer").build();
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

  private CreateSimulationRequest request(PostedInvoicePayload... invoices) {
    CreateSimulationRequest request = new CreateSimulationRequest();
    request.setInvoices(List.of(invoices));
    return request;
  }

  private SimulationDisburseResult result() {
    return SimulationDisburseResult.builder()
      .financingAmount(new BigDecimal("900000"))
      .estimatedDisburseAmount(new BigDecimal("850000"))
      .totalInvoiceAmount(new BigDecimal("1000000"))
      .interestFeeAmount(new BigDecimal("10000"))
      .provisionFeeAmount(new BigDecimal("5000"))
      .adminFeeAmount(new BigDecimal("1000"))
      .othersFeeAmount(new BigDecimal("0"))
      .legalFeeAmount(new BigDecimal("0"))
      .surveyFeeAmount(new BigDecimal("0"))
      .provisionRate(1.0)
      .effectiveRate(12.0)
      .adminRate(0.5)
      .tenor(3L)
      .top(30L)
      .retention(10.0)
      .maxInvoiceDate(new Date())
      .build();
  }

  @Test
  @DisplayName("Invoice & detail sudah ada -> header LAMA dipakai & di-update (tidak buat baru)")
  void existingDetailReusesExistingHeader() {
    UUID invoiceCode = UUID.randomUUID();
    UUID existingHeaderCode = UUID.randomUUID();

    FinancingHdr existingHeader = new FinancingHdr();
    existingHeader.setFinancingHdrCode(existingHeaderCode);

    FinancingDtl existingDetail = new FinancingDtl();
    existingDetail.setFinancingHdr(existingHeader);
    existingDetail.setInvoice(Invoice.builder().invoiceCode(invoiceCode).build());

    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-1"))
      .thenReturn(Optional.of(Invoice.builder().invoiceCode(invoiceCode).build()));
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of(existingDetail));
    when(financingHdrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, bouwheer, request(posted("INV-B-1", "INV-C-1")), result(), 1_000_000d);

    assertThat(resolved.getFinancingHdrCode()).isEqualTo(existingHeaderCode);
    assertThat(resolved.getUsrUpd()).isEqualTo("PT Debitur");
    assertThat(resolved.getTenor()).isEqualTo(3L);
    // header baru TIDAK dibuat
    verify(financingHdrService, never()).create(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Invoice sudah ada tapi detail belum ada -> header baru dibuat")
  void existingInvoiceWithoutDetailCreatesNewHeader() {
    UUID invoiceCode = UUID.randomUUID();
    FinancingHdr newHeader = new FinancingHdr();
    newHeader.setFinancingHdrCode(UUID.randomUUID());

    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-1"))
      .thenReturn(Optional.of(Invoice.builder().invoiceCode(invoiceCode).build()));
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of());
    when(financingHdrService.create(any(), any(), any(), any())).thenReturn(newHeader);

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, bouwheer, request(posted("INV-B-1", "INV-C-1")), result(), 1_000_000d);

    assertThat(resolved).isSameAs(newHeader);
    verify(financingHdrRepository, never()).save(any());
  }

  @Test
  @DisplayName("Invoice belum pernah ada -> header baru dibuat")
  void brandNewInvoiceCreatesNewHeader() {
    FinancingHdr newHeader = new FinancingHdr();
    newHeader.setFinancingHdrCode(UUID.randomUUID());

    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-NEW", "INV-C-NEW"))
      .thenReturn(Optional.empty());
    when(financingHdrService.create(any(), any(), any(), any())).thenReturn(newHeader);

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, bouwheer, request(posted("INV-B-NEW", "INV-C-NEW")), result(), 1_000_000d);

    assertThat(resolved).isSameAs(newHeader);
    verify(financingDtlRepository, never()).findByInvoice_InvoiceCodeIn(anyList());
  }

  @Test
  @DisplayName("Sebagian invoice existing -> header dari detail existing dipakai")
  void partiallyExistingInvoicesReuseHeader() {
    UUID existingInvoiceCode = UUID.randomUUID();
    UUID existingHeaderCode = UUID.randomUUID();

    FinancingHdr existingHeader = new FinancingHdr();
    existingHeader.setFinancingHdrCode(existingHeaderCode);

    FinancingDtl detail = new FinancingDtl();
    detail.setFinancingHdr(existingHeader);
    detail.setInvoice(Invoice.builder().invoiceCode(existingInvoiceCode).build());

    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-1", "INV-C-1"))
      .thenReturn(Optional.of(Invoice.builder().invoiceCode(existingInvoiceCode).build()));
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(customer, bouwheer, "INV-B-2", "INV-C-2"))
      .thenReturn(Optional.empty());
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList())).thenReturn(List.of(detail));
    when(financingHdrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, bouwheer,
      request(posted("INV-B-1", "INV-C-1"), posted("INV-B-2", "INV-C-2")),
      result(), 2_000_000d);

    assertThat(resolved.getFinancingHdrCode()).isEqualTo(existingHeaderCode);
    assertThat(resolved.getInvoiceQty()).isEqualTo(2L);
    verify(financingHdrService, never()).create(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Invoice tanpa bouwheerInvoiceNo -> tidak query invoice, header baru")
  void invoiceWithoutNumberCreatesNewHeader() {
    FinancingHdr newHeader = new FinancingHdr();
    when(financingHdrService.create(any(), any(), any(), any())).thenReturn(newHeader);

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, bouwheer, request(posted(null, "INV-C-1")), result(), 1_000_000d);

    assertThat(resolved).isSameAs(newHeader);
    verify(invoiceRepository, never())
      .findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(any(), any(), any(), any());
  }

  // ===========================================================================
  // Validasi status financing: invoice yang sedang diproses tidak boleh
  // diajukan ulang -> error "No invoice ####### sedang di proses"
  // ===========================================================================

  private FinancingDtl detailWithStatus(String invoiceNo, String financingStatus) {
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setFinancingStatus(financingStatus);

    FinancingDtl dtl = new FinancingDtl();
    dtl.setFinancingHdr(hdr);
    dtl.setBouwheerInvNo(invoiceNo);
    dtl.setInvoice(Invoice.builder().invoiceCode(UUID.randomUUID()).build());
    return dtl;
  }

  private void stubExistingDetail(FinancingDtl detail) {
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
        any(), any(), any(), any()))
      .thenReturn(Optional.of(detail.getInvoice()));
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList()))
      .thenReturn(List.of(detail));
  }

  @Test
  @DisplayName("Status INPROCESS -> error 'No invoice X sedang di proses'")
  void inProcessStatusThrowsError() {
    stubExistingDetail(detailWithStatus("INV-B-1", "INPROCESS"));

    assertThatThrownBy(() -> service.resolveFinancingHdr(
      customer, bouwheer, request(posted("INV-B-1", "INV-C-1")), result(), 1_000_000d))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("No invoice INV-B-1 sedang di proses");

    verify(financingHdrRepository, never()).save(any());
    verify(financingHdrService, never()).create(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Status ASSIGNMENT / SIGNING / LIVE / PAID -> error")
  void variousInProcessStatusesThrowError() {
    for (String status : List.of("ASSIGNMENT", "SIGNING", "SIGNED", "LIVE", "GOLIVE", "PAID", "NEW", "UNPAID")) {
      stubExistingDetail(detailWithStatus("INV-" + status, status));

      assertThatThrownBy(() -> service.resolveFinancingHdr(
        customer, bouwheer, request(posted("INV-" + status, "CINV")), result(), 1_000_000d))
        .as("status %s harus ditolak", status)
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("sedang di proses");
    }
  }

  @Test
  @DisplayName("Status kosong string \"\" -> boleh lanjut (update header)")
  void emptyStatusAllowsUpdate() {
    FinancingDtl detail = detailWithStatus("INV-B-1", "");
    stubExistingDetail(detail);
    when(financingHdrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, bouwheer, request(posted("INV-B-1", "INV-C-1")), result(), 1_000_000d);

    assertThat(resolved.getFinancingHdrCode()).isEqualTo(detail.getFinancingHdr().getFinancingHdrCode());
  }

  @Test
  @DisplayName("Status null -> boleh lanjut (update header)")
  void nullStatusAllowsUpdate() {
    FinancingDtl detail = detailWithStatus("INV-B-1", null);
    stubExistingDetail(detail);
    when(financingHdrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, bouwheer, request(posted("INV-B-1", "INV-C-1")), result(), 1_000_000d);

    assertThat(resolved.getFinancingHdrCode()).isEqualTo(detail.getFinancingHdr().getFinancingHdrCode());
  }

  @Test
  @DisplayName("Status whitespace '   ' -> dianggap kosong, boleh lanjut")
  void blankStatusAllowsUpdate() {
    FinancingDtl detail = detailWithStatus("INV-B-1", "   ");
    stubExistingDetail(detail);
    when(financingHdrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, bouwheer, request(posted("INV-B-1", "INV-C-1")), result(), 1_000_000d);

    assertThat(resolved.getFinancingHdrCode()).isEqualTo(detail.getFinancingHdr().getFinancingHdrCode());
  }

  @Test
  @DisplayName("Beberapa invoice sedang diproses -> semua nomor muncul di pesan error")
  void multipleInProcessInvoicesAreAllListed() {
    FinancingDtl d1 = detailWithStatus("INV-B-1", "INPROCESS");
    FinancingDtl d2 = detailWithStatus("INV-B-2", "LIVE");

    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(any(), any(), any(), any()))
      .thenReturn(Optional.of(Invoice.builder().invoiceCode(UUID.randomUUID()).build()));
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList()))
      .thenReturn(List.of(d1, d2));

    assertThatThrownBy(() -> service.resolveFinancingHdr(
      customer, bouwheer,
      request(posted("INV-B-1", "INV-C-1"), posted("INV-B-2", "INV-C-2")),
      result(), 2_000_000d))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("INV-B-1")
      .hasMessageContaining("INV-B-2")
      .hasMessageContaining("sedang di proses");
  }

  @Test
  @DisplayName("Satu invoice in-process + satu masih draft -> tetap error (cegah partial submit)")
  void oneInProcessAmongDraftsStillThrows() {
    FinancingDtl inProcess = detailWithStatus("INV-B-1", "INPROCESS");
    FinancingDtl draft = detailWithStatus("INV-B-2", "");

    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(any(), any(), any(), any()))
      .thenReturn(Optional.of(Invoice.builder().invoiceCode(UUID.randomUUID()).build()));
    when(financingDtlRepository.findByInvoice_InvoiceCodeIn(anyList()))
      .thenReturn(List.of(inProcess, draft));

    assertThatThrownBy(() -> service.resolveFinancingHdr(
      customer, bouwheer,
      request(posted("INV-B-1", "INV-C-1"), posted("INV-B-2", "INV-C-2")),
      result(), 2_000_000d))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("INV-B-1 sedang di proses");
  }

  @Test
  @DisplayName("Lookup invoice memakai 4 kunci: customer + bouwheer + bouwheerInvNo + custInvNo")
  void invoiceLookupUsesBouwheerAndBothInvoiceNumbers() {
    FinancingHdr newHeader = new FinancingHdr();
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
      customer, bouwheer, "INV-B-1", "INV-C-1")).thenReturn(Optional.empty());
    when(financingHdrService.create(any(), any(), any(), any())).thenReturn(newHeader);

    service.resolveFinancingHdr(
      customer, bouwheer, request(posted("INV-B-1", "INV-C-1")), result(), 1_000_000d);

    verify(invoiceRepository).findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
      customer, bouwheer, "INV-B-1", "INV-C-1");
    verify(invoiceRepository, never())
      .findFirstByCustomerAndBouwheerInvNoAndCustInvNo(any(), any(), any());
  }

  @Test
  @DisplayName("Invoice milik bouwheer lain tidak dianggap existing -> tidak memicu error in-process")
  void invoiceFromOtherBouwheerIsNotTreatedAsExisting() {
    Bouwheer otherBouwheer = Bouwheer.builder().bouwheerName("PT Bouwheer Lain").build();
    FinancingHdr newHeader = new FinancingHdr();
    newHeader.setFinancingHdrCode(UUID.randomUUID());

    // lookup dengan bouwheer lain -> kosong (invoice bukan milik bouwheer ini)
    when(invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
      customer, otherBouwheer, "INV-B-1", "INV-C-1")).thenReturn(Optional.empty());
    when(financingHdrService.create(any(), any(), any(), any())).thenReturn(newHeader);

    FinancingHdr resolved = service.resolveFinancingHdr(
      customer, otherBouwheer, request(posted("INV-B-1", "INV-C-1")), result(), 1_000_000d);

    assertThat(resolved).isSameAs(newHeader);
    verify(financingDtlRepository, never()).findByInvoice_InvoiceCodeIn(anyList());
  }
}
