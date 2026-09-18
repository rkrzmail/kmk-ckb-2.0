package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.dto.CwrListDto;
import com.kmkbe.core.domain.dto.SimulationHistDto;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PaginationSortTest {
  @ParameterizedTest
  @CsvSource({"financingHdrCode,financingHdrCode", "custName,custName", "NamaDebitur,custName",
    "bouwheerName,bouwheerName", "PemberiKerja,bouwheerName", "dtmCrt,dtmCrt", "dueDate,dueDate",
    "financingAmount,financingAmount", "branchRecommended,branchRecommended", "Cabang,branchRecommended",
    "currentBranch,currentBranch", "custStatus,custStatus", "status,status", "Status,status", "city,city", "ao,ao"})
  void distributionFieldsSortBothDirectionsWithNullsLast(String field, String property) {
    var first = new com.kmkbe.core.domain.dto.DistributionSubmissionDto();
    var second = new com.kmkbe.core.domain.dto.DistributionSubmissionDto();
    var empty = new com.kmkbe.core.domain.dto.DistributionSubmissionDto();
    first.setFinancingHdrCode("A");
    second.setFinancingHdrCode("B");
    if (property.equals("status")) {
      first.setStatus(com.kmkbe.core.domain.dto.StatusLabelDto.builder().status("A").build());
      second.setStatus(com.kmkbe.core.domain.dto.StatusLabelDto.builder().status("B").build());
    } else {
      Object low = switch (property) {
        case "dtmCrt" -> java.time.LocalDateTime.of(2026, 9, 1, 0, 0);
        case "dueDate" -> new java.util.Date(1);
        case "financingAmount" -> new java.math.BigDecimal("2");
        default -> "A";
      };
      Object high = switch (property) {
        case "dtmCrt" -> java.time.LocalDateTime.of(2026, 9, 2, 0, 0);
        case "dueDate" -> new java.util.Date(2);
        case "financingAmount" -> new java.math.BigDecimal("10");
        default -> "B";
      };
      new org.springframework.beans.BeanWrapperImpl(first).setPropertyValue(property, low);
      new org.springframework.beans.BeanWrapperImpl(second).setPropertyValue(property, high);
    }
    var rows = new ArrayList<>(List.of(second, empty, first));
    rows.sort(PaginationSort.distributionComparator(request(field, "asc")));
    assertThat(rows).containsExactly(first, second, empty);
    rows.sort(PaginationSort.distributionComparator(request(field, "desc")));
    assertThat(rows).containsExactly(second, first, empty);
    assertThat(PaginationSort.distributionComparator(request(null, null))).isNull();
  }

  @ParameterizedTest
  @CsvSource({"customerInvoiceNo,invoice.custInvNo", "custInvNo,invoice.custInvNo",
    "bouwheerInvoiceNo,invoice.bouwheerInvNo", "poNumber,invoice.poNumber",
    "invoiceDate,invoice.invoiceDate", "invoiceDueDate,invoice.invoiceDueDate",
    "invoiceAmount,invoice.invoiceAmt", "invoiceDescription,invoice.invoiceDescription",
    "bouwheerName,financingHdr.bouwheer.bouwheerName", "dtmCrt,dtmCrt"})
  void invoiceFields(String field, String property) {
    for (String direction : List.of("asc", "DESC")) {
      assertThat(PaginationSort.invoices(request(field, direction)).getOrderFor(property).getDirection())
        .isEqualTo(Sort.Direction.fromString(direction));
    }
  }

  @ParameterizedTest
  @CsvSource({"invoiceNo,custInvNo", "custName,customer.custName", "bouwheerName,bouwheer.bouwheerName",
    "paidDate,invoiceDate", "dueDate,invoiceDueDate", "paidAmount,invoiceAmt"})
  void paidInvoiceFields(String field, String property) {
    assertThat(PaginationSort.paidInvoices(request(field, "desc")).getOrderFor(property).isDescending()).isTrue();
  }

  @ParameterizedTest
  @CsvSource({"agreementNo,agreementCode", "custName,financingHdr.customer.custName",
    "bouwheerName,financingHdr.bouwheer.bouwheerName", "disburseDate,financingHdr.disburseDate",
    "paidDate,financingHdr.disburseDate", "disburseAmount,financingHdr.disburseAmt"})
  void disbursementFields(String field, String property) {
    assertThat(PaginationSort.disbursements(request(field, "asc")).getOrderFor(property).isAscending()).isTrue();
  }

  @ParameterizedTest
  @CsvSource({"agreementNo,agreement_code", "agreementCode,agreement_code",
    "custName,ct.cust_name", "bouwheerName,bw.bouwheer_name", "financingAmt,financing_amt",
    "disburseDate,fh.disburse_date", "disburseAmt,fh.disburse_amt", "currency,currency"})
  void agreementFields(String field, String property) {
    assertThat(PaginationSort.agreements(request(field, "desc")).getOrderFor(property).isDescending()).isTrue();
    assertThat(PaginationSort.agreements(request(field, "asc")).getOrderFor(property).isAscending()).isTrue();
  }

  @ParameterizedTest
  @CsvSource({"financingHdrCode,financing_hdr_code", "custName,c.cust_name",
    "bouwheerName,bw.bouwheer_name", "dueDate,financing_due_date",
    "financingAmount,financing_amt", "status,financing_status",
    "dtmCrt,dtm_crt", "financingDate,financing_date"})
  void assignmentFields(String field, String property) {
    assertThat(PaginationSort.assignments(request(field, "desc")).getOrderFor(property).isDescending()).isTrue();
    assertThat(PaginationSort.assignments(request(field, "asc")).getOrderFor(property).isAscending()).isTrue();
  }

  @Test
  void assignmentVerificationDateSortsBeforePaginationWithNullsLast() {
    var early = com.kmkbe.core.domain.dto.AssignmentDto.builder()
      .financingHdrCode(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))
      .verifDate(java.time.LocalDateTime.of(2026, 9, 17, 1, 0)).build();
    var late = com.kmkbe.core.domain.dto.AssignmentDto.builder()
      .financingHdrCode(java.util.UUID.fromString("00000000-0000-0000-0000-000000000002"))
      .verifDate(java.time.LocalDateTime.of(2026, 9, 17, 2, 0)).build();
    var missing = com.kmkbe.core.domain.dto.AssignmentDto.builder()
      .financingHdrCode(java.util.UUID.fromString("00000000-0000-0000-0000-000000000003")).build();
    var rows = new ArrayList<>(List.of(missing, late, early));

    assertThat(PaginationSort.assignments(request("verifDate", "asc")).getOrderFor("financing_hdr_id"))
      .isNotNull();
    rows.sort(PaginationSort.assignmentVerifDateComparator(request("verifDate", "asc")));
    assertThat(rows).containsExactly(early, late, missing);
    rows.sort(PaginationSort.assignmentVerifDateComparator(request("verifDate", "desc")));
    assertThat(rows).containsExactly(late, early, missing);
  }

  @ParameterizedTest
  @CsvSource({"simulationHistCode,simulationHistCode", "financingAmt,financingAmt", "schema,schema", "effectiveRate,effetiveRate",
    "effetiveRate,effetiveRate", "adminFee,adminFee", "disbursementAmt,dibursmentAmt",
    "dibursmentAmt,dibursmentAmt"})
  void tocFieldsSortBothDirections(String field, String ignoredProperty) {
    var low = SimulationHistDto.builder().simulationHistCode(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))
      .financingAmt(10D).schema(80D).effetiveRate(1D).adminFee(2D).dibursmentAmt(3D).build();
    var high = SimulationHistDto.builder().simulationHistCode(java.util.UUID.fromString("00000000-0000-0000-0000-000000000002"))
      .financingAmt(20D).schema(90D).effetiveRate(4D).adminFee(5D).dibursmentAmt(6D).build();
    var rows = new ArrayList<>(List.of(high, low));
    rows.sort(PaginationSort.tocComparator(request(field, "asc")));
    assertThat(rows).containsExactly(low, high);
    rows.sort(PaginationSort.tocComparator(request(field, "desc")));
    assertThat(rows).containsExactly(high, low);
  }

  @Test
  void defaultsAndTieBreakers() {
    assertThat(PaginationSort.cwrComparator(request(null, null))).isNull();
    assertThat(PaginationSort.invoices(request(null, null))).isEqualTo(Sort.by("financingDtlCode"));
    assertThat(PaginationSort.agreements(request(null, null)).getOrderFor("fh.disburse_date").isDescending()).isTrue();
    assertThat(PaginationSort.assignments(request(null, null)).getOrderFor("dtm_crt").isDescending()).isTrue();
    assertThat(PaginationSort.invoices(request(" poNumber ", " ")).getOrderFor("invoice.poNumber").isAscending()).isTrue();
    assertThat(PaginationSort.agreements(request("custName", null)).getOrderFor("agreement_id")).isNotNull();
    assertThat(PaginationSort.assignments(request("custName", null)).getOrderFor("financing_hdr_id")).isNotNull();
  }

  @Test
  void cwrNumericSortingKeepsNullsLastAndBreaksTies() {
    CwrListDto a = CwrListDto.builder().cwrCode("A").plafondAmt(10D).build();
    CwrListDto b = CwrListDto.builder().cwrCode("B").plafondAmt(2D).build();
    CwrListDto c = CwrListDto.builder().cwrCode("C").plafondAmt(10D).build();
    CwrListDto empty = CwrListDto.builder().cwrCode("D").build();
    var rows = new ArrayList<>(List.of(c, empty, b, a));
    rows.sort(PaginationSort.cwrComparator(request("plafondValue", "asc")));
    assertThat(rows).containsExactly(b, a, c, empty);
    rows.sort(PaginationSort.cwrComparator(request("plafondAmt", "desc")));
    assertThat(rows).containsExactly(a, c, b, empty);
  }

  @Test
  void rejectsInvalidFieldsAndDirections() {
    for (var resolver : List.<java.util.function.Function<PaginationRequest, ?>>of(
      PaginationSort::invoices, PaginationSort::paidInvoices, PaginationSort::disbursements,
      PaginationSort::agreements, PaginationSort::assignments,
      PaginationSort::cwrComparator, PaginationSort::distributionComparator, PaginationSort::tocComparator)) {
      assertThatThrownBy(() -> resolver.apply(request("x; DROP TABLE customer", "asc")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("sortBy tidak didukung");
      assertThatThrownBy(() -> resolver.apply(request("status", "sideways")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("sortType harus asc atau desc");
      assertThatThrownBy(() -> resolver.apply(request(null, "desc")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("sortBy wajib diisi");
    }
  }

  private PaginationRequest request(String field, String direction) {
    var request = new PaginationRequest();
    request.setSortBy(field);
    request.setSortType(direction);
    return request;
  }
}
