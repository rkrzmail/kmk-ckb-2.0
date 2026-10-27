package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.dto.email.MailDataDto;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.*;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.major_account.request.DistributionSubmissionListRequest;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.helpers.utils.SpecPagination;
import com.kmkbe.helpers.utils.PaginationSort;
import com.kmkbe.helpers.utils.PaginationRequests;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.utils.Utils;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.LinkedHashSet;
import java.util.Set;


@Slf4j
@Service
public class DistributionSubmissionService {
  private final FinancingHdrRepository financingHdrRepository;
  private final EmailService emailService;
  private final MstBranchRepository mstBranchRepository;
  private final BranchAreaMappingRepository branchAreaMappingRepository;
  private final ConfigRemoteService configRemoteService;
  private final CurrentUserService currentUserService;
  private final AuditTrailService auditTrailService;


  public DistributionSubmissionService(FinancingHdrRepository financingHdrRepository,
                                       EmailService emailService,
                                       MstBranchRepository mstBranchRepository,
                                       BranchAreaMappingRepository branchAreaMappingRepository,
                                       ConfigRemoteService configRemoteService,
                                       CustomerRepository customerRepository,
                                       CurrentUserService currentUserService,
                                       AuditTrailService auditTrailService) {
    this.financingHdrRepository = financingHdrRepository;
    this.emailService = emailService;
    this.mstBranchRepository = mstBranchRepository;
    this.branchAreaMappingRepository = branchAreaMappingRepository;
    this.configRemoteService = configRemoteService;
    this.currentUserService = currentUserService;
    this.auditTrailService = auditTrailService;
  }

  public PaginationResult<DistributionSubmissionDto> submissionDistribution(PaginationRequest request) {
    return submissionDistribution(request, null, null);
  }

  private PaginationResult<DistributionSubmissionDto> submissionDistribution(
    PaginationRequest request,
    java.time.LocalDate startDate,
    java.time.LocalDate endDate
  ) {
    try {
      var comparator = PaginationSort.distributionComparator(request);
      List<FinancingHdr> finHdrAll = financingHdrRepository.findAllForDistribution().stream()
        .filter(financingHdr -> isWithinDateRange(financingHdr, startDate, endDate))
        .toList();
      return SpecPagination.paginationData(new SpecPagination<FinancingHdr, DistributionSubmissionDto>(finHdrAll, request) {
        @Override
        public void sort(List<DistributionSubmissionDto> data) {
          if (comparator != null) data.sort(comparator);
        }

        @Override
        public DistributionSubmissionDto eval(FinancingHdr e) {
          Customer customer = safeCustomer(e);
          Bouwheer bouwheer = safeBouwheer(e);

          String city = "";
          String address = "";
          if (customer != null && customer.getCustTypeCode() != null) {
            if (customer.getCustTypeCode().equalsIgnoreCase("company")) {
              if (customer.getCompany() != null) {
                city = customer.getCompany().getCity();
                address = customer.getCompany().getCompanyAddress();
              }
            } else {
              if (customer.getPersonal() != null) {
                city = customer.getPersonal().getCity();
                address = customer.getPersonal().getLegalAddress();
              }
            }
          }

          String color;
          String currentBranch = null;
          String currentBranchCode = null;
          String branchRecommended = null;
          String branchRecommendedCode = null;
          if (e.getFinancingStatus().equalsIgnoreCase("new")) {
            color = "#808080";
          } else if (
            e.getFinancingStatus().equalsIgnoreCase("inprocess")
              || e.getFinancingStatus().equalsIgnoreCase("signing")
              || e.getFinancingStatus().equalsIgnoreCase("signed")
              || e.getFinancingStatus().equalsIgnoreCase("live")
              || e.getFinancingStatus().equalsIgnoreCase("golive")
              || e.getFinancingStatus().equalsIgnoreCase("paid")

          ) {
            color = "#ccffcc";
          } else {
            color = "#FF5C5C";
          }

          MstBranch mstBranch = safeMstBranch(e);
          if (mstBranch != null) {
            branchRecommendedCode = mstBranch.getBranchCode();
            branchRecommended = mstBranch.getBranchName();
            currentBranchCode = mstBranch.getBranchCode();
            currentBranch = mstBranch.getBranchName();

          }
          if (branchRecommendedCode == null) {

            Optional<BranchAreaMapping> branchAreaMapping = branchAreaMappingRepository.findByCityIgnoreCase(Utils.valueOf(city));
            if (branchAreaMapping.isPresent()) {
              branchRecommendedCode = branchAreaMapping.get().getMstBranch().getBranchCode();
              branchRecommended = branchAreaMapping.get().getMstBranch().getBranchName();
            }
          }
          MappedFinancingStatus mappedFinancingStatus = new MappedFinancingStatus(
            e,
            MappedFinancingStatus.Type.MajorAccount
          );

          return DistributionSubmissionDto.builder()
            .financingHdrCode(e.getFinancingHdrCode().toString())
            .custName(customer != null ? customer.getCustName() : null)
            .bouwheerName(bouwheer != null ? bouwheer.getBouwheerName() : null)
            .city(city)
            .dueDate(Utils.fromInstant(e.getFinancingDueDate()))
            .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
            .totalInvoiceAmount(e.getTotalInvoiceAmt() == null ? null : BigDecimal.valueOf(e.getTotalInvoiceAmt()))
            .invoiceQty(e.getInvoiceQty())
            .branchRecommendedCode(branchRecommendedCode)
            .branchRecommended(branchRecommended)
            .currentBranchCode(currentBranchCode)
            .currentBranch(currentBranch)
            .status(StatusLabelDto.builder()
              .status(mappedFinancingStatus.getStatus())
              .statusLabel(mappedFinancingStatus.getLabel())
              .color(color)
              .build())
            .custStatus(customer != null ? customer.getExistingCust() : null)
            .npwp(customer != null ? customer.getNpwp() : null)
            .address(address)
            .ao(e.getAssignedAoName() != null ? e.getAssignedAoName() : Optional.ofNullable(mstBranch)
              .map(MstBranch::getEmployees)
              .map(Collection::stream)
              .flatMap(stream -> stream.map(MstEmployee::getEmployeeName).findFirst())
              .orElse(null))
            .aoEmployeeCode(e.getAssignedAoEmployeeCode())
            .dtmCrt(e.getDtmCrt())
            .updatedAt(e.getDtmUpd())
            .build();
        }

        @Override
        public DistributionSubmissionDto filter(DistributionSubmissionDto data) {

          if (isSearchBy("Status") && like(data.getStatus().getStatus())) {
            return data;
          } else if (isSearchBy("NamaDebitur") && like(data.getCustName())) {
            return data;
          } else if (isSearchBy("PemberiKerja") && like(data.getBouwheerName())) {
            return data;
          } else if (isSearchBy("Cabang") && like(data.getBranchRecommended())) {
            return data;
          }


          return null;
        }
      });

    } catch (Exception e) {
      log.error("submissionDistribution: error {}", e.getMessage());
      throw e;
    }
  }

