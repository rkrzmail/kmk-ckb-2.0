package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.InvoiceMapper;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.PaymentReceiveHistoryRepository;
import com.kmkbe.core.domain.request.*;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.nikita.data.Nson;
import com.kmkbe.nikita.utils.Utils;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class FinancingDtlService {
  private final FinancingDtlRepository financingDtlRepository;
  private final PaymentReceiveHistoryRepository paymentReceiveHistoryRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final BaseRemoteService baseRemoteService;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  public FinancingDtlService(FinancingDtlRepository financingDtlRepository,
                             PaymentReceiveHistoryRepository paymentReceiveHistoryRepository,
                             FinancingHdrRepository financingHdrRepository,
                             BaseRemoteService baseRemoteService,
                             ObjectMapper objectMapper,
                             RestTemplate restTemplate) {
    this.financingDtlRepository = financingDtlRepository;
    this.paymentReceiveHistoryRepository = paymentReceiveHistoryRepository;
    this.financingHdrRepository = financingHdrRepository;
    this.baseRemoteService = baseRemoteService;
    this.objectMapper = objectMapper;
    this.restTemplate = restTemplate;
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

  /**
   * Update Invoice PAID
   * @param request
   * @param financingHdr
   */
  public void updatePaid(FinancingInvoicePaidRequest request, FinancingHdr financingHdr) {
    try {
      // 1. Ambil data detail financing dari repository
      List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr)
        .orElse(Collections.emptyList());

      if (financingDtls.isEmpty()) {
        return;
      }

      String invoiceDetails = financingDtls.stream()
        .map(dtl -> String.format("%s(Rp%,d)", dtl.getInvoice().getCustInvNo(), dtl.getInvoice().getInvoiceAmt().longValue()))
        .collect(Collectors.joining(", "));

      // 2. Petakan data sistem & request ke dalam Map berdasarkan Invoice Number untuk pencarian cepat (O(1))
      Map<String, FinancingDtl> systemInvoices = financingDtls.stream()
        .collect(Collectors.toMap(dtl -> dtl.getInvoice().getCustInvNo(), dtl -> dtl));

      Map<String, FinancingInvoicePaidRequest.InvoicePaid> requestInvoices = request.getInvoicePaid().stream()
        .collect(Collectors.toMap(FinancingInvoicePaidRequest.InvoicePaid::getInvoiceNo, req -> req));

      log.info("Financing Detail: Header Code {}, Step {}, Status {}, Invoices  {}",
        request.getFinancingCode(), financingHdr.getFinancingStep(), financingHdr.getFinancingStatus(), invoiceDetails);

      // 3. Validasi Kehadiran Data (Apakah ada invoice yang kurang atau kelebihan?)
      validateInvoicePresence(systemInvoices, requestInvoices, request.getFinancingCode());

      // 4. Validasi Nominal Uang & Update Data ke Database
      String updater = financingHdr.getUsrUpd();

      for (FinancingDtl financingDtl : financingDtls) {
        String invoiceNo = financingDtl.getInvoice().getCustInvNo();
        FinancingInvoicePaidRequest.InvoicePaid reqInvoice = requestInvoices.get(invoiceNo);

        // Validasi kecocokan nominal uang
        long systemAmount = financingDtl.getInvoice().getInvoiceAmt().longValue();
        long reqAmount = reqInvoice.getInvoiceAmount();

        // Cek jika nominal uang yang dibayar kurang dari tagihan sistem
        if (reqAmount < systemAmount) {
          long selisih = systemAmount - reqAmount;
          log.warn("Kurang Bayar (Nominal) pada Invoice {}: Tagihan {}, Dibayar {}, Kurang {}",
            invoiceNo, systemAmount, reqAmount, selisih);

          throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80,
            "Invoice " + invoiceNo + " kurang bayar sebesar Rp " + String.format("%,d", selisih));
        }
        // Cek jika nominal uang kelebihan (opsional, tetap dianggap tidak sesuai)
        else if (reqAmount > systemAmount) {
          log.warn("Kelebihan Bayar pada Invoice {}: Tagihan {}, Dibayar {}", invoiceNo, systemAmount, reqAmount);
          throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Nominal Invoice tidak sesuai (Kelebihan Bayar)");
        }

        // Update data entitas (Bug .getFirst() sudah diperbaiki di sini menggunakan reqInvoice)
        financingDtl.getInvoice().setStatus("PAID");
        financingDtl.getInvoice().setUsrUpd(updater);
        financingDtl.getInvoice().setDtmUpd(LocalDateTime.now());

        financingDtl.setBouwheerPaidDate(Utils.toInstant(reqInvoice.getPostingDate()));
        financingDtl.setUsrUpd(updater);
        financingDtl.setDtmUpd(LocalDateTime.now());

        // Simpan perubahan
        financingDtlRepository.save(financingDtl);
      }

    } catch (Exception e) {
      log.error("updatePaid error: {}", e.getMessage());
      throw e;
    }
  }

  // Fungsi pembantu untuk memisahkan logika pengecekan list invoice
  private void validateInvoicePresence(Map<String, FinancingDtl> systemInvoices, Map<String, FinancingInvoicePaidRequest.InvoicePaid> requestInvoices, String financingCode) {
    // Cek apakah ada invoice di sistem yang tidak dikirim oleh request
    for (String sysKey : systemInvoices.keySet()) {
      if (!requestInvoices.containsKey(sysKey)) {
        log.info("{} {}", ErrorConstant.ERROR_MESSAGE_81, financingCode);
        throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Ada Invoice yang belum masuk list");
      }
    }

    // Cek apakah ada invoice di request yang sebenarnya tidak ada di sistem
    for (String reqKey : requestInvoices.keySet()) {
      if (!systemInvoices.containsKey(reqKey)) {
        log.info("{} {}", ErrorConstant.ERROR_MESSAGE_81, financingCode);
        throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Ada Invoice yang tidak ada disystem");
      }
    }
  }

  public void paymentReceive(FinancingInvoicePaidRequest request, FinancingHdr financingHdr) throws Exception {
    try {
      String noAggrNo = financingHdr.getAgreement().size() == 0 ? "" : financingHdr.getAgreement().iterator().next().getAgreementCode();
      LocalDateTime settelmanetDate = request.getInvoicePaid().isEmpty() ? DateTimeUtils.now() : Utils.toInstant(request.getInvoicePaid().get(0).getClearingDate());
      ;
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
      inquiryNewInfoAgreementRequest.setValueDt(DateTimeUtils.SDF_STANDARD_DATE.format(Utils.fromInstant(settelmanetDate)));
      inquiryNewInfoAgreementRequest.setRequestDateTime(sToday);
      InquiryNewInfoAgreementDto inquiryNewInfoAgreementDto = inquiryNewInfoAgreement(inquiryNewInfoAgreementRequest);

      //Inquiry Outstanding Bill Detail
      InquiryOutstandingBillDetailtRequest inquiryOutstandingBillDetailtRequest = new InquiryOutstandingBillDetailtRequest();
      inquiryOutstandingBillDetailtRequest.setRequestDateTime(sToday);
      inquiryOutstandingBillDetailtRequest.setAgrmntNo(noAggrNo);
      inquiryOutstandingBillDetailtRequest.setValueDt(DateTimeUtils.SDF_STANDARD_DATE.format(Utils.fromInstant(settelmanetDate)));
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
        int tenor = 0;
        if (settelmanetDate.toInstant(ZoneOffset.UTC).toEpochMilli() < inquiryDataAgreementDto.getAgrmntObj().nextInstDueDt.toInstant(ZoneOffset.UTC).toEpochMilli()) {
          tenor = (int) Utils.dateDuration(settelmanetDate, inquiryDataAgreementDto.getAgrmntObj().effectiveDt).toDays();
        } else {
          tenor = (int) Utils.dateDuration(inquiryDataAgreementDto.getAgrmntObj().nextInstDueDt, inquiryDataAgreementDto.getAgrmntObj().effectiveDt).toDays();
        }
        paymentReceiveHistory.setRealTenor(Math.abs(tenor));//financingHdr.getTenor().intValue()
        paymentReceiveHistory.setNtfAmt(inquiryDataAgreementDto.getAgrmntObj().ntfAmt);
        paymentReceiveHistory.setTotalInvAmt(financingHdr.getTotalInvoiceAmt());
        paymentReceiveHistory.setLcRate(inquiryDataAgreementDto.getAgrmntObj().lcInstRatePrml);
        paymentReceiveHistory.setLcDays(0);//Jika settlement_date > due_date, maka settlement_date – due_date, jika tidak maka lc_days = 0
        paymentReceiveHistory.setLcAmt(getBillingAmount(inquiryOutstandingBillDetailtDto.getListBillingDetail(), "LC Installment Amount"));//BillDetailName = LC Installment Amount
        paymentReceiveHistory.setInterestAmt(inquiryNewInfoAgreementDto.interestAmt);
        paymentReceiveHistory.setRetention(financingHdr.getRetention());
        paymentReceiveHistory.setRetentionAmt(getBillingAmount(inquiryOutstandingBillDetailtDto.getListBillingDetail(), "Retention Amount"));//Retention Amount
        paymentReceiveHistory.setSettlementAmt(getBillingAmount(inquiryOutstandingBillDetailtDto.getListBillingDetail(), "Amount To Be Paid"));//BillDetailName = Amount To Be Paid
        paymentReceiveHistory.setRefundAmt(paymentReceiveHistory.getTotalInvAmt() - paymentReceiveHistory.getSettlementAmt());//Nilai pada kolom total_inv_amt - settlement_amt
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

  public void paymentReceiveSubmitUpdate(FinancingHdr financingHdr, LocalDateTime settelmanetDate) throws Exception {
    try {
      String noAggrNo = financingHdr.getAgreement().size() == 0 ? "" : financingHdr.getAgreement().iterator().next().getAgreementCode();
      String sToday = DateTimeUtils.SDF_STANDARD_DATE.format(new Date());
      String noCode = financingHdr.getFinancingHdrCode().toString();

      Optional<PaymentReceiveHistory> paymentReceiveHistoryDto = paymentReceiveHistoryRepository.findTopByAgreementCode(noAggrNo);
            /*
            Inquiry than Submit
             */
      //Inquiry Receive Amount
      InquiryReceiveAmountRequest receiveAmountRequest = new InquiryReceiveAmountRequest();
      receiveAmountRequest.setRequestDateTime(sToday);
      ArrayList<InquiryReceiveAmountRequest.RcvAmtList> rcvAmtLists = new ArrayList<>();
      List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr).orElse(null);

      InquiryReceiveAmountRequest.RcvAmtList rcvAmtList = new InquiryReceiveAmountRequest.RcvAmtList();
      rcvAmtList.agrmntNo = noAggrNo;//karena hanya 1 agrrement per financeheader, isi 1 saja
      rcvAmtList.rcvAmt = Utils.formatNoExponent(financingHdr.getTotalInvoiceAmt());
      rcvAmtLists.add(rcvAmtList);
      receiveAmountRequest.setValueDt(DateTimeUtils.SDF_STANDARD_DATE.format(Utils.fromInstant(settelmanetDate)));
      receiveAmountRequest.setRcvAmtList(rcvAmtLists);
      InquiryReceiveAmountRequestDto receiveAmountRequestDto = inquiryReceiveAmountRequest(receiveAmountRequest);//CALL API


      //Submit Payment Receive
      SubmitPaymentReceiveRequest submitPaymentReceiveRequest = new SubmitPaymentReceiveRequest();
      submitPaymentReceiveRequest.setRefNo(noCode);

      String rcvStr = "";
      if (financingHdr.getCustomer() != null) {
        try {
          rcvStr = financingHdr.getCustomer().getCustNo() + "-" + financingHdr.getCustomer().getCustName();
        } catch (Exception ignored) {
        }
      } else {
        Optional<FinancingHdr> hdr = financingHdrRepository.findByFinancingHdrCode(financingHdr.getFinancingHdrCode());
        if (!hdr.isEmpty()) {
          try {
            rcvStr = hdr.get().getCustomer().getCustNo() + "-" + hdr.get().getCustomer().getCustName();
          } catch (Exception ignored) {
          }
        }
      }
      Optional<FinancingHdr> hdr = financingHdrRepository.findByFinancingHdrCode(financingHdr.getFinancingHdrCode());
      if (!hdr.isEmpty()) {
        try {
          rcvStr = hdr.get().getCustomer().getCustNo() + "-" + hdr.get().getCustomer().getCustName();
        } catch (Exception ignored) {
        }
      }
      submitPaymentReceiveRequest.setRefNo(noCode);
      submitPaymentReceiveRequest.setReceiptFormNo("");
      submitPaymentReceiveRequest.setRcvFrom(rcvStr);//gabungan

      submitPaymentReceiveRequest.setOfficeBankAccCode("MDRLSIDR99");
      submitPaymentReceiveRequest.setExchangeRateAmt(1);
      submitPaymentReceiveRequest.isTempReceiptForm = false;
      submitPaymentReceiveRequest.setWopCode("BANK");
      //submitPaymentReceiveRequest.setValueDt( sToday  );//DateTimeUtils.getDateOnly(settelmanetDate.toString())
      submitPaymentReceiveRequest.setValueDt(DateTimeUtils.SDF_STANDARD_DATE.format(Utils.fromInstant(settelmanetDate)));

      submitPaymentReceiveRequest.setMrPayRecipientCode("CSH");//MrPayRecipientCode
      submitPaymentReceiveRequest.setSuspdNo("");
      ArrayList<SubmitPaymentReceiveRequest.ListPayRcvDApiObj> listPayRcvDApiObjs = new ArrayList<>();
      SubmitPaymentReceiveRequest.ListPayRcvDApiObj listPayRcvDApiObj = new SubmitPaymentReceiveRequest.ListPayRcvDApiObj();
      listPayRcvDApiObj.agrmntNo = noAggrNo;
      listPayRcvDApiObj.rcvAmt = financingHdr.getTotalInvoiceAmt();

      listPayRcvDApiObj.isAutoAlloc = true;
      listPayRcvDApiObj.allocCurrCode = "IDR";
      listPayRcvDApiObj.rcvTrxType = "AGR";
      listPayRcvDApiObj.refNo = noCode;
      listPayRcvDApiObj.exchangeRateAmt = 1;
      if (paymentReceiveHistoryDto.isPresent()) {
        PaymentReceiveHistory paymentReceiveHistory = paymentReceiveHistoryDto.get();
        listPayRcvDApiObj.totalAmtToBePaid = Utils.formatNoExponent(paymentReceiveHistory.getSettlementAmt());//settlement_amt
      }
      ArrayList<SubmitPaymentReceiveRequest.PayRcvDAllocAPIList> payRcvDAllocAPILists = new ArrayList<>();
      ArrayList<InquiryReceiveAmountRequestDto.AllocMapList> allocMapLists = receiveAmountRequestDto.getAllocMapList();

      if (allocMapLists != null && !allocMapLists.isEmpty()) {
        InquiryReceiveAmountRequestDto.AllocMapList allocMapList = allocMapLists.getFirst();//ambil yng pertama
        for (int j = 0; j < allocMapList.allocList.size(); j++) {
          InquiryReceiveAmountRequestDto.AllocList allocList = allocMapList.allocList.get(j);

          if (allocList.rcvAmt >= 1) {
            SubmitPaymentReceiveRequest.PayRcvDAllocAPIList payRcvDAllocAPIList = new SubmitPaymentReceiveRequest.PayRcvDAllocAPIList();
            payRcvDAllocAPIList.refPaymentAllocCode = allocList.paymentAllocCode;   // RcvAmt memiliki nilai > 0
            payRcvDAllocAPIList.allocAmt = Utils.formatNoExponent(allocList.rcvAmt);
            payRcvDAllocAPIList.officeCode = "";
            payRcvDAllocAPIList.descr = "";
            payRcvDAllocAPIList.bizUnitCode = "";

            payRcvDAllocAPILists.add(payRcvDAllocAPIList);
          }
        }
      }


      listPayRcvDApiObj.payRcvDAllocAPIList = payRcvDAllocAPILists;
      listPayRcvDApiObjs.add(listPayRcvDApiObj);
      submitPaymentReceiveRequest.setListPayRcvDApiObj(listPayRcvDApiObjs);
      submitPaymentReceiveRequest.setExchangeRateAmt(1);

      submitPaymentReceiveRequest.setRequestDateTime(sToday);

      SubmitPaymentReceiveRequestDto submitPaymentReceiveRequestDto = submitPaymentReceiveRequest(submitPaymentReceiveRequest);

      if (submitPaymentReceiveRequestDto.getStatusCode().equalsIgnoreCase("200")) {
        //Optional<PaymentReceiveHistory> paymentReceiveHistoryDto = paymentReceiveHistoryRepository.findTopByAgreementCode(noAggrNo);

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

    @Nullable InquiryNewInfoAgreementRequest inquiryNewInfoAgreementRequest) throws JsonProcessingException {
    try {
      return postRemote(
        baseRemoteService.Agrmnt_GetNewInfoByAgrmntNo(),
        inquiryNewInfoAgreementRequest,
        InquiryNewInfoAgreementDto.class
      );
    } catch (Exception e) {
      log.error("mstRefMasterInput: {}", e.getMessage());
      throw e;
    }
  }

  public InquiryDataAgreementDto inquiryDataAgreement(
    @Nullable InquiryDataAgreementRequest inquiryDataAgreementRequest) throws JsonProcessingException {
    try {
      return postRemote(
        baseRemoteService.Agrmnt_GetAgrmntByAgrmntNo(),
        inquiryDataAgreementRequest,
        InquiryDataAgreementDto.class
      );
    } catch (Exception e) {
      log.error("mstRefMasterInput: {}", e.getMessage());
      throw e;
    }
  }

  public InquiryOutstandingBillDetailtDto inquiryOutstandingBillDetailt(

    @Nullable InquiryOutstandingBillDetailtRequest inquiryOutstandingBillDetailtRequest) throws JsonProcessingException {
    try {
      return postRemote(
        baseRemoteService.OnlinePayment_GetOutstandingBillDetailByAgrmntNo(),
        inquiryOutstandingBillDetailtRequest,
        InquiryOutstandingBillDetailtDto.class
      );
    } catch (Exception e) {
      log.error("mstRefMasterInput: {}", e.getMessage());
      throw e;
    }
  }


  public InquiryReceiveAmountRequestDto inquiryReceiveAmountRequest(

    @Nullable InquiryReceiveAmountRequest inquiryReceiveAmountRequest) throws JsonProcessingException {
    try {
      return postRemote(
        baseRemoteService.PrepaidAlloc_GetRcvAmtValueByParamPriorityCodeV2(),
        inquiryReceiveAmountRequest,
        InquiryReceiveAmountRequestDto.class
      );
    } catch (Exception e) {
      log.error("mstRefMasterInput: {}", e.getMessage());
      throw e;
    }
  }


  public SubmitPaymentReceiveRequestDto submitPaymentReceiveRequest(

    @Nullable SubmitPaymentReceiveRequest submitPaymentReceiveRequest) throws JsonProcessingException {
    try {
      return postRemote(
        baseRemoteService.PaymentReceive_SubmitPaymentReceiveFromApi(),
        submitPaymentReceiveRequest,
        SubmitPaymentReceiveRequestDto.class
      );
    } catch (Exception e) {
      log.error("mstRefMasterInput: {}", e.getMessage());
      throw e;
    }
  }

  private <T> T postRemote(String url, Object request, Class<T> responseType) throws JsonProcessingException {
    final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
    final HttpEntity<Object> requestArgs = new HttpEntity<>(
      request,
      headers
    );

    final ResponseEntity<String> response = restTemplate.exchange(
      url,
      HttpMethod.POST,
      requestArgs,
      new ParameterizedTypeReference<>() {
      }
    );

    String responseBody = String.valueOf(response.getBody());
    return objectMapper.readValue(responseBody, responseType);
  }

  public double getBillingAmount(ArrayList<InquiryOutstandingBillDetailtDto.ListBillingDetail> listBillingDetails, String billName) {
    //BillDetailName = LC Installment Amount
    for (int i = 0; i < listBillingDetails.size(); i++) {
      InquiryOutstandingBillDetailtDto.ListBillingDetail listBillingDetail = listBillingDetails.get(i);
      if (listBillingDetail.billDetailName.equalsIgnoreCase(billName)) {

        return Utils.formatNoExponent(listBillingDetail.billDetailAmt);
      }
    }
    return 0;
  }


}
