package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.CustomerCreditFacilityDueDateDto;
import com.kmkbe.core.domain.dto.CustomerCreditFacilityNewDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.utils.PaginationRequests;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.helpers.utils.Utils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerDashboardListService {
  private static final Set<String> CREDIT_FACILITY_FIELDS = Set.of(
    "invoiceno", "agreementcode", "financinghdrcode", "custname", "namadebitur",
    "bouwheername", "pemberikerja", "city", "duedate", "financingamount",
    "branchrecommended", "currentbranch", "custstatus", "status", "dtmcrt"
  );
  private static final Set<String> DUE_DATE_FIELDS = Set.of(
    "invoiceno", "agreementcode", "ponumber", "postingdate", "financinghdrcode",
    "custname", "namadebitur", "bouwheername", "pemberikerja", "city", "duedate",
    "financingamount", "branchrecommended", "currentbranch", "custstatus", "status", "dtmcrt"
  );
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final FinancingHdrRepository financingHdrRepository;
  private final InvoiceRepository invoiceRepository;
  private final FinancingDtlRepository financingDtlRepository;
  private final MstBranchRepository mstBranchRepository;

  private final AgreementRepository agreementRepository;

  public PaginationResult<CustomerCreditFacilityNewDto> listcreditfacilities(
    Customer customer,
    BasePaginationRequest request
  ) throws SignatureException {
    PaginationRequest paginationRequest = PaginationRequests.from(request);
    Page<FinancingHdr> financingPage = financingHdrRepository.findAllByRawOrder(
      customer.getCustCode().toString(), Pageable.unpaged()
    );
    List<CustomerCreditFacilityNewDto> facilities = financingPage.getContent().stream()
      .map(this::toCreditFacilityDto)
      .toList();

    return filterSortAndPaginate(
      facilities,
      paginationRequest,
      CREDIT_FACILITY_FIELDS,
      this::creditFacilityField,
      CustomerCreditFacilityNewDto::getFinancingHdrCode
    );
  }

  public PaginationResult<CustomerCreditFacilityNewDto> listcreditfacilities(
    Customer customer,
    PaginationRequest request
  ) throws SignatureException {
    try {
      int pageNo = 0, pageSize = 10;

      if (request.getPageNo() != null) {
        pageNo = request.getPageNo();
      }

      if (request.getPageSize() != null) {
        pageSize = request.getPageSize();
      }

      if (pageNo > 0) {
        pageNo = pageNo - 1;
      }

      final Page<FinancingHdr> paginationFinancing = financingHdrRepository.findAllByRawOrder(
        customer.getCustCode().toString(), PageRequest.of(pageNo, pageSize)

      );

      //DateTimeUtils.now()

      final List<CustomerCreditFacilityNewDto> list = paginationFinancing.getContent()
        .stream()
        .map(this::toCreditFacilityDto)
        .toList();

      return PaginationResult.<CustomerCreditFacilityNewDto>builder()
        .currentPage(pageNo + 1)
        .totalData(paginationFinancing.getTotalElements())
        .totalPage(paginationFinancing.getTotalPages())
        .list(list)
        .build();
    } catch (Exception e) {
      log.error("submissionDistribution: error {}", e.getMessage());
      throw e;
    }
  }

  String invoiceNumbers(List<FinancingDtl> financingDetails) {
    if (financingDetails == null) {
      return "";
    }

    return financingDetails.stream()
      .map(FinancingDtl::getInvoice)
      .filter(invoice -> invoice != null && invoice.getCustInvNo() != null)
      .map(invoice -> invoice.getCustInvNo().trim())
      .filter(invoiceNo -> !invoiceNo.isEmpty())
      .distinct()
      .sorted()
      .collect(Collectors.joining(", "));
  }

  public PaginationResult<CustomerCreditFacilityDueDateDto> listinvoicesduedate(
    Customer customer,
    BasePaginationRequest request
  ) throws SignatureException {
    PaginationRequest paginationRequest = PaginationRequests.from(request);
    Page<FinancingDtl> financingPage = financingDtlRepository.findByCustomer(
      customer.getCustCode().toString(), Pageable.unpaged()
    );
    List<CustomerCreditFacilityDueDateDto> invoices = financingPage.getContent().stream()
      .map(this::toDueDateDto)
      .toList();

    return filterSortAndPaginate(
      invoices,
      paginationRequest,
      DUE_DATE_FIELDS,
      this::dueDateField,
      CustomerCreditFacilityDueDateDto::getFinancingHdrCode
    );
  }


  public PaginationResult<CustomerCreditFacilityDueDateDto> listinvoicesduedate(
    Customer customer,
    PaginationRequest request
  ) throws SignatureException {
        /*
        status samapi paid
        nama debitur diganti PO
        tgl ver digato postdate
         */

    try {
      int pageNo = 0, pageSize = 10;

      if (request.getPageNo() != null) {
        pageNo = request.getPageNo();
      }

      if (request.getPageSize() != null) {
        pageSize = request.getPageSize();
      }

      if (pageNo > 0) {
        pageNo = pageNo - 1;
      }

      final Page<FinancingDtl> financingDtls = financingDtlRepository.findByCustomer(customer.getCustCode().toString(), PageRequest.of(pageNo, pageSize));

      final List<CustomerCreditFacilityDueDateDto> list = financingDtls.getContent()
        .stream()
        .map(this::toDueDateDto)
        .toList();

      return PaginationResult.<CustomerCreditFacilityDueDateDto>builder()
        .currentPage(pageNo + 1)
        .totalData(financingDtls.getTotalElements())
        .totalPage(financingDtls.getTotalPages())
        .list(list)
        .build();
    } catch (Exception e) {
      log.error("submissionDistribution: error {}", e.getMessage());
      throw e;
    }
  }

  private CustomerCreditFacilityNewDto toCreditFacilityDto(FinancingHdr financing) {
    CustomerLocation location = customerLocation(financing);
    BranchInfo branch = branchInfo(financing, location);
    boolean isNewCustomer = financingHdrRepository.countByCustomerAndFinancingStatus(
      financing.getCustomer(), "PAID"
    ) == 0;
    MappedFinancingStatus mappedStatus = new MappedFinancingStatus(
      financing, MappedFinancingStatus.Type.Customer
    );

    return CustomerCreditFacilityNewDto.builder()
      .agreementCode(financing.getAgreement().isEmpty()
        ? "" : financing.getAgreement().stream().toList().getLast().getAgreementCode())
      .invoiceNo(invoiceNumbers(financingDtlRepository.findAllByFinancingHdrOrderByDtmCrtDesc(financing)))
      .financingHdrCode(financing.getFinancingHdrCode().toString())
      .custName(financing.getCustomer().getCustName())
      .bouwheerName(financing.getBouwheer().getBouwheerName())
      .city(location.city())
      .dueDate(Utils.fromInstant(financing.getFinancingDueDate()))
      .financingAmount(BigDecimal.valueOf(financing.getFinancingAmt()))
      .branchRecommendedCode(branch.recommendedCode())
      .branchRecommended(branch.recommendedName())
      .currentBranchCode(branch.currentCode())
      .currentBranch(branch.currentName())
      .custStatus(isNewCustomer ? "New Customer" : "Existing Customer")
      .status(StatusLabelDto.builder()
        .status(mappedStatus.getStatus())
        .statusLabel(mappedStatus.getLabel())
        .color(creditFacilityColor(financing.getFinancingStatus()))
        .build())
      .dtmCrt(financing.getDtmCrt())
      .build();
  }

  private CustomerCreditFacilityDueDateDto toDueDateDto(FinancingDtl financingDetail) {
    FinancingHdr financing = financingDetail.getFinancingHdr();
    CustomerLocation location = customerLocation(financing);
    BranchInfo branch = branchInfo(financing, location);
    boolean isNewCustomer = financingHdrRepository.countByCustomerAndFinancingStatus(
      financing.getCustomer(), "PAID"
    ) == 0;
    MappedFinancingStatus mappedStatus = new MappedFinancingStatus(
      financing, MappedFinancingStatus.Type.MajorAccount
    );

    return CustomerCreditFacilityDueDateDto.builder()
      .financingHdrCode(financing.getFinancingHdrCode().toString())
      .agreementCode(financing.getAgreement().isEmpty()
        ? "" : financing.getAgreement().stream().toList().getLast().getAgreementCode())
      .invoiceNo(financingDetail.getInvoice().getCustInvNo())
      .poNumber(financingDetail.getInvoice().getPoNumber())
      .postingDate(financingDetail.getInvoice().getPostingDate())
      .custName(financing.getCustomer().getCustName())
      .bouwheerName(financing.getBouwheer().getBouwheerName())
      .city(location.city())
      .dueDate(Utils.fromInstant(financing.getFinancingDueDate()))
      .financingAmount(BigDecimal.valueOf(financing.getFinancingAmt()))
      .branchRecommendedCode(branch.recommendedCode())
      .branchRecommended(branch.recommendedName())
      .currentBranchCode(branch.currentCode())
      .currentBranch(branch.currentName())
      .custStatus(isNewCustomer ? "New Customer" : "Existing Customer")
      .status(StatusLabelDto.builder()
        .status(mappedStatus.getStatus())
        .statusLabel(mappedStatus.getLabel())
        .color(dueDateColor(financing.getFinancingStatus()))
        .build())
      .dtmCrt(financingDetail.getDtmCrt())
      .build();
  }

  private CustomerLocation customerLocation(FinancingHdr financing) {
    String city = "";
    String kelurahan = "";
    String kecamatan = "";
    Customer customer = financing.getCustomer();
    if (customer != null && "company".equalsIgnoreCase(customer.getCustTypeCode()) && customer.getCompany() != null) {
      city = customer.getCompany().getCity();
      kelurahan = customer.getCompany().getKelurahan();
      kecamatan = customer.getCompany().getKecamatan();
    } else if (customer != null && customer.getPersonal() != null) {
      city = customer.getPersonal().getCity();
      kelurahan = customer.getPersonal().getKelurahan();
      kecamatan = customer.getPersonal().getKecamatan();
    }
    return new CustomerLocation(city, kelurahan, kecamatan);
  }

  private BranchInfo branchInfo(FinancingHdr financing, CustomerLocation location) {
    if (financing.getMstBranch() != null) {
      String code = financing.getMstBranch().getBranchCode();
      String name = financing.getMstBranch().getBranchName();
      return new BranchInfo(code, name, code, name);
    }

    if (!StringUtil.isNullOrEmpty(location.city())
      && !StringUtil.isNullOrEmpty(location.kelurahan())
      && !StringUtil.isNullOrEmpty(location.kecamatan())) {
      Optional<MstBranch> branch = mstBranchRepository.findTopLikeBranchNameRawQuery(
        location.city(), location.kelurahan(), location.kecamatan()
      );
      if (branch.isPresent()) {
        return new BranchInfo(branch.get().getBranchCode(), branch.get().getBranchName(), null, null);
      }
    }
    return new BranchInfo(null, null, null, null);
  }

  private String creditFacilityColor(String status) {
    if ("new".equalsIgnoreCase(status) || "live".equalsIgnoreCase(status)) return "#808080";
    if ("inprocess".equalsIgnoreCase(status)) return "#FF5C5C";
    return "#ccffcc";
  }

  private String dueDateColor(String status) {
    if ("new".equalsIgnoreCase(status)) return "#808080";
    if ("inprocess".equalsIgnoreCase(status)
      || "signing".equalsIgnoreCase(status)
      || "signed".equalsIgnoreCase(status)
      || "live".equalsIgnoreCase(status)
      || "golive".equalsIgnoreCase(status)) return "#ccffcc";
    return "#FF5C5C";
  }

  private Comparable<?> creditFacilityField(CustomerCreditFacilityNewDto dto, String field) {
    return switch (normalizeField(field)) {
      case "invoiceno" -> dto.getInvoiceNo();
      case "agreementcode" -> dto.getAgreementCode();
      case "financinghdrcode" -> dto.getFinancingHdrCode();
      case "custname", "namadebitur" -> dto.getCustName();
      case "bouwheername", "pemberikerja" -> dto.getBouwheerName();
      case "city" -> dto.getCity();
      case "duedate" -> dto.getDueDate();
      case "financingamount" -> dto.getFinancingAmount();
      case "branchrecommended" -> dto.getBranchRecommended();
      case "currentbranch" -> dto.getCurrentBranch();
      case "custstatus" -> dto.getCustStatus();
      case "status" -> statusText(dto.getStatus());
      case "dtmcrt" -> dto.getDtmCrt();
      default -> null;
    };
  }

  private Comparable<?> dueDateField(CustomerCreditFacilityDueDateDto dto, String field) {
    return switch (normalizeField(field)) {
      case "invoiceno" -> dto.getInvoiceNo();
      case "agreementcode" -> dto.getAgreementCode();
      case "ponumber" -> dto.getPoNumber();
      case "postingdate" -> dto.getPostingDate();
      case "financinghdrcode" -> dto.getFinancingHdrCode();
      case "custname", "namadebitur" -> dto.getCustName();
      case "bouwheername", "pemberikerja" -> dto.getBouwheerName();
      case "city" -> dto.getCity();
      case "duedate" -> dto.getDueDate();
      case "financingamount" -> dto.getFinancingAmount();
      case "branchrecommended" -> dto.getBranchRecommended();
      case "currentbranch" -> dto.getCurrentBranch();
      case "custstatus" -> dto.getCustStatus();
      case "status" -> statusText(dto.getStatus());
      case "dtmcrt" -> dto.getDtmCrt();
      default -> null;
    };
  }

  private String statusText(StatusLabelDto status) {
    if (status == null) return null;
    return String.join(" ", Optional.ofNullable(status.getStatus()).orElse(""),
      Optional.ofNullable(status.getStatusLabel()).orElse("")).trim();
  }

  private <T> PaginationResult<T> filterSortAndPaginate(
    List<T> source,
    PaginationRequest request,
    Set<String> supportedFields,
    FieldAccessor<T> accessor,
    Function<T, String> tieBreaker
  ) {
    String searchBy = normalizeField(request.getSearchBy());
    String sortBy = normalizeField(request.getSortBy());
    validateField("searchBy", searchBy, request.getSearchValue(), supportedFields);
    validateField("sortBy", sortBy, request.getSortBy(), supportedFields);
    if (!isBlank(request.getSortType()) && isBlank(sortBy)) {
      throw invalid("sortBy wajib diisi jika sortType digunakan.");
    }

    List<T> result = source;
    if (!isBlank(request.getSearchValue())) {
      String searchValue = request.getSearchValue().trim().toLowerCase(Locale.ROOT);
      result = result.stream()
        .filter(item -> searchableText(accessor.get(item, searchBy)).contains(searchValue))
        .toList();
    }

    if (!isBlank(sortBy)) {
      boolean descending;
      if (isBlank(request.getSortType()) || "asc".equalsIgnoreCase(request.getSortType())) {
        descending = false;
      } else if ("desc".equalsIgnoreCase(request.getSortType())) {
        descending = true;
      } else {
        throw invalid("sortType harus asc atau desc.");
      }
      Comparator<T> comparator = (left, right) -> compareValues(
        accessor.get(left, sortBy), accessor.get(right, sortBy), descending
      );
      comparator = comparator.thenComparing(tieBreaker, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
      result = result.stream().sorted(comparator).toList();
    }

    int pageNo = request.getPageNo() == null ? 1 : request.getPageNo();
    int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
    int totalData = result.size();
    int totalPage = (int) Math.ceil((double) totalData / pageSize);
    int fromIndex = Math.min((pageNo - 1) * pageSize, totalData);
    int toIndex = Math.min(fromIndex + pageSize, totalData);

    return PaginationResult.<T>builder()
      .currentPage(pageNo)
      .totalData((long) totalData)
      .totalPage(totalPage)
      .list(result.subList(fromIndex, toIndex))
      .build();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private int compareValues(Comparable left, Comparable right, boolean descending) {
    if (left == null) return right == null ? 0 : 1;
    if (right == null) return -1;
    int comparison;
    if (left instanceof String leftString && right instanceof String rightString) {
      comparison = leftString.compareToIgnoreCase(rightString);
    } else {
      comparison = left.compareTo(right);
    }
    return descending ? -comparison : comparison;
  }

  private String searchableText(Object value) {
    if (value == null) return "";
    if (value instanceof Date date) {
      return (new SimpleDateFormat("dd/MM/yyyy").format(date) + " " + date).toLowerCase(Locale.ROOT);
    }
    if (value instanceof LocalDateTime dateTime) {
      return (DATE_TIME_FORMATTER.format(dateTime) + " " + dateTime).toLowerCase(Locale.ROOT);
    }
    return value.toString().toLowerCase(Locale.ROOT);
  }

  private void validateField(String parameter, String field, String suppliedValue, Set<String> supportedFields) {
    if (isBlank(suppliedValue)) return;
    if (isBlank(field) || !supportedFields.contains(field)) {
      throw invalid(parameter + " tidak didukung: " + suppliedValue + ". Pilihan: "
        + String.join(", ", supportedFields));
    }
  }

  private String normalizeField(String field) {
    return isBlank(field) ? null : field.trim().toLowerCase(Locale.ROOT);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private BusinessException invalid(String message) {
    return new BusinessException(HttpStatus.BAD_REQUEST, 400, message);
  }

  @FunctionalInterface
  private interface FieldAccessor<T> {
    Comparable<?> get(T item, String field);
  }

  private record CustomerLocation(String city, String kelurahan, String kecamatan) {}

  private record BranchInfo(
    String recommendedCode,
    String recommendedName,
    String currentCode,
    String currentName
  ) {}

}