  private Customer safeCustomer(FinancingHdr financingHdr) {
    try {
      Customer customer = financingHdr.getCustomer();
      if (customer != null) {
        customer.getCustName();
      }
      return customer;
    } catch (EntityNotFoundException e) {
      log.warn(
        "submissionDistribution: customer reference not found for financingHdrCode {}",
        financingHdr.getFinancingHdrCode()
      );
      return null;
    }
  }

  private Bouwheer safeBouwheer(FinancingHdr financingHdr) {
    try {
      Bouwheer bouwheer = financingHdr.getBouwheer();
      if (bouwheer != null) {
        bouwheer.getBouwheerName();
      }
      return bouwheer;
    } catch (EntityNotFoundException e) {
      log.warn(
        "submissionDistribution: bouwheer reference not found for financingHdrCode {}",
        financingHdr.getFinancingHdrCode()
      );
      return null;
    }
  }

  private MstBranch safeMstBranch(FinancingHdr financingHdr) {
    try {
      MstBranch mstBranch = financingHdr.getMstBranch();
      if (mstBranch != null) {
        mstBranch.getBranchCode();
      }
      return mstBranch;
    } catch (EntityNotFoundException e) {
      log.warn(
        "submissionDistribution: branch reference not found for financingHdrCode {}",
        financingHdr.getFinancingHdrCode()
      );
      return null;
    }
  }

  public PaginationResult<DistributionSubmissionDto> submissionDistribution(
    BasePaginationRequest request) {
    if (request instanceof DistributionSubmissionListRequest datedRequest) {
      return submissionDistribution(
        PaginationRequests.from(request),
        datedRequest.getStartDate(),
        datedRequest.getEndDate()
      );
    }
    return submissionDistribution(PaginationRequests.from(request), null, null);
  }

  private boolean isWithinDateRange(
    FinancingHdr financingHdr,
    java.time.LocalDate startDate,
    java.time.LocalDate endDate
  ) {
    if (startDate == null && endDate == null) return true;
    if (financingHdr.getDtmCrt() == null) return false;

    java.time.LocalDate submissionDate = financingHdr.getDtmCrt().toLocalDate();
    return (startDate == null || !submissionDate.isBefore(startDate))
      && (endDate == null || !submissionDate.isAfter(endDate));
  }

