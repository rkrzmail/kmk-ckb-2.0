package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.CustomerCreditFacilityDueDateDto;
import com.kmkbe.core.domain.dto.CustomerCreditFacilityNewDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDashboardListServiceCoverageTest {
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock InvoiceRepository invoiceRepository;
  @Mock FinancingDtlRepository financingDtlRepository;
  @Mock MstBranchRepository mstBranchRepository;
  @Mock AgreementRepository agreementRepository;
  CustomerDashboardListService service;

  @BeforeEach
  void setUp() {
    service = new CustomerDashboardListService(financingHdrRepository, invoiceRepository,
      financingDtlRepository, mstBranchRepository, agreementRepository);
  }

  @Test
  void invoiceNumbersCoversBlankAndNullValues() {
    FinancingDtl noInvoice = FinancingDtl.builder().build();
    FinancingDtl blankInvoice = FinancingDtl.builder()
      .invoice(Invoice.builder().custInvNo("   ").build()).build();
    FinancingDtl valid = FinancingDtl.builder()
      .invoice(Invoice.builder().custInvNo(" INV-1 ").build()).build();

    assertThat((String) ReflectionTestUtils.invokeMethod(service, "invoiceNumbers",
      List.of(noInvoice, blankInvoice, valid, valid))).isEqualTo("INV-1");
  }

  @Test
  void listInvoicesDueDatePaginationBranches() {
    Customer customer = customer("Company", null, null);
    FinancingHdr hdr = financing(customer);
    FinancingDtl detail = detail(hdr, "INV-1");
    when(financingDtlRepository.findByCustomer(eq(customer.getCustCode().toString()), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(detail)));

    PaginationRequest request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    assertThat(service.listinvoicesduedate(customer, request).getCurrentPage()).isEqualTo(2);

    when(financingDtlRepository.findByCustomer(eq(customer.getCustCode().toString()), any(Pageable.class)))
      .thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.listinvoicesduedate(customer, request)).hasMessage("db down");
  }

  @Test
  void mappingCoversCompanyPersonalAgreementAndBranchPaths() {
    CustomerCompany company = new CustomerCompany();
    company.setCity("Depok");
    company.setKelurahan("Citayam");
    company.setKecamatan("Citayam");
    Customer companyCustomer = customer("Company", company, null);
    FinancingHdr hdr = financing(companyCustomer);
    Agreement agreement = new Agreement();
    agreement.setAgreementCode("AGR-1");
    hdr.setAgreement(Set.of(agreement));
    MstBranch branch = new MstBranch();
    branch.setBranchCode("BR-1");
    branch.setBranchName("Branch");
    hdr.setMstBranch(branch);
    when(financingHdrRepository.findAllByRawOrder(eq(companyCustomer.getCustCode().toString()), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(hdr)));
    when(financingDtlRepository.findAllByFinancingHdrOrderByDtmCrtDesc(hdr)).thenReturn(null);

    assertThat(service.listcreditfacilities(companyCustomer, new BasePaginationRequest()).getList())
      .singleElement().extracting(CustomerCreditFacilityNewDto::getAgreementCode).isEqualTo("AGR-1");

    CustomerPersonal personal = new CustomerPersonal();
    personal.setCity("Jakarta");
    personal.setKelurahan("Menteng");
    personal.setKecamatan("Menteng");
    Customer personalCustomer = customer("Personal", null, personal);
    FinancingHdr personalHdr = financing(personalCustomer);
    personalHdr.setAgreement(Set.of(agreement));
    when(financingDtlRepository.findByCustomer(eq(personalCustomer.getCustCode().toString()), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(detail(personalHdr, "INV-2"))));
    when(mstBranchRepository.findTopLikeBranchNameRawQuery("Jakarta", "Menteng", "Menteng"))
      .thenReturn(java.util.Optional.of(branch));

    assertThat(service.listinvoicesduedate(personalCustomer, new BasePaginationRequest()).getList())
      .singleElement().extracting(CustomerCreditFacilityDueDateDto::getAgreementCode).isEqualTo("AGR-1");

    when(mstBranchRepository.findTopLikeBranchNameRawQuery("Jakarta", "Menteng", "Menteng"))
      .thenReturn(java.util.Optional.empty());
    assertThat(service.listinvoicesduedate(personalCustomer, new BasePaginationRequest()).getList()).hasSize(1);
  }

  @Test
  void colorsAccessorsAndHelpers() throws Exception {
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "creditFacilityColor", "new"))
      .isEqualTo("#808080");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "creditFacilityColor", "live"))
      .isEqualTo("#808080");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "creditFacilityColor", "inprocess"))
      .isEqualTo("#FF5C5C");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "creditFacilityColor", "paid"))
      .isEqualTo("#ccffcc");

    assertThat((String) ReflectionTestUtils.invokeMethod(service, "dueDateColor", "new"))
      .isEqualTo("#808080");
    for (String status : List.of("inprocess", "signing", "signed", "live", "golive", "paid")) {
      assertThat((String) ReflectionTestUtils.invokeMethod(service, "dueDateColor", status))
        .isEqualTo("#ccffcc");
    }
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "dueDateColor", "other"))
      .isEqualTo("#FF5C5C");

    StatusLabelDto status = StatusLabelDto.builder().status("NEW").statusLabel("New").build();
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "statusText", status)).isEqualTo("NEW New");
    assertThat((String) invoke("statusText", StatusLabelDto.class, null)).isNull();
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "statusText", StatusLabelDto.builder().build()))
      .isEmpty();

    CustomerCreditFacilityNewDto facility = CustomerCreditFacilityNewDto.builder()
      .invoiceNo("INV").agreementCode("AGR").financingHdrCode("FH").custName("Name")
      .bouwheerName("Bouwheer").city("City").dueDate(new Date()).financingAmount(BigDecimal.ONE)
      .branchRecommended("Rec").currentBranch("Current").custStatus("NEW")
      .status(status).dtmCrt(LocalDateTime.now()).build();
    for (String field : List.of("invoiceNo", "agreementCode", "financingHdrCode", "custName", "namadebitur",
      "bouwheerName", "pemberikerja", "city", "dueDate", "financingAmount", "branchRecommended",
      "currentBranch", "custStatus", "status", "dtmcrt", "unknown")) {
      assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "creditFacilityField", facility, field))
        .doesNotThrowAnyException();
    }

    CustomerCreditFacilityDueDateDto due = CustomerCreditFacilityDueDateDto.builder()
      .invoiceNo("INV").agreementCode("AGR").poNumber("PO").postingDate(new Date())
      .financingHdrCode("FH").custName("Name").bouwheerName("Bouwheer").city("City")
      .dueDate(new Date()).financingAmount(BigDecimal.ONE).branchRecommended("Rec")
      .currentBranch("Current").custStatus("NEW").status(status).dtmCrt(LocalDateTime.now()).build();
    for (String field : List.of("invoiceNo", "agreementCode", "poNumber", "postingDate", "financingHdrCode",
      "custName", "namadebitur", "bouwheerName", "pemberikerja", "city", "duedate", "financingAmount",
      "branchRecommended", "currentBranch", "custStatus", "status", "dtmcrt", "unknown")) {
      assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "dueDateField", due, field))
        .doesNotThrowAnyException();
    }

    assertThat((int) ReflectionTestUtils.invokeMethod(service, "compareValues", null, null, false)).isZero();
    assertThat((int) ReflectionTestUtils.invokeMethod(service, "compareValues", null, "a", false)).isPositive();
    assertThat((int) ReflectionTestUtils.invokeMethod(service, "compareValues", "a", null, false)).isNegative();
    assertThat((int) ReflectionTestUtils.invokeMethod(service, "compareValues", "a", "b", true)).isPositive();
    assertThat((int) ReflectionTestUtils.invokeMethod(service, "compareValues", 1, 2, false)).isNegative();

    assertThat((String) invoke("searchableText", Object.class, null)).isEmpty();
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "searchableText", new Date(1000))).isNotEmpty();
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "searchableText",
      LocalDateTime.of(2026, 11, 24, 10, 0))).isNotEmpty();
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "searchableText", "VALUE")).isEqualTo("value");
  }

  @Test
  void invalidSearchAndSortFieldsAreRejected() {
    Customer customer = customer("Company", null, null);
    FinancingHdr hdr = financing(customer);
    when(financingHdrRepository.findAllByRawOrder(eq(customer.getCustCode().toString()), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(hdr)));
    when(financingDtlRepository.findAllByFinancingHdrOrderByDtmCrtDesc(hdr)).thenReturn(List.of());

    BasePaginationRequest invalidSearch = new BasePaginationRequest();
    invalidSearch.setSearchBy("unknown");
    invalidSearch.setSearchValue("value");
    assertThatThrownBy(() -> service.listcreditfacilities(customer, invalidSearch))
      .isInstanceOf(RuntimeException.class);

    BasePaginationRequest sortTypeWithoutField = new BasePaginationRequest();
    sortTypeWithoutField.setSortType("desc");
    assertThatThrownBy(() -> service.listcreditfacilities(customer, sortTypeWithoutField))
      .isInstanceOf(RuntimeException.class);

    BasePaginationRequest invalidSortType = new BasePaginationRequest();
    invalidSortType.setSortBy("custName");
    invalidSortType.setSortType("sideways");
    assertThatThrownBy(() -> service.listcreditfacilities(customer, invalidSortType))
      .isInstanceOf(RuntimeException.class);

    assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "validateField",
      "searchBy", "unknown", "value", Set.of("known"))).isInstanceOf(RuntimeException.class);
  }

  private Object invoke(String name, Class<?> parameterType, Object argument) throws Exception {
    Method method = CustomerDashboardListService.class.getDeclaredMethod(name, parameterType);
    method.setAccessible(true);
    return method.invoke(service, argument);
  }

  private static Customer customer(String type, CustomerCompany company, CustomerPersonal personal) {
    Customer customer = Customer.builder().custCode(UUID.randomUUID()).custName("Debitur")
      .custTypeCode(type).company(company).personal(personal).build();
    return customer;
  }

  private static FinancingHdr financing(Customer customer) {
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingHdrCode(UUID.randomUUID());
    financing.setCustomer(customer);
    financing.setBouwheer(Bouwheer.builder().bouwheerName("CKB").build());
    financing.setFinancingStatus("NEW");
    financing.setFinancingStep("NEW");
    financing.setFinancingDueDate(LocalDateTime.of(2026, 10, 1, 0, 0));
    financing.setFinancingAmt(1_000_000D);
    financing.setAgreement(Set.of());
    financing.setDtmCrt(LocalDateTime.of(2026, 9, 30, 0, 0));
    return financing;
  }

  private static FinancingDtl detail(FinancingHdr hdr, String invoiceNo) {
    return FinancingDtl.builder().financingHdr(hdr)
      .invoice(Invoice.builder().custInvNo(invoiceNo).poNumber("PO").postingDate(new Date()).build())
      .dtmCrt(hdr.getDtmCrt()).build();
  }
}
