package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.exception.LoanDocMandatoryException;
import com.kmkbe.core.model.JwtSimulasiModel;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.common.model.LoanDisburseEmailPayload;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.dto.CustomerDto;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.constant.FinancingStatus;
import com.kmkbe.modules.loan_submission.dto.*;
import com.kmkbe.modules.loan_submission.entity.*;
import com.kmkbe.modules.loan_submission.model.PostedInvoicePayload;
import com.kmkbe.modules.loan_submission.model.SimulationDisburseResult;
import com.kmkbe.modules.loan_submission.repository.BouwheerRepository;
import com.kmkbe.modules.loan_submission.repository.ProductRepository;
import com.kmkbe.modules.loan_submission.request.*;
import com.kmkbe.modules.remote.dto.InquiryInvoiceRemoteDto;
import com.kmkbe.modules.remote.dto.InquiryVendorRemoteDto;
import com.kmkbe.modules.remote.dto.PostedInvoiceDto;
import com.kmkbe.modules.remote.request.FinancingSubmissionRequest;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.remote.service.InvoiceRemoteDto;
import com.kmkbe.modules.remote.service.LoanSubmissionRemoteService;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanSubmissionService {
    private final ProductRepository productRepository;
    private final BouwheerRepository bouwheerRepository;
    private final BCryptPasswordEncoder bcryptEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final JwtLoanSubmissionService jwtLoanSubmissionService;

    private final LoanSubmissionRemoteService loanSubmissionRemoteService;
    private final FinancingRemoteService financingRemoteService;
    private final CustomerRemoteService customerRemoteService;
    private final InvoiceRemoteDto invoiceRemoteDto;

    private final InvoiceService invoiceService;
    private final FinancingHdrService financingHdrService;
    private final FinancingDtlService financingDtlService;
    private final MstFileTypeService mstFileTypeService;
    private final EmailService emailService;
    private final SimulationHistoryService simulationHistoryService;

    public List<PostedInvoiceDto> fetchActiveInvoice(
            Authentication authentication,
            String token
    ) throws Exception {
        try {
            if (token != null) {
                switch (token) {
                    case "1" -> throw CommonInvalidException.builder()
                            .title("Tidak Terdapat Invoice Yang Dapat Dibiayai")
                            .message("Mohon maaf, saat ini Anda belum dapat menggunakan " +
                                    "Dana Sakti. Harap melakukan pengecekan ulang " +
                                    "dengan pihak PT. Trakindo Utama.")
                            .build();
                    case "2" -> throw CommonInvalidException.builder()
                            .title("Perusahaan Anda Terdaftar dalam Daftar Blacklist")
                            .message("Perusahaan Anda saat ini terdaftar dalam daftar " +
                                    "blacklist PT Trakindo Utama, sehingga Anda " +
                                    "belum dapat menggunakan Dana Sakti.")
                            .build();
                    case "3" -> throw CommonInvalidException.builder()
                            .title("Mohon Maaf, Anda Tidak Memenuhi Syarat")
                            .message("Mohon maaf, saat ini Anda belum dapat menggunakan " +
                                    "Dana Sakti. Harap melakukan pengecekan ulang " +
                                    "dengan pihak PT. Trakindo Utama.")
                            .build();
                }
            }

            final VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(authentication, token);
            final InquiryInvoiceRemoteDto inquiryInvoiceRemote;

            try {
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

            final SimpleDateFormat sdfNoSeperator = new SimpleDateFormat("yyyyMMdd");

            List<PostedInvoiceDto> result = new ArrayList<>();
            for (int i = 0; i < inquiryInvoiceRemote.getRow().size(); i++) {
                FinancingDtl financingDtl = financingDtlService.findBy(inquiryInvoiceRemote.getRow().get(i).getAccountingDocument());
                if (financingDtl != null) {
                    FinancingHdr financingHdr = financingDtl.getFinancingHdr();
                    if (
                            financingHdr.getFinancingStatus().equals(FinancingStatus.NEW.getValue())
                    ) {

                    } else {
                        continue;
                    }
                }

                Date invDate, invDueDate;
                try {
                    invDate = DateTimeUtils.SDF_STANDARD_DATE.parse(inquiryInvoiceRemote.getRow().get(i).getPostingDate());
                    invDueDate = DateTimeUtils.SDF_STANDARD_DATE.parse(inquiryInvoiceRemote.getRow().get(i).getNetDueDate());
                } catch (Exception e) {
                    invDate = sdfNoSeperator.parse(inquiryInvoiceRemote.getRow().get(i).getPostingDate());
                    invDueDate = sdfNoSeperator.parse(inquiryInvoiceRemote.getRow().get(i).getNetDueDate());
                }

                result.add(PostedInvoiceDto.builder()
                        .bouwheerCode(vendorTokenExtractor.getBouwheerCode().toString())
                        .bouwheerName(vendorTokenExtractor.getBouwheerName())
                        .customerInvoiceNo(inquiryInvoiceRemote.getRow().get(i).getReference())
                        .bouwheerInvoiceNo(inquiryInvoiceRemote.getRow().get(i).getAccountingDocument())
                        .invoiceDate(invDate)
                        .invoiceDueDate(invDueDate)
                        .invoiceAmount(BigDecimal.valueOf(Double.parseDouble(inquiryInvoiceRemote.getRow().get(i).getAmount().trim())))
                        .invoiceDescription(inquiryInvoiceRemote.getRow().get(i).getDescription())
                        .currencyCode(inquiryInvoiceRemote.getRow().get(i).getCurrency())
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

    public EstimatedDisburseDto calculateDisburse(CalculateSimulationRequest request) {
        try {
            final BigDecimal ntfResult = request.getTotalInvoiceAmount()
                    .multiply(BigDecimal.valueOf(request.getDisbursePercentage() / 100.0));

            final Optional<Product> findProduct = productRepository.findNtfRange(ntfResult.doubleValue());

            if (findProduct.isEmpty()) {
                return null;
            }

            final Product product = findProduct.get();
            final BigDecimal serviceFee = BigDecimal.valueOf(
                    product.getSurveyFee()
                            + product.getLegalFee()
                            + product.getAdminLimitFee()
                            + product.getOthersFee()
            );
            final BigDecimal estimateDisburse = ntfResult.subtract(serviceFee);

            return EstimatedDisburseDto.builder()
                    .productId(product.getProductId())
                    .financingAmount(ntfResult)
                    .serviceFeeAmount(serviceFee)
                    .estimatedDisburseAmount(estimateDisburse)
                    .build();
        } catch (Exception e) {
            log.error("calculateDisburse, error {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public CreatedSimulationDto createSimulation(
            Authentication authentication,
            CreateSimulationRequest request
    ) throws Exception {
        try {
            final String bouwheerCode = request.getInvoices().getFirst().getBouwheerCode();
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final Product product = productRepository.findById(request.getProductId()).orElseThrow();

            Bouwheer bouwheer;
            Optional<Bouwheer> findBouwheer = bouwheerRepository.findByBouwheerCode(UUID.fromString(bouwheerCode));
            if (findBouwheer.isEmpty()) {
                findBouwheer = bouwheerRepository.findFirstByBouwheerName("PT. Truckindo Utama");
                if (findBouwheer.isPresent()) {
                    bouwheer = findBouwheer.get();
                } else {
                    bouwheer = new Bouwheer();
                    bouwheer.setBouwheerCode(UUID.randomUUID());
                    bouwheer.setBouwheerName("PT. Truckindo Utama");
                    bouwheer.setLegalAddress("");
                    bouwheer.setRt("");
                    bouwheer.setRw("");
                    bouwheer.setKelurahan("");
                    bouwheer.setKecamatan("");
                    bouwheer.setCity("");
                    bouwheer.setProvince("");
                    bouwheer.setZipcode("");
                    bouwheer.setArea("");
                    bouwheer.setPhone("");
                    bouwheer.setPicName("");
                    bouwheer.setPicEmail("");
                    bouwheer.setPicMobilePhone("");
                    bouwheer.setTermOfPayment(0L);
                    bouwheer.setGracePeriod(0L);
                    bouwheer.setAesKey("");
                    bouwheer.setSecretKey("");
                    bouwheer.setApiKey("");
                    bouwheer.setIsActive(true);
                    bouwheer = bouwheerRepository.save(bouwheer);
                }
            } else {
                bouwheer = findBouwheer.get();
            }

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
            }

            final EstimatedDisburseDto calculateDisburse = calculateDisburse(simulation);
            final SimulationDisburseResult simulationDisburseResult = SimulationDisburseResult.builder()
                    .financingAmount(calculateDisburse.getFinancingAmount())
                    .estimatedDisburseAmount(calculateDisburse.getEstimatedDisburseAmount())
                    .maxInvoiceDate(maxInvoiceDueDate)
                    .totalInvoiceAmount(totalInvoiceAmount)
                    .build();

            final FinancingHdr createdFinancingHdr = financingHdrService.create(
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

            simulationHistoryService.create(
                    customer,
                    createdFinancingHdr,
                    totalInvoiceAmount,
                    createdFinancingHdr.getRetention(),
                    createdFinancingHdr.getAdminFeeAmt(),
                    createdFinancingHdr.getFinancingAmt()
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
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            if (!bcryptEncoder.matches(request.getPin(), customer.getCustPin())) {
                throw new BadCredentialsException("Pin is invalid, try to entry right pin");
            }

            mstFileTypeService.getAll()
                    .forEach(mstFileType -> {
                        if (
                                request.getDocuments()
                                        .stream()
                                        .noneMatch(document -> document.getFileTypeCode().equals(mstFileType.getFileTypeCode()))
                        ) {
                            throw new LoanDocMandatoryException("Mandatory file: " + mstFileType.getFileTypeDesc() + " is not present, try to attach the file");
                        }
                    });

            final FinancingHdrDto createdFinancing = financingHdrService.getByCode(request.getFinancingHdrCode());
            final List<LoanDisburseEmailPayload.InvoicePayload> invoices = createdFinancing.getDetails()
                    .stream()
                    .map((item) ->
                            LoanDisburseEmailPayload.InvoicePayload.builder()
                                    .seq(item.getInvoiceSeqno())
                                    .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt().doubleValue()))
                                    .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
                                    .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
                                    .description(item.getInvoice().getInvoiceDescription())
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

            financingRemoteService.postedSubmission(
                    FinancingSubmissionRequest.builder()
                            .vendorCode(customer.getCustExternalCode())
                            .build()
            );

            emailService.sendNotificationLoanDisbursement(
                    customer,
                    LoanDisburseEmailPayload.builder()
                            .financingCode(createdFinancing.getFinancingHdrCode().toString())
                            .applicationDate(DateTimeUtils.formatToDate(createdFinancing.getDisburseDate()))
                            .companyName(createdFinancing.getBouwheer().getBouwheerName())
                            .phoneNumber(createdFinancing.getCustomer().getCustMobilePhone())
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
            log.error("createLoanSubmission, error {}", e.getMessage());
            throw e;
        }
    }

    public ExternalIntegrationLoanSimulationDto externalIntegrationSimulation(
            Authentication authentication,
            String token
    ) throws JsonProcessingException, SignatureException {
        try {
            if (
                    token.equals("1")
                            || token.equals("2")
                            || token.equals("3")
            ) {
                token = "eyJCb3V3aGVlckNvZGUiOiJiOGVlODViMC0wYjExLTQ5MDMtYWYxZS0xOWFkZGI2NTM0NjIiLCJDcmVhdGVkRGF0ZVN0cmluZyI6IjIwMjQtMDgtMjEgMTU6MDQ6MzIiLCJWZW5kb3JDb2RlIjoiMDAwMTAwMDAwNiJ9.CEC649B96AB33D8736A6838302CF4213";
            }

            VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(authentication, token);
            Optional<ExternalIntegrationLoanSimulationDto> find = findExternalIntegrationByBouwheerCode(
                    vendorTokenExtractor.getBouwheerCode().toString(),
                    vendorTokenExtractor.getVendorCode()
            );

            InquiryVendorRemoteDto inquiryVendorRemote = customerRemoteService.inquiryVendor(vendorTokenExtractor.getVendorCode()).getData();
            ExternalIntegrationLoanSimulationDto.VendorDto vendor = ExternalIntegrationLoanSimulationDto.VendorDto.builder()
                    .vendorCode(vendorTokenExtractor.getVendorCode())
                    .name(inquiryVendorRemote.getVendorName())
                    .customerType("Perusahaan")
                    .email(inquiryVendorRemote.getEmail())
                    .mobilePhone(inquiryVendorRemote.getPhone())
                    .customerIdNo(inquiryVendorRemote.getNpwp())
                    .build();

            ExternalIntegrationLoanSimulationDto result = find.orElseGet(() -> ExternalIntegrationLoanSimulationDto.builder()
                    .bouwheerCode(vendorTokenExtractor.getBouwheerCode().toString())
                    .alreadyAcceptImportantNotes(false)
                    .build());

            result.setVendor(Base64.getUrlEncoder().encodeToString(ObjectUtils.jsonToStr(vendor).getBytes(StandardCharsets.UTF_8)));

            return result;
        } catch (Exception e) {
            log.error("externalIntegrationLoanSimulation, error {}", e.getMessage());
            throw e;
        }
    }

    public ImportantNotesDto importanceNotes() {
        return ImportantNotesDto.builder()
                .description("<p>Semua <b>data legalitas, keuangan, & informasi transaksi</b> perusahaan bapak/ibu dalam E-procurement dan POST (Purchase Order System Tracking) PT. Trakindo Utama akan diberikan secara otomatis kepada PT. Chandra Sakti Utama Leasing (CSULfinance) sebagai anak usaha Grup TMT (Tiara Marga Trakindo) yang memfasilitasi pembiayaan tagihan antara vendor dengan PT.Trakindo Utama. Semua data informasi ini dipakai hanya untuk transaksi pembiayaan anjak piutang / tagihan di CSULfinance. \n" +
                        "CSULfinance berizin dan diawasi oleh Otoritas Jasa Keuangan (OJK)</p>")
                .legals(List.of(
                        "Akta Pendirian",
                        "Akta Penyesuaian Anggaran Dasar terhadap UU 40/2007 (Jika PT)",
                        "Akta Perubahan mengenai Modal Ditempatkan dan Disetor",
                        "Akta Perubahan Maksud dan Tujuan Persero",
                        "Akta Perubahan Terakhir mengenai Perubahan Susunan Pengurus Perseroan",
                        "Akta-Akta Perubahan Terakhir Lainnya + SK Persetujuan Menkumhan / Surat Penerimaan Pemberitahuan Perubahan Anggaran Dasar / Data Perseroan (Jika ada)",
                        "Identitas Pengurus (KTP/Paspor/KITAS)",
                        "NPWP",
                        "NIB (RBA)",
                        "Izin Usaha Lainnya",
                        "Izin Lokasi",
                        "Company Profile",
                        "Rekap Invoice Tagihan Trakindo",
                        "Rekening Koran",
                        "PO dari Trakindo",
                        "FAP (Formulir Aplikasi Pembiayaan)",
                        "Laporan Keuangan",
                        "Foto Gedung",
                        "Pengalaman Kerja",
                        "Struktur Organisasi",
                        "Bank Detail"
                ))
                .build();
    }

    public ExternalIntegrationLoanSimulationDto saveImportantNotes(SaveImportantNotesRequest request) throws SignatureException, JsonProcessingException {
        try {
            VendorTokenExtractor vendorTokenExtractor = vendorTokenExtractor(null, request.getToken());
            Optional<ExternalIntegrationLoanSimulationDto> find = findExternalIntegrationByBouwheerCode(
                    vendorTokenExtractor.getBouwheerCode().toString(),
                    vendorTokenExtractor.getVendorCode()
            );

            final ExternalIntegrationLoanSimulationDto result;
            if (find.isEmpty()) {
                jdbcTemplate.update(
                        "insert into _loan_important_notes (vendor_code, bouwheer_code, already_accept_important_notes, dtm_crt) values (?, ?, ?, ?)",
                        vendorTokenExtractor.getVendorCode(),
                        vendorTokenExtractor.getBouwheerCode(),
                        true,
                        new Date()
                );

                result = findExternalIntegrationByBouwheerCode(vendorTokenExtractor.getBouwheerCode().toString(), vendorTokenExtractor.getVendorCode())
                        .get();
            } else {
                result = find.get();
            }

            final InquiryVendorRemoteDto inquiryVendorRemote = customerRemoteService.inquiryVendor(vendorTokenExtractor.getVendorCode()).getData();
            final ExternalIntegrationLoanSimulationDto.VendorDto vendor = ExternalIntegrationLoanSimulationDto.VendorDto.builder()
                    .vendorCode(vendorTokenExtractor.getVendorCode())
                    .name(inquiryVendorRemote.getVendorName())
                    .customerType("Perusahaan")
                    .email(inquiryVendorRemote.getEmail())
                    .mobilePhone(inquiryVendorRemote.getPhone())
                    .customerIdNo(inquiryVendorRemote.getNpwp())
                    .build();

            result.setVendor(Base64.getUrlEncoder().encodeToString(ObjectUtils.jsonToStr(vendor).getBytes(StandardCharsets.UTF_8)));

            return result;
        } catch (Exception e) {
            log.error("saveImportantNotes, error {}", e.getMessage());
            throw e;
        }
    }

    public SimulationHistDto lastSimulationHistory(Authentication authentication) throws SignatureException {
        try {
            Customer customer = CustomerUtils.authenticateCustomer(authentication);
            FinancingHdr financingHdr = financingHdrService.findLastBy(customer);

            return simulationHistoryService.findLastBy(financingHdr);
        } catch (Exception e) {
            log.error("lastSimulationHistory, error {}", e.getMessage());
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

    /**
     * <h4>Dummy for crud operation of modal dialog important notes (FE)</h4>
     * <p>This can be determine modal show only once</p>
     */
    private void initTblImportantNotes() {
        final String query =
                "select count(*) "
                        + "from information_schema.tables "
                        + "where table_name = ? and table_schema = 'public'";

        Integer result = jdbcTemplate.queryForObject(query, Integer.class, "_loan_important_notes");
        if (result == null || result == 0) {
            final String createTable =
                    """
                                create table public._loan_important_notes
                                (
                                    id                          int generated by default as identity primary key,
                                    vendor_code                 varchar(255)                  not null,
                                    bouwheer_code               varchar(255)                  not null,
                                    already_accept_important_notes boolean default false not null,
                                    dtm_crt                     timestamp             not null
                                );
                            """;

            jdbcTemplate.execute(createTable);
        }
    }

    private Optional<ExternalIntegrationLoanSimulationDto> findExternalIntegrationByBouwheerCode(String bouwheerCode, String vendorCode) {
        try {
            initTblImportantNotes();
            ExternalIntegrationLoanSimulationDto result = jdbcTemplate.queryForObject(
                    "select bouwheer_code, vendor_code, already_accept_important_notes, dtm_crt from public._loan_important_notes where bouwheer_code = ? and vendor_code = ? order by id desc limit 1",
                    (rs, rowNum) -> ExternalIntegrationLoanSimulationDto.builder()
                            .bouwheerCode(rs.getString("bouwheer_code"))
                            .alreadyAcceptImportantNotes(rs.getBoolean("already_accept_important_notes"))
                            .dtmCrt(rs.getTimestamp("dtm_crt"))
                            .build(),
                    bouwheerCode,
                    vendorCode
            );
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("findExternalIntegrationByBouwheerCode, error {}", e.getMessage());
            throw e;
        }
    }

    @Getter
    private static class VendorTokenExtractor {
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
            bouwheerCode = jwtSimulasiModel != null ? UUID.fromString(jwtSimulasiModel.getBouwheerCode()) : UUID.randomUUID();

            Bouwheer bouwheer = bouwheerRepository.findByBouwheerCode(bouwheerCode).orElse(null);
            if (bouwheer != null) {
                bouwheerName = bouwheer.getBouwheerName();
            } else {
                bouwheerName = "PT. Truckindo Utama";
            }

            if (jwtSimulasiModel != null) {
                vendorCode = jwtSimulasiModel.getVendorCode();
            } else if (authentication != null) {
                CustomerDto cust = CustomerUtils.authenticateCustomerDto(authentication);
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

        private void validate() {

        }
    }
}
