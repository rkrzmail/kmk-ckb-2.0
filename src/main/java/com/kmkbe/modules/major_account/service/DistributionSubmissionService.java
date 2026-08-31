package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
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
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.helpers.utils.SpecPagination;
import com.kmkbe.helpers.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
public class DistributionSubmissionService {
  private final FinancingHdrRepository financingHdrRepository;
  private final EmailService emailService;
  private final MstBranchRepository mstBranchRepository;
  private final BranchAreaMappingRepository branchAreaMappingRepository;
  private final ConfigRemoteService configRemoteService;
  private final CustomerRepository customerRepository;
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
    this.customerRepository = customerRepository;
    this.currentUserService = currentUserService;
    this.auditTrailService = auditTrailService;
  }

  public PaginationResult<DistributionSubmissionDto> submissionDistribution(PaginationRequest request) {
    try {

      List<FinancingHdr> finHdrAll = financingHdrRepository.findAllByRaw();
      return SpecPagination.paginationData(new SpecPagination<FinancingHdr, DistributionSubmissionDto>(finHdrAll, request) {
        @Override
        public DistributionSubmissionDto eval(FinancingHdr e) {
          String city = "";
          String address = "";
          if (e.getCustomer() != null) {
            if (e.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
              if (e.getCustomer().getCompany() != null) {
                city = e.getCustomer().getCompany().getCity();
                address = e.getCustomer().getCompany().getCompanyAddress();
              }
            } else {
              if (e.getCustomer().getPersonal() != null) {
                city = e.getCustomer().getPersonal().getCity();
                address = e.getCustomer().getPersonal().getLegalAddress();
              }
            }
          }

          boolean isNewCust = financingHdrRepository.countByCustomerAndFinancingStatus(e.getCustomer(), "PAID") == 0;

          String color,
            currentBranch = null,
            currentBranchCode = null,
            branchRecommended = null,
            branchRecommendedCode = null;
          if (e.getFinancingStatus().equalsIgnoreCase("new")) {
            color = "#808080";
          } else if (
            e.getFinancingStatus().equalsIgnoreCase("inprocess")
              || e.getFinancingStatus().equalsIgnoreCase("signing")
              || e.getFinancingStatus().equalsIgnoreCase("signed")
              || e.getFinancingStatus().equalsIgnoreCase("live")
              || e.getFinancingStatus().equalsIgnoreCase("golive")

          ) {
            color = "#ccffcc";
          } else {
            color = "#FF5C5C";
          }

          if (e.getMstBranch() != null) {
            branchRecommendedCode = e.getMstBranch().getBranchCode();
            branchRecommended = e.getMstBranch().getBranchName();
            currentBranchCode = e.getMstBranch().getBranchCode();
            currentBranch = e.getMstBranch().getBranchName();

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
            .custName(e.getCustomer().getCustName())
            .bouwheerName(e.getBouwheer().getBouwheerName())
            .city(city)
            .dueDate(Utils.fromInstant(e.getFinancingDueDate()))
            .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
            .branchRecommendedCode(branchRecommendedCode)
            .branchRecommended(branchRecommended)
            .currentBranchCode(currentBranchCode)
            .currentBranch(currentBranch)
            .custStatus(isNewCust ? "New Customer" : "Existing Customer")
            .status(StatusLabelDto.builder()
              .status(mappedFinancingStatus.getStatus())
              .statusLabel(mappedFinancingStatus.getLabel())
              .color(color)
              .build())
            .npwp(e.getCustomer().getNpwp())
            .address(address)
            .ao(Optional.ofNullable(e.getMstBranch())
              .map(MstBranch::getEmployees)
              .map(Collection::stream)
              .flatMap(stream -> stream.map(MstEmployee::getEmployeeName).findFirst())
              .orElse(null))
            .dtmCrt(e.getDtmCrt())
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

    /**
     * Assign process
     */
    financingHdr.setFinancingStatus("INPROCESS");
    financingHdr.setFinancingStep("ASSIGNMENT");
    financingHdr.setMstBranch(mstBranch);
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

    if (mstBranch.getEmployees() != null && !mstBranch.getEmployees().isEmpty()) {
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

      //getAPI AO,BH
      MailPositionDto to = configRemoteService.getEmailByPosition("", financingHdr.getMstBranch().getBranchCode(), "BM/BOH");
      MailPositionDto ccRM = configRemoteService.getEmailByPosition("", financingHdr.getMstBranch().getBranchCode(), "RM");
      MailPositionDto ccAO = configRemoteService.getEmailByPosition("", financingHdr.getMstBranch().getBranchCode(), "AO/AM");

      String toEmail = mstBranch.getEmployees().stream().toList().getFirst().getEmail();  //"radema.panjaitan@csul.co.id",
      String ccEmail = null;
      if (to != null && to.getData() != null && to.getData().size() > 0) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < to.getData().size(); i++) {
          stringBuilder.append(!stringBuilder.isEmpty() ? ";" : "");
          stringBuilder.append(to.getData().get(i).getEmail());
        }
        toEmail = stringBuilder.toString();
      }
      StringBuilder stringBuilder = new StringBuilder();
      if (ccRM != null && ccRM.getData() != null && ccRM.getData().size() > 0) {
        for (int i = 0; i < ccRM.getData().size(); i++) {
          stringBuilder.append(!stringBuilder.isEmpty() ? ";" : "");
          stringBuilder.append(ccRM.getData().get(i).getEmail());
        }
        ccEmail = stringBuilder.toString();
      }
      if (ccAO != null && ccAO.getData() != null && ccAO.getData().size() > 0) {
        for (int i = 0; i < ccAO.getData().size(); i++) {
          stringBuilder.append(!stringBuilder.isEmpty() ? ";" : "");
          stringBuilder.append(ccAO.getData().get(i).getEmail());
        }
        ccEmail = stringBuilder.toString();
      }

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
          .applicationDate(DateTimeUtils.formatToDate(financingHdr.getFinancingDate()))
          .companyName(financingHdr.getCustomer().getCustName())
          .email(financingHdr.getCustomer().getCustEmail())
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
