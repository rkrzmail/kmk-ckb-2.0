package com.kmkbe.modules.branch_admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.BouwheerPaymentEmailPayload;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.FileUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.branch_admin.request.CreateInquiryAgreementRequest;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.remote.request.FinancingSubmissionRequest;
import com.kmkbe.modules.remote.request.InquiryAgreementRemoteRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgreementService {
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
            String cwrCode,
            String financingHdrCode,
            PaginationRequest request
    ) throws JsonProcessingException {
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

            Page<Map<String, Object>> page = agreementRepository.findAllListByCwrAndFinancingRaw(
                    cwrCode,
                    financingHdrCode,
                    PageRequest.of(pageNo, pageSize)
            );

            Map<String, Object> obj = findCsulBank();

            List<AgreementDto> result = page
                    .stream()
                    .map((e) ->
                            AgreementDto.builder()
                                    .no(e.get("no") != null ? Integer.parseInt(e.get("no").toString()) : 0)
                                    .cwrCode(cwrCode)
                                    .agreementNo(e.get("agreement_code") != null ? e.get("agreement_code").toString() : null)
                                    .financingHdrCode(e.get("financing_hdr_code") != null ? UUID.fromString(e.get("financing_hdr_code").toString()) : null)
                                    .bouwheerCode(e.get("bouwheer_code") != null ? UUID.fromString(e.get("bouwheer_code").toString()) : null)
                                    .custCode(e.get("cust_code") != null ? UUID.fromString(e.get("cust_code").toString()) : null)
                                    .bouwheerName(e.get("bouwheer_name") != null ? e.get("bouwheer_name").toString() : null)
                                    .custName(e.get("cust_name") != null ? e.get("cust_name").toString() : null)
                                    /*.bankName(e.get("bank_name") != null ? e.get("bank_name").toString() : null)
                                    .rekeningNo(e.get("rekening_no") != null ? e.get("rekening_no").toString() : null)*/
                                    .bankName(obj.get("bankName").toString())
                                    .rekeningNo(obj.get("accountNo").toString())
                                    .financingAmt(new BigDecimal(e.get("financing_amt") != null ? Double.parseDouble(e.get("financing_amt").toString()) : 0, MathContext.DECIMAL64))
                                    .disburseDate(e.get("disburse_date") != null ? Timestamp.valueOf(e.get("disburse_date").toString()) : null)
                                    .disburseAmt(new BigDecimal(e.get("disburse_amt") != null ? Double.parseDouble(e.get("disburse_amt").toString()) : 0, MathContext.DECIMAL64))
                                    .currency(e.get("currency") != null ? e.get("currency").toString() : null)
                                    .build()
                    )
                    .collect(Collectors.toList());

            if (result.isEmpty()) {
                /*result.add(AgreementDto.builder()
                        .no(1)
                        .cwrCode(cwrCode)
                        .agreementNo("001")
                        .financingHdrCode(UUID.randomUUID())
                        .bouwheerCode(UUID.randomUUID())
                        .custCode(UUID.randomUUID())
                        .bouwheerName("PT. Trakindo Utama")
                        .custName("Test")
                        .bankName("Mandiri")
                        .rekeningNo("123")
                        .financingAmt(BigDecimal.valueOf(1000000))
                        .disburseDate(new Date())
                        .disburseAmt(BigDecimal.valueOf(10000000))
                        .currency("IDR")
                        .build());*/
            }

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

    public void upload(
            Authentication authentication,
            MultipartFile multipartFile,
            String agreementCode
    ) throws Exception {
        try {
            final MstFileType mstFileType = mstFileTypeRepository.findByFileTypeCode("AGGREMENT01")
                    .orElseThrow(
                            () -> new IllegalArgumentException("File type not found")
                    );

            final Agreement agreement = agreementRepository.findTopByAgreementCodeOrderByAgreementId(
                    agreementCode
            ).orElseThrow(() -> new IllegalArgumentException("Agreement not found"));

            AgreementFile agreementFile = agreementFileRepository.findTopByAgreementOrderByAgreementFileId(agreement)
                    .orElse(null);

            final MstUser user = UserInternalUtils.authenticateUser(authentication);

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
                        .dtmCrt(DateTimeUtils.now())
                        .build();

                agreementFileRepository.save(agreementFile);
            } else {
                //fileStorageService.delete(agreementFile.getFilePath() + "/" + agreementFile.getFileName(), "");

                agreementFile.setFileName(uploadName);
                agreementFile.setFilePath(FileUtils.getFilePathFromFullPath(uploadedPath));
                agreementFile.setDtmUpd(DateTimeUtils.now());
                agreementFile.setUsrUpd(user.getUsername());
                agreementFileRepository.save(agreementFile);
            }


        } catch (Exception e) {
            /*if (code != null && multipartFile != null && multipartFile.getOriginalFilename() != null) {
                fileStorageService.delete(multipartFile.getOriginalFilename(), code);
            }*/

            log.error("upload, error {}", e.getMessage());
            throw e;
        }
    }

    public InquiryAgreementDto inquiryAgreementCwr(
            String cwrCode,
            String agreementNo
    ) throws JsonProcessingException {
        try {
            validateAgreement(agreementNo);
            CommonInvalidException ex = CommonInvalidException.builder()
                    .title("Peringatan")
                    .message("Tidak bisa mencari Agreement, terjadi kesalahan")
                    .build();

            final List<InquiryAgreementCwrDto> data;
            try {
                BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = cwrRemoteService.inquiryAgreementByNoAgreement(
                        InquiryAgreementRemoteRequest.builder()
                                .agreementNo(agreementNo)
                                .build()
                );

                data = response.getData();
            } catch (Exception e) {
                ex.setMessage(String.valueOf(e.getMessage()));
                throw ex;
            }

            if (data != null && !data.isEmpty()) {
                Map<String, Object> obj = findCsulBank();

                /*if (!data.getFirst().getCwrNo().equalsIgnoreCase("")) {
                    throw new IllegalStateException("Nomor CWR tidak sesuai dengan Nomor Pencairan, pastikan Nomor Pencairan benar");
                }*/

                return InquiryAgreementDto.builder()
                        .bankName(obj.get("bankName").toString())
                        .rekeningNo(obj.get("accountNo").toString())
                        .currency(data.getFirst().getCurrency())
                        .disburseAmt(new BigDecimal(data.getFirst().getNtfAmt(), MathContext.DECIMAL64))
                        .build();
            }else{
                ex.setMessage("Failed to inquiry Agreement result null ");
            }

            throw ex;
        } catch (Exception e) {
            log.error("inquiryAgreementCwr: error {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void createInquiryAgreement(
            Authentication authentication,
            CreateInquiryAgreementRequest request
    ) throws Exception {
        try {
            validateAgreement(request.getAgreementNo());
            final List<InquiryAgreementCwrDto> data;
            try {
                BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = cwrRemoteService.inquiryAgreementByNoAgreement(
                        InquiryAgreementRemoteRequest.builder()
                                .agreementNo(request.getAgreementNo())
                                .build()
                );

                data = response.getData();
            } catch (Exception e) {
                throw CommonInvalidException.builder()
                        .title("Peringatan")
                        .message("Tidak bisa mencari Data Pencairan, Pastikan No. Pencairan benar")
                        .build();
            }

            final MstUser user = UserInternalUtils.authenticateUser(authentication);
            final FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(request.getFinancingHdrCode()))
                    .orElseThrow(() -> new IllegalStateException("Financing not found or not valid"));
            final Customer customer = financingHdr.getCustomer();
            if (customer == null) {
                throw new IllegalStateException("Customer not found or not valid");
            }

            List<Agreement> agreements = new ArrayList<>();
            if (!data.isEmpty()) {
                for (InquiryAgreementCwrDto inquiryAgreement : data) {
                    String cwrCode = inquiryAgreement.getCwrNo();
                    final Cwr cwr = cwrRepository.findTopByCwrCode(cwrCode)
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
                                    .dtmCrt(DateTimeUtils.now())//DateTimeUtils.now()
                                    .build()
                    );
                }
            }

            if (!agreements.isEmpty()) {
                agreementRepository.saveAll(agreements);
            }

            proceedFinancing(
                    financingHdr,
                    request,
                    customer
            );

            // Branch admin melakukan singkron agreement
            financingHdr.setFinancingStatus("INPROCESS");
            financingHdr.setFinancingStep("INPROCESS");
            financingHdrRepository.save(financingHdr);
        } catch (Exception e) {
            log.error("createAgreement: error {}", e.getMessage());
            throw e;
        }
    }

    private InquiryAgreementCwrDto sampleResponse() throws JsonProcessingException {
        String sample = "{\"AppId\":19,\"Cmo\":\"RIZKIAALDAZABRINA\",\"Office\":\"JAKARTA3\",\"DebtorNo\":\"41400001208\",\"DebtorName\":\"JOMONPERSADANUSANTARA\",\"DebtorType\":\"COMPANY\",\"CWRNo\":\"41450CWR2024626\",\"AppNo\":\"41450APP20241627\",\"ProductOffering\":\"ANJAKPIUTANGIDR-PMKFC\",\"CurrStep\":\"Live\",\"LastStep\":\"PreGoLive\",\"AgrmntNo\":\"41450241613\",\"Status\":\"Expired\",\"Facility\":\"MODALKERJA\",\"Currency\":\"IDR\",\"NtfAmt\":703296000.00,\"LastApprover\":\"-\"}";
        return objectMapper.readValue(sample, new TypeReference<>() {
        });
    }

    private void proceedFinancing(
            FinancingHdr financingHdr,
            CreateInquiryAgreementRequest request,
            Customer customer
    ) throws Exception {
        List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr)
                .orElseThrow(() -> new IllegalStateException("Financing Invoice not found or not valid"));

        List<FinancingSubmissionRequest.FinancingInvoice> financingInvoices = financingDtls.stream()
                .filter((e) -> e.getInvoice() != null && !StringUtil.isNullOrEmpty(e.getInvoice().getPoNumber()))
                .map((e) -> FinancingSubmissionRequest.FinancingInvoice.builder()
                        .invoiceAmount(new BigDecimal(e.getInvoice().getInvoiceAmt(), MathContext.DECIMAL64).toString())
                        .poNumber(e.getInvoice().getPoNumber())
                        .reference(e.getInvoice().getCustInvNo())
                        .accountingDocument(e.getInvoice().getBouwheerInvNo())
                        .build())
                .toList();

        if (financingInvoices.isEmpty()) {
            throw new IllegalStateException("No Valid Invoice to submit Agreement");
        }

        Map<String, Object> obj = findCsulBank();

        BaseSimpleRemoteResponseDto<Object> postedResponse = financingRemoteService.postedSubmission(
                FinancingSubmissionRequest.builder()
                        .vendorCode(customer.getCustExternalCode())
                        .accountNo(obj.get("accountNo").toString())
                        .accountName(obj.get("accountName").toString())
                        .bankName(obj.get("bankName").toString())
                        .bankKey(obj.get("bankKey").toString())
                        .financingCode(request.getFinancingHdrCode())
                        .financingAmount(new BigDecimal(financingHdr.getFinancingAmt(), MathContext.DECIMAL64).toString())
                        .financingInvoices(financingInvoices)
                        .build()
        );

        final List<InvoiceEmailPayload> invoices = financingDtls
                .stream()
                .map((item) ->
                        InvoiceEmailPayload.builder()
                                .invoiceNo(item.getInvoice().getCustInvNo())
                                .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt()))
                                .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
                                .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
                                .description("Invoice By Trakindo")
                                .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
                                .build()
                ).toList();

        String email = financingHdr.getBouwheer().getPicEmail();
        if (
                postedResponse.getData() instanceof Map<?, ?> body
                        && body.get("email_address") != null
        ) {
            email = body.get("email_address").toString();
        }

        emailService.sendNotificationBouwheerPayment(
                email,
                BouwheerPaymentEmailPayload.builder()
                        .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
                        .vendorCode(customer.getCustExternalCode())
                        .vendorName(customer.getCustName())
                        .accountNo(obj.get("accountNo").toString())
                        .bankAccount(obj.get("accountName").toString())
                        .bankName(obj.get("bankName").toString())
                        .bankKey(obj.get("bankKey").toString())
                        .tglPengajuan(DateTimeUtils.formatToDate(financingHdr.getFinancingDate()))
                        .invoices(invoices)
                        .build()
        );
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
}
