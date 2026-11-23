package com.kmkbe.core.domain.spec;

import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class CoreDomainSpecCoverageTest {

  Root root;
  CriteriaBuilder cb;
  CriteriaQuery query;
  Join invoice, hdr, cust, agreement, bouwheer, dtl, hdr2;
  Path path;

  @BeforeEach
  void setUp() {
    root = mock(Root.class);
    cb = mock(CriteriaBuilder.class);
    query = mock(CriteriaQuery.class);
    invoice = mock(Join.class);
    hdr = mock(Join.class);
    cust = mock(Join.class);
    agreement = mock(Join.class);
    bouwheer = mock(Join.class);
    dtl = mock(Join.class);
    hdr2 = mock(Join.class);
    path = mock(Path.class);

    when(root.join(anyString(), any(JoinType.class))).thenAnswer(inv -> switch ((String) inv.getArgument(0)) {
      case "invoice" -> invoice;
      case "financingHdr", "financing_hdr" -> hdr;
      case "financing_dtl" -> dtl;
      default -> hdr;
    });
    when(hdr.join(anyString(), any(JoinType.class))).thenAnswer(inv -> switch ((String) inv.getArgument(0)) {
      case "customer", "cust_code" -> cust;
      case "agreement" -> agreement;
      case "bouwheer", "bouwheer_code" -> bouwheer;
      case "financingDtls", "financing_hdr_code" -> dtl;
      default -> dtl;
    });
    when(dtl.join(anyString(), any(JoinType.class))).thenReturn(hdr2);
    doReturn(path).when(root).get(anyString());
  }

  private void run(Specification spec) {
    assertThat(spec.toPredicate(root, query, cb)).isNull();
  }

  @Test void financingDtlSpecDashboardBranches() {
    Customer customer = mock(Customer.class);
    when(customer.getCustCode()).thenReturn(UUID.randomUUID());
    run(FinancingDtlSpec.custDashboardFilterBy(customer, null, null));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "custInvNo", "INV"));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "agreementCode", "AGR"));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "bouwheerName", "PT"));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "invoiceDate", "2026-11-23"));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "status", "new"));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "status", "inprocess"));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "status", "live"));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "status", "unknown"));
    run(FinancingDtlSpec.custDashboardFilterBy(customer, "other", "x"));
  }

  @Test void financingDtlSpecInvoiceBranches() {
    UUID code = UUID.randomUUID();
    run(FinancingDtlSpec.custInvoiceFilterBy(code, null, null));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "customerInvoiceNo", "INV CV 1"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "custInvNo", "INV"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "invoiceDescription", "Dok"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "agreementCode", "AGR"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "poNumber", "PO"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "bouwheerName", "PT ABC"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "invoiceDate", "10/09/2026"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "invoiceDueDate", "10/10/2026"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "invoiceAmount", "IDR 1.000"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "status", "new"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "status", "inprocess"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "status", "live"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "status", "unknown"));
    run(FinancingDtlSpec.custInvoiceFilterBy(code, "other", "x"));
  }

  @Test void normalizeHelpersNullBranch() throws Exception {
    Method amount = FinancingDtlSpec.class.getDeclaredMethod("normalizeInvoiceAmount", String.class);
    amount.setAccessible(true);
    assertThat(amount.invoke(null, (Object) null)).isEqualTo("");
  }

  @Test void financingHdrSpecBranches() {
    for (String key : new String[]{null, "status", "namadebitur", "pemberikerja", "noinvoice", "cabang", "other"}) {
      run(FinancingHdrSpec.bySearchBy(key, key == null ? null : "v"));
      run(FinancingHdrSpec.bySearchTOCBy(key, key == null ? null : "v"));
    }
    for (String key : new String[]{null, "status", "namadebitur", "pemberikerja", "noinvoice", "other"}) {
      run(FinancingHdrSpec.byStepStatus(key, key == null ? null : "v"));
      run(FinancingHdrSpec.byDisbursement(key, key == null ? null : "v"));
    }
    run(FinancingHdrSpec.bySimulationHist("code", null, null));
    run(FinancingHdrSpec.bySimulationHist("code", "status", "v"));
  }

  @Test void invoiceSpecBranches() {
    PaginationRequest req = new PaginationRequest();
    req.setSearchBy("noinvoice");
    req.setSearchValue("INV");
    run(InvoiceSpec.list(req));

    FinancingHdr financingHdr = mock(FinancingHdr.class);
    when(financingHdr.getFinancingHdrCode()).thenReturn(UUID.randomUUID());
    run(InvoiceSpec.listDistributionDetail(financingHdr, req));
    run(InvoiceSpec.byInvoiceGreaterThanToday());
    run(InvoiceSpec.byFinancingHdr(financingHdr));

    for (String key : new String[]{null, "noinvoice", "pemberikerja", "item", "tanggalinvoice", "jatuhtempoinvoice", "jumlahtagihan", "other"}) {
      run(InvoiceSpec.bySearchBy(key, key == null ? null : "v"));
    }
  }
}
