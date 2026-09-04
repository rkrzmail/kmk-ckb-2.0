package com.kmkbe.modules.customer.service;

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

  private FinancingDtl financingDetail(String invoiceNo) {
    return FinancingDtl.builder()
      .invoice(Invoice.builder().custInvNo(invoiceNo).build())
      .build();
  }
}
