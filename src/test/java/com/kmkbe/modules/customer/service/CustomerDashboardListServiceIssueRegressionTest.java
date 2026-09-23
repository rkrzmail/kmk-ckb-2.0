package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.CustomerCreditFacilityDueDateDto;
import com.kmkbe.core.domain.dto.CustomerCreditFacilityNewDto;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDashboardListServiceIssueRegressionTest {

  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private FinancingDtlRepository financingDtlRepository;
  @Mock private MstBranchRepository mstBranchRepository;
  @Mock private AgreementRepository agreementRepository;

  private CustomerDashboardListService service;

  @BeforeEach
  void setUp() {
    service = new CustomerDashboardListService(
      financingHdrRepository,
      invoiceRepository,
      financingDtlRepository,
      mstBranchRepository,
      agreementRepository
    );
  }

  @Test
  void activeCreditFacilityContainsAllDistinctCustomerInvoiceNumbers() throws Exception {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custTypeCode("Company")
      .build();
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingHdrCode(UUID.randomUUID());
    financing.setCustomer(customer);
    financing.setBouwheer(Bouwheer.builder().bouwheerName("CKB").build());
    financing.setFinancingStatus("NEW");
    financing.setFinancingStep("NEW");
    financing.setFinancingDueDate(LocalDateTime.of(2026, 10, 1, 0, 0));
    financing.setFinancingAmt(1_000_000D);
    financing.setAgreement(Set.of());
    java.util.List<FinancingDtl> financingDetails = java.util.List.of(
      financingDetail(" INV-002 "),
      financingDetail("INV-001"),
      financingDetail("INV-001")
    );
    when(financingHdrRepository.findAllByRawOrder(eq(customer.getCustCode().toString()), any(Pageable.class)))
      .thenReturn(new PageImpl<>(java.util.List.of(financing)));
    when(financingHdrRepository.countByCustomerAndFinancingStatus(customer, "PAID")).thenReturn(0L);
    when(financingDtlRepository.findAllByFinancingHdrOrderByDtmCrtDesc(financing))
      .thenReturn(financingDetails);

    PaginationResult<CustomerCreditFacilityNewDto> result = service.listcreditfacilities(
      customer,
      new PaginationRequest()
    );

    assertThat(result.getList()).singleElement()
      .extracting(CustomerCreditFacilityNewDto::getInvoiceNo)
      .isEqualTo("INV-001, INV-002");
  }

  @Test
  void activeCreditFacilityUsesEmptyInvoiceNumberWhenDetailsAreUnavailable() {
    assertThat(service.invoiceNumbers(java.util.List.of())).isEmpty();
    assertThat(service.invoiceNumbers(null)).isEmpty();
  }

  @Test
  void invoiceDueDateContainsCustomerInvoiceNumber() throws Exception {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custTypeCode("Company")
      .build();
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingHdrCode(UUID.randomUUID());
    financing.setCustomer(customer);
    financing.setBouwheer(Bouwheer.builder().bouwheerName("CKB").build());
    financing.setFinancingStatus("NEW");
    financing.setFinancingStep("NEW");
    financing.setFinancingDueDate(LocalDateTime.of(2026, 10, 1, 0, 0));
    financing.setFinancingAmt(1_000_000D);
    financing.setAgreement(Set.of());

    FinancingDtl financingDtl = FinancingDtl.builder()
      .financingHdr(financing)
      .invoice(Invoice.builder()
        .custInvNo("INV-DUE-001")
        .poNumber("PO-001")
        .postingDate(new Date())
        .build())
      .build();
    when(financingDtlRepository.findByCustomer(eq(customer.getCustCode().toString()), any(Pageable.class)))
      .thenReturn(new PageImpl<>(java.util.List.of(financingDtl)));
    when(financingHdrRepository.countByCustomerAndFinancingStatus(customer, "PAID")).thenReturn(0L);

    PaginationResult<CustomerCreditFacilityDueDateDto> result = service.listinvoicesduedate(
      customer,
      new PaginationRequest()
    );

    assertThat(result.getList()).singleElement()
      .extracting(CustomerCreditFacilityDueDateDto::getInvoiceNo)
      .isEqualTo("INV-DUE-001");
  }

  @Test
  void activeCreditFacilitiesApplySearchSortAndPagination() throws Exception {
    Customer customer = customer();
    FinancingHdr lowerAmount = financing(customer, 1_000_000D, LocalDateTime.of(2026, 10, 1, 0, 0));
    FinancingHdr higherAmount = financing(customer, 2_000_000D, LocalDateTime.of(2026, 11, 1, 0, 0));

    when(financingHdrRepository.findAllByRawOrder(
      eq(customer.getCustCode().toString()), eq(Pageable.unpaged())
    )).thenReturn(new PageImpl<>(java.util.List.of(lowerAmount, higherAmount)));
    when(financingHdrRepository.countByCustomerAndFinancingStatus(customer, "PAID")).thenReturn(0L);
    when(financingDtlRepository.findAllByFinancingHdrOrderByDtmCrtDesc(lowerAmount))
      .thenReturn(java.util.List.of(financingDetail("INV-LOW")));
    when(financingDtlRepository.findAllByFinancingHdrOrderByDtmCrtDesc(higherAmount))
      .thenReturn(java.util.List.of(financingDetail("INV-HIGH")));

    BasePaginationRequest request = request("invoiceNo", "INV", "financingAmount", "desc", 1, 1);
    PaginationResult<CustomerCreditFacilityNewDto> result = service.listcreditfacilities(customer, request);

    assertThat(result.getTotalData()).isEqualTo(2L);
    assertThat(result.getTotalPage()).isEqualTo(2);
    assertThat(result.getList()).singleElement()
      .extracting(CustomerCreditFacilityNewDto::getInvoiceNo)
      .isEqualTo("INV-HIGH");
  }

  @Test
  void dueDateInvoicesApplyAliasSearchAndPostingDateSort() throws Exception {
    Customer customer = customer();
    FinancingHdr financing = financing(customer, 1_000_000D, LocalDateTime.of(2026, 10, 1, 0, 0));
    FinancingDtl older = dueDateDetail(financing, "INV-OLD", "PO-MATCH-1", new Date(1000));
    FinancingDtl newer = dueDateDetail(financing, "INV-NEW", "PO-MATCH-2", new Date(2000));
    FinancingDtl excluded = dueDateDetail(financing, "INV-OTHER", "OTHER", new Date(3000));

    when(financingDtlRepository.findByCustomer(
      eq(customer.getCustCode().toString()), eq(Pageable.unpaged())
    )).thenReturn(new PageImpl<>(java.util.List.of(older, excluded, newer)));
    when(financingHdrRepository.countByCustomerAndFinancingStatus(customer, "PAID")).thenReturn(0L);

    BasePaginationRequest request = request("poNumber", "match", "postingDate", "desc", 1, 10);
    PaginationResult<CustomerCreditFacilityDueDateDto> result = service.listinvoicesduedate(customer, request);

    assertThat(result.getTotalData()).isEqualTo(2L);
    assertThat(result.getList())
      .extracting(CustomerCreditFacilityDueDateDto::getInvoiceNo)
      .containsExactly("INV-NEW", "INV-OLD");
  }

  private Customer customer() {
    return Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custTypeCode("Company")
      .build();
  }

  private FinancingHdr financing(Customer customer, double amount, LocalDateTime dueDate) {
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingHdrCode(UUID.randomUUID());
    financing.setCustomer(customer);
    financing.setBouwheer(Bouwheer.builder().bouwheerName("CKB").build());
    financing.setFinancingStatus("NEW");
    financing.setFinancingStep("NEW");
    financing.setFinancingDueDate(dueDate);
    financing.setFinancingAmt(amount);
    financing.setAgreement(Set.of());
    financing.setDtmCrt(dueDate.minusDays(1));
    return financing;
  }

  private FinancingDtl dueDateDetail(
    FinancingHdr financing,
    String invoiceNo,
    String poNumber,
    Date postingDate
  ) {
    return FinancingDtl.builder()
      .financingHdr(financing)
      .invoice(Invoice.builder()
        .custInvNo(invoiceNo)
        .poNumber(poNumber)
        .postingDate(postingDate)
        .build())
      .dtmCrt(financing.getDtmCrt())
      .build();
  }

  private BasePaginationRequest request(
    String searchBy,
    String searchValue,
    String sortBy,
    String sortType,
    int pageNo,
    int pageSize
  ) {
    BasePaginationRequest request = new BasePaginationRequest();
    request.setSearchBy(searchBy);
    request.setSearchValue(searchValue);
    request.setSortBy(sortBy);
    request.setSortType(sortType);
    request.setPageNo(pageNo);
    request.setPageSize(pageSize);
    return request;
  }

  private FinancingDtl financingDetail(String invoiceNo) {
    return FinancingDtl.builder()
      .invoice(Invoice.builder().custInvNo(invoiceNo).build())
      .build();
  }
}
