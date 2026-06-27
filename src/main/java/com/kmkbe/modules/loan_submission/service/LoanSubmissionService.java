package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.constant.FinancingStatus;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.*;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.exception.LoanDocMandatoryException;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.service.ExistingCustomerService;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateLoanApplicationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.SaveImportantNotesRequest;
import com.kmkbe.modules.remote.request.ExistingCustomerRequest;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.remote.service.CurrencyRemoteService;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.remote.service.InvoiceRemoteDto;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.nikita.utils.Utils;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanSubmissionService {
  private final ProductRepository productRepository;
  private final BouwheerRepository bouwheerRepository;
  private final BCryptPasswordEncoder bcryptEncoder;
  private final JdbcTemplate jdbcTemplate;
  private final ConfigRemoteService configRemoteService;
  private final JwtLoanSubmissionService jwtLoanSubmissionService;

  private final SimulationHistRepository simulationHistRepository;
  private final InvoiceRepository invoiceRepository;
  private final CustomerRemoteService customerRemoteService;
  private final InvoiceRemoteDto invoiceRemoteDto;
  private final CurrencyRemoteService currencyRemoteService;
  private final ExistingCustomerService existingCustomerService;

  private final CustomerCompanyRepository customerCompanyRepository;
  private final CustomerPersonalRepository customerPersonalRepository;
  private final AgreementRepository agreementRepository;


  private final InvoiceService invoiceService;
  private final FinancingHdrService financingHdrService;
  private final FinancingDtlService financingDtlService;
  private final MstFileTypeService mstFileTypeService;
  private final EmailService emailService;
  private final LegalFileRepository legalFileRepository;
  private final ImportantNotesService importantNotesService;
  private final FinancingHdrRepository financingHdrRepository;
  private final MstBranchRepository mstBranchRepository;
  private final CustomerRepository customerRepository;

  public List<PostedInvoiceDto> fetchActiveInvoice(
    Authentication authentication,
    String token
  ) throws Exception {
    try {
      final VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(authentication, token);
      final InquiryInvoiceRemoteDto inquiryInvoiceRemote;

      try {
        //ambil data dari api
        inquiryInvoiceRemote = invoiceRemoteDto.inquiryInvoice(vendorTokenExtractor.getVendorCode()).getData();
      } catch (Exception e) {
        throw new IllegalStateException("Terjdi kesalahan saat mengambil data invoice dari pihak PT. Trakindo Utama.");
      }

      if (inquiryInvoiceRemote == null) {
        throw CommonInvalidException.builder()
          .title("Tidak Terdapat Invoice Yang Dapat Dibiayai")
          .message("Mohon maaf, saat ini Anda belum dapat menggunakan " +
            "Dana Sakti. Harap melakukan pengecekan ulang " +
            "dengan pihak PT. Trakindo Utama.")
          .build();
      }

      if (inquiryInvoiceRemote.getBlacklistStatus()) {
        throw CommonInvalidException.builder()
          .title("Perusahaan Anda Terdaftar dalam Daftar Blacklist")
          .message("Perusahaan Anda saat ini terdaftar dalam daftar " +
            "blacklist PT Trakindo Utama, sehingga Anda " +
            "belum dapat menggunakan Dana Sakti.")
          .build();
      }
      if (
        inquiryInvoiceRemote.getDocumentStatus().equalsIgnoreCase("03")
          || inquiryInvoiceRemote.getDocumentStatus().equalsIgnoreCase("04")
      ) {
        throw CommonInvalidException.builder()
          .title("Mohon Maaf, Anda Tidak Memenuhi Syarat")
          .message("Mohon maaf, saat ini Anda belum dapat menggunakan " +
            "Dana Sakti. Harap melakukan pengecekan ulang " +
            "dengan pihak PT. Trakindo Utama.")
          .build();
      }

      if (inquiryInvoiceRemote.getRow().isEmpty()) {
        throw CommonInvalidException.builder()
          .title("Tidak Terdapat Invoice Yang Dapat Dibiayai")
          .message("Mohon maaf, saat ini Anda belum dapat menggunakan " +
            "Dana Sakti. Harap melakukan pengecekan ulang " +
            "dengan pihak PT. Trakindo Utama.")
          .build();
      }

      final SimpleDateFormat sdfNoSeperator = new SimpleDateFormat("yyyyMMdd");
      //double baseUsdToIdr = currencyRemoteService.fetchIdrFrom("usd");


      //jika data banyak berpotensi timeout
      List<PostedInvoiceDto> result = new ArrayList<>();
      for (int i = 0; i < inquiryInvoiceRemote.getRow().size(); i++) {
        InquiryInvoiceRemoteDto.InvoiceRemoteDto remoteDto = inquiryInvoiceRemote.getRow().get(i);
        if (StringUtil.isNullOrEmpty(inquiryInvoiceRemote.getRow().get(i).getPoNumber())) {
          continue;//hide yng po nya kosong
        }

        /// hide check
                /*Optional<Invoice>  invoice =  invoiceRepository.findByBouwheerInvNAndCustInvNo(
                        vendorTokenExtractor.getBouwheerCode().toString(),
                        inquiryInvoiceRemote.getRow().get(i).getReference());
                if (invoice.isPresent()) {
                    //invoice sudah direquest
                    continue;
                }*/

        Date invDate, invDueDate;
        try {
          invDate = DateTimeUtils.SDF_STANDARD_DATE.parse(inquiryInvoiceRemote.getRow().get(i).getPostingDate());
          invDueDate = DateTimeUtils.SDF_STANDARD_DATE.parse(inquiryInvoiceRemote.getRow().get(i).getNetDueDate());
        } catch (Exception e) {
          invDate = sdfNoSeperator.parse(inquiryInvoiceRemote.getRow().get(i).getPostingDate());
          invDueDate = sdfNoSeperator.parse(inquiryInvoiceRemote.getRow().get(i).getNetDueDate());
        }

        BigDecimal invoiceAmount = BigDecimal.valueOf(Double.parseDouble(inquiryInvoiceRemote.getRow().get(i).getAmount().trim()));
        String currency = inquiryInvoiceRemote.getRow().get(i).getCurrency(),
          description = inquiryInvoiceRemote.getRow().get(i).getDescription();
        if (
          !currency.equalsIgnoreCase("idr")
            && !currency.equalsIgnoreCase("rupiah")
            && !currency.equalsIgnoreCase("rp")
        ) {
          //invoiceAmount = invoiceAmount.multiply(BigDecimal.valueOf(baseUsdToIdr));
          currency = "IDR";
        }

        if (StringUtil.isNullOrEmpty(description)) {
          description = "Invoice By Trakindo";
        }

        Date postingDate = null;
        try {
          postingDate = DateTimeUtils.SDF_STANDARD_DATE.parse(inquiryInvoiceRemote.getRow().get(i).getPostingDate());
        } catch (Exception e) {
          try {
            postingDate = sdfNoSeperator.parse(inquiryInvoiceRemote.getRow().get(i).getPostingDate());
          } catch (Exception ignored) {
          }
        }

        result.add(PostedInvoiceDto.builder()
          .bouwheerCode(vendorTokenExtractor.getBouwheerCode().toString())
          .bouwheerName(vendorTokenExtractor.getBouwheerName())
          .customerInvoiceNo(inquiryInvoiceRemote.getRow().get(i).getReference())
          .bouwheerInvoiceNo(inquiryInvoiceRemote.getRow().get(i).getAccountingDocument())
          .poNumber(inquiryInvoiceRemote.getRow().get(i).getPoNumber())
          .postingDate(postingDate)
          .invoiceDate(invDate)
          .invoiceDueDate(invDueDate)
          .invoiceAmount(invoiceAmount)
          .invoiceDescription(description)
          .currencyCode(currency)
          .amountConverter(
            PostedInvoiceDto.AmountConverter.builder()
              //.base(BigDecimal.valueOf(baseUsdToIdr))
              .fromCurrencyCode(inquiryInvoiceRemote.getRow().get(i).getCurrency())
              .toCurrencyCode("IDR")
              .amount(BigDecimal.valueOf(Double.parseDouble(inquiryInvoiceRemote.getRow().get(i).getAmount().trim())))
              .build()
          )
          .build());
      }

      return result;
    } catch (Exception e) {
      log.error("fetchActiveInvoice, error {}", e.getMessage());
      throw e;
    }
  }

  public List<DisbursePercentageDto> fetchDisbursePercentage() {
    try {
      List<DisbursePercentageDto> result = new ArrayList<>();
      for (double i = 50.0; i <= 90.0; i += 5.0) {
        result.add(
          DisbursePercentageDto.builder()
            .disbursePercentage(i)
            .build()
        );
      }

      return result;
    } catch (Exception e) {
      log.error("fetchDisbursePercentage, error {}", e.getMessage());
      throw e;
    }
  }

  public EstimatedDisburseDto calculateDisburse(
    Authentication authentication,
    CalculateSimulationRequest request
  ) throws SignatureException, JsonProcessingException, ParseException {
    try {
      final BigDecimal ntfResult = request.getTotalInvoiceAmount()
        .multiply(BigDecimal.valueOf(request.getDisbursePercentage() / 100.0));
      //.setScale(0, RoundingMode.UP);

      final Optional<Product> findProduct = productRepository.findNtfRange(ntfResult.doubleValue());

      if (findProduct.isEmpty()) {
        // return null;
        throw new IllegalStateException("Mohon maaf, Product yang sesuai limit tidak ditemukan");

      }

      final Product product = findProduct.get();
      Double provisionRate = findProduct.get().getProvisionRate(),
        effectiveRate = findProduct.get().getEffectiveRate(),
        adminRate = findProduct.get().getAdminRate();
      boolean byPass = false;
      boolean isCustomerExisting = false;
      Cwr validateCwr = null;
      if (byPass) {
        isCustomerExisting = true;
        validateCwr = null;

        try {
          final Customer customer = CustomerUtils.authenticateCustomer(authentication);
          if (customer != null) {
            if (customer.getExistingCust() == null) {
              isCustomerExisting = false;
            } else {
              if (customer.getExistingCust().equalsIgnoreCase("")) {
                isCustomerExisting = false;
              }
            }
            isCustomerExisting = true;
          } else {
            isCustomerExisting = false;
          }
        } catch (Exception ignored) {

        }
      } else if (authentication != null || !StringUtil.isNullOrEmpty(request.getToken())) {
        validateCwr = isCustomerExistingByCwr(authentication, request.getToken());
        isCustomerExisting = validateCwr != null;
      }
      BigDecimal
        provisionFeeAmount,
        adminFeeAmount,
        othersFeeAmount,
        surveyFeeAmount,
        legalFeeAmount;

      int tenor = 90; // (duedate - skr) + gp bouwherr
      if (request.getBouwheerCode() != null && request.getInvoiceDueDate() != null) {
        Optional<Bouwheer> bouwheer = bouwheerRepository.findByBouwheerCode(UUID.fromString(request.getBouwheerCode()));
        int top = DateTimeUtils.dateDiffInDay(new Date(), DateTimeUtils.SDF_STANDARD_RESPONSE_DATE.parse(request.getInvoiceDueDate()));
        tenor = top + (bouwheer.map(value -> value.getGracePeriod().intValue()).orElse(0));
      }

      BigDecimal plafondLimitBigDecimal = validateCwr != null
        ? BigDecimal.valueOf(validateCwr.getPlafondAmt())
        : ntfResult.multiply(BigDecimal.valueOf(3.0));

      double plafonLimit = plafondLimitBigDecimal.doubleValue();
      double nilaiPembiayaan = ntfResult.doubleValue(); //total invaoice * % pembiayaan
      double effective_Rate = product.getEffectiveRate();
      double interestAmount = nilaiPembiayaan * effective_Rate / 100 * tenor / 360;//6 digit
      double adminFee = product.getAdminRate() * nilaiPembiayaan / 100;
      double jumlahBiaya;
      double provisionRateFee = product.getProvisionRate() * plafonLimit / 100;

      if (!isCustomerExisting) {
        jumlahBiaya = provisionRateFee
          + product.getSurveyFee()
          + product.getLegalFee()
          + adminFee
          + product.getOthersFee();
        provisionFeeAmount = new BigDecimal(provisionRateFee).setScale(0, RoundingMode.HALF_UP);
        surveyFeeAmount = new BigDecimal(product.getSurveyFee()).setScale(0, RoundingMode.HALF_UP);
        legalFeeAmount = new BigDecimal(product.getLegalFee()).setScale(0, RoundingMode.HALF_UP);
        adminFeeAmount = new BigDecimal(adminFee).setScale(0, RoundingMode.HALF_UP);
        othersFeeAmount = new BigDecimal(product.getOthersFee()).setScale(0, RoundingMode.HALF_UP);
      } else {
        provisionFeeAmount = new BigDecimal(0);
        surveyFeeAmount = new BigDecimal(0);
        legalFeeAmount = new BigDecimal(0);
        adminFeeAmount = new BigDecimal(0);
        othersFeeAmount = new BigDecimal(0);
        jumlahBiaya = adminFee + product.getOthersFee();
      }

      double nilaiYangdiCarikan = nilaiPembiayaan - jumlahBiaya;
      final BigDecimal serviceFee = new BigDecimal(jumlahBiaya).setScale(0, RoundingMode.HALF_UP);
      final BigDecimal estimated = new BigDecimal(nilaiYangdiCarikan).setScale(0, RoundingMode.HALF_UP);

      return EstimatedDisburseDto.builder()
        .productId(product.getProductId())
        .financingAmount(ntfResult.setScale(0, RoundingMode.HALF_UP)) //yng diajukan
        .serviceFeeAmount(serviceFee)
        .estimatedDisburseAmount(estimated)
        .interestFeeAmount(new BigDecimal(interestAmount).setScale(0, RoundingMode.HALF_UP))//interest
        .provisionFeeAmount(provisionFeeAmount)
        .adminFeeAmount(adminFeeAmount)
        .othersFeeAmount(othersFeeAmount)
        .legalFeeAmount(legalFeeAmount)
        .surveyFeeAmount(surveyFeeAmount)
        .adminRate(adminRate)
        .effectiveRate(effectiveRate)
        .provisionRate(provisionRate)
        .totalInvoiceAmount(request.getTotalInvoiceAmount())
        .build();
    } catch (Exception e) {
      log.error("calculateDisburse, error {}", e.getMessage());
      throw e;
    }
  }

  public FinancingHdrDto viewCulateDisburse(
    Authentication authentication,
    String financeCode,
    String histCode
  ) throws SignatureException, JsonProcessingException, ParseException {
    try {

      Optional<FinancingHdr> financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financeCode));
      if (financingHdr.isPresent()) {
        FinancingHdr fin = financingHdr.get();
                /*fin.setCustomer(null);
                fin.setBouwheer(null);
                fin.setMstBranch(null);
                fin.setFinancingDtls(null);
                fin.setAgreement(null);
                fin.setSimulationHistories(null);*/
        double effectiveRate = fin.getEffectiveRate();
        if (effectiveRate < 1) {
          effectiveRate = effectiveRate * 100;
        } else if (effectiveRate > 100) {
          effectiveRate = Math.floor(effectiveRate / 100);
        }

        FinancingHdrDto financingHdrDto = FinancingHdrDto.builder()
          .financingDate(fin.getFinancingDate())
          .adminFeeAmt(fin.getAdminFeeAmt())
          .disburseDate(fin.getDisburseDate())
          .disburseAmt(fin.getDisburseAmt())
          .adminFeePercentage(fin.getAdminFeePercentage())
          .adminFeeAmt(fin.getAdminFeeAmt())
          .adminLimitAmt(fin.getAdminLimitAmt())
          .currencyCode(fin.getCurrencyCode())
          .effectiveRate(effectiveRate)
          .financingAmt(fin.getFinancingAmt())
          .financingDueDate(fin.getFinancingDueDate())
          .financingStatus(fin.getFinancingStatus())
          .financingHdrCode(fin.getFinancingHdrCode())
          .retention(fin.getRetention())
          .totalInvoiceAmt(fin.getTotalInvoiceAmt())
          .interestAmt(fin.getInterestAmt())


          .build();

        if (histCode != null && histCode.length() >= 32) {
          Optional<SimulationHist> simulationHist = simulationHistRepository.findTopBySimulationHistCode(UUID.fromString(histCode));
          if (simulationHist.isPresent()) {
            SimulationHist simH = simulationHist.get();
            effectiveRate = simH.getEffectiveRate();
            if (effectiveRate < 1) {
              effectiveRate = effectiveRate * 100;
            } else if (effectiveRate > 100) {
              effectiveRate = Math.floor(effectiveRate / 100);
            }

            SimulationHistDto simulationHistDto = SimulationHistDto.builder()
              .schema(100 - simH.getRetention())
              .effetiveRate(effectiveRate)
              .dibursmentAmt(simH.getEstDisbust())
              .totalInvoiceAmt(simH.getTotalInvoiceAmt())
              .adminAmt(simH.getAdminAmt())
              .isUsed(simH.getIsUsed())
              .retention(simH.getRetention())
              .build();

            financingHdrDto.setSimulationHist(simulationHistDto);
          }
        }

        return financingHdrDto;
      }
      return null;
    } catch (Exception e) {
      log.error("calculateDisburse, error {}", e.getMessage());
      throw e;
    }
  }

  public EstimatedDisburseDto recalculateDisburse(
    Authentication authentication,
    HttpServletRequest request
  ) throws Exception {
    try {
      int schemaRate = Utils.getInt(request.getParameter("schemaRate"));
      int intestRate = Utils.getInt(request.getParameter("intestRate"));

      double adminFee = Utils.getInt(request.getParameter("adminFee"));
      String financingHdrCode = request.getParameter("financingHdrCode");
      Optional<FinancingHdr> financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
      if (financingHdr.isEmpty()) {
        throw new Exception("financingHdr not found");
      }
      FinancingHdr finHdr = financingHdr.get();

      final BigDecimal ntfResult = new BigDecimal(finHdr.getTotalInvoiceAmt())
        .multiply(BigDecimal.valueOf(schemaRate / 100.0));
      //.setScale(0, RoundingMode.UP);

      final Optional<Product> findProduct = productRepository.findNtfRange(ntfResult.doubleValue());

      if (findProduct.isEmpty()) {
        // return null;
        throw new IllegalStateException("Mohon maaf, Product yang sesuai limit tidak ditemukan");

      }

      final Product product = findProduct.get();

      Double provisionRate = findProduct.get().getProvisionRate(),
        effectiveRate = findProduct.get().getEffectiveRate(),
        adminRate = findProduct.get().getAdminRate();


      Optional<Agreement> agreements = agreementRepository.findTopByFinancingHdr(finHdr);
      if (agreements.isEmpty()) {
        //throw new Exception("agreements not found");
      }


      BigDecimal
        provisionFeeAmount,
        adminFeeAmount,
        othersFeeAmount,
        surveyFeeAmount,
        legalFeeAmount;


      int tenor = finHdr.getTenor().intValue();


      double nilaiPembiayaan = ntfResult.doubleValue(); //total invaoice * % pembiayaan
      double effective_Rate = product.getEffectiveRate();
      effective_Rate = intestRate;//kiriman dari client

      if (effective_Rate < 1) {
        effective_Rate = effective_Rate * 100;
      } else if (effective_Rate > 100) {
        effective_Rate = Math.floor(effective_Rate / 100);
      }

      double interestAmount = nilaiPembiayaan * effective_Rate / 100;
      //double adminFee = product.getAdminRate() * nilaiPembiayaan / 100;
      double jumlahBiaya = 0;
      double provisionRateFee = finHdr.getProvisionFeeAmt();//  product.getProvisionRate() * plafonLimit / 100;


      boolean isCustomerExisting = finHdr.getSurveyFeeAmt().intValue() != 0;
      if (!isCustomerExisting) {
        //newCustomer
        jumlahBiaya = provisionRateFee
          + product.getSurveyFee()
          + product.getLegalFee()
          + adminFee
          + product.getOthersFee();
        provisionFeeAmount = new BigDecimal(provisionRateFee).setScale(0, RoundingMode.HALF_UP);
        surveyFeeAmount = new BigDecimal(product.getSurveyFee()).setScale(0, RoundingMode.HALF_UP);
        legalFeeAmount = new BigDecimal(product.getLegalFee()).setScale(0, RoundingMode.HALF_UP);
        adminFeeAmount = new BigDecimal(adminFee).setScale(0, RoundingMode.HALF_UP);
        othersFeeAmount = new BigDecimal(product.getOthersFee()).setScale(0, RoundingMode.HALF_UP);
      } else {
        provisionFeeAmount = new BigDecimal(0);
        surveyFeeAmount = new BigDecimal(0);
        legalFeeAmount = new BigDecimal(0);
        adminFeeAmount = new BigDecimal(adminFee).setScale(0, RoundingMode.HALF_UP);
        othersFeeAmount = new BigDecimal(0);
        jumlahBiaya = adminFee + product.getOthersFee();
      }

      double nilaiYangdiCarikan = nilaiPembiayaan - jumlahBiaya;
      final BigDecimal serviceFee = new BigDecimal(jumlahBiaya).setScale(0, RoundingMode.HALF_UP);
      final BigDecimal estimated = new BigDecimal(nilaiYangdiCarikan).setScale(0, RoundingMode.HALF_UP);

      return EstimatedDisburseDto.builder()
        .productId(product.getProductId())
        .financingAmount(ntfResult.setScale(0, RoundingMode.HALF_UP)) //yng diajukan
        .serviceFeeAmount(serviceFee)
        .estimatedDisburseAmount(estimated)
        .interestFeeAmount(new BigDecimal(interestAmount).setScale(0, RoundingMode.HALF_UP))//interest
        .provisionFeeAmount(provisionFeeAmount)
        .adminFeeAmount(adminFeeAmount)
        .othersFeeAmount(othersFeeAmount)
        .legalFeeAmount(legalFeeAmount)
        .surveyFeeAmount(surveyFeeAmount)
        .adminRate(adminRate)
        .effectiveRate(effectiveRate)
        .provisionRate(provisionRate)
        .build();
    } catch (Exception e) {
      log.error("recalculateDisburse, error {}", e.getMessage());
      throw e;
    }
  }

  @Transactional
  public CreatedSimulationDto updateSimulation(
    Authentication authentication,
    HttpServletRequest request
  ) throws Exception {
    try {
      String histCode = request.getParameter("histCode");
      String financingHdrCode = request.getParameter("financingHdrCode");
      if (histCode != null && histCode.length() >= 32) {
        Optional<SimulationHist> simulationHistOptional = simulationHistRepository.findTopBySimulationHistCode(UUID.fromString(histCode));
        if (simulationHistOptional.isPresent()) {
          SimulationHist simulationHist = simulationHistOptional.get();
          Optional<FinancingHdr> financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
          if (financingHdr.isEmpty()) {
            throw new Exception("financingHdr not found");
          }
          FinancingHdr finHdr = financingHdr.get();
          finHdr.setDisburseAmt(simulationHist.getEstDisbust());
          finHdr.setRetention(simulationHist.getRetention());
          finHdr.setAdminFeeAmt(simulationHist.getAdminAmt());
          finHdr.setInterestAmt(simulationHist.getInterestAmt());
          if (simulationHist.getEffectiveRate() < 1) {
            finHdr.setEffectiveRate(simulationHist.getEffectiveRate() * 100);
          } else if (simulationHist.getEffectiveRate() > 100) {
            finHdr.setEffectiveRate(Math.floor(simulationHist.getEffectiveRate() / 100));
          } else {
            finHdr.setEffectiveRate(simulationHist.getEffectiveRate());
          }
          finHdr.setFinancingAmt(simulationHist.getFinancingAmt());
          financingHdrRepository.save(finHdr);

          //send email Perubahan Simuilasi Ke debitur
          try {
            final FinancingHdr financing = finHdr;
            Customer customer = financing.getCustomer();


            final FinancingHdrDto createdFinancing = financingHdrService.dtoFromEntity(financing);

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
                  .bouwheerName(createdFinancing.getBouwheer().getBouwheerName())
                  .build()
              ).toList();


            final double totalFeeAmt =
              createdFinancing.getAdminFeeAmt()
                + createdFinancing.getLegalFeeAmtNett()
                + createdFinancing.getInsuranceFeeAmt()
                + createdFinancing.getOthersFeeAmt()
                + createdFinancing.getProvisionFeeAmt()
                + createdFinancing.getSurveyFeeAmtNett();

            String phoneNumber = createdFinancing.getCustomer().getCustMobilePhone();
            if (createdFinancing.getCustomer().getCustTypeCode().equalsIgnoreCase("Company")) {
              Optional<CustomerCompany> customerCompany = customerCompanyRepository.findByCustomer(createdFinancing.getCustomer());
              if (customerCompany.isPresent()) {
                if (customerCompany.get().getPhone() != null && !customerCompany.get().getPhone().equalsIgnoreCase("")) {
                  phoneNumber = customerCompany.get().getPhone();
                }
              }
            } else {
              Optional<CustomerPersonal> customerPersonal = customerPersonalRepository.findByCustomer(createdFinancing.getCustomer());
              if (customerPersonal.isPresent()) {
                if (customerPersonal.get().getPhone() != null && !customerPersonal.get().getPhone().equalsIgnoreCase("")) {
                  phoneNumber = customerPersonal.get().getPhone();
                }
              }
            }
            emailService.sendPerubahanSimulasi(
              customer,
              LoanDisburseEmailPayload.builder()
                .financingCode(createdFinancing.getFinancingHdrCode().toString())
                .applicationDate(DateTimeUtils.formatToDate(createdFinancing.getDisburseDate()))
                .companyName(customer.getCustName())//createdFinancing.getBouwheer().getBouwheerName()
                .phoneNumber(phoneNumber)
                .tenor(createdFinancing.getTenor())
                .financingCode(createdFinancing.getFinancingHdrCode().toString())
                .financingDueDate(DateTimeUtils.formatToDate(createdFinancing.getFinancingDueDate()))
                .retention(CommonFormattingUtils.formatAmount(createdFinancing.getRetention()))
                .financingAmt(CommonFormattingUtils.formatAmount(createdFinancing.getFinancingAmt()))
                .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
                .invoiceAmt(CommonFormattingUtils.formatAmount(createdFinancing.getTotalInvoiceAmt()))
                .disburseAmt(CommonFormattingUtils.formatAmount(createdFinancing.getDisburseAmt()))
                .invoices(invoices)
                .build()
            );
          } catch (Exception e) {
          }

          return null;
        } else {
          throw new Exception("SimulationHist not found");
        }
      }


      EstimatedDisburseDto estimatedDisburseDto = recalculateDisburse(authentication, request);


      Optional<FinancingHdr> financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
      if (financingHdr.isEmpty()) {
        throw new Exception("financingHdr not found");

      }
      int schemaRate = Utils.getInt(request.getParameter("schemaRate"));
      double intestRate = Utils.getInt(request.getParameter("intestRate"));

      double effRate = intestRate / 100;

      FinancingHdr finHdr = financingHdr.get();
          /*finHdr.setDisburseAmt(estimatedDisburseDto.getEstimatedDisburseAmount().doubleValue());
          finHdr.setRetention(Double.valueOf(100-schemaRate));
          finHdr.setAdminFeeAmt(estimatedDisburseDto.getAdminFeeAmount().doubleValue());
          finHdr.setInterestAmt(estimatedDisburseDto.getInterestFeeAmount().doubleValue());
          finHdr.setEffectiveRate(Double.valueOf(effRate));
          finHdr.setFinancingAmt(estimatedDisburseDto.getFinancingAmount().doubleValue());
          //financingHdrRepository.save(finHdr);tidak jadi insert*/


      SimulationHist simulationHist = SimulationHist.builder()
        .simulationHistCode(UUID.randomUUID())
        .financingHdr(finHdr)
        .adminAmt(estimatedDisburseDto.getAdminFeeAmount().doubleValue())
        .retention((double) (100 - schemaRate))
        .totalInvoiceAmt(finHdr.getTotalInvoiceAmt())
        .financingAmt(estimatedDisburseDto.getFinancingAmount().doubleValue())
        .effectiveRate(effRate)
        .estDisbust(estimatedDisburseDto.getEstimatedDisburseAmount().doubleValue())
        .interestAmt(estimatedDisburseDto.getInterestFeeAmount().doubleValue())
        .usrCrt("system")
        .dtmCrt(DateTimeUtils.nowLocal())
        .build();


      String histCodeUpdate = request.getParameter("histCodeUpdate");
      if (histCodeUpdate != null && histCodeUpdate.length() >= 32) {
        Optional<SimulationHist> simulationHistOptional = simulationHistRepository.findTopBySimulationHistCode(UUID.fromString(histCodeUpdate));
        if (simulationHistOptional.isPresent()) {
          simulationHist = simulationHistOptional.get();

          simulationHist.setAdminAmt(estimatedDisburseDto.getAdminFeeAmount().doubleValue());
          simulationHist.setRetention((double) (100 - schemaRate));
          simulationHist.setTotalInvoiceAmt(finHdr.getTotalInvoiceAmt());
          simulationHist.setFinancingAmt(estimatedDisburseDto.getFinancingAmount().doubleValue());
          simulationHist.setEffectiveRate(effRate);
          simulationHist.setEstDisbust(estimatedDisburseDto.getEstimatedDisburseAmount().doubleValue());
          simulationHist.setInterestAmt(estimatedDisburseDto.getInterestFeeAmount().doubleValue());
          simulationHist.setUsrUpd("system");
          simulationHist.setDtmUpd(DateTimeUtils.nowLocal());

        }
      }
      simulationHistRepository.save(simulationHist);

           /*emailService.sendNotificationChangeLimit(
                customer,
                LoanDisburseEmailPayload.builder()
                        .financingCode(createdFinancing.getFinancingHdrCode().toString())
                        .applicationDate(DateTimeUtils.formatToDate(createdFinancing.getDisburseDate()))
                        //.companyName(createdFinancing.getBouwheer().getBouwheerName())
                        .companyName(createdFinancing.getCustomer().getCustName())
                        .phoneNumber(phoneNumber)
                        .tenor(createdFinancing.getTenor())
                        .financingCode(createdFinancing.getFinancingHdrCode().toString())
                        .financingDueDate(DateTimeUtils.formatToDate(createdFinancing.getFinancingDueDate()))
                        .retention(CommonFormattingUtils.formatAmount(createdFinancing.getRetention()))
                        .financingAmt(CommonFormattingUtils.formatAmount(createdFinancing.getFinancingAmt()))
                        .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
                        .invoiceAmt(CommonFormattingUtils.formatAmount(createdFinancing.getTotalInvoiceAmt()))
                        .disburseAmt(CommonFormattingUtils.formatAmount(createdFinancing.getDisburseAmt()))
                        .invoices(invoices)
                        .build()
        );*/
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception(e.getMessage());
    }


    return null;
       /* try {
            final String bouwheerCode = "";//request.getInvoices().getFirst().getBouwheerCode();
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            if (customer == null) {
                throw CommonInvalidException.cannotAccessResource();
            }

            final Product product = productRepository.findById(request.getProductId()).orElseThrow();
            final Bouwheer bouwheer = bouwheerRepository.findByBouwheerCode(UUID.fromString(bouwheerCode))
                    .orElseThrow(() -> new IllegalStateException("Bouwheer not found or not valid"));

            final double totalInvoiceAmount = request.getInvoices()
                    .stream()
                    .mapToDouble((item) -> item.getInvoiceAmount().doubleValue())
                    .sum();

            final Date maxInvoiceDueDate = request.getInvoices()
                    .stream()
                    .map(PostedInvoicePayload::getInvoiceDueDate)
                    .max(Date::compareTo)
                    .get();

            final CalculateSimulationRequest simulation = new CalculateSimulationRequest();
            {
                simulation.setDisbursePercentage(request.getDisbursePercentage());
                simulation.setTotalInvoiceAmount(BigDecimal.valueOf(totalInvoiceAmount).setScale(2, RoundingMode.CEILING));
                simulation.setBouwheerCode(request.getInvoices().getFirst().getBouwheerCode());
                simulation.setInvoiceDueDate(
                        DateTimeUtils.SDF_STANDARD_RESPONSE_DATE.format(request.getInvoices().getFirst().getInvoiceDueDate())
                );
            }

            final EstimatedDisburseDto calculateDisburse = calculateDisburse(authentication, simulation);
            if (calculateDisburse.getEstimatedDisburseAmount().doubleValue() < 0) {
                throw new IllegalStateException("Mohon maaf anda tidak dapat melanjutkan pengajuan\n" +
                        "Saat ini pengajuan Anda negatif, silakan tambahkan invoice untuk melanjutkan pengajuan");
            }

            final SimulationDisburseResult simulationDisburseResult = SimulationDisburseResult.builder()
                    .financingAmount(calculateDisburse.getFinancingAmount())
                    .estimatedDisburseAmount(calculateDisburse.getEstimatedDisburseAmount())
                    .maxInvoiceDate(maxInvoiceDueDate)
                    .totalInvoiceAmount(totalInvoiceAmount)
                    .interestFeeAmount(calculateDisburse.getInterestFeeAmount())
                    .provisionFeeAmount(calculateDisburse.getProvisionFeeAmount())
                    .adminFeeAmount(calculateDisburse.getAdminFeeAmount())
                    .othersFeeAmount(calculateDisburse.getOthersFeeAmount())
                    .legalFeeAmount(calculateDisburse.getLegalFeeAmount())
                    .surveyFeeAmount(calculateDisburse.getSurveyFeeAmount())
                    .adminRate(calculateDisburse.getAdminRate())
                    .effectiveRate(calculateDisburse.getEffectiveRate())
                    .provisionRate(calculateDisburse.getProvisionRate())
                    .build();

            final FinancingHdr createdFinancingHdr = financingHdrService.create(
                    authentication,
                    customer,
                    bouwheer,
                    product,
                    request,
                    simulationDisburseResult
            );

           *//* final VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(authentication, null);
            final InquiryInvoiceRemoteDto inquiryInvoiceRemote;

            try {
                inquiryInvoiceRemote = invoiceRemoteDto.inquiryInvoice(vendorTokenExtractor.getVendorCode()).getData();
                List<InquiryInvoiceRemoteDto.InvoiceRemoteDto> invoiceRemoteDto = inquiryInvoiceRemote.getRow();
                List<PostedInvoiceDto> postedInvoices = new ArrayList<>();
                for (InquiryInvoiceRemoteDto.InvoiceRemoteDto invoice : invoiceRemoteDto) {
                    for (PostedInvoicePayload postedInvoicePayload : request.getInvoices()) {
                        if (invoice.getReference().equals(postedInvoicePayload.getInvoiceCode())) {
                            postedInvoices.add(postedInvoicePayload.toPostedInvoiceDto());
                        }
                    }
                }
            } catch (Exception e) {
                //throw new IllegalStateException("Terjdi kesalahan saat mengambil data invoice dari pihak PT. Trakindo Utama.");
            }*//*

            final List<InvoiceDto> createdInvoices = invoiceService.createBulk(customer, bouwheer, request);

            financingDtlService.createBulk(
                    customer,
                    bouwheer,
                    createdFinancingHdr,
                    request.getInvoices(),
                    createdInvoices
            );

            return CreatedSimulationDto.builder()
                    .productId(request.getProductId())
                    .financingHdrCode(createdFinancingHdr.getFinancingHdrCode())
                    .invoices(createdInvoices)
                    .build();
        } catch (Exception e) {
            log.error("createSimulation, error {}", e.getMessage());
            throw e;
        }*/
  }

  @Transactional
  public CreatedSimulationDto createSimulation(
    Authentication authentication,
    CreateSimulationRequest request
  ) throws Exception {
    try {
      final String bouwheerCode = request.getInvoices().getFirst().getBouwheerCode();
      final Customer customer = CustomerUtils.authenticateCustomer(authentication);
      if (customer == null) {
        throw CommonInvalidException.cannotAccessResource();
      }

      final Product product = productRepository.findById(request.getProductId()).orElseThrow();
      final Bouwheer bouwheer = bouwheerRepository.findByBouwheerCode(UUID.fromString(bouwheerCode))
        .orElseThrow(() -> new IllegalStateException("Bouwheer not found or not valid"));

      final double totalInvoiceAmount = request.getInvoices()
        .stream()
        .mapToDouble((item) -> item.getInvoiceAmount().doubleValue())
        .sum();

      final Date maxInvoiceDueDate = request.getInvoices()
        .stream()
        .map(PostedInvoicePayload::getInvoiceDueDate)
        .max(Date::compareTo)
        .get();

      final CalculateSimulationRequest simulation = new CalculateSimulationRequest();
      {
        simulation.setDisbursePercentage(request.getDisbursePercentage());
        simulation.setTotalInvoiceAmount(BigDecimal.valueOf(totalInvoiceAmount).setScale(2, RoundingMode.CEILING));
        simulation.setBouwheerCode(request.getInvoices().getFirst().getBouwheerCode());
        simulation.setInvoiceDueDate(
          DateTimeUtils.SDF_STANDARD_RESPONSE_DATE.format(request.getInvoices().getFirst().getInvoiceDueDate())
        );
      }

      final EstimatedDisburseDto calculateDisburse = calculateDisburse(authentication, simulation);
      if (calculateDisburse.getEstimatedDisburseAmount().doubleValue() < 0) {
        throw new IllegalStateException("Mohon maaf anda tidak dapat melanjutkan pengajuan\n" +
          "Saat ini pengajuan Anda negatif, silakan tambahkan invoice untuk melanjutkan pengajuan");
      }


      if (calculateDisburse.getTotalInvoiceAmount().doubleValue() < 50000000) {
        throw new IllegalStateException("Untuk melanjukan pengajuan silahkan tambahkan jumlah invoice yang ingin" + " " +
          "diajukan hingga mencapai minimal   Rp 50.000.000");
      }

      final SimulationDisburseResult simulationDisburseResult = SimulationDisburseResult.builder()
        .financingAmount(calculateDisburse.getFinancingAmount())
        .estimatedDisburseAmount(calculateDisburse.getEstimatedDisburseAmount())
        .maxInvoiceDate(maxInvoiceDueDate)
        .totalInvoiceAmount(totalInvoiceAmount)
        .interestFeeAmount(calculateDisburse.getInterestFeeAmount())
        .provisionFeeAmount(calculateDisburse.getProvisionFeeAmount())
        .adminFeeAmount(calculateDisburse.getAdminFeeAmount())
        .othersFeeAmount(calculateDisburse.getOthersFeeAmount())
        .legalFeeAmount(calculateDisburse.getLegalFeeAmount())
        .surveyFeeAmount(calculateDisburse.getSurveyFeeAmount())
        .adminRate(calculateDisburse.getAdminRate())
        .effectiveRate(calculateDisburse.getEffectiveRate())
        .provisionRate(calculateDisburse.getProvisionRate())
        .build();

      final FinancingHdr createdFinancingHdr = financingHdrService.create(
        authentication,
        customer,
        bouwheer,
        product,
        request,
        simulationDisburseResult
      );

      final List<InvoiceDto> createdInvoices = invoiceService.createBulk(customer, bouwheer, request);

      financingDtlService.createBulk(
        customer,
        bouwheer,
        createdFinancingHdr,
        request.getInvoices(),
        createdInvoices
      );

      return CreatedSimulationDto.builder()
        .productId(request.getProductId())
        .financingHdrCode(createdFinancingHdr.getFinancingHdrCode())
        .invoices(createdInvoices)
        .build();
    } catch (Exception e) {
      log.error("createSimulation, error {}", e.getMessage());
      throw e;
    }
  }


  @Transactional
  public CreatedSimulationDto createSimulation_TU(
    Authentication authentication,
    CreateSimulationRequest request
  ) throws Exception {
    try {
      final String bouwheerCode = request.getInvoices().getFirst().getBouwheerCode();
      final Customer customer = CustomerUtils.authenticateCustomer(authentication);
      if (customer == null) {
        throw CommonInvalidException.cannotAccessResource();
      }

      final Product product = productRepository.findById(request.getProductId()).orElseThrow();
      final Bouwheer bouwheer = bouwheerRepository.findByBouwheerCode(UUID.fromString(bouwheerCode))
        .orElseThrow(() -> new IllegalStateException("Bouwheer not found or not valid"));

      final double totalInvoiceAmount = request.getInvoices()
        .stream()
        .mapToDouble((item) -> item.getInvoiceAmount().doubleValue())
        .sum();

      final Date maxInvoiceDueDate = request.getInvoices()
        .stream()
        .map(PostedInvoicePayload::getInvoiceDueDate)
        .max(Date::compareTo)
        .get();

      final CalculateSimulationRequest simulation = new CalculateSimulationRequest();
      {
        simulation.setDisbursePercentage(request.getDisbursePercentage());
        simulation.setTotalInvoiceAmount(BigDecimal.valueOf(totalInvoiceAmount).setScale(2, RoundingMode.CEILING));
        simulation.setBouwheerCode(request.getInvoices().getFirst().getBouwheerCode());
        simulation.setInvoiceDueDate(
          DateTimeUtils.SDF_STANDARD_RESPONSE_DATE.format(request.getInvoices().getFirst().getInvoiceDueDate())
        );
      }

      final EstimatedDisburseDto calculateDisburse = calculateDisburse(authentication, simulation);
      if (calculateDisburse.getEstimatedDisburseAmount().doubleValue() < 0) {
        throw new IllegalStateException("Mohon maaf anda tidak dapat melanjutkan pengajuan\n" +
          "Saat ini pengajuan Anda negatif, silakan tambahkan invoice untuk melanjutkan pengajuan");
      }


      if (calculateDisburse.getTotalInvoiceAmount().doubleValue() < 50000000) {
        throw new IllegalStateException("Untuk melanjukan pengajuan silahkan tambahkan jumlah invoice yang ingin" + " " +
          "diajukan hingga mencapai minimal   Rp 50.000.000");
      }

      final SimulationDisburseResult simulationDisburseResult = SimulationDisburseResult.builder()
        .financingAmount(calculateDisburse.getFinancingAmount())
        .estimatedDisburseAmount(calculateDisburse.getEstimatedDisburseAmount())
        .maxInvoiceDate(maxInvoiceDueDate)
        .totalInvoiceAmount(totalInvoiceAmount)
        .interestFeeAmount(calculateDisburse.getInterestFeeAmount())
        .provisionFeeAmount(calculateDisburse.getProvisionFeeAmount())
        .adminFeeAmount(calculateDisburse.getAdminFeeAmount())
        .othersFeeAmount(calculateDisburse.getOthersFeeAmount())
        .legalFeeAmount(calculateDisburse.getLegalFeeAmount())
        .surveyFeeAmount(calculateDisburse.getSurveyFeeAmount())
        .adminRate(calculateDisburse.getAdminRate())
        .effectiveRate(calculateDisburse.getEffectiveRate())
        .provisionRate(calculateDisburse.getProvisionRate())
        .build();

      final FinancingHdr createdFinancingHdr = financingHdrService.create(
        authentication,
        customer,
        bouwheer,
        product,
        request,
        simulationDisburseResult
      );

           /* final VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(authentication, null);
            final InquiryInvoiceRemoteDto inquiryInvoiceRemote;

            try {
                inquiryInvoiceRemote = invoiceRemoteDto.inquiryInvoice(vendorTokenExtractor.getVendorCode()).getData();
                List<InquiryInvoiceRemoteDto.InvoiceRemoteDto> invoiceRemoteDto = inquiryInvoiceRemote.getRow();
                List<PostedInvoiceDto> postedInvoices = new ArrayList<>();
                for (InquiryInvoiceRemoteDto.InvoiceRemoteDto invoice : invoiceRemoteDto) {
                    for (PostedInvoicePayload postedInvoicePayload : request.getInvoices()) {
                        if (invoice.getReference().equals(postedInvoicePayload.getInvoiceCode())) {
                            postedInvoices.add(postedInvoicePayload.toPostedInvoiceDto());
                        }
                    }
                }
            } catch (Exception e) {
                //throw new IllegalStateException("Terjdi kesalahan saat mengambil data invoice dari pihak PT. Trakindo Utama.");
            }*/

      final List<InvoiceDto> createdInvoices = invoiceService.createBulk(customer, bouwheer, request);

      financingDtlService.createBulk(
        customer,
        bouwheer,
        createdFinancingHdr,
        request.getInvoices(),
        createdInvoices
      );

      return CreatedSimulationDto.builder()
        .productId(request.getProductId())
        .financingHdrCode(createdFinancingHdr.getFinancingHdrCode())
        .invoices(createdInvoices)
        .build();
    } catch (Exception e) {
      log.error("createSimulation, error {}", e.getMessage());
      throw e;
    }
  }

  public void createLoanSubmission(
    Authentication authentication,
    CreateLoanApplicationRequest request
  ) throws Exception {
    try {
      final Customer custCOde = CustomerUtils.authenticateCustomer(authentication);
      if (custCOde == null) {
        throw CommonInvalidException.cannotAccessResource();
      }

      if (!bcryptEncoder.matches(request.getPin(), custCOde.getCustPin())) {
        throw new BadCredentialsException("Pin is invalid, try to entry right pin");
      }

      List<LegalFile> legalFiles = legalFileRepository.findAllByCustCode(custCOde);
      if (
        legalFiles
          .stream()
          .noneMatch(
            file -> file.getFileTypeCode()
              .getFileTypeCode()
              .equalsIgnoreCase("DOC006")
              && DateTimeUtils.SDF_STANDARD_DATE.format(Utils.fromInstant(file.getDtmUpd()))
              .equalsIgnoreCase(DateTimeUtils.SDF_STANDARD_DATE.format(new Date()))
          )
      ) {
        //throw new LoanDocMandatoryException("Surat Instruksi Transfer belum di perbaharui, silahkan upload terlebih dahulu");
      }

      List<MstFileType> mandatoryFile = mstFileTypeService.getAllMandatory();
      for (MstFileType mstFileType : mandatoryFile) {
        if (
          legalFiles
            .stream()
            .noneMatch(file -> file.getFileTypeCode().getFileTypeCode().equals(mstFileType.getFileTypeCode()))
        ) {

          // throw new LoanDocMandatoryException("Harap upload semua dokumen mandatory terlebih dahulu");//AGGREMENT01 bypass
        }
      }


      Customer customer = customerRepository.findByCustCode(custCOde.getCustCode()).get();


      final FinancingHdr financing = financingHdrService.getByCode(request.getFinancingHdrCode());
      {
        financing.setFinancingStatus(FinancingStatus.NEW.name());
        financing.setFinancingStep(FinancingStatus.NEW.name());
        financing.setDtmUpd(DateTimeUtils.now());
        financing.setUsrUpd(customer.getCustName());


        try {
          String city = "", kelurahan = "", kecamatan = "";
          if (financing.getCustomer() != null) {
            if (financing.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
              if (financing.getCustomer().getCompany() != null) {
                city = financing.getCustomer().getCompany().getCity();
                kelurahan = financing.getCustomer().getCompany().getKelurahan();
                kecamatan = financing.getCustomer().getCompany().getKecamatan();
              }
            } else {
              if (financing.getCustomer().getPersonal() != null) {
                city = financing.getCustomer().getPersonal().getCity();
                kelurahan = financing.getCustomer().getPersonal().getKelurahan();
                kecamatan = financing.getCustomer().getPersonal().getKecamatan();
              }
            }
          }
          Optional<MstBranch> mstBranch = mstBranchRepository.findTopLikeBranchNameRawQuery(city, kelurahan, kecamatan);
          mstBranch.ifPresent(financing::setMstBranch);
        } catch (Exception ignored) {
        }


        boolean isAutoASSIGNMENT = false;
        //set auto ASSIGNMENT
        try {
          List<FinancingHdr> financingHdrs = financingHdrRepository.findAllByCustomerOrderByDtmCrtDesc(customer);
          for (int t = 0; t < financingHdrs.size(); t++) {
            MstBranch hdrBranch = financingHdrs.get(t).getMstBranch();

            if (hdrBranch != null) {

              //update jadi Assign
              financing.setFinancingStatus("INPROCESS");
              financing.setFinancingStep("ASSIGNMENT");
              financing.setMstBranch(hdrBranch);
              financing.setDtmUpd(DateTimeUtils.now());
              //send email

              try {
                final List<InvoiceEmailPayload> invoices = financing.getFinancingDtls()
                  .stream()
                  .map((item) ->
                    InvoiceEmailPayload.builder()
                      .invoiceNo(item.getInvoice().getCustInvNo())
                      .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt().doubleValue()))
                      .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
                      .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
                      .description("Invoice By Trakindo")
                      .bouwheerName(financing.getBouwheer().getBouwheerName())
                      .build()
                  ).toList();
                final double totalFeeAmt =
                  financing.getAdminFeeAmt()
                    + financing.getLegalFeeAmtNett()
                    + financing.getInsuranceFeeAmt()
                    + financing.getOthersFeeAmt()
                    + financing.getProvisionFeeAmt()
                    + financing.getSurveyFeeAmtNett();
                //getAPI AO,BH
                MailPositionDto to = configRemoteService.getEmailByPosition("", hdrBranch.getBranchCode(), "BM/BOH");
                MailPositionDto ccRM = configRemoteService.getEmailByPosition("", hdrBranch.getBranchCode(), "RM");
                MailPositionDto ccAO = configRemoteService.getEmailByPosition("", hdrBranch.getBranchCode(), "AO/AM");

                String toEmail = hdrBranch.getEmployees().stream().toList().getFirst().getEmail();  //"radema.panjaitan@csul.co.id",
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

                String phone = financing.getCustomer().getCustMobilePhone();
                if (financing.getCustomer().getCustTypeCode().equalsIgnoreCase("Company")) {
                  if (financing.getCustomer().getCompany() != null) {
                    phone = financing.getCustomer().getCompany().getPhone();
                  }
                }

                isAutoASSIGNMENT = true;
                //kirim email assign dan re assign
                emailService.sendNotificationBranchAssign(
                  toEmail,
                  financing.getBouwheer().getBouwheerName(),
                  financing.getMstBranch().getBranchName(),
                  LoanDisburseEmailPayload.builder()
                    .financingCode(financing.getFinancingHdrCode().toString())
                    .applicationDate(DateTimeUtils.formatToDate(financing.getFinancingDate()))
                    .companyName(financing.getCustomer().getCustName())
                    .email(financing.getCustomer().getCustEmail())
                    .phoneNumber(phone)
                    .tenor(financing.getTenor())
                    .toEmail(toEmail)
                    .ccEmail(ccEmail)
                    .financingCode(financing.getFinancingHdrCode().toString())
                    .financingDueDate(DateTimeUtils.formatToDate(financing.getFinancingDueDate()))
                    .retention(CommonFormattingUtils.formatAmount(financing.getRetention()))
                    .financingAmt(CommonFormattingUtils.formatAmount(financing.getFinancingAmt()))
                    .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
                    .invoiceAmt(CommonFormattingUtils.formatAmount(financing.getTotalInvoiceAmt()))
                    .disburseAmt(CommonFormattingUtils.formatAmount(financing.getDisburseAmt()))
                    .invoices(invoices)
                    .build()
                );
              } catch (Exception ignored) {

              }
              break;
            }
          }

        } catch (Exception e) {
        }


        if (!isAutoASSIGNMENT) {
          //sed to major account
          try {
//                        String mjrEmail = "radema.panjaitan@csul.co.id";


            final List<InvoiceEmailPayload> invoices = financing.getFinancingDtls()
              .stream()
              .map((item) ->
                InvoiceEmailPayload.builder()
                  .invoiceNo(item.getInvoice().getCustInvNo())
                  .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt().doubleValue()))
                  .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
                  .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
                  .description("Invoice By Trakindo")
                  .bouwheerName(financing.getBouwheer().getBouwheerName())
                  .build()
              ).toList();
            final double totalFeeAmt =
              financing.getAdminFeeAmt()
                + financing.getLegalFeeAmtNett()
                + financing.getInsuranceFeeAmt()
                + financing.getOthersFeeAmt()
                + financing.getProvisionFeeAmt()
                + financing.getSurveyFeeAmtNett();
            //getAPI CMS
            String branchCode = mstBranchRepository.findByBranchName("HEAD OFFICE")
              .map(MstBranch::getBranchCode)
              .orElseThrow(() -> {
                return new RuntimeException("BranchCode tidak ditemukan");
              });
            MailPositionDto ccBM = configRemoteService.getEmailByPosition("", branchCode, "CMS");

//                        String toEmail =  "radema.panjaitan@csul.co.id";
            StringBuilder ccEmailBuilder = new StringBuilder();

            if (ccBM != null && ccBM.getData() != null && !ccBM.getData().isEmpty()) {
              for (int i = 0; i < ccBM.getData().size(); i++) {
                ccEmailBuilder.append(!ccEmailBuilder.isEmpty() ? ";" : "");
                ccEmailBuilder.append(ccBM.getData().get(i).getEmail());
              }
            }

            String mjrEmail = ccEmailBuilder.toString();
            String toEmail = ccEmailBuilder.toString();
//                        String ccEmail = ccEmailBuilder.toString();
            log.info("email CMS: {}", ccEmailBuilder.toString());

            String phone = financing.getCustomer().getCustMobilePhone();
            if (financing.getCustomer().getCustTypeCode().equalsIgnoreCase("Company")) {
              if (financing.getCustomer().getCompany() != null) {
                phone = financing.getCustomer().getCompany().getPhone();
              }
            }
            //kirim email assign dan re assign
            emailService.sendNotificationMajorAccount(
              mjrEmail,
              financing.getBouwheer().getBouwheerName(),
              financing.getMstBranch().getBranchName(),
              LoanDisburseEmailPayload.builder()
                .financingCode(financing.getFinancingHdrCode().toString())
                .applicationDate(DateTimeUtils.formatToDate(financing.getFinancingDate()))
                .companyName(financing.getCustomer().getCustName())
                .email(financing.getCustomer().getCustEmail())
                .phoneNumber(phone)
                .tenor(financing.getTenor())
                .toEmail(toEmail)
                .ccEmail(toEmail)
                .financingCode(financing.getFinancingHdrCode().toString())
                .financingDueDate(DateTimeUtils.formatToDate(financing.getFinancingDueDate()))
                .retention(CommonFormattingUtils.formatAmount(financing.getRetention()))
                .financingAmt(CommonFormattingUtils.formatAmount(financing.getFinancingAmt()))
                .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
                .invoiceAmt(CommonFormattingUtils.formatAmount(financing.getTotalInvoiceAmt()))
                .disburseAmt(CommonFormattingUtils.formatAmount(financing.getDisburseAmt()))
                .invoices(invoices)
                .build()
            );
          } catch (Exception e) {

          }
        }


        financingHdrRepository.save(financing);
      }

      final FinancingHdrDto createdFinancing = financingHdrService.dtoFromEntity(financing);

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
            .bouwheerName(createdFinancing.getBouwheer().getBouwheerName())
            .build()
        ).toList();


      final double totalFeeAmt =
        createdFinancing.getAdminFeeAmt()
          + createdFinancing.getLegalFeeAmtNett()
          + createdFinancing.getInsuranceFeeAmt()
          + createdFinancing.getOthersFeeAmt()
          + createdFinancing.getProvisionFeeAmt()
          + createdFinancing.getSurveyFeeAmtNett();

      String phoneNumber = createdFinancing.getCustomer().getCustMobilePhone();
      if (createdFinancing.getCustomer().getCustTypeCode().equalsIgnoreCase("Company")) {
        Optional<CustomerCompany> customerCompany = customerCompanyRepository.findByCustomer(createdFinancing.getCustomer());
        if (customerCompany.isPresent()) {
          if (customerCompany.get().getPhone() != null && !customerCompany.get().getPhone().equalsIgnoreCase("")) {
            phoneNumber = customerCompany.get().getPhone();
          }
        }
      } else {
        Optional<CustomerPersonal> customerPersonal = customerPersonalRepository.findByCustomer(createdFinancing.getCustomer());
        if (customerPersonal.isPresent()) {
          if (customerPersonal.get().getPhone() != null && !customerPersonal.get().getPhone().equalsIgnoreCase("")) {
            phoneNumber = customerPersonal.get().getPhone();
          }
        }
      }

      //kirim email setelah submit debitur
      emailService.sendNotificationLoanSubmited(
        customer,
//                    "",
        LoanDisburseEmailPayload.builder()
          .financingCode(createdFinancing.getFinancingHdrCode().toString())
          .applicationDate(DateTimeUtils.formatToDate(createdFinancing.getDisburseDate()))
          .companyName(customer.getCustName())//createdFinancing.getBouwheer().getBouwheerName()
          .phoneNumber(phoneNumber)
          .tenor(createdFinancing.getTenor())
          .financingCode(createdFinancing.getFinancingHdrCode().toString())
          .financingDueDate(DateTimeUtils.formatToDate(createdFinancing.getFinancingDueDate()))
          .retention(CommonFormattingUtils.formatAmount(createdFinancing.getRetention()))
          .financingAmt(CommonFormattingUtils.formatAmount(createdFinancing.getFinancingAmt()))
          .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
          .toEmail(customer.getCustEmail())
          .ccEmail(customer.getCustEmail())
          .invoiceAmt(CommonFormattingUtils.formatAmount(createdFinancing.getTotalInvoiceAmt()))
          .disburseAmt(CommonFormattingUtils.formatAmount(createdFinancing.getDisburseAmt()))
          .invoices(invoices)
          .build()
      );

    } catch (Exception e) {
      e.printStackTrace();
      log.error("createLoanSubmission, error {}", e.getMessage());
      throw e;
    }
  }

  public ExternalIntegrationLoanSimulationDto externalIntegrationSimulation(
    Authentication authentication,
    String token
  ) throws JsonProcessingException, SignatureException {
    try {
      if (StringUtil.isNullOrEmpty(token)) {
        return null;
      }

      VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(authentication, token);
      Optional<ExternalIntegrationLoanSimulationDto> find = importantNotesService.findExternalIntegrationByBouwheerCode(
        vendorTokenExtractor.getBouwheerCode().toString(),
        vendorTokenExtractor.getVendorCode()
      );

      InquiryVendorRemoteDto inquiryVendorRemote = customerRemoteService.inquiryVendorByBouwheer(vendorTokenExtractor).getData();

      ExternalIntegrationLoanSimulationDto result = find.orElseGet(() -> ExternalIntegrationLoanSimulationDto.builder()
        .bouwheerCode(vendorTokenExtractor.getBouwheerCode().toString())
        .alreadyAcceptImportantNotes(false)
        .build());

      result.setVendor(
        Base64
          .getUrlEncoder()
          .encodeToString(
            ObjectUtils
              .jsonToStr(
                importantNotesService
                  .generateVendor(
                    inquiryVendorRemote,
                    vendorTokenExtractor.getVendorCode(),
                    "Perusahaan"
                  )
              )
              .getBytes(StandardCharsets.UTF_8)
          )
      );

      return result;
    } catch (Exception e) {
      log.error("externalIntegrationLoanSimulation, error {}", e.getMessage());
      throw e;
    }
  }

  public ImportantNotesDto importanceNotes() {
    return importantNotesService.importantNotesDto();
  }

  public ExternalIntegrationLoanSimulationDto saveImportantNotes(SaveImportantNotesRequest request) throws SignatureException, JsonProcessingException {
    try {
      VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(null, request.getToken());
      final InquiryVendorRemoteDto inquiryVendorRemote = customerRemoteService.inquiryVendor(vendorTokenExtractor.getVendorCode()).getData();

      return importantNotesService.create(
        inquiryVendorRemote,
        vendorTokenExtractor.getBouwheerCode().toString(),
        vendorTokenExtractor.getVendorCode()
      );
    } catch (Exception e) {
      log.error("saveImportantNotes, error {}", e.getMessage());
      throw e;
    }
  }

  private VendorTokenExtractor vendorTokenExtractor(Authentication authentication, String token) throws SignatureException {
    return new VendorTokenExtractor(
      bouwheerRepository,
      jwtLoanSubmissionService,
      authentication,
      token
    );
  }

  private Cwr isCustomerExistingByCwr(Authentication authentication, String token) throws SignatureException, JsonProcessingException {
    final VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(authentication, token);
    final ExistingCustomerDto existingCustomer = existingCustomerService.findLastByVendorCode(vendorTokenExtractor.getVendorCode())
      .orElse(null);
    if (authentication == null) {
      InquiryVendorRemoteDto inquiryVendorRemote = customerRemoteService
        .inquiryVendor(vendorTokenExtractor.getVendorCode())
        .getData();

      Cwr cwr = validateCwrForCustomerExisting(
        ExistingCustomerRequest.KeyType.npwp.name(),
        inquiryVendorRemote.getNpwp(),
        null,
        null
      );

      if (existingCustomer == null) {
        existingCustomerService.createOrUpdate(
          vendorTokenExtractor.getVendorCode(),
          cwr != null,
          ExistingCustomerRequest.KeyType.npwp.name().toUpperCase(),
          inquiryVendorRemote.getNpwp()
        );
      }

      return cwr;
    }

    final Customer customer = CustomerUtils.authenticateCustomer(authentication);
    final String identityType = customer.getCustIdTypeCode(),
      identityNo = customer.getCompany() != null
        ? customer.getCompany().getIdentityNo()
        : (customer.getPersonal() != null ? customer.getPersonal().getIdentityNo() : customer.getCustIdNo());

    Cwr cwr = validateCwrForCustomerExisting(
      identityType,
      identityNo,
      customer,
      bouwheerRepository.findByBouwheerCode(vendorTokenExtractor.getBouwheerCode())
        .orElse(null)
    );

    existingCustomerService.createOrUpdate(
      customer.getCustExternalCode(),
      cwr != null,
      identityType,
      identityNo
    );

    return cwr;
  }

  private Cwr validateCwrForCustomerExisting(
    String identityType,
    String identityNo,
    Customer customer,
    Bouwheer bouwheer
  ) throws JsonProcessingException {
    try {
      return existingCustomerService.inquiryAndDecideExistingCustomer(
        ExistingCustomerRequest.KeyType.valueOf(identityType.toLowerCase()),
        identityNo,
        customer,
        bouwheer
      );
    } catch (Exception e) {
      return null;
    }
  }

  @Getter
  public static class VendorTokenExtractor {
    private final UUID bouwheerCode;
    private final String vendorCode;
    private final String bouwheerName;

    public VendorTokenExtractor(
      BouwheerRepository bouwheerRepository,
      JwtLoanSubmissionService jwtLoanSubmissionService,
      Authentication authentication,
      String token
    ) throws SignatureException {
      JwtSimulasiModel jwtSimulasiModel = jwtLoanSubmissionService.extractToken(token);
      UUID bc = jwtSimulasiModel != null ? UUID.fromString(jwtSimulasiModel.getBouwheerCode()) : UUID.randomUUID();

      final Bouwheer bouwheer;
      if (jwtSimulasiModel != null) {
        bouwheer = bouwheerRepository.findByBouwheerCode(bc).orElse(null);
      } else {
        bouwheer = bouwheerRepository.findFirstByBouwheerName();
      }

      if (bouwheer != null) {
        bouwheerCode = bouwheer.getBouwheerCode();
        bouwheerName = bouwheer.getBouwheerName();
      } else {
        bouwheerName = "PT. Trakindo Utama";
        bouwheerCode = bc;
      }

      if (jwtSimulasiModel != null) {
        vendorCode = jwtSimulasiModel.getVendorCode();
      } else if (authentication != null) {
        CustomerDto cust = CustomerUtils.authenticateCustomerDto(authentication);
        if (cust == null) {
          throw CommonInvalidException.cannotAccessResource();
        }

        vendorCode = cust.getCustExternalCode();
      } else {
        vendorCode = null;
      }

      if (vendorCode == null) {
        throw CommonInvalidException.builder()
          .title("Tidak Terdapat Invoice Yang Dapat Dibiayai")
          .message("Mohon maaf, saat ini Anda belum dapat menggunakan " +
            "Dana Sakti. Harap melakukan pengecekan ulang " +
            "dengan pihak PT. Trakindo Utama.")
          .build();
      }
    }
  }
}
