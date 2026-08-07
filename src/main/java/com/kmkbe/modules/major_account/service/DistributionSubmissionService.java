package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.*;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import com.kmkbe.nikita.utils.SpecPagination;
import com.kmkbe.nikita.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
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

  public DistributionSubmissionService(FinancingHdrRepository financingHdrRepository,
                                       EmailService emailService,
                                       MstBranchRepository mstBranchRepository,
                                       BranchAreaMappingRepository branchAreaMappingRepository,
                                       ConfigRemoteService configRemoteService) {
    this.financingHdrRepository = financingHdrRepository;
    this.emailService = emailService;
    this.mstBranchRepository = mstBranchRepository;
    this.branchAreaMappingRepository = branchAreaMappingRepository;
    this.configRemoteService = configRemoteService;
  }

  public PaginationResult<DistributionSubmissionDto> submissionDistribution(
    PaginationRequest request
  ) {
    try {

      List<FinancingHdr> finHdrAll = financingHdrRepository.findAllByRaw();
      return SpecPagination.paginationData(new SpecPagination<FinancingHdr, DistributionSubmissionDto>(finHdrAll, request) {
        @Override
        public DistributionSubmissionDto eval(FinancingHdr e) {
          String city = "";
          if (e.getCustomer() != null) {
            if (e.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
              if (e.getCustomer().getCompany() != null) {
                city = e.getCustomer().getCompany().getCity();
              }
            } else {
              if (e.getCustomer().getPersonal() != null) {
                city = e.getCustomer().getPersonal().getCity();
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

  public void assignSubmission(
    Authentication authentication,
    AssignInvoiceToBranchRequest request
  ) throws SignatureException {
    try {
      final UUID financingHdrCode;
      try {
        financingHdrCode = UUID.fromString(request.getFinancingHdrCode());
      } catch (IllegalArgumentException ignored) {
        throw new IllegalStateException("Invalid given financingHdrCode");
      }

      MstUser authenticateUser = UserInternalUtils.authenticateUser(authentication);
      MstBranch mstBranch = mstBranchRepository.findByBranchCode(request.getBranchCode())
        .orElseThrow(() -> new IllegalStateException("Branch Not Found with given argument"));
      FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(financingHdrCode)
        .orElseThrow(() -> new IllegalStateException("Financing Not Found with given argument"));

      String sring = Utils.valueOf(financingHdr.getFinancingStep());//ASSIGNMENT
      if (Utils.valueOf(financingHdr.getFinancingStep()).equalsIgnoreCase("PAID")) {
        throw new IllegalStateException("PAID given financingHdrCode");
      }
      if (Utils.valueOf(financingHdr.getFinancingStep()).equalsIgnoreCase("NEW")
        || Utils.valueOf(financingHdr.getFinancingStep()).equalsIgnoreCase("ASSIGNMENT")
        || Utils.valueOf(financingHdr.getFinancingStep()).equalsIgnoreCase("ASSIGN")) {

      } else {
        throw new IllegalStateException("Status is not assigned");
      }

      // Major account melakukan assignment leads ke cabang
      financingHdr.setFinancingStatus("INPROCESS");
      financingHdr.setFinancingStep("ASSIGNMENT");
      financingHdr.setMstBranch(mstBranch);
      financingHdr.setDtmUpd(DateTimeUtils.now());
      financingHdr.setUsrUpd(authenticateUser.getUsername());
      financingHdrRepository.save(financingHdr);

      if (mstBranch.getEmployees() != null && !mstBranch.getEmployees().isEmpty()) {
        final List<InvoiceEmailPayload> invoices = financingHdr.getFinancingDtls()
          .stream()
          .map((item) ->
            InvoiceEmailPayload.builder()
              .invoiceNo(item.getInvoice().getCustInvNo())
              .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt().doubleValue()))
              .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
              .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
              .description("Invoice By Trakindo")
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

        //kirim email assign dan re assign
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

    } catch (Exception e) {
      log.error("assignSubmission: error {}", e.getMessage());
      throw e;
    }
  }
}
