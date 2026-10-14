package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.model.PencarianPayload;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.*;
import com.kmkbe.core.domain.response.InquiryDisburseDatum;
import com.kmkbe.core.domain.response.InquiryDisburseResult;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryDisburseService {
  private final FinancingHdrRepository financingHdrRepository;
  private final EmailService emailService;
  private final FinancingHdrService financingHdrService;
  private final BaseRemoteService baseRemoteService;
  private final RestTemplate restTemplate;
  private final DisbursementLogRepository disbursementLogRepository;
  private final FinancingRemoteService financingRemoteService;

  public InquiryDisburseResult inquiryDisburse(@Nullable InquiryDisburseRequest inquiryDisburseRequest) throws JsonProcessingException {
    try {
      final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
      final HttpEntity<InquiryDisburseRequest> requestArgs = new HttpEntity<>(
        inquiryDisburseRequest,
        headers
      );

      final ResponseEntity<String> response = restTemplate.exchange(
        baseRemoteService.inquiry_Disburse(),
        HttpMethod.POST,
        requestArgs,
        new ParameterizedTypeReference<>() {
        }
      );

      String stsr = String.valueOf(response.getBody());
      ObjectMapper om = new ObjectMapper();
      om.registerModule(new JavaTimeModule());
      InquiryDisburseResult root = om.readValue(stsr, InquiryDisburseResult.class);
      return root;
    } catch (Exception e) {
      log.error("mstRefMasterInput: {}", e.getMessage());
      throw e;
    }
  }

  public boolean inquiryDisburseAuto(Agreement agreement) throws Exception {
    log.info("Inquiry disbursement  Agreement {}", agreement.getAgreementCode());
    boolean result = false;
    boolean isDisbursP = false;
    try {
      String aggrNo = agreement.getAgreementCode();
      String sToday = DateTimeUtils.SDF_STANDARD_DATE.format(new Date());

      //inquiryDisburseAuto
      InquiryDisburseRequest inquiryDisburseRequest = new InquiryDisburseRequest();
      InquiryDisburseQueryString inquiryDisburseQueryString = new InquiryDisburseQueryString();
      inquiryDisburseQueryString.setName("searchAPInquiry");
      inquiryDisburseRequest.setQueryString(inquiryDisburseQueryString);
      inquiryDisburseRequest.setIncludeCount(true);
      inquiryDisburseRequest.setIncludeData(true);
      inquiryDisburseRequest.setLoading(true);
      inquiryDisburseRequest.setRowVersion("");
      inquiryDisburseRequest.setIntegrationObj(null);
      inquiryDisburseRequest.setJoinType("INNER");
      inquiryDisburseRequest.setPageNo(1);
      inquiryDisburseRequest.setRowPerPage(10);
      InquiryDisburseOrderBy inquiryDisburseOrderBy = new InquiryDisburseOrderBy();
      inquiryDisburseOrderBy.setKey("AP.ACC_PAYABLE_ID");
      inquiryDisburseOrderBy.setValue("false");
      inquiryDisburseRequest.setOrderBy(inquiryDisburseOrderBy);

      ArrayList<InquiryDisburseCriterion> inquiryDisburseCriterias = new ArrayList<>();
      InquiryDisburseCriterion inquiryDisburseCriterion = new InquiryDisburseCriterion();
      inquiryDisburseCriterion.setHigh(0);
      inquiryDisburseCriterion.setLow(0);
      inquiryDisburseCriterion.setDataType("text");
      inquiryDisburseCriterion.setCriteriaDataTable(false);
      inquiryDisburseCriterion.setRestriction("Eq");
      inquiryDisburseCriterion.setPropName("AP.REF_ACC_PAYABLE_TYPE_ID");
      inquiryDisburseCriterion.setValue("10009");
      inquiryDisburseCriterias.add(inquiryDisburseCriterion);
      inquiryDisburseCriterion = new InquiryDisburseCriterion();
      inquiryDisburseCriterion.setHigh(0);
      inquiryDisburseCriterion.setLow(0);
      inquiryDisburseCriterion.setDataType("");
      inquiryDisburseCriterion.setCriteriaDataTable(false);
      inquiryDisburseCriterion.setRestriction("Eq");
      inquiryDisburseCriterion.setPropName("AP.AGRMNT_NO");
      inquiryDisburseCriterion.setValue(aggrNo);
      inquiryDisburseCriterias.add(inquiryDisburseCriterion);
      inquiryDisburseCriterion = new InquiryDisburseCriterion();
      inquiryDisburseCriterion.setHigh(0);
      inquiryDisburseCriterion.setLow(0);
      inquiryDisburseCriterion.setDataType("text");
      inquiryDisburseCriterion.setCriteriaDataTable(false);
      inquiryDisburseCriterion.setRestriction("Eq");
      inquiryDisburseCriterion.setPropName("AP.CURR_CODE");
      inquiryDisburseCriterion.setValue("IDR");
      inquiryDisburseCriterias.add(inquiryDisburseCriterion);
      inquiryDisburseRequest.setRequestDateTime(sToday);
      inquiryDisburseRequest.setCriteria(inquiryDisburseCriterias);
      InquiryDisburseResult inquiryDisburseResult = inquiryDisburse(inquiryDisburseRequest);//CALL API
      try {
        if (inquiryDisburseResult.getStatusCode().equalsIgnoreCase("200")) {
          for (int i = 0; i < inquiryDisburseResult.getData().size(); i++) {
            InquiryDisburseDatum inquiryDisburseDatum = inquiryDisburseResult.getData().get(i);
            if (inquiryDisburseDatum.agreementNo.equalsIgnoreCase(aggrNo)) {
              log.info("Confins inquiry AP Stat code  {}", inquiryDisburseDatum.getAPStatCode());
              if (inquiryDisburseDatum.getAPStatCode().equalsIgnoreCase("P")) {
                isDisbursP = true;
                FinancingHdr financingHdr = agreement.getFinancingHdr();
                if (financingHdr.getFinancingStatus().equalsIgnoreCase("INPROCESS") || financingHdr.getFinancingStep().equalsIgnoreCase("SIGNED")) {
                  financingHdr.setFinancingStatus("LIVE");
                  financingHdr.setFinancingStep("GOLIVE");
                  financingHdr.setDtmUpd(DateTimeUtils.nowLocal());
                  financingHdrRepository.save(financingHdr);
                }
              }
            }
          }
        }
      } catch (Exception ignored) {
        ignored.printStackTrace();
      }

      if (inquiryDisburseResult.getStatusCode().equalsIgnoreCase("200") && isDisbursP) {
        List<DisbursementLog> l = disbursementLogRepository.findAllByAgreement(agreement);
        if (l.isEmpty()) {
          //send B to mst
          UpdateFinancingStatusRequest updateFinancingStatusRequest = null;
          try {
            updateFinancingStatusRequest = UpdateFinancingStatusRequest.builder()
              .vendorCode(agreement.getFinancingHdr().getCustomer().getCustExternalCode())
              .financingCode(agreement.getFinancingHdr().getFinancingHdrCode().toString())
              .status(UpdateFinancingStatusRequest.Status.Disburse)
              .build();
            /**
             * For TU
             */
//         financingRemoteService.updateFinancingStatus(updateFinancingStatusRequest);
          } catch (Exception ignored) {
          }

          //send email sendNotification Pencairan
          try {
            FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(agreement.getFinancingHdr().getFinancingHdrCode()).get();
            final FinancingHdrDto createdFinancing = financingHdrService.dtoFromEntity(financingHdr);

            final List<InvoiceEmailPayload> invoices = createdFinancing.getDetails()
              .stream()
              .map((item) ->
                InvoiceEmailPayload.builder()
                  .invoiceNo(item.getInvoice().getCustInvNo())
                  .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt().doubleValue()))
                  .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
                  .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
                  .description(item.getInvoice().getInvoiceDescription())
                  .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
                  .build()
              ).toList();

            String phone = financingHdr.getCustomer().getCustMobilePhone();
            if (financingHdr.getCustomer().getCustTypeCode().equalsIgnoreCase("Company")) {
              if (financingHdr.getCustomer().getCompany() != null) {
                phone = financingHdr.getCustomer().getCompany().getPhone();
              }
            }

            final double totalFeeAmt =
              financingHdr.getAdminFeeAmt()
                + financingHdr.getLegalFeeAmtNett()
                + financingHdr.getInsuranceFeeAmt()
                + financingHdr.getOthersFeeAmt()
                + financingHdr.getProvisionFeeAmt()
                + financingHdr.getSurveyFeeAmtNett();

            log.info("Send disbursement email to {}", financingHdr.getCustomer().getCustEmail());
            emailService.sendNotificationPencairan(
              financingHdr.getCustomer().getCustEmail(),
              financingHdr.getBouwheer().getBouwheerName(),
              financingHdr.getMstBranch().getBranchName(),
              PencarianPayload.builder()
                .financingCode(financingHdr.getFinancingHdrCode().toString())
                .applicationDate(DateTimeUtils.formatToDate(financingHdr.getFinancingDate()))
                .companyName(financingHdr.getCustomer().getCustName())
                .phoneNumber(phone)
                .tenor(financingHdr.getTenor())
                .financingDueDate(DateTimeUtils.formatToDate(financingHdr.getFinancingDueDate()))
                .retention(CommonFormattingUtils.formatAmount(financingHdr.getRetention()))
                .financingAmt(CommonFormattingUtils.formatAmount(financingHdr.getFinancingAmt()))
                .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
                .invoiceAmt(CommonFormattingUtils.formatAmount(financingHdr.getTotalInvoiceAmt()))
                .disburseAmt(CommonFormattingUtils.formatAmount(financingHdr.getDisburseAmt()))
                .invoices(invoices)
                .build()
            );
          } catch (Exception ignored) {
            ignored.printStackTrace();
          }
        }
      }

      //insert Table
      try {
        if (inquiryDisburseResult.getStatusCode().equalsIgnoreCase("200") && isDisbursP) {
          disbursementLogRepository.deleteAll(disbursementLogRepository.findAllByAgreement(agreement));
          DisbursementLog disbursementLog = DisbursementLog.builder().build();
          disbursementLog.setDisbursementCode(UUID.randomUUID());
          disbursementLog.setAgreement(agreement);
          InquiryDisburseDatum disburseDatum = inquiryDisburseResult.getData().getFirst();
          disbursementLog.setApNo(disburseDatum.getAPNo());//getAPNo
          disbursementLog.setApDesc(disburseDatum.getAPDescr());
          disbursementLog.setCurrency(disburseDatum.getCurrCode());
          disbursementLog.setApAmt(disburseDatum.getApAmt());
          disbursementLog.setApPaidAmt(disburseDatum.getAPPaidAmt());
          disbursementLog.setApAmtInprocess(disburseDatum.getAPAmtInProces());
          disbursementLog.setApUnpaidAmt(disburseDatum.getUnpaidAmt());
          disbursementLog.setApTypeCode(disburseDatum.getAPTypeCode());
          disbursementLog.setApTypeName(disburseDatum.getAPTypeName());
          disbursementLog.setApDueDate(disburseDatum.getAPDueDt());
          disbursementLog.setBranchCode(disburseDatum.getOfficeCode());
          disbursementLog.setApPaidLocation(disburseDatum.getApPaidLocCode());
          disbursementLog.setUsrCrt("AUTO");
          disbursementLog.setDtmCrt(DateTimeUtils.now());
          disbursementLog.setUsrUpd("AUTO");
          disbursementLog.setDtmUpd(DateTimeUtils.now());
          disbursementLogRepository.save(disbursementLog);
        }
      } catch (Exception ignored) {
        ignored.printStackTrace();
      }
    } catch (Exception e) {
      log.error("updatePaid, error {}", e.getMessage());
      throw e;
    }
    return result;
  }
}
