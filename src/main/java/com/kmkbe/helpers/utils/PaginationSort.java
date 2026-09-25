package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.dto.CwrListDto;
import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.ProyeksiReportDto;
import com.kmkbe.core.domain.dto.SimulationHistDto;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.exception.BusinessException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

public final class PaginationSort {
  private PaginationSort() {}

  private static final Map<String, String> CWR = Map.ofEntries(
    Map.entry("cwrCode", "cwrCode"), Map.entry("cwrNo", "cwrCode"),
    Map.entry("bouwheerName", "bouwheerName"), Map.entry("office", "bouwheerName"),
    Map.entry("custName", "custName"), Map.entry("cwrStartDate", "cwrStartDate"),
    Map.entry("cwrEndDate", "cwrEndDate"), Map.entry("currency", "currency"),
    Map.entry("typeCurrency", "currency"), Map.entry("plafondAmt", "plafondAmt"),
    Map.entry("plafondValue", "plafondAmt"), Map.entry("realisationAmt", "realisationAmt"),
    Map.entry("submissionValue", "realisationAmt"), Map.entry("financingAmt", "financingAmt"),
    Map.entry("status", "status")
  );

  private static final Map<String, String> INVOICE = Map.ofEntries(
    Map.entry("customerInvoiceNo", "invoice.custInvNo"), Map.entry("custInvNo", "invoice.custInvNo"),
    Map.entry("bouwheerInvoiceNo", "invoice.bouwheerInvNo"), Map.entry("poNumber", "invoice.poNumber"),
    Map.entry("invoiceDate", "invoice.invoiceDate"), Map.entry("invoiceDueDate", "invoice.invoiceDueDate"),
    Map.entry("invoiceAmount", "invoice.invoiceAmt"), Map.entry("invoiceDescription", "invoice.invoiceDescription"),
    Map.entry("bouwheerName", "financingHdr.bouwheer.bouwheerName"),
    Map.entry("dtmCrt", "dtmCrt")
  );

  // Spring Data adds the root table alias; only joined columns need explicit aliases.
  private static final Map<String, String> AGREEMENT = Map.ofEntries(
    Map.entry("agreementNo", "agreement_code"), Map.entry("agreementCode", "agreement_code"),
    Map.entry("custName", "ct.cust_name"), Map.entry("bouwheerName", "bw.bouwheer_name"),
    Map.entry("financingAmt", "financing_amt"), Map.entry("disburseDate", "fh.disburse_date"),
    Map.entry("disburseAmt", "fh.disburse_amt"), Map.entry("currency", "currency"),
    Map.entry("bankName", "agreement_id"), Map.entry("rekeningNo", "agreement_id")
  );

  private static final Map<String, String> ASSIGNMENT = Map.ofEntries(
    Map.entry("financingHdrCode", "financing_hdr_code"), Map.entry("custName", "c.cust_name"),
    Map.entry("bouwheerName", "bw.bouwheer_name"), Map.entry("dueDate", "financing_due_date"),
    Map.entry("financingAmount", "financing_amt"), Map.entry("status", "financing_status"),
    Map.entry("dtmCrt", "dtm_crt"), Map.entry("financingDate", "financing_date")
  );

  private static final Map<String, String> TOC = Map.ofEntries(
    Map.entry("simulationHistCode", "simulationHistCode"),
    Map.entry("financingAmt", "financingAmt"), Map.entry("schema", "schema"),
    Map.entry("effectiveRate", "effetiveRate"), Map.entry("effetiveRate", "effetiveRate"),
    Map.entry("adminFee", "adminFee"),
    Map.entry("disbursementAmt", "dibursmentAmt"), Map.entry("dibursmentAmt", "dibursmentAmt")
  );

  public static Sort invoices(PaginationRequest request) {
    return resolve(request, INVOICE, Sort.by("financingDtlCode"), "financingDtlCode");
  }

  private static final Map<String, String> PAID_INVOICE = Map.ofEntries(
    Map.entry("invoiceNo", "custInvNo"), Map.entry("custName", "customer.custName"),
    Map.entry("bouwheerName", "bouwheer.bouwheerName"), Map.entry("paidDate", "invoiceDate"),
    Map.entry("dueDate", "invoiceDueDate"), Map.entry("paidAmount", "invoiceAmt")
  );

