package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.DisburseInvoiceDto;
import com.kmkbe.core.domain.dto.PaidInvoiceDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.DisbursementLog;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.model.SimulationDisburseResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.DisbursementLogRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancingHdrServiceCoverageTest {
  @Mock FinancingHdrRepository repository;
  @Mock InvoiceRepository invoiceRepository;
  @Mock RedisRepository redisRepository;
  @Mock AgreementRepository agreementRepository;
  @Mock DisbursementLogRepository disbursementLogRepository;
  @Mock FinancingDtlRepository financingDtlRepository;
  FinancingHdrService service;

  @BeforeEach
  void setUp() {
    service = new FinancingHdrService(repository, invoiceRepository, redisRepository,
      agreementRepository, disbursementLogRepository, financingDtlRepository);
  }

  @Test
  void createGetDtoDeleteAndFindByCode() {
    Customer customer = customer();
    Bouwheer bouwheer = bouwheer();
    PostedInvoicePayload invoice = new PostedInvoicePayload();
    invoice.setCurrencyCode("IDR");
    CreateSimulationRequest request = CreateSimulationRequest.builder()
      .invoices(List.of(invoice)).totalInvoiceAmount(1000.0).build();
    SimulationDisburseResult simulation = simulation();
    when(repository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer)).thenReturn(Optional.empty());
    when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
    FinancingHdr created = service.create(customer, bouwheer, request, simulation);
    assertThat(created.getFinancingAmt()).isEqualTo(900.0);

    when(repository.findByFinancingHdrCode(created.getFinancingHdrCode())).thenReturn(Optional.of(created));
    assertThat(service.getByCode(created.getFinancingHdrCode())).isSameAs(created);
    assertThat(service.dtoFromEntity(created)).isNotNull();
    assertThat(service.findByCode(created.getFinancingHdrCode().toString())).isSameAs(created);

    UUID missingCode = UUID.randomUUID();
    when(repository.findByFinancingHdrCode(missingCode)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.findByCode(missingCode.toString()))
      .isInstanceOf(IllegalStateException.class);

    service.delete(created);
    verify(repository).delete(created);
    org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(repository).delete(created);
    assertThatThrownBy(() -> service.delete(created)).hasMessage("db down");
  }

  @Test
  void paidInvoiceListsAndPagination() {
    FinancingHdr hdr = financingHdr();
    FinancingDtl detail = detail(hdr);
    when(repository.findAllByRawOrder(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(hdr)));
    when(financingDtlRepository.findAllByFinancingHdrOrderByDtmCrtDesc(hdr)).thenReturn(List.of(detail));
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    assertThat(service.paidInvoiceNew(request).getList()).hasSize(1);

    when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<FinancingHdr>>any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(hdr)));
    assertThat(service.paidInvoice(request).getList()).hasSize(1);

    PageImpl<FinancingHdr> page = new PageImpl<>(List.of(hdr));
    assertThat(service.paginatePaidInvoice(page, 2).getList()).hasSize(1);
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "getColor", hdr)).isEqualTo("#FF5C5C");

    Agreement agreement = agreement(hdr);
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(hdr.getFinancingHdrCode()))
      .thenReturn(List.of(agreement));
    hdr.setDisburseDate(LocalDateTime.of(2026, 11, 20, 0, 0));
    assertThat(service.paginatePaidDisbursement(page, 2).getList()).hasSize(1);
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(hdr.getFinancingHdrCode())).thenReturn(List.of());
    assertThat(service.paginatePaidDisbursement(page, 0).getList()).isEmpty();
  }

  @Test
  void paidUnpaidAndDisbursementLists() {
    FinancingHdr hdr = financingHdr();
    Invoice invoice = invoice(detail(hdr));
    when(invoiceRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Invoice>>any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(invoice, new Invoice())));
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    assertThat(service.listPaidUnpaidInvoice(request).getList()).hasSize(1);

    var converted = new com.kmkbe.modules.loan_submission.request.FinancingInvoicePaginationRequest();
    converted.setPageNo(1);
    converted.setPageSize(10);
    assertThat(service.listPaidUnpaidInvoice(converted).getList()).hasSize(1);

    Agreement agreement = agreement(hdr);
    when(agreementRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Agreement>>any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(agreement)));
    when(disbursementLogRepository.findAllByAgreement(agreement))
      .thenReturn(List.of(disbursementLog()), List.of());
    assertThat(service.listdisburseAggrement(request).getList()).hasSize(1);

    assertThat(service.listdisburseAggrement(converted).getList()).hasSize(1);

    when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<FinancingHdr>>any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(hdr)));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(hdr.getFinancingHdrCode()))
      .thenReturn(List.of(agreement));
    assertThat(service.disburseInvoice(request, hdr).getList()).hasSize(1);
  }

  @Test
  void paidFinancingAndHelpers() {
    FinancingHdr hdr = financingHdr();
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    assertThatThrownBy(() -> service.paidFinancing(request)).isInstanceOf(RuntimeException.class);

    UUID code = UUID.randomUUID();
    request.setFinancingCode(code.toString());
    when(repository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    hdr.setAgreement(Set.of());
    assertThatThrownBy(() -> service.paidFinancing(request)).isInstanceOf(RuntimeException.class);

    hdr.setAgreement(Set.of(agreement(hdr)));
    when(repository.save(hdr)).thenReturn(hdr);
    assertThat(service.paidFinancing(request).getFinancingStatus()).isEqualTo("PAID");

    assertThat((int) ReflectionTestUtils.invokeMethod(service, "getZeroBasedPageNo", new PaginationRequest())).isZero();
    assertThat((int) ReflectionTestUtils.invokeMethod(service, "getPageSize", new PaginationRequest())).isEqualTo(10);
    assertThat((Date) ReflectionTestUtils.invokeMethod(service, "parseSearchDate", "24/11/2026")).isNotNull();
    assertThat((Date) ReflectionTestUtils.invokeMethod(service, "parseSearchDate", "2026-11-24")).isNotNull();
    assertThat((Date) ReflectionTestUtils.invokeMethod(service, "parseSearchDate", "2026-11-24 00:00:00")).isNotNull();
    assertThat((Date) ReflectionTestUtils.invokeMethod(service, "parseSearchDate", "invalid")).isNull();
    PaginationRequest normalizeRequest = new PaginationRequest();
    normalizeRequest.setSearchBy("Cust Name");
    normalizeRequest.setSearchValue(" value ");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "normalizeSearchBy", normalizeRequest))
      .isEqualTo("custname");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "normalizeSearchValue", normalizeRequest))
      .isEqualTo("value");
    assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "toStartOfDay", new Date())).doesNotThrowAnyException();
    assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "toEndOfDay", new Date())).doesNotThrowAnyException();
  }

  @Test
  void paidInvoiceSpecBranches() {
    PaginationRequest request = new PaginationRequest();
    request.setSearchValue("value");
    request.setStartDate(new Date());
    request.setEndDate(new Date());
    Specification<Invoice> spec = (Specification<Invoice>) ReflectionTestUtils.invokeMethod(
      service, "buildPaidInvoiceSpecification", request);
    for (String field : List.of("invoiceNo", "custName", "bouwheerName", "paidDate",
      "invoiceDueDate", "paidAmount", "status", "unknown")) {
      request.setSearchBy(field);
      assertThatCode(() -> evaluate(spec)).doesNotThrowAnyException();
    }
    request.setSearchValue(null);
    assertThatCode(() -> evaluate(spec)).doesNotThrowAnyException();

    PaginationRequest disbursementRequest = new PaginationRequest();
    disbursementRequest.setSearchValue("value");
    disbursementRequest.setStartDate(new Date());
    disbursementRequest.setEndDate(new Date());
    Specification<Agreement> disbursementSpec = (Specification<Agreement>) ReflectionTestUtils.invokeMethod(
      service, "buildDisbursementSpecification", disbursementRequest);
    for (String field : List.of("agreementNo", "custName", "bouwheerName", "disburseAmount",
      "paidDate", "status", "unknown")) {
      disbursementRequest.setSearchBy(field);
      assertThatCode(() -> evaluate(disbursementSpec)).doesNotThrowAnyException();
    }
    disbursementRequest.setSearchValue(null);
    assertThatCode(() -> evaluate(disbursementSpec)).doesNotThrowAnyException();
  }

  private void evaluate(Specification<?> spec) {
    Root root = mock(Root.class);
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Join join = mock(Join.class);
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);
    lenient().when(root.join(anyString(), any(JoinType.class))).thenReturn(join);
    lenient().when(join.join(anyString(), any(JoinType.class))).thenReturn(join);
    lenient().when(root.get(anyString())).thenReturn(path);
    lenient().when(join.get(anyString())).thenReturn(path);
    lenient().when(builder.lower(any())).thenReturn(mock(Expression.class));
    lenient().when(builder.isNotNull(any())).thenReturn(predicate);
    lenient().when(builder.equal(any(), any())).thenReturn(predicate);
    lenient().when(builder.not(any())).thenReturn(predicate);
    lenient().when(builder.or(any(Predicate[].class))).thenReturn(predicate);
    lenient().when(builder.and(any(Predicate[].class))).thenReturn(predicate);
    lenient().when(builder.greaterThanOrEqualTo(org.mockito.ArgumentMatchers.<Expression<LocalDateTime>>any(), any(LocalDateTime.class))).thenReturn(predicate);
    lenient().when(builder.lessThanOrEqualTo(org.mockito.ArgumentMatchers.<Expression<LocalDateTime>>any(), any(LocalDateTime.class))).thenReturn(predicate);
    lenient().when(builder.between(org.mockito.ArgumentMatchers.<Expression<LocalDateTime>>any(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(predicate);
    lenient().when(builder.like(any(), anyString())).thenReturn(predicate);
    lenient().when(path.in(anyCollection())).thenReturn(predicate);
    assertThat(spec.toPredicate(root, mock(jakarta.persistence.criteria.CriteriaQuery.class), builder))
      .isSameAs(predicate);
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustName("Customer");
    customer.setCustCode(UUID.randomUUID());
    customer.setCustTypeCode("Company");
    CustomerCompany company = new CustomerCompany();
    company.setCompanyAddress("Address");
    company.setPhone("021");
    customer.setCompany(company);
    return customer;
  }

  private static Bouwheer bouwheer() {
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerCode(UUID.randomUUID());
    bouwheer.setBouwheerName("Bouwheer");
    bouwheer.setGracePeriod(3L);
    return bouwheer;
  }

  private static FinancingHdr financingHdr() {
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setCustomer(customer());
    hdr.setBouwheer(bouwheer());
    hdr.setFinancingStatus("PAID");
    hdr.setFinancingStep("PAID");
    hdr.setDisburseDate(LocalDateTime.of(2026, 11, 20, 0, 0));
    hdr.setTotalInvoiceAmt(1000.0);
    hdr.setFinancingAmt(900.0);
    hdr.setInvoiceQty(1L);
    return hdr;
  }

  private static FinancingDtl detail(FinancingHdr hdr) {
    FinancingDtl detail = new FinancingDtl();
    detail.setFinancingHdr(hdr);
    detail.setInvoice(invoice(null));
    return detail;
  }

  private static Invoice invoice(FinancingDtl detail) {
    Invoice invoice = Invoice.builder().custInvNo("INV-1").bouwheerInvNo("B-INV-1")
      .invoiceAmt(1000.0).invoiceDescription("desc")
      .invoiceDate(LocalDateTime.of(2026, 11, 1, 0, 0))
      .invoiceDueDate(LocalDateTime.of(2026, 12, 1, 0, 0)).build();
    if (detail != null) {
      detail.setInvoice(invoice);
      invoice.setFinancingDtl(detail);
    }
    return invoice;
  }

  private static Agreement agreement(FinancingHdr hdr) {
    Agreement agreement = new Agreement();
    agreement.setAgreementCode("AGR-1");
    agreement.setFinancingHdr(hdr);
    return agreement;
  }

  private static DisbursementLog disbursementLog() {
    DisbursementLog log = new DisbursementLog();
    log.setApPaidAmt(1000.0);
    return log;
  }

  private static SimulationDisburseResult simulation() {
    return SimulationDisburseResult.builder()
      .tenor(12L).retention(5.0).totalInvoiceAmount(BigDecimal.valueOf(1000))
      .financingAmount(BigDecimal.valueOf(900)).estimatedDisburseAmount(BigDecimal.valueOf(850))
      .interestFeeAmount(BigDecimal.TEN).maxInvoiceDate(new Date())
      .provisionFeeAmount(BigDecimal.ONE).provisionRate(1.0).adminRate(1.0)
      .effectiveRate(1.0).surveyFeeAmount(BigDecimal.ONE).legalFeeAmount(BigDecimal.ONE)
      .othersFeeAmount(BigDecimal.ONE).adminFeeAmount(BigDecimal.ONE).build();
  }
}
