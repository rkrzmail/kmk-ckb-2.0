package com.kmkbe.modules.branch_admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.BouwheerPaymentEmailPayload;
import com.kmkbe.core.domain.model.AgreementContractEmailPayload;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.model.LoanDisburseEmailPayload;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.helpers.utils.PaginationSort;
import com.kmkbe.helpers.utils.PaginationRequests;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.FileUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.branch_admin.request.CreateInquiryAgreementRequest;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.request.FinancingSubmissionRequest;
import com.kmkbe.modules.remote.request.InquiryAgreementRemoteRequest;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgreementService {
  private static final String AGREEMENT_AUDIT_MODULE = "AGREEMENT";
  private static final String FINANCING_INVOICE_NOT_FOUND_MESSAGE = "Financing Invoice not found or not valid";
  private static final String BANK_NAME_KEY = "bankName";
  private static final String BANK_ACCOUNT_NO_KEY = "accountNo";

  private final MstFileTypeRepository mstFileTypeRepository;
  private final AgreementFileRepository agreementFileRepository;
  private final AgreementRepository agreementRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final FinancingDtlRepository financingDtlRepository;
  private final CwrRepository cwrRepository;
  private final GeneralSettingDtlRepository generalSettingDtlRepository;

  private final FinancingRemoteService financingRemoteService;
  private final CwrRemoteService cwrRemoteService;
  private final FileStorageService fileStorageService;
  private final ObjectMapper objectMapper;
  private final EmailService emailService;
  private final AuditTrailService auditTrailService;
  private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;

  public Agreement findByCode(String code) {
    try {
      return agreementRepository.findById(code).orElse(null);
    } catch (Exception e) {
      log.error("findByCode, error {}", e.getMessage());
      throw e;
    }

  }

  public Agreement findByFinancingHdr(FinancingHdr financingHdr) {
    try {
      return agreementRepository.findTopByFinancingHdr(financingHdr).orElse(null);
    } catch (Exception e) {
      log.error("findByFinancingHdr, error {}", e.getMessage());
      throw e;
    }
  }

  public PaginationResult<AgreementDto> list(
    String cwrCode, String financingHdrCode, BasePaginationRequest request
  ) throws JsonProcessingException {
    return list(cwrCode, financingHdrCode, PaginationRequests.from(request));
  }

  public void validateInvoicesForContractUpload(UUID financingHdrCode) {
    long missingInvoices = financingDtlRepository.countMissingInvoicesByFinancingHdrCode(financingHdrCode);
    if (missingInvoices > 0) {
      log.warn("Contract upload blocked: {} missing invoice(s) for financingHdrCode={}", missingInvoices, financingHdrCode);
      throw new BusinessException(
        HttpStatus.CONFLICT,
        HttpStatus.CONFLICT.value(),
        "Data invoice pengajuan tidak lengkap. Kontrak belum dapat diunggah. Hubungi administrator."
      );
    }
  }

  public PaginationResult<AgreementDto> list(
    String cwrCode,
    String financingHdrCode,
    PaginationRequest request
  ) throws JsonProcessingException {
    try {
      int pageNo = request.getPageNo() != null ? request.getPageNo() : 0;
      int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
      if (pageNo > 0) {
        pageNo = pageNo - 1;
      }

      Page<Map<String, Object>> page = findAgreementPage(cwrCode, financingHdrCode, request, pageNo, pageSize);
      Map<String, Object> bank = findCsulBank();
      List<AgreementDto> result = page.stream()
        .map(row -> toAgreementDto(row, cwrCode, bank))
        .toList();

      return PaginationResult.<AgreementDto>builder()
        .currentPage(pageNo + 1)
        .totalData(page.getTotalElements())
        .totalPage(page.getTotalPages())
        .list(result)
        .build();
    } catch (Exception e) {
      log.error("list, error {}", e.getMessage());
      throw e;
    }
  }

  private Page<Map<String, Object>> findAgreementPage(
    String cwrCode,
    String financingHdrCode,
    PaginationRequest request,
    int pageNo,
    int pageSize
  ) {
    boolean searching = request.getSearchBy() != null && request.getSearchValue() != null
      && !request.getSearchValue().isBlank();
    PageRequest pageable = PageRequest.of(pageNo, pageSize, PaginationSort.agreements(request));
    if (searching) {
      return agreementRepository.findAllListByCwrAndFinancingSearch(
        cwrCode, financingHdrCode, request.getSearchBy().toLowerCase(Locale.ROOT),
        request.getSearchValue().trim(), pageable);
    }
    return agreementRepository.findAllListByCwrAndFinancingRaw(cwrCode, financingHdrCode, pageable);
  }

  private AgreementDto toAgreementDto(Map<String, Object> row, String cwrCode, Map<String, Object> bank) {
    return AgreementDto.builder()
      .no(row.get("no") != null ? Integer.parseInt(row.get("no").toString()) : 0)
      .cwrCode(cwrCode)
      .agreementNo(row.get("agreement_code") != null ? row.get("agreement_code").toString() : null)
      .financingHdrCode(row.get("financing_hdr_code") != null ? UUID.fromString(row.get("financing_hdr_code").toString()) : null)
      .bouwheerCode(row.get("bouwheer_code") != null ? UUID.fromString(row.get("bouwheer_code").toString()) : null)
      .custCode(row.get("cust_code") != null ? UUID.fromString(row.get("cust_code").toString()) : null)
      .bouwheerName(row.get("bouwheer_name") != null ? row.get("bouwheer_name").toString() : null)
      .custName(row.get("cust_name") != null ? row.get("cust_name").toString() : null)
      .bankName(bank.get(BANK_NAME_KEY).toString())
      .rekeningNo(bank.get(BANK_ACCOUNT_NO_KEY).toString())
      .financingAmt(toDecimal(row.get("financing_amt")))
      .disburseDate(toLocalDate(row.get("disburse_date")))
      .disburseAmt(toDecimal(row.get("disburse_amt")))
      .currency(row.get("currency") != null ? row.get("currency").toString() : null)
      .build();
  }

  private LocalDate toLocalDate(Object value) {
    return value == null ? null : LocalDateTime.parse(value.toString().replace(' ', 'T')).toLocalDate();
  }

  private static BigDecimal toDecimal(Object value) {
    return new BigDecimal(value == null ? "0" : value.toString(), MathContext.DECIMAL64);
  }

  public String upload(
    MstUser user,
    MultipartFile multipartFile,
    String agreementCode,
    String bouwheerCode
  ) throws Exception {
    try {
      UUID requestedBouwheerCode = UUID.fromString(bouwheerCode);
      final MstFileType mstFileType = mstFileTypeRepository.findById("AGGREMENT01")
        .filter(fileType -> fileType.getBouwheerCode() == null
          || fileType.getBouwheerCode().equals(requestedBouwheerCode))
        .orElseThrow(
          () -> new IllegalArgumentException("File type AGGREMENT01 tidak tersedia untuk bouwheer " + bouwheerCode)
        );

      final Agreement agreement = agreementRepository.findTopByAgreementCodeOrderByAgreementId(
        agreementCode
      ).orElseThrow(() -> new IllegalArgumentException("Agreement not found"));

      AgreementFile agreementFile = agreementFileRepository.findTopByAgreementOrderByAgreementFileId(agreement)
        .orElse(null);
      AgreementFileAuditData before = toAgreementFileAuditData(agreementFile);

      if (agreement.getFinancingHdr() == null) {
        throw new IllegalArgumentException("Agreement Financing not found");
      }

      final String uploadDir = agreement.getFinancingHdr().getCustomer().getCustCode() + "/agreement";
      final String uploadName = mstFileType.getFileTypeCode() + "_" + multipartFile.getOriginalFilename();
      final String uploadedPath = fileStorageService.save(
        multipartFile,
        uploadDir,
        uploadName,//,
        null
      );

      if (agreementFile == null) {
        agreementFile = AgreementFile.builder()
          .agreement(agreement)
          .mstFileType(mstFileType)
          .fileName(uploadName)
          .filePath(FileUtils.getFilePathFromFullPath(uploadedPath))
          .contentType(multipartFile.getContentType())
          .usrCrt(user.getUsername())
          .dtmCrt(LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE))
          .build();

        AgreementFile savedAgreementFile = agreementFileRepository.save(agreementFile);
        auditTrailService.recordEvent(AGREEMENT_AUDIT_MODULE, AuditAction.UPLOAD, "AgreementFile", savedAgreementFile.getAgreementFileId(), null, toAgreementFileAuditData(savedAgreementFile));

      } else {
        agreementFile.setFileName(uploadName);
        agreementFile.setFilePath(FileUtils.getFilePathFromFullPath(uploadedPath));
        agreementFile.setDtmUpd(LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE));
        agreementFile.setUsrUpd(user.getUsername());
        AgreementFile savedAgreementFile = agreementFileRepository.save(agreementFile);
        auditTrailService.recordEvent(AGREEMENT_AUDIT_MODULE, AuditAction.UPLOAD, "AgreementFile", savedAgreementFile.getAgreementFileId(), before, toAgreementFileAuditData(savedAgreementFile));
      }

      // Split the URL by the '/' delimiter
      String[] parts = uploadedPath.split("/");
      // Get the last part of the URL
      return parts[parts.length - 1];

    } catch (Exception e) {
      log.error("upload, error {}", e.getMessage());
      throw e;
    }
  }

  public InquiryAgreementDto inquiryAgreementCwr(String agreementNo) throws JsonProcessingException {
    try {
      validateAgreement(agreementNo);
      List<InquiryAgreementCwrDto> data = fetchInquiryAgreement(agreementNo, null, true);
      if (data != null && !data.isEmpty()) {
        Map<String, Object> bank = findCsulBank();
        return InquiryAgreementDto.builder()
          .bankName(bank.get(BANK_NAME_KEY).toString())
          .rekeningNo(bank.get(BANK_ACCOUNT_NO_KEY).toString())
          .currency(data.getFirst().getCurrency())
          .disburseAmt(toDecimal(data.getFirst().getNtfAmt()))
          .build();
      }

      throw CommonInvalidException.builder()
        .title("Peringatan")
        .message("Failed to inquiry Agreement result null ")
        .build();
    } catch (Exception e) {
      log.error("inquiryAgreementCwr: error {}", e.getMessage());
      throw e;
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void createInquiryAgreement(
    MstUser user,
    CreateInquiryAgreementRequest request
  ) throws Exception {
    try {
      validateAgreement(request.getAgreementNo());
      List<InquiryAgreementCwrDto> data = fetchInquiryAgreement(
        request.getAgreementNo(),
        "Tidak bisa mencari Data Pencairan, Pastikan No. Pencairan benar",
        false
      );

      FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(request.getFinancingHdrCode()))
        .orElseThrow(() -> new IllegalStateException("Financing not found or not valid"));
      Customer customer = financingHdr.getCustomer();
      if (customer == null) {
        throw new IllegalStateException("Customer not found or not valid");
      }

      List<Agreement> agreements = buildAgreements(user, request, financingHdr, data);
      saveAgreements(agreements, request.getAgreementNo());

      FinancingAgreementAuditData before = toFinancingAgreementAuditData(financingHdr);
      proceedFinancing(financingHdr, request, customer);
      setFinancingInProcess(financingHdr);

      FinancingHdr savedFinancing = financingHdrRepository.save(financingHdr);
      auditTrailService.recordEvent(AGREEMENT_AUDIT_MODULE, AuditAction.UPDATE, "FinancingHdr", savedFinancing.getFinancingHdrCode(), before, toFinancingAgreementAuditData(savedFinancing));
      sendContractUploadRequiredNotification(savedFinancing, request.getAgreementNo(), request.getCwrCode());
    } catch (Exception e) {
      log.error("createAgreement: error {}", e.getMessage());
      throw e;
    }
  }

  private List<InquiryAgreementCwrDto> fetchInquiryAgreement(
    String agreementNo,
    String fallbackMessage,
    boolean useCauseMessage
  ) throws JsonProcessingException {
    try {
      BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = cwrRemoteService.inquiryAgreementByNoAgreement(
        InquiryAgreementRemoteRequest.builder()
          .agreementNo(agreementNo)
          .build()
      );
      return response.getData();
    } catch (Exception e) {
      throw CommonInvalidException.builder()
        .title("Peringatan")
        .message(useCauseMessage ? String.valueOf(e.getMessage()) : fallbackMessage)
        .build();
    }
  }

  private List<Agreement> buildAgreements(
    MstUser user,
    CreateInquiryAgreementRequest request,
    FinancingHdr financingHdr,
    List<InquiryAgreementCwrDto> data
  ) {
    if (data.isEmpty()) {
      return List.of();
    }

    List<Agreement> agreements = new ArrayList<>();
    for (InquiryAgreementCwrDto inquiryAgreement : data) {
      Cwr cwr = cwrRepository.findTopByCwrCode(inquiryAgreement.getCwrNo())
        .orElseThrow(() -> new IllegalStateException("Nomor CWR tidak ditemukan, pastikan nomor CWR benar"));

      if (!inquiryAgreement.getCwrNo().equalsIgnoreCase(request.getCwrCode())) {
        throw new IllegalStateException("Nomor CWR tidak sesuai dengan Nomor Pencairan, pastikan Nomor Pencairan benar");
      }

      agreements.add(
        Agreement.builder()
          .agreementCode(inquiryAgreement.getAgrmntNo())
          .cwr(cwr)
          .applicationCode(inquiryAgreement.getAppNo())
          .financingHdr(financingHdr)
          .facility(inquiryAgreement.getFacility())
          .currency(inquiryAgreement.getCurrency())
          .financingAmt(inquiryAgreement.getNtfAmt())
          .status(inquiryAgreement.getStatus())
          .productOffering(inquiryAgreement.getProductOffering())
          .usrCrt(user.getUsrCrt())
          .dtmCrt(DateTimeUtils.now())
          .build()
      );
    }
    return agreements;
  }

  private void saveAgreements(List<Agreement> agreements, String agreementNo) {
    if (agreements.isEmpty()) {
      return;
    }

    Iterable<Agreement> savedAgreements = agreementRepository.saveAll(agreements);
    auditTrailService.recordEvent(
      AGREEMENT_AUDIT_MODULE,
      AuditAction.CREATE,
      "Agreement",
      agreementNo,
      null,
      agreementsAuditData(savedAgreements)
    );
  }

  private void setFinancingInProcess(FinancingHdr financingHdr) {
    financingHdr.setFinancingStatus(financingHdr.getFinancingStatus().equalsIgnoreCase("LIVE")
      ? financingHdr.getFinancingStatus()
      : "INPROCESS");
    financingHdr.setFinancingStep(financingHdr.getFinancingStep().equalsIgnoreCase("GOLIVE")
      ? financingHdr.getFinancingStep()
      : "INPROCESS");
  }

  private void proceedFinancing(
    FinancingHdr financingHdr,
    CreateInquiryAgreementRequest request,
    Customer customer
  ) throws Exception {
    List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr)
      .orElseThrow(() -> new IllegalStateException(FINANCING_INVOICE_NOT_FOUND_MESSAGE));

    List<FinancingSubmissionRequest.FinancingInvoice> financingInvoices = financingDtls.stream()
      .filter(e -> e.getInvoice() != null && !StringUtil.isNullOrEmpty(e.getInvoice().getPoNumber()))
      .map(e -> FinancingSubmissionRequest.FinancingInvoice.builder()
        .invoiceAmount(toDecimal(e.getInvoice().getInvoiceAmt()).toString())
        .poNumber(e.getInvoice().getPoNumber())
        .reference(e.getInvoice().getCustInvNo())
        .accountingDocument(e.getInvoice().getBouwheerInvNo())
        .build())
      .toList();

    if (financingInvoices.isEmpty()) {
      throw new IllegalStateException("No Valid Invoice to submit Agreement");
    }

    Map<String, Object> obj = findCsulBank();


    if (!bypassRemotePosting()) {
      financingRemoteService.postedSubmission(
        FinancingSubmissionRequest.builder()
          .vendorCode(customer.getCustExternalCode())
          .accountNo(obj.get(BANK_ACCOUNT_NO_KEY).toString())
          .accountName(obj.get("accountName").toString())
          .bankName(obj.get(BANK_NAME_KEY).toString())
          .bankKey(obj.get("bankKey").toString())
          .financingCode(request.getFinancingHdrCode())
          .financingAmount(toDecimal(financingHdr.getFinancingAmt()).toString())
          .financingInvoices(financingInvoices)
          .build()
      );
    }
  }

  public void sendBouwheerPaymentNotification(FinancingHdr financingHdr) {
    try {
      List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr)
        .orElseThrow(() -> new IllegalStateException(FINANCING_INVOICE_NOT_FOUND_MESSAGE));
      Map<String, Object> bank = findCsulBank();

      emailService.sendNotificationBouwheerPayment(
        financingHdr.getBouwheer().getPicEmail(),
        BouwheerPaymentEmailPayload.builder()
          .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
          .vendorCode(financingHdr.getCustomer().getCustExternalCode())
          .vendorName(financingHdr.getCustomer().getCustName())
          .accountNo(bank.get(BANK_ACCOUNT_NO_KEY).toString())
          .bankAccount(bank.get("accountName").toString())
          .bankName(bank.get(BANK_NAME_KEY).toString())
          .bankKey(bank.get("bankKey").toString())
          .tglPengajuan(DateTimeUtils.formatToDate(financingHdr.getDtmCrt()))
          .invoices(toInvoiceEmailPayloads(financingHdr, financingDtls))
          .build()
      );
    } catch (Exception e) {
      log.error(
        "sendBouwheerPaymentNotification failed. financingHdrCode={}, recipients={}",
        financingHdr == null ? null : financingHdr.getFinancingHdrCode(),
        financingHdr.getBouwheer().getPicEmail(),
        e
      );
    }
  }

  public void sendDebtorDisbursementNotification(FinancingHdr financingHdr) {
    try {
      List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr)
        .orElseThrow(() -> new IllegalStateException(FINANCING_INVOICE_NOT_FOUND_MESSAGE));
      Customer customer = financingHdr.getCustomer();
      String phoneNumber = customer.getCustMobilePhone();

      if ("Company".equalsIgnoreCase(customer.getCustTypeCode())
        && customer.getCompany() != null
        && !StringUtil.isNullOrEmpty(customer.getCompany().getPhone())) {
        phoneNumber = customer.getCompany().getPhone();
      } else if ("Personal".equalsIgnoreCase(customer.getCustTypeCode())
        && customer.getPersonal() != null
        && !StringUtil.isNullOrEmpty(customer.getPersonal().getPhone())) {
        phoneNumber = customer.getPersonal().getPhone();
      }

      double totalFeeAmount = totalFeeAmount(financingHdr);

      emailService.sendNotificationLoanDisbursement(
        customer,
        LoanDisburseEmailPayload.builder()
          .financingCode(financingHdr.getFinancingHdrCode().toString())
          .applicationDate(DateTimeUtils.formatToDate(financingHdr.getDtmCrt()))
          .companyName(customer.getCustName())
          .phoneNumber(phoneNumber)
          .tenor(financingHdr.getTenor())
          .financingDueDate(DateTimeUtils.formatToDate(financingHdr.getFinancingDueDate()))
          .retention(CommonFormattingUtils.formatAmount(valueOrZero(financingHdr.getRetention())))
          .financingAmt(CommonFormattingUtils.formatAmount(valueOrZero(financingHdr.getFinancingAmt())))
          .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmount))
          .invoiceAmt(CommonFormattingUtils.formatAmount(valueOrZero(financingHdr.getTotalInvoiceAmt())))
          .disburseAmt(CommonFormattingUtils.formatAmount(valueOrZero(financingHdr.getDisburseAmt())))
          .invoices(toInvoiceEmailPayloads(financingHdr, financingDtls))
          .build()
      );
    } catch (Exception e) {
      log.error(
        "sendDebtorDisbursementNotification failed. financingHdrCode={}, email={}",
        financingHdr == null ? null : financingHdr.getFinancingHdrCode(),
        financingHdr == null || financingHdr.getCustomer() == null
          ? null
          : financingHdr.getCustomer().getCustEmail(),
        e
      );
    }
  }

  private double valueOrZero(Double value) {
    return value == null ? 0D : value;
  }

  private void sendContractUploadRequiredNotification(FinancingHdr financingHdr, String agreementCode, String cwrCode) {
    try {
      if (financingHdr.getMstBranch() == null) {
        log.warn("Contract upload notification skipped: branch is empty for financingHdrCode={}", financingHdr.getFinancingHdrCode());
        return;
      }

      String branchCode = financingHdr.getMstBranch().getBranchCode();
      List<String> recipients = resolveBranchAdminRecipients(branchCode);
      if (recipients.isEmpty()) {
        log.warn(
          "Contract upload notification skipped: no active Branch Admin email in DB. financingHdrCode={}, agreementCode={}, branchCode={}",
          financingHdr.getFinancingHdrCode(), agreementCode, branchCode
        );
        return;
      }

      log.info(
        "Branch Admin emails resolved from DB. financingHdrCode={}, agreementCode={}, branchCode={}, recipientCount={}",
        financingHdr.getFinancingHdrCode(), agreementCode, branchCode, recipients.size()
      );

      List<FinancingDtl> details = financingDtlRepository.findAllByFinancingHdr(financingHdr).orElse(List.of());
      if (details.isEmpty()) {
        log.warn("Contract upload notification skipped: no invoice for financingHdrCode={}", financingHdr.getFinancingHdrCode());
        return;
      }

      double totalFeeAmount = totalFeeAmount(financingHdr);
      NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"));
      numberFormat.setMaximumFractionDigits(2);
      List<InvoiceEmailPayload> invoices = toContractInvoicePayloads(financingHdr, details, numberFormat);
      if (invoices.isEmpty()) {
        log.warn("Contract upload notification skipped: no valid invoice for financingHdrCode={}", financingHdr.getFinancingHdrCode());
        return;
      }

      AgreementContractEmailPayload payload = buildContractEmailPayload(
        financingHdr, agreementCode, cwrCode, totalFeeAmount, numberFormat, invoices
      );
      sendContractNotification(recipients, payload);
    } catch (Exception e) {
      log.error(
        "Contract upload notification failed while resolving Branch Admin emails from DB. financingHdrCode={}, agreementCode={}, branchCode={}",
        financingHdr == null ? null : financingHdr.getFinancingHdrCode(),
        agreementCode,
        financingHdr == null || financingHdr.getMstBranch() == null ? null : financingHdr.getMstBranch().getBranchCode(),
        e
      );
    }
  }

  private List<String> resolveBranchAdminRecipients(String branchCode) {
    List<String> emails = mstAppRoleFormUserRepository.findActiveBranchAdminEmails(branchCode);
    if (emails == null) {
      return List.of();
    }
    return emails.stream()
      .filter(email -> email != null && !email.isBlank())
      .map(String::trim)
      .distinct()
      .toList();
  }

  private List<InvoiceEmailPayload> toContractInvoicePayloads(
    FinancingHdr financingHdr,
    List<FinancingDtl> details,
    NumberFormat numberFormat
  ) {
    return details.stream()
      .filter(detail -> detail.getInvoice() != null)
      .map(detail -> InvoiceEmailPayload.builder()
        .invoiceNo(detail.getInvoice().getCustInvNo())
        .description(detail.getInvoice().getInvoiceDescription())
        .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
        .invoiceDate(DateTimeUtils.formatToDate(detail.getInvoice().getInvoiceDate()))
        .invoiceDueDate(DateTimeUtils.formatToDate(detail.getInvoice().getInvoiceDueDate()))
        .invoiceAmt(numberFormat.format(detail.getInvoice().getInvoiceAmt()))
        .build())
      .toList();
  }

  private AgreementContractEmailPayload buildContractEmailPayload(
    FinancingHdr financingHdr,
    String agreementCode,
    String cwrCode,
    double totalFeeAmount,
    NumberFormat numberFormat,
    List<InvoiceEmailPayload> invoices
  ) {
    Customer customer = financingHdr.getCustomer();
    return AgreementContractEmailPayload.builder()
      .agreementCode(agreementCode)
      .financingCode(financingHdr.getFinancingHdrCode().toString())
      .vendorCode(customer.getCustExternalCode())
      .vendorName(customer.getCustName())
      .vendorEmail(customer.getCustEmail())
      .vendorPhone(customer.getCustMobilePhone())
      .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
      .bouwheerPicEmails(financingHdr.getBouwheer().getPicEmail())
      .branchName(financingHdr.getMstBranch().getBranchName())
      .submissionDate(DateTimeUtils.formatToDate(financingHdr.getFinancingDate()))
      .cwrCode(cwrCode)
      .totalInvoiceAmt("Rp " + numberFormat.format(valueOrZero(financingHdr.getTotalInvoiceAmt())))
      .retention(numberFormat.format(valueOrZero(financingHdr.getRetention())) + "%")
      .financingAmt("Rp " + numberFormat.format(valueOrZero(financingHdr.getFinancingAmt())))
      .totalFeeAmt("Rp " + numberFormat.format(totalFeeAmount))
      .tenor(financingHdr.getTenor() == null ? "-" : financingHdr.getTenor() + " hari")
      .disburseAmt("Rp " + numberFormat.format(valueOrZero(financingHdr.getDisburseAmt())))
      .invoices(InvoiceEmailPayload.toHtmlListBody(invoices))
      .build();
  }

  private void sendContractNotification(List<String> recipients, AgreementContractEmailPayload payload) {
    String recipientList = String.join(";", recipients);
    if (TransactionSynchronizationManager.isSynchronizationActive()
      && TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          emailService.sendNotificationContractUploadRequired(recipientList, payload);
        }
      });
    } else {
      emailService.sendNotificationContractUploadRequired(recipientList, payload);
    }
  }

  private double totalFeeAmount(FinancingHdr financingHdr) {
    return valueOrZero(financingHdr.getAdminFeeAmt())
      + valueOrZero(financingHdr.getLegalFeeAmtNett())
      + valueOrZero(financingHdr.getInsuranceFeeAmt())
      + valueOrZero(financingHdr.getOthersFeeAmt())
      + valueOrZero(financingHdr.getProvisionFeeAmt())
      + valueOrZero(financingHdr.getSurveyFeeAmtNett());
  }

  private List<InvoiceEmailPayload> toInvoiceEmailPayloads(
    FinancingHdr financingHdr,
    List<FinancingDtl> financingDtls
  ) {
    return financingDtls.stream()
      .filter(detail -> detail.getInvoice() != null)
      .map(detail -> InvoiceEmailPayload.builder()
        .invoiceNo(detail.getInvoice().getCustInvNo())
        .invoiceAmt(CommonFormattingUtils.formatAmount(detail.getInvoice().getInvoiceAmt()))
        .invoiceDate(DateTimeUtils.formatToDate(detail.getInvoice().getInvoiceDate()))
        .invoiceDueDate(DateTimeUtils.formatToDate(detail.getInvoice().getInvoiceDueDate()))
        .description(detail.getInvoice().getInvoiceDescription())
        .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
        .build())
      .toList();
  }

  boolean bypassRemotePosting() {
    return true;
  }

  private Map<String, Object> findCsulBank() throws JsonProcessingException {
    GeneralSettingDtl bank = generalSettingDtlRepository.findTopByGsDtlCode("DTLBANK001")
      .orElseThrow(() -> new IllegalStateException("Bank not found or not valid"));

    return ObjectUtils.strToJson(objectMapper.readValue(bank.getGsDtlValue(), new TypeReference<>() {

    }));
  }

  private void validateAgreement(String agreementNo) {
    final Agreement agreement = agreementRepository.findById(agreementNo)
      .orElse(null);
    if (agreement != null) {
      throw new IllegalStateException("Nomor Pencairan sudah di masukkan sebelumnya, silahkan masukkan Nomor Pencairan yg lain");
    }
  }

  private AgreementFileAuditData toAgreementFileAuditData(AgreementFile agreementFile) {
    if (agreementFile == null) {
      return null;
    }

    Agreement agreement = agreementFile.getAgreement();
    return new AgreementFileAuditData(
      agreementFile.getAgreementFileId(),
      agreement != null ? agreement.getAgreementCode() : null,
      agreement != null && agreement.getFinancingHdr() != null ? agreement.getFinancingHdr().getFinancingHdrCode() : null,
      agreementFile.getMstFileType() != null ? agreementFile.getMstFileType().getFileTypeCode() : null,
      agreementFile.getFileName(),
      agreementFile.getFilePath(),
      agreementFile.getContentType(),
      agreementFile.getUsrCrt(),
      agreementFile.getDtmCrt(),
      agreementFile.getUsrUpd(),
      agreementFile.getDtmUpd()
    );
  }

  private List<AgreementAuditData> agreementsAuditData(Iterable<Agreement> agreements) {
    List<AgreementAuditData> result = new ArrayList<>();
    agreements.forEach(agreement -> result.add(toAgreementAuditData(agreement)));
    return result;
  }

  private AgreementAuditData toAgreementAuditData(Agreement agreement) {
    if (agreement == null) {
      return null;
    }

    return new AgreementAuditData(
      agreement.getAgreementCode(),
      agreement.getApplicationCode(),
      agreement.getCwr() != null ? agreement.getCwr().getCwrCode() : null,
      agreement.getFinancingHdr() != null ? agreement.getFinancingHdr().getFinancingHdrCode() : null,
      agreement.getFacility(),
      agreement.getCurrency(),
      agreement.getFinancingAmt(),
      agreement.getStatus(),
      agreement.getProductOffering()
    );
  }

  private FinancingAgreementAuditData toFinancingAgreementAuditData(FinancingHdr financingHdr) {
    if (financingHdr == null) {
      return null;
    }

    return new FinancingAgreementAuditData(
      financingHdr.getFinancingHdrCode(),
      financingHdr.getFinancingStatus(),
      financingHdr.getFinancingStep(),
      financingHdr.getTotalInvoiceAmt(),
      financingHdr.getFinancingAmt(),
      financingHdr.getDisburseAmt()
    );
  }

  private record AgreementFileAuditData(
    Long agreementFileId,
    String agreementCode,
    UUID financingHdrCode,
    String fileTypeCode,
    String fileName,
    String filePath,
    String contentType,
    String usrCrt,
    java.time.LocalDateTime dtmCrt,
    String usrUpd,
    java.time.LocalDateTime dtmUpd
  ) {
  }

  private record AgreementAuditData(
    String agreementCode,
    String applicationCode,
    String cwrCode,
    UUID financingHdrCode,
    String facility,
    String currency,
    Double financingAmt,
    String status,
    String productOffering
  ) {
  }

  private record FinancingAgreementAuditData(
    UUID financingHdrCode,
    String financingStatus,
    String financingStep,
    Double totalInvoiceAmt,
    Double financingAmt,
    Double disburseAmt
  ) {
  }
}