  public List<MailDataDto> aoAmByBranch(String branchCode) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    MstBranch branch = mstBranchRepository.findByBranchCode(branchCode)
      .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 404, "Cabang tidak ditemukan."));
    if (!Boolean.TRUE.equals(branch.getIsActive()) || !"CBU".equalsIgnoreCase(branch.getBusinessUnit())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "Cabang tidak aktif atau bukan CBU.");
    }
    return positionEmployees(branchCode, "AO/AM").stream()
      .filter(employee -> employee.getEmployeeCode() != null && !employee.getEmployeeCode().isBlank())
      .filter(employee -> employee.getEmployeeName() != null && !employee.getEmployeeName().isBlank())
      .toList();
  }

  private List<MailDataDto> positionEmployees(String branchCode, String position) {
    MailPositionDto response = configRemoteService.getEmailByPosition("", branchCode, position);
    List<MailDataDto> employees = response == null || response.getData() == null ? List.of() : response.getData().stream()
      .filter(employee -> employee.getEmail() != null && !employee.getEmail().isBlank())
      .filter(employee -> employee.getBranchCode() == null || branchCode.equalsIgnoreCase(employee.getBranchCode()))
      .toList();
    if ("ASM".equals(position) && employees.isEmpty()) {
      log.warn("SISCA returned no eligible ASM for branchCode={}; response={}", branchCode, response);
    }
    return employees;
  }

  private List<String> positionEmails(String branchCode, String position) {
    List<String> emails = positionEmployees(branchCode, position).stream().map(MailDataDto::getEmail).toList();
    if (emails.isEmpty()) {
      throw new BusinessException(HttpStatus.CONFLICT, 409, "Email " + position + " tidak tersedia untuk cabang ini.");
    }
    return emails;
  }

  private String joinEmails(List<String> first, List<String> second) {
    Set<String> addresses = new LinkedHashSet<>();
    first.stream().filter(email -> email != null && !email.isBlank()).map(String::trim).forEach(addresses::add);
    second.stream().filter(email -> email != null && !email.isBlank()).map(String::trim).forEach(addresses::add);
    return String.join(";", addresses);
  }

  /**
   *
   * @param request
   * @return
   * @throws SignatureException
   */
  public BaseResponse assignSubmission(AssignInvoiceToBranchRequest request) throws SignatureException {
    final UUID financingHdrCode;

    financingHdrCode = UUID.fromString(request.getFinancingHdrCode());
    MstUser authenticateUser = currentUserService.internalUser();

    /**
     * Find branch
     */
    MstBranch mstBranch = mstBranchRepository.findByBranchCode(request.getBranchCode())
      .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Branch Not Found with given argument"));

    /**
     * Find financeing HDR
     */
    FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(financingHdrCode)
      .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Financing Not Found with given argument"));
    DistributionSubmissionAuditData before = toAuditData(financingHdr);

    /**
     * Validate staus
     */
    if (Utils.valueOf(financingHdr.getFinancingStep()).equalsIgnoreCase("PAID")) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", financingHdr.getFinancingStep());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 + "Financing step has been " + financingHdr.getFinancingStep());
    }

    if (Utils.valueOf(financingHdr.getFinancingStep()).equalsIgnoreCase("NEW")
      || Utils.valueOf(financingHdr.getFinancingStep()).equalsIgnoreCase("ASSIGNMENT")
      || Utils.valueOf(financingHdr.getFinancingStep()).equalsIgnoreCase("ASSIGN")) {
    } else {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", financingHdr.getFinancingStep());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 + "Financing step has been " + financingHdr.getFinancingStep());
    }

    MailDataDto selectedAo = aoAmByBranch(mstBranch.getBranchCode()).stream()
      .filter(employee -> employee.getEmployeeCode().equals(request.getEmployeeCode()))
      .findFirst()
      .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, 400,
        "AO/AM tidak tersedia pada cabang yang dipilih."));
    String toEmail = joinEmails(List.of(selectedAo.getEmail()),
      positionEmails(mstBranch.getBranchCode(), "BM/BOH"));
    String ccEmail = joinEmails(positionEmployees(mstBranch.getBranchCode(), "ASM").stream()
      .map(MailDataDto::getEmail).toList(), List.of());
    Set<String> toAddresses = new LinkedHashSet<>(List.of(toEmail.split(";")));
    ccEmail = String.join(";", java.util.Arrays.stream(ccEmail.split(";"))
      .filter(email -> !toAddresses.contains(email)).toList());

    /**
     * Assign process
     */
    financingHdr.setFinancingStatus("INPROCESS");
    financingHdr.setFinancingStep("ASSIGNMENT");
    financingHdr.setMstBranch(mstBranch);
    financingHdr.setAssignedAoEmployeeCode(selectedAo.getEmployeeCode());
    financingHdr.setAssignedAoName(selectedAo.getEmployeeName());
    financingHdr.setAssignedAoEmail(selectedAo.getEmail());
    financingHdr.setDtmUpd(DateTimeUtils.now());
    financingHdr.setUsrUpd(authenticateUser.getUsername());
    FinancingHdr savedFinancing = financingHdrRepository.save(financingHdr);

    auditTrailService.record(
      "DISTRIBUTION_SUBMISSION",
      AuditAction.UPDATE,
      "FinancingHdr",
      savedFinancing.getFinancingHdrCode(),
      before,
      toAuditData(savedFinancing)
    );

    {
      final List<InvoiceEmailPayload> invoices = financingHdr.getFinancingDtls()
        .stream()
        .map((item) ->
          InvoiceEmailPayload.builder()
            .invoiceNo(item.getInvoice().getCustInvNo())
            .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt()))
            .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
            .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
            .description(item.getInvoice().getInvoiceDescription())
            .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
            .build()
        ).toList();

      final double totalFeeAmt =
        financingHdr.getAdminFeeAmt()
          + financingHdr.getLegalFeeAmtNett()
          + financingHdr.getInsuranceFeeAmt()
          + financingHdr.getOthersFeeAmt()
          + financingHdr.getProvisionFeeAmt()
          + financingHdr.getSurveyFeeAmtNett();

      log.info("Final To Emails : {}", toEmail);
      log.info("Final Cc Emails : {}", ccEmail);

      String phone = financingHdr.getCustomer().getCustMobilePhone();
      if (financingHdr.getCustomer().getCustTypeCode().equalsIgnoreCase("Company")) {
        if (financingHdr.getCustomer().getCompany() != null) {
          phone = financingHdr.getCustomer().getCompany().getPhone();
        }
      }

      /**
       * Send email assign or reassign
       */
      emailService.sendNotificationBranchAssign(
        toEmail,
        financingHdr.getBouwheer().getBouwheerName(),
        mstBranch.getBranchName(),
        LoanDisburseEmailPayload.builder()
          .financingCode(financingHdr.getFinancingHdrCode().toString())
          .applicationDate(DateTimeUtils.formatToDate(financingHdr.getDtmCrt()))
          .companyName(financingHdr.getCustomer().getCustName())
          .email(financingHdr.getCustomer().getCustEmail())
          .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
          .phoneNumber(phone)
          .tenor(financingHdr.getTenor())
          .toEmail(toEmail)
          .ccEmail(ccEmail)
          .financingCode(financingHdr.getFinancingHdrCode().toString())
          .financingDueDate(DateTimeUtils.formatToDate(financingHdr.getFinancingDueDate()))
          .retention(CommonFormattingUtils.formatAmount(financingHdr.getRetention()))
          .financingAmt(CommonFormattingUtils.formatAmount(financingHdr.getFinancingAmt()))
          .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
          .invoiceAmt(CommonFormattingUtils.formatAmount(financingHdr.getTotalInvoiceAmt()))
          .disburseAmt(CommonFormattingUtils.formatAmount(financingHdr.getDisburseAmt()))
          .invoices(invoices)
          .build()
      );
    }

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }

  private DistributionSubmissionAuditData toAuditData(FinancingHdr financingHdr) {
    if (financingHdr == null) {
      return null;
    }

    var customer = financingHdr.getCustomer();
    var bouwheer = financingHdr.getBouwheer();
    MstBranch branch = financingHdr.getMstBranch();
    return new DistributionSubmissionAuditData(
      financingHdr.getFinancingHdrCode(),
      customer != null ? customer.getCustCode() : null,
      customer != null ? customer.getCustName() : null,
      customer != null ? customer.getCustEmail() : null,
      bouwheer != null ? bouwheer.getBouwheerCode() : null,
      bouwheer != null ? bouwheer.getBouwheerName() : null,
      branch != null ? branch.getBranchCode() : null,
      branch != null ? branch.getBranchName() : null,
      financingHdr.getAssignedAoEmployeeCode(),
      financingHdr.getAssignedAoName(),
      financingHdr.getAssignedAoEmail(),
      financingHdr.getFinancingStatus(),
      financingHdr.getFinancingStep(),
      financingHdr.getTotalInvoiceAmt(),
      financingHdr.getFinancingAmt(),
      financingHdr.getDisburseAmt(),
      financingHdr.getUsrUpd(),
      financingHdr.getDtmUpd()
    );
  }

  private record DistributionSubmissionAuditData(
    UUID financingHdrCode,
    UUID custCode,
    String custName,
    String custEmail,
    UUID bouwheerCode,
    String bouwheerName,
    String branchCode,
    String branchName,
    String assignedAoEmployeeCode,
    String assignedAoName,
    String assignedAoEmail,
    String financingStatus,
    String financingStep,
    Double totalInvoiceAmt,
    Double financingAmt,
    Double disburseAmt,
    String usrUpd,
    java.time.LocalDateTime dtmUpd
  ) {
  }
}
