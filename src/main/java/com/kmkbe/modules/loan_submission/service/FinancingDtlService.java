package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.InvoiceMapper;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.repository.PaymentReceiveHistoryRepository;
import com.kmkbe.core.domain.request.*;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.remote.request.RefMasterRequest;
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

import java.time.Instant;
import java.time.temporal.TemporalField;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingDtlService {
    private final FinancingDtlRepository financingDtlRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentReceiveHistoryRepository PaymentReceiveHistoryRepository;
    private final PaymentReceiveHistoryRepository paymentReceiveHistoryRepository;


    private final BaseRemoteService baseRemoteService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;


    public FinancingDtl findBy(String bouwheerInvNo) {
        try {
            return financingDtlRepository.findFirstByBouwheerInvNo(bouwheerInvNo).orElse(null);
        } catch (Exception e) {
            log.error("findBy, error {}", e.getMessage());
            throw e;
        }
    }

    public List<FinancingDtl> findAllBy(FinancingHdr financingHdr) {
        try {
            return financingDtlRepository.findAllByFinancingHdr(financingHdr).orElse(new ArrayList<>());
        } catch (Exception e) {
            log.error("findAllBy, error {}", e.getMessage());
            throw e;
        }
    }

    /**
     * <p>@postedInvoices is invoices that already posted from MST integration</p>
     * <p>@createdInvoices is invoices that created from Entity or DB</p>
     */
    public List<FinancingDtl> createBulk(
            Customer customer,
            Bouwheer bouwheer,
            FinancingHdr financingHdr,
            List<PostedInvoicePayload> postedInvoices,
            List<InvoiceDto> createdInvoices
    ) {
        try {
            return IntStream.range(0, postedInvoices.size())
                    .mapToObj((index) -> {
                        final FinancingDtl detail = new FinancingDtl();
                        {
                            Invoice invoice = InvoiceMapper.INSTANCE.entityFromDto(
                                    createdInvoices
                                            .stream()
                                            .filter(item -> item.getBouwheerInvNo().equals(postedInvoices.get(index).getBouwheerInvoiceNo()))
                                            .findFirst()
                                            .orElseThrow(() -> new IllegalStateException(""))
                            );

                            invoice.setUsrCrt(customer.getCustName());
                            invoice.setDtmCrt(DateTimeUtils.now());
                            invoice.setCustomer(customer);
                            invoice.setBouwheer(bouwheer);

                            detail.setInvoice(invoice);
                            detail.setFinancingDtlCode(UUID.randomUUID());
                            detail.setBouwheerInvNo(postedInvoices.get(index).getBouwheerInvoiceNo());
                            //detail.setInvoiceSeqno((long) index + 1);
                            detail.setFinancingHdr(financingHdr);
                            detail.setUsrCrt(customer.getCustName());
                            detail.setDtmCrt(DateTimeUtils.now());


                        }

                        financingDtlRepository.save(detail);
                        return detail;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("createBulk, error {}", e.getMessage());
            throw e;
        }
    }

    public void delete(FinancingDtl financingDtl) {
        try {
            financingDtlRepository.delete(financingDtl);
        } catch (Exception e) {
            log.error("delete, error {}", e.getMessage());
            throw e;
        }
    }

    public void updatePaid(FinancingInvoicePaidRequest request, FinancingHdr financingHdr) throws Exception {
        try {
            List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr).orElse(null);
            if (financingDtls != null) {
                for (FinancingDtl financingDtl : financingDtls) {
                    for (FinancingInvoicePaidRequest.InvoicePaid invoicePaid : request.getInvoicePaid()) {
                        if (invoicePaid.getInvoiceNo().equalsIgnoreCase(financingDtl.getInvoice().getCustInvNo()) &&
                                invoicePaid.getInvoiceAmount() == financingDtl.getInvoice().getInvoiceAmt().intValue()) {
                        } else {
                            throw new Exception("Invoice No/Amount tidak sesuai");
                        }
                    }//for
                }//for

                for (FinancingDtl financingDtl : financingDtls) {
                    for (FinancingInvoicePaidRequest.InvoicePaid invoicePaid : request.getInvoicePaid()) {
                        if (invoicePaid.getInvoiceNo().equalsIgnoreCase(financingDtl.getInvoice().getCustInvNo())) {
                            financingDtl.getInvoice().setStatus("PAID");
                            financingDtl.getInvoice().setUsrUpd(financingHdr.getUsrUpd());
                            financingDtl.getInvoice().setDtmUpd(DateTimeUtils.now());

                            financingDtl.setBouwheerPaidDate(request.getInvoicePaid().getFirst().getPostingDate().toInstant());
                            financingDtl.setDtmUpd(DateTimeUtils.now());
                            financingDtl.setUsrUpd(financingHdr.getUsrUpd());
                            financingDtlRepository.save(financingDtl);
                            break;
                        }
                    }//for
                }//for
            }

        } catch (Exception e) {
            log.error("updatePaid, error {}", e.getMessage());
            throw e;
        }
    }


    public void paymentReceive(FinancingInvoicePaidRequest request, FinancingHdr financingHdr) throws Exception {
        try {
            String noAggrNo = financingHdr.getAgreement().size() == 0 ? "" : financingHdr.getAgreement().iterator().next().getAgreementCode();
            Instant settelmanetDate = request.getInvoicePaid().isEmpty()?DateTimeUtils.now(): request.getInvoicePaid().get(0).getClearingDate().toInstant();;
            String sToday = DateTimeUtils.SDF_STANDARD_DATE.format(new Date());


            //Inquiry Data Agreement
            InquiryDataAgreementRequest inquiryReceiveAmountRequest = new InquiryDataAgreementRequest();
            inquiryReceiveAmountRequest.setRequestDateTime(sToday);
            inquiryReceiveAmountRequest.setTrxNo(noAggrNo);
            InquiryDataAgreementDto inquiryDataAgreementDto = inquiryDataAgreement(inquiryReceiveAmountRequest);

            //Inquiry New Info by Agreement
            InquiryNewInfoAgreementRequest inquiryNewInfoAgreementRequest = new InquiryNewInfoAgreementRequest();
            inquiryNewInfoAgreementRequest.setRequestDateTime(sToday);
            inquiryNewInfoAgreementRequest.setAgrmntNo(noAggrNo);
            inquiryNewInfoAgreementRequest.setRequestDateTime(sToday);
            InquiryNewInfoAgreementDto inquiryNewInfoAgreementDto = inquiryNewInfoAgreement(inquiryNewInfoAgreementRequest);

            //Inquiry Outstanding Bill Detail
            InquiryOutstandingBillDetailtRequest inquiryOutstandingBillDetailtRequest = new InquiryOutstandingBillDetailtRequest();
            inquiryOutstandingBillDetailtRequest.setRequestDateTime(sToday);
            inquiryOutstandingBillDetailtRequest.setAgrmntNo(noAggrNo);
            inquiryOutstandingBillDetailtRequest.setRequestDateTime(sToday);
            InquiryOutstandingBillDetailtDto inquiryOutstandingBillDetailtDto = inquiryOutstandingBillDetailt(inquiryOutstandingBillDetailtRequest);


            //insert Table
            try {
                PaymentReceiveHistory paymentReceiveHistory = PaymentReceiveHistory.builder().build();
                paymentReceiveHistory.setAgreementCode(noAggrNo);
                paymentReceiveHistory.setBouwheer(financingHdr.getBouwheer());
                paymentReceiveHistory.setCurrency(inquiryDataAgreementDto.getAgrmntObj().currCode);
                paymentReceiveHistory.setGoliveDate(inquiryDataAgreementDto.getAgrmntObj().goLiveDt);
                paymentReceiveHistory.setEffectiveDate(inquiryDataAgreementDto.getAgrmntObj().effectiveDt);
                paymentReceiveHistory.setDueDate(inquiryDataAgreementDto.getAgrmntObj().nextInstDueDt);

                paymentReceiveHistory.setSettlementDte(settelmanetDate);
                paymentReceiveHistory.setRealTenor(financingHdr.getTenor().intValue());
                paymentReceiveHistory.setNtfAmt(inquiryDataAgreementDto.getAgrmntObj().ntfAmt);
                paymentReceiveHistory.setTotalInvAmt(financingHdr.getTotalInvoiceAmt());
                paymentReceiveHistory.setLcRate(inquiryDataAgreementDto.getAgrmntObj().lcInsRatePrml);
                paymentReceiveHistory.setLcDays(0);//Jika settlement_date > due_date, maka settlement_date – due_date, jika tidak maka lc_days = 0
                paymentReceiveHistory.setLcAmt(getBillingAmount(inquiryOutstandingBillDetailtDto.getListBillingDetail(), "LC Installment Amount"));//BillDetailName = LC Installment Amount
                paymentReceiveHistory.setInterestAmt(inquiryNewInfoAgreementDto.interestAmt);
                paymentReceiveHistory.setRetention(financingHdr.getRetention());
                paymentReceiveHistory.setRetentionAmt(getBillingAmount(inquiryOutstandingBillDetailtDto.getListBillingDetail(), "Retention Amount"));//Retention Amount
                paymentReceiveHistory.setSettlementAmt(getBillingAmount(inquiryOutstandingBillDetailtDto.getListBillingDetail(), "Amount To Be Paid"));//BillDetailName = Amount To Be Paid
                paymentReceiveHistory.setRefundAmt(paymentReceiveHistory.getTotalInvAmt()-paymentReceiveHistory.getSettlementAmt());//Nilai pada kolom total_inv_amt - settlement_amt
                //paymentReceiveHistory.setPaymentReceiveNo(null);
                paymentReceiveHistory.setUsrCrt(financingHdr.getUsrCrt());
                paymentReceiveHistory.setDtmCrt(DateTimeUtils.now());
                paymentReceiveHistoryRepository.save(paymentReceiveHistory);
            } catch (Exception ignored) {
                //on duplicate update
                ignored.printStackTrace();
            }

            paymentReceiveSubmitUpdate(financingHdr, settelmanetDate);
        } catch (Exception e) {
            log.error("updatePaid, error {}", e.getMessage());
            throw e;
        }
    }
    public void paymentReceiveSubmitUpdate(FinancingHdr financingHdr, Instant settelmanetDate ) throws Exception {
        try {
            String noAggrNo = financingHdr.getAgreement().size() == 0 ? "" : financingHdr.getAgreement().iterator().next().getAgreementCode();

            String sToday = DateTimeUtils.SDF_STANDARD_DATE.format(new Date());


            /*
            Inquiry than Submit
             */
            //Inquiry Receive Amount
            InquiryReceiveAmountRequest receiveAmountRequest = new InquiryReceiveAmountRequest();
            receiveAmountRequest.setRequestDateTime(sToday);
            receiveAmountRequest.setRequestDateTime(sToday);
            ArrayList<InquiryReceiveAmountRequest.RcvAmtList> rcvAmtLists = new ArrayList<>();
            List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr).orElse(null);
            /*if (financingDtls != null) {
                for (FinancingDtl financingDtl : financingDtls) {
                    InquiryReceiveAmountRequest.RcvAmtList rcvAmtList = new InquiryReceiveAmountRequest.RcvAmtList();
                    rcvAmtList.agrmntNo = noAggrNo;
                    rcvAmtList.rcvAmt = financingDtl.getInvoice().getInvoiceAmt();
                    rcvAmtLists.add(rcvAmtList);
                }//for
            }*/
            InquiryReceiveAmountRequest.RcvAmtList rcvAmtList = new InquiryReceiveAmountRequest.RcvAmtList();
            rcvAmtList.agrmntNo = noAggrNo;//karena hanya 1 agrrement per financeheader, isi 1 saja
            rcvAmtList.rcvAmt = financingHdr.getTotalInvoiceAmt();
            rcvAmtLists.add(rcvAmtList);

            receiveAmountRequest.setRcvAmtList(rcvAmtLists);
            InquiryReceiveAmountRequestDto receiveAmountRequestDto = inquiryReceiveAmountRequest(receiveAmountRequest);


            //Submit Payment Receive
            SubmitPaymentReceiveRequest submitPaymentReceiveRequest = new SubmitPaymentReceiveRequest();
            submitPaymentReceiveRequest.setRefNo(financingHdr.getFinancingHdrCode().toString());
            submitPaymentReceiveRequest.setRefNo(financingHdr.getCustomer().getCustNo() + "-" + financingHdr.getCustomer().getCustName());
            submitPaymentReceiveRequest.setReceiptFormNo("");
            submitPaymentReceiveRequest.setOfficeBankAccCode("MDRLSIDR99");
            submitPaymentReceiveRequest.setExchangeRateAmt(1);
            //ISTEMPRECEIPTFORM			Default value “false”
            submitPaymentReceiveRequest.setWopCode("BANK");
            submitPaymentReceiveRequest.setValueDt(settelmanetDate.toString());
            submitPaymentReceiveRequest.setMrPayRecipientCode("CSH");
            submitPaymentReceiveRequest.setSuspdNo("");
            ArrayList<SubmitPaymentReceiveRequest.ListPayRcvDApiObj> listPayRcvDApiObjs = new ArrayList<>();
            SubmitPaymentReceiveRequest.ListPayRcvDApiObj listPayRcvDApiObj = new SubmitPaymentReceiveRequest.ListPayRcvDApiObj();
            listPayRcvDApiObj.agrmntNo = noAggrNo;
            listPayRcvDApiObj.rcvAmt = financingHdr.getTotalInvoiceAmt();
            //ISAUTOALLOC			Default value “false”
            //RCVTRXTYPE			Default value “AGR”
            listPayRcvDApiObj.refNo = financingHdr.getFinancingHdrCode().toString();
            listPayRcvDApiObj.totalAmtToBePaid = financingHdr.getDisburseAmt();//settlement_amt
            ArrayList<SubmitPaymentReceiveRequest.PayRcvDAllocAPIList> payRcvDAllocAPILists = new ArrayList<>();
            ArrayList<InquiryReceiveAmountRequestDto.AllocMapList> allocMapLists = receiveAmountRequestDto.getAllocMapList();
            /*for (int i = 0; i < 1; i++) {//allocMapLists.size();
                InquiryReceiveAmountRequestDto.AllocMapList allocMapList = allocMapLists.get(i);
                for (int j = 0; j < allocMapList.allocList.size(); j++) {
                    InquiryReceiveAmountRequestDto.AllocList allocList = allocMapList.allocList.get(j);
                    SubmitPaymentReceiveRequest.PayRcvDAllocAPIList payRcvDAllocAPIList = new SubmitPaymentReceiveRequest.PayRcvDAllocAPIList();
                    payRcvDAllocAPIList.refPaymentAllocCode = allocList.paymentAllocCode;   // RcvAmt memiliki nilai > 0
                    payRcvDAllocAPIList.allocAmt = allocList.rcvAmt;
                    payRcvDAllocAPIList.officeCode = "";
                    payRcvDAllocAPIList.descr = "";
                    payRcvDAllocAPIList.bizUnitCode = "";

                    payRcvDAllocAPILists.add(payRcvDAllocAPIList);
                }
            }*/
            if (allocMapLists != null && !allocMapLists.isEmpty()) {
                InquiryReceiveAmountRequestDto.AllocMapList allocMapList = allocMapLists.getFirst();//ambil yng pertama
                for (int j = 0; j < allocMapList.allocList.size(); j++) {
                    InquiryReceiveAmountRequestDto.AllocList allocList = allocMapList.allocList.get(j);
                    SubmitPaymentReceiveRequest.PayRcvDAllocAPIList payRcvDAllocAPIList = new SubmitPaymentReceiveRequest.PayRcvDAllocAPIList();
                    payRcvDAllocAPIList.refPaymentAllocCode = allocList.paymentAllocCode;   // RcvAmt memiliki nilai > 0
                    payRcvDAllocAPIList.allocAmt = allocList.rcvAmt;
                    payRcvDAllocAPIList.officeCode = "";
                    payRcvDAllocAPIList.descr = "";
                    payRcvDAllocAPIList.bizUnitCode = "";

                    payRcvDAllocAPILists.add(payRcvDAllocAPIList);
                }
            }


            listPayRcvDApiObj.payRcvDAllocAPIList = payRcvDAllocAPILists;
            listPayRcvDApiObjs.add(listPayRcvDApiObj);
            submitPaymentReceiveRequest.setListPayRcvDApiObj(listPayRcvDApiObjs);
            submitPaymentReceiveRequest.setExchangeRateAmt(1);
            submitPaymentReceiveRequest.setMrPayRecipientCode("");
            submitPaymentReceiveRequest.setRequestDateTime(sToday);

            SubmitPaymentReceiveRequestDto submitPaymentReceiveRequestDto = submitPaymentReceiveRequest(submitPaymentReceiveRequest);

            if (submitPaymentReceiveRequestDto.getStatusCode().equalsIgnoreCase("200")) {
                Optional<PaymentReceiveHistory> paymentReceiveHistoryDto = paymentReceiveHistoryRepository.findTopByAgreementCode(noAggrNo);

                //Update Table
                try {
                    if (paymentReceiveHistoryDto.isPresent()) {
                        PaymentReceiveHistory paymentReceiveHistory = paymentReceiveHistoryDto.get();
                        paymentReceiveHistory.setPaymentReceiveNo(submitPaymentReceiveRequestDto.getPayRcvNo());
                        paymentReceiveHistory.setUsrUpd(financingHdr.getUsrCrt());
                        paymentReceiveHistory.setDtmUpd(DateTimeUtils.now());
                        paymentReceiveHistoryRepository.save(paymentReceiveHistory);
                    }
                } catch (Exception ignored) {
                    //on duplicate update
                }
            }

        } catch (Exception e) {
            log.error("updatePaid, error {}", e.getMessage());
            throw e;
        }
    }

    public InquiryNewInfoAgreementDto inquiryNewInfoAgreement(

            @Nullable InquiryNewInfoAgreementRequest inquiryNewInfoAgreementRequest ) {
        try {
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<InquiryNewInfoAgreementRequest> requestArgs = new HttpEntity<>(
                    inquiryNewInfoAgreementRequest,
                    headers
            );

            final ResponseEntity<InquiryNewInfoAgreementDto> response = restTemplate.exchange(
                    baseRemoteService.Agrmnt_GetNewInfoByAgrmntNo(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return null;//response.getBody();
        } catch (Exception e) {
            log.error("mstRefMasterInput: {}", e.getMessage());
            throw e;
        }
    }

    public InquiryDataAgreementDto inquiryDataAgreement(

            @Nullable InquiryDataAgreementRequest inquiryDataAgreementRequest ) {
        try {
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<InquiryDataAgreementRequest> requestArgs = new HttpEntity<>(
                    inquiryDataAgreementRequest,
                    headers
            );

            final ResponseEntity<Object> response = restTemplate.exchange(
                    baseRemoteService.Agrmnt_GetAgrmntByAgrmntNo(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return null;//response.getBody();
        } catch (Exception e) {
            log.error("mstRefMasterInput: {}", e.getMessage());
            throw e;
        }
    }

    public InquiryOutstandingBillDetailtDto inquiryOutstandingBillDetailt(

            @Nullable InquiryOutstandingBillDetailtRequest inquiryOutstandingBillDetailtRequest ) {
        try {
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<InquiryOutstandingBillDetailtRequest> requestArgs = new HttpEntity<>(
                    inquiryOutstandingBillDetailtRequest,
                    headers
            );

            final ResponseEntity<InquiryOutstandingBillDetailtDto> response = restTemplate.exchange(
                    baseRemoteService.OnlinePayment_GetOutstandingBillDetailByAgrmntNo(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return null;//response.getBody();
        } catch (Exception e) {
            log.error("mstRefMasterInput: {}", e.getMessage());
            throw e;
        }
    }


    public InquiryReceiveAmountRequestDto inquiryReceiveAmountRequest(

            @Nullable InquiryReceiveAmountRequest inquiryReceiveAmountRequest ) {
        try {
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<InquiryReceiveAmountRequest> requestArgs = new HttpEntity<>(
                    inquiryReceiveAmountRequest,
                    headers
            );

            final ResponseEntity<InquiryReceiveAmountRequestDto> response = restTemplate.exchange(
                    baseRemoteService.PrepaidAlloc_GetRcvAmtValueByParamPriorityCodeV2(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return null;//response.getBody();
        } catch (Exception e) {
            log.error("mstRefMasterInput: {}", e.getMessage());
            throw e;
        }
    }


    public SubmitPaymentReceiveRequestDto submitPaymentReceiveRequest(

            @Nullable SubmitPaymentReceiveRequest submitPaymentReceiveRequest ) {
        try {
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<SubmitPaymentReceiveRequest> requestArgs = new HttpEntity<>(
                    submitPaymentReceiveRequest,
                    headers
            );

            final ResponseEntity<SubmitPaymentReceiveRequestDto> response = restTemplate.exchange(
                    baseRemoteService.PaymentReceive_SubmitPaymentReceiveFromApi(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return null;//response.getBody();
        } catch (Exception e) {
            log.error("mstRefMasterInput: {}", e.getMessage());
            throw e;
        }
    }

    public double getBillingAmount(ArrayList<InquiryOutstandingBillDetailtDto.ListBillingDetail>  listBillingDetails, String billName){
        //InquiryOutstandingBillDetailtDto inquiryOutstandingBillDetailtDto;
        //inquiryOutstandingBillDetailtDto.listBillingDetail
        //BillDetailName = LC Installment Amount
        for (int i = 0; i < listBillingDetails.size(); i++) {
            InquiryOutstandingBillDetailtDto.ListBillingDetail listBillingDetail = listBillingDetails.get(i) ;
            if (listBillingDetail.billDetailName.equalsIgnoreCase(billName)){

                return listBillingDetail.billDetailAmt;
            }
        }
        return  0;
    }


}