  private static final Map<String, String> DISBURSEMENT = Map.ofEntries(
    Map.entry("agreementNo", "agreementCode"),
    Map.entry("custName", "financingHdr.customer.custName"),
    Map.entry("bouwheerName", "financingHdr.bouwheer.bouwheerName"),
    Map.entry("disburseDate", "financingHdr.disburseDate"),
    Map.entry("paidDate", "financingHdr.disburseDate"),
    Map.entry("disburseAmount", "financingHdr.disburseAmt")
  );

  public static Sort paidInvoices(PaginationRequest request) {
    return resolve(request, PAID_INVOICE, Sort.by("invoiceCode"), "invoiceCode");
  }

  public static Sort disbursements(PaginationRequest request) {
    return resolve(request, DISBURSEMENT, Sort.by("agreementCode"), "agreementCode");
  }

  private static final Map<String, String> DISTRIBUTION = Map.ofEntries(
    Map.entry("financingHdrCode", "financingHdrCode"), Map.entry("custName", "custName"),
    Map.entry("NamaDebitur", "custName"), Map.entry("bouwheerName", "bouwheerName"),
    Map.entry("PemberiKerja", "bouwheerName"), Map.entry("dtmCrt", "dtmCrt"),
    Map.entry("dueDate", "dueDate"), Map.entry("financingAmount", "financingAmount"),
    Map.entry("branchRecommended", "branchRecommended"), Map.entry("Cabang", "branchRecommended"),
    Map.entry("currentBranch", "currentBranch"), Map.entry("custStatus", "custStatus"),
    Map.entry("status", "status.status"), Map.entry("Status", "status.status"),
    Map.entry("city", "city"), Map.entry("ao", "ao")
  );

  @SuppressWarnings("unchecked")
  public static Comparator<DistributionSubmissionDto> distributionComparator(PaginationRequest request) {
    Sort.Direction direction = direction(request);
    if (isBlank(request.getSortBy())) return null;
    String property = property(request, DISTRIBUTION);
    Comparator<Object> values = (left, right) -> ((Comparable<Object>) left).compareTo(right);
    if (direction == Sort.Direction.DESC) values = values.reversed();
    Comparator<DistributionSubmissionDto> comparator = Comparator.comparing(
      dto -> property.equals("status.status")
        ? (dto.getStatus() == null ? null : dto.getStatus().getStatus())
        : new BeanWrapperImpl(dto).getPropertyValue(property), Comparator.nullsLast(values));
    return comparator.thenComparing(DistributionSubmissionDto::getFinancingHdrCode,
      Comparator.nullsLast(Comparator.naturalOrder()));
  }

  private static final Map<String, String> PROYEKSI = Map.ofEntries(
    Map.entry("debtorName", "debtorName"), Map.entry("NamaDebitur", "debtorName"),
    Map.entry("debtorStatus", "debtorStatus"), Map.entry("StatusDebitur", "debtorStatus"),
    Map.entry("bouwheerName", "bouwheerName"), Map.entry("PemberiKerja", "bouwheerName"),
    Map.entry("invoiceNo", "invoiceNo"), Map.entry("NoInvoice", "invoiceNo"),
    Map.entry("amountInvoice", "amountInvoice"), Map.entry("NilaiInvoice", "amountInvoice"),
    Map.entry("amountFinancing", "amountFinancing"), Map.entry("NilaiPembiayaan", "amountFinancing"),
    Map.entry("invoiceDueDate", "invoiceDueDate"), Map.entry("JatuhTempo", "invoiceDueDate"),
    Map.entry("effectiveDate", "effectiveDate"), Map.entry("TanggalEfektif", "effectiveDate")
  );

  public static Comparator<ProyeksiReportDto> proyeksiComparator(PaginationRequest request) {
    return dtoComparator(request, PROYEKSI, ProyeksiReportDto::getInvoiceNo);
  }

  public static Sort agreements(PaginationRequest request) {
    if (!isBlank(request.getSortBy())
        && (request.getSortBy().trim().equals("bankName") || request.getSortBy().trim().equals("rekeningNo"))) {
      direction(request);
      // Both displayed values come from one CSUL bank setting, so all rows tie.
      return Sort.by("agreement_id");
    }
    return resolve(request, AGREEMENT, Sort.by(Sort.Direction.DESC, "fh.disburse_date"), "agreement_id");
  }

