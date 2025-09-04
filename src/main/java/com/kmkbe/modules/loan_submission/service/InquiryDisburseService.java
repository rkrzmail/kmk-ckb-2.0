package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.BouwheerPaymentEmailPayload;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.model.LoanDisburseEmailPayload;
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
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.nikita.utils.Utils;
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

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryDisburseService {
    private final FinancingDtlRepository financingDtlRepository;
    private final FinancingHdrRepository  financingHdrRepository;
    private final EmailService emailService;
    private final FinancingHdrService financingHdrService;
    private final BaseRemoteService baseRemoteService;
    private final RestTemplate restTemplate;
    private final DisbursementLogRepository disbursementLogRepository;
    private final ConfigRemoteService configRemoteService;

    private final FinancingRemoteService financingRemoteService;

    public double getBillingAmount(ArrayList<InquiryOutstandingBillDetailtDto.ListBillingDetail>  listBillingDetails, String billName){
        //BillDetailName = LC Installment Amount
        for (int i = 0; i < listBillingDetails.size(); i++) {
            InquiryOutstandingBillDetailtDto.ListBillingDetail listBillingDetail = listBillingDetails.get(i) ;
            if (listBillingDetail.billDetailName.equalsIgnoreCase(billName)){

                return Utils.formatNoExponent(listBillingDetail.billDetailAmt) ;
            }
        }
        return  0;
    }


    public InquiryDisburseResult inquiryDisburse ( @Nullable InquiryDisburseRequest inquiryDisburseRequest ) throws JsonProcessingException {
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
            //int  o = response.getStatusCode().value();
            String stsr = String.valueOf(response.getBody());
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            InquiryDisburseResult root = om.readValue(stsr, InquiryDisburseResult.class);
            return  root;//response.getBody();

        } catch (Exception e) {
            log.error("mstRefMasterInput: {}", e.getMessage());
            throw e;
        }
    }
    public boolean inquiryDisburseAuto  (Agreement agreement) throws Exception {
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
            inquiryDisburseRequest.setJoinType( "INNER");
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
                 if (inquiryDisburseResult.getStatusCode().equalsIgnoreCase("200")){
                     for (int i = 0; i < inquiryDisburseResult.getData().size(); i++) {
                         InquiryDisburseDatum inquiryDisburseDatum = inquiryDisburseResult.getData().get(i);
                         if (inquiryDisburseDatum.agreementNo.equalsIgnoreCase(aggrNo)){
                            if (inquiryDisburseDatum.getAPStatCode().equalsIgnoreCase("P")){
                                isDisbursP = true;
                                //disUpdate Financing Status = Disburse
                                //Optional<Agreement>  agreement = agreementRepository.findTopByAgreementCode(aggrNo);
                                //if (agreement.isPresent()){
                                    FinancingHdr financingHdr = agreement.getFinancingHdr();

                                    if (financingHdr.getFinancingStatus().equalsIgnoreCase("INPROCESS")){
                                        financingHdr.setFinancingStatus("LIVE");//"Disburse"
                                        financingHdr.setFinancingStep("GOLIVE");
                                        financingHdr.setDtmUpd(DateTimeUtils.nowLocal());
                                        financingHdrRepository.save(financingHdr);
                                    }

                               // }
                            }
                         }
                     }
                 }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }


            if (inquiryDisburseResult.getStatusCode().equalsIgnoreCase("200") && isDisbursP){
                List<DisbursementLog> l = disbursementLogRepository.findAllByAgreement(agreement);
                if (l.isEmpty()){
                    //send B to mst
                    UpdateFinancingStatusRequest updateFinancingStatusRequest = null;
                    try {
                        updateFinancingStatusRequest = UpdateFinancingStatusRequest.builder()
                                .vendorCode(agreement.getFinancingHdr().getCustomer().getCustExternalCode())
                                .financingCode(agreement.getFinancingHdr().getFinancingHdrCode().toString())
                                .status(UpdateFinancingStatusRequest.Status.Disburse)
                                .build();
                    } catch (Exception ignored) {  }
                    try {
                        financingRemoteService.updateFinancingStatus(updateFinancingStatusRequest);
                    } catch (Exception ignored) {  }

                    //send email sendNotification Pencairan
                    try {
                        //FinancingHdr financingHdr = agreement.getFinancingHdr();
                        FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(agreement.getFinancingHdr().getFinancingHdrCode()).get();
                        final FinancingHdrDto createdFinancing = financingHdrService.dtoFromEntity(financingHdr);

                        final List<InvoiceEmailPayload> invoices = createdFinancing.getDetails()
                                .stream()
                                .map((item) ->
                                        InvoiceEmailPayload.builder()
                                                //.seq(item.getInvoiceSeqno())
                                                .invoiceNo(item.getInvoice().getCustInvNo())
                                                .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt().doubleValue()))
                                                .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
                                                .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
                                                .description("Invoice By Trakindo")
                                                .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
                                                .build()
                                ).toList();

                        String agreementCode = agreement.getAgreementCode();

                        String phone = financingHdr.getCustomer().getCustMobilePhone();
                        if (financingHdr.getCustomer().getCustTypeCode().equalsIgnoreCase("Company")){
                            if (financingHdr.getCustomer().getCompany() !=null ){
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

                        //kirim email pencarian berhasil ke debitur (terjadi sekali saat mengisi disbursementLog pertama kali)
                        emailService.sendNotificationPencairan(
                                financingHdr.getCustomer().getCustEmail(),
                                financingHdr.getBouwheer().getBouwheerName(),
                                financingHdr.getMstBranch().getBranchName(),
                                PencarianPayload.builder()
//                                        .loan_number(agreementCode)
//                                        .account_number( financingHdr.getCustomer().getCustName() )
//                                        .total_disbursement(CommonFormattingUtils.formatAmount(financingHdr.getFinancingAmt()))
//                                        .bank_name( "Mandiri" )
//                                        .disbursement_date(DateTimeUtils.formatToDate(financingHdr.getFinancingDate()))
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
                    } catch (Exception ignored) {    ignored.printStackTrace(); }


                }
            }

            //insert Table
            try {
                if (inquiryDisburseResult.getStatusCode().equalsIgnoreCase("200") && isDisbursP){
                    try {

                        disbursementLogRepository.deleteAll(disbursementLogRepository.findAllByAgreement(agreement));
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }

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
                    //}

                }
            } catch (Exception ignored) {
                //on duplicate update
                ignored.printStackTrace();
            }

        } catch (Exception e) {
            log.error("updatePaid, error {}", e.getMessage());
            throw e;
        }
        return result;
    }

    public void debugSendEmail() {
        try {
            List<FinancingDtl> financingDtls = financingDtlRepository.findByUsrCrt("abdul")
                    .orElseThrow(() -> new IllegalStateException("Financing Invoice not found or not valid"));

            MailPositionDto ccBM = configRemoteService.getEmailByPosition("", "999", "CMS");
            StringBuilder ccEmailBuilder = new StringBuilder();

            if (ccBM != null && ccBM.getData() != null && !ccBM.getData().isEmpty()) {
                for (int i = 0; i < ccBM.getData().size(); i++) {
                    ccEmailBuilder.append(!ccEmailBuilder.isEmpty() ? ";" : "");
                    ccEmailBuilder.append(ccBM.getData().get(i).getEmail());
                }
            }

            String mjrEmail = ccEmailBuilder.toString();
            log.info("email CMS: {}", mjrEmail);

//            List<InvoiceEmailPayload> invoices = financingDtls
//                    .stream()
//                            .map((item) ->
//                    InvoiceEmailPayload.builder()
//                            .invoiceNo("INV-DEBUG-001")
//                            .description("Invoice Test Debug")
//                            .bouwheerName("PT Bouwheer Debug")
////                            .invoiceDate("01/01/2024")
//                            .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
//                            .invoiceDueDate("15/01/2024")
//                            .invoiceAmt("1.000.000")
//                            .build()
//                            ).toList();
            List<InvoiceEmailPayload> invoices = Arrays.asList(
                    InvoiceEmailPayload.builder()
                            .invoiceNo("INV-DEBUG-001")
                            .description("Invoice by Trakindo")
                            .bouwheerName("PT. Trakindo Utama")
                            .invoiceDate("01/01/2024")
                            .invoiceDueDate("15/01/2024")
                            .invoiceAmt("1.000.000")
                            .build(),
                    InvoiceEmailPayload.builder()
                            .invoiceNo("INV-DEBUG-002")
                            .description("Invoice by Trakindo")
                            .bouwheerName("PT. Trakindo Utama")
                            .invoiceDate("02/01/2024")
                            .invoiceDueDate("16/01/2024")
                            .invoiceAmt("2.000.000")
                            .build()
            );
            PencarianPayload payload = PencarianPayload.builder()
                    .financingCode("FIN-DEBUG-001")
                    .applicationDate("15/01/2024")
                    .companyName("Customer Company")
                    .phoneNumber("081234567890")
                    .tenor(Long.valueOf("40"))
                    .financingDueDate("15/02/2024")
                    .retention("500.000")
                    .financingAmt("10.000.000")
                    .totalFeeAmt("500.000")
                    .invoiceAmt("3.000.000")
                    .disburseAmt("9.500.000")
//                    .email("radema.panjaitan@csul.co.id")
//                    .toEmail("radema.panjaitan@csul.co.id")
//                    .ccEmail("radema.panjaitan@csul.co.id")
                    .invoices(invoices)
                    .build();

            // to trakindo
//            BouwheerPaymentEmailPayload payload = BouwheerPaymentEmailPayload.builder()
//                    .bouwheerName("TRAKINDO")
//                    .vendorCode("04euw-1038s-21kks1-1233o")
//                    .vendorName("Tedy Aditia")
//                    .accountNo("7005592119")
//                    .bankAccount("014")
//                    .bankName("BCA")
//                    .bankKey("100211")
//                    .tglPengajuan("02/09/2025")
//                    .invoices(invoices)
//                    .build();

            emailService.sendNotificationPencairan(
                    "radema.panjaitan@csul.co.id",
                    "PT. Trakindo Utama",
                    "Jakarta 1",
//                    Customer.builder().build(),
                    payload
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
