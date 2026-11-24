package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.spec.FinancingDtlSpec;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceCoverageTest {
  @Mock FinancingDtlRepository financingDtlRepository;
  @Mock InvoiceRepository invoiceRepository;
  InvoiceService service;

  @BeforeEach
  void setUp() {
    service = new InvoiceService(financingDtlRepository, invoiceRepository);
  }

  @Test
  void createDeleteAndErrorPaths() throws Exception {
    Invoice invoice = invoice("INV-1");
    service.create(invoice);
    verify(invoiceRepository).save(invoice);
    when(invoiceRepository.save(any())).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.create(invoice)).hasMessage("db down");

    service.delete(invoice);
    verify(invoiceRepository).delete(invoice);
    org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(invoiceRepository).delete(invoice);
    assertThatThrownBy(() -> service.delete(invoice)).hasMessage("db down");
  }

  @Test
  void invoiceSubmissionByFinancingHdrBranches() {
    FinancingHdr hdr = financingHdr();
    FinancingDtl detail = detail(hdr, "INV-1");
    when(financingDtlRepository.findByFinancingHdr(eq(hdr), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(detail)));

    PaginationRequest request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    assertThat(service.invoiceSubmissionByFinancingHdr(hdr, request).getList()).hasSize(1);

    request.setSearchBy("custInvoiceNo");
    request.setSearchValue("INV");
    when(financingDtlRepository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(detail)));
    assertThat(service.invoiceSubmissionByFinancingHdr(hdr, request).getList()).hasSize(1);

    when(financingDtlRepository.findByFinancingHdr(eq(hdr), any(Pageable.class)))
      .thenThrow(new RuntimeException("db down"));
    request.setSearchBy(null);
    assertThatThrownBy(() -> service.invoiceSubmissionByFinancingHdr(hdr, request)).hasMessage("db down");
  }

  @Test
  void dashboardInvoicesAndActiveInvoices() throws Exception {
    Customer customer = new Customer();
    customer.setCustName("Customer");
    Authentication auth = new UsernamePasswordAuthenticationToken(customer, null, List.of());
    FinancingHdr hdr = financingHdr();
    FinancingDtl detail = detail(hdr, "INV-1");
    when(financingDtlRepository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(detail)));

    PaginationRequest request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    assertThat(service.customerCreditFacilities(auth, request).getList()).hasSize(1);
    assertThat(service.customerDueDateInvoices(auth, request).getList()).hasSize(1);

    when(financingDtlRepository.findAll(any(Specification.class), any(Pageable.class)))
      .thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.customerCreditFacilities(auth, request)).hasMessage("db down");
    assertThatThrownBy(() -> service.customerDueDateInvoices(auth, request)).hasMessage("db down");

    PaginationResult<?> active = service.customerActiveInvoices(request);
    assertThat(active.getCurrentPage()).isEqualTo(2);
    assertThat(active.getList()).isEmpty();
  }

  @Test
  void paginateInvoiceFiltersNullAndUsesDefaults() {
    FinancingHdr hdr = financingHdr();
    FinancingDtl valid = detail(hdr, "INV-1");
    valid.getInvoice().setInvoiceDescription("");
    FinancingDtl missingInvoice = FinancingDtl.builder().financingHdr(hdr).build();
    FinancingDtl missingHdr = FinancingDtl.builder().invoice(invoice("INV-2")).build();
    var page = new PageImpl<>(List.of(valid, missingInvoice, missingHdr));

    assertThat(service.paginateInvoice(page, 2).getList()).hasSize(1);
    assertThat(service.paginateInvoice(page, 0).getList().getFirst().getInvoiceDescription())
      .isEqualTo("Invoice By Bouwheer");
  }

  @Test
  void paginateCustDashboardAndPageNikita() {
    FinancingHdr hdr = financingHdr();
    Agreement agreement = new Agreement();
    agreement.setAgreementCode("AGR-1");
    hdr.setAgreement(Set.of(agreement));
    FinancingDtl detail = detail(hdr, "INV-1");
    detail.getInvoice().setInvoiceDescription("");
    detail.setBouwheerPaidDate(LocalDateTime.of(2026, 11, 20, 0, 0));
    var page = new PageImpl<>(List.of(detail));

    assertThat(service.paginateCustDashboard(page, 2, 5).getList()).hasSize(1);
    assertThat(service.pageNikitaDebitur(page, 2, 5).getList().getFirst().getNo()).isEqualTo(6);

    FinancingHdr noAgreement = financingHdr();
    FinancingDtl noAgreementDetail = detail(noAgreement, "INV-2");
    var noAgreementPage = new PageImpl<>(List.of(noAgreementDetail));
    assertThat(service.paginateCustDashboard(noAgreementPage, 0, 5).getList()).hasSize(1);
    assertThat(service.pageNikitaDebitur(noAgreementPage, 0, 5).getList()).hasSize(1);
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustName("Customer");
    customer.setCustCode(UUID.randomUUID());
    return customer;
  }

  private static Bouwheer bouwheer() {
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerCode(UUID.randomUUID());
    bouwheer.setBouwheerName("Bouwheer");
    return bouwheer;
  }

  private static FinancingHdr financingHdr() {
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setCustomer(customer());
    hdr.setBouwheer(bouwheer());
    hdr.setFinancingStatus("NEW");
    hdr.setFinancingStep("NEW");
    hdr.setFinancingDueDate(LocalDateTime.of(2026, 12, 1, 0, 0));
    hdr.setFinancingAmt(1000.0);
    hdr.setAgreement(Set.of());
    return hdr;
  }

  private static FinancingDtl detail(FinancingHdr hdr, String invoiceNo) {
    FinancingDtl detail = FinancingDtl.builder().financingHdr(hdr).invoice(invoice(invoiceNo)).build();
    detail.setBouwheerPaidDate(LocalDateTime.of(2026, 11, 20, 0, 0));
    return detail;
  }

  private static Invoice invoice(String invoiceNo) {
    return Invoice.builder().custInvNo(invoiceNo).bouwheerInvNo("B-" + invoiceNo)
      .invoiceAmt(1000.0).invoiceDescription("Description")
      .invoiceDate(LocalDateTime.of(2026, 11, 1, 0, 0))
      .invoiceDueDate(LocalDateTime.of(2026, 12, 1, 0, 0)).build();
  }
}