  public static Sort assignments(PaginationRequest request) {
    if (!isBlank(request.getSortBy()) && request.getSortBy().trim().equals("verifDate")) {
      direction(request);
      return Sort.by("financing_hdr_id");
    }
    return resolve(request, ASSIGNMENT, Sort.by(Sort.Direction.DESC, "dtm_crt"), "financing_hdr_id");
  }

  public static Comparator<AssignmentDto> assignmentVerifDateComparator(PaginationRequest request) {
    if (isBlank(request.getSortBy()) || !request.getSortBy().trim().equals("verifDate")) return null;
    Sort.Direction direction = direction(request);
    Comparator<LocalDateTime> dates = direction == Sort.Direction.DESC
      ? Comparator.reverseOrder() : Comparator.naturalOrder();
    return Comparator.comparing(AssignmentDto::getVerifDate, Comparator.nullsLast(dates))
      .thenComparing(AssignmentDto::getFinancingHdrCode, Comparator.nullsLast(Comparator.naturalOrder()));
  }

  public static Comparator<SimulationHistDto> tocComparator(PaginationRequest request) {
    return dtoComparator(request, TOC, SimulationHistDto::getSimulationHistCode);
  }

  @SuppressWarnings("unchecked")
  public static Comparator<CwrListDto> cwrComparator(PaginationRequest request) {
    Sort.Direction direction = direction(request);
    if (isBlank(request.getSortBy())) return null;
    String property = property(request, CWR);
    Comparator<Object> values = (left, right) -> ((Comparable<Object>) left).compareTo(right);
    if (direction == Sort.Direction.DESC) values = values.reversed();
    Comparator<CwrListDto> comparator = Comparator.comparing(
      dto -> new BeanWrapperImpl(dto).getPropertyValue(property), Comparator.nullsLast(values));
    return comparator.thenComparing(CwrListDto::getCwrCode, Comparator.nullsLast(Comparator.naturalOrder()));
  }

  private static Sort resolve(PaginationRequest request, Map<String, String> fields, Sort defaultSort, String tieBreaker) {
    Sort.Direction direction = direction(request);
    Sort sort = isBlank(request.getSortBy()) ? defaultSort : Sort.by(direction, property(request, fields));
    return sort.getOrderFor(tieBreaker) == null ? sort.and(Sort.by(tieBreaker)) : sort;
  }

  @SuppressWarnings("unchecked")
  private static <T> Comparator<T> dtoComparator(
    PaginationRequest request, Map<String, String> fields,
    java.util.function.Function<T, ? extends Comparable<?>> tieBreaker
  ) {
    Sort.Direction direction = direction(request);
    if (isBlank(request.getSortBy())) return null;
    String property = property(request, fields);
    Comparator<Object> values = (left, right) -> ((Comparable<Object>) left).compareTo(right);
    if (direction == Sort.Direction.DESC) values = values.reversed();
    Comparator<T> comparator = Comparator.comparing(
      dto -> new BeanWrapperImpl(dto).getPropertyValue(property), Comparator.nullsLast(values));
    Comparator<T> ties = (left, right) -> {
      Comparable<Object> leftValue = (Comparable<Object>) tieBreaker.apply(left);
      Object rightValue = tieBreaker.apply(right);
      if (leftValue == null) return rightValue == null ? 0 : 1;
      return rightValue == null ? -1 : leftValue.compareTo(rightValue);
    };
    return comparator.thenComparing(ties);
  }

  private static String property(PaginationRequest request, Map<String, String> fields) {
    String property = fields.get(request.getSortBy().trim());
    if (property == null) {
      throw invalid("sortBy tidak didukung: " + request.getSortBy() + ". Pilihan: " + String.join(", ", fields.keySet()));
    }
    return property;
  }

  private static Sort.Direction direction(PaginationRequest request) {
    if (isBlank(request.getSortType())) return Sort.Direction.ASC;
    if (isBlank(request.getSortBy())) throw invalid("sortBy wajib diisi jika sortType digunakan.");
    try {
      return Sort.Direction.fromString(request.getSortType().trim());
    } catch (IllegalArgumentException exception) {
      throw invalid("sortType harus asc atau desc.");
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static BusinessException invalid(String message) {
    return new BusinessException(HttpStatus.BAD_REQUEST, 400, message);
  }
}
