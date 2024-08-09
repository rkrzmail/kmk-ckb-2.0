package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.exception.LoanDocMandatoryException;
import com.kmkbe.core.model.JwtSimulasiModel;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.model.LoanDisburseEmailPayload;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.dto.*;
import com.kmkbe.modules.loan_submission.entity.Bouwheer;
import com.kmkbe.modules.loan_submission.entity.Product;
import com.kmkbe.modules.loan_submission.model.PostedInvoicePayload;
import com.kmkbe.modules.loan_submission.model.SimulationDisburseResult;
import com.kmkbe.modules.loan_submission.repository.BouwheerRepository;
import com.kmkbe.modules.loan_submission.repository.ProductRepository;
import com.kmkbe.modules.loan_submission.request.*;
import com.kmkbe.modules.remote.dto.PostedInvoiceDto;
import com.kmkbe.modules.remote.service.LoanSubmissionRemoteService;
import jakarta.transaction.Transactional;
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
    private final InvoiceService invoiceService;
    private final FinancingService financingService;
    private final MstFileTypeService mstFileTypeService;
    private final EmailService emailService;

    public List<PostedInvoiceDto> fetchActiveInvoice(Authentication authentication) {
        try {

            return loanSubmissionRemoteService.fetchListOfPostedInvoice(authentication);
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
            final Bouwheer bouwheer = bouwheerRepository.findByBouwheerCode(UUID.fromString(bouwheerCode)).get();
            final Product product = productRepository.findById(request.getProductId()).orElseThrow();

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
            final List<InvoiceDto> invoices = invoiceService.createBulk(customer, bouwheer, request);

            final SimulationDisburseResult simulationDisburseResult = SimulationDisburseResult.builder()
                    .financingAmount(calculateDisburse.getFinancingAmount())
                    .estimatedDisburseAmount(calculateDisburse.getEstimatedDisburseAmount())
                    .maxInvoiceDate(maxInvoiceDueDate)
                    .totalInvoiceAmount(totalInvoiceAmount)
                    .createdInvoices(invoices)
                    .build();

            final UUID financingHdrCode = financingService.create(
                    customer,
                    bouwheer,
                    product,
                    request,
                    simulationDisburseResult
            );

            return CreatedSimulationDto.builder()
                    .productId(request.getProductId())
                    .financingHdrCode(financingHdrCode)
                    .invoices(invoices)
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

            final FinancingHdrDto createdFinancing = financingService.getByCode(request.getFinancingHdrCode());
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

    public ExternalIntegrationLoanSimulationDto externalIntegrationSimulation(RemoteBouwheerRequest request) {
        try {
            JwtSimulasiModel jwtSimulasiModel = jwtLoanSubmissionService.extractToken(request.getToken());

            Optional<ExternalIntegrationLoanSimulationDto> find = findExternalIntegrationByBouwheerCode(jwtSimulasiModel.getBouwheerCode());
            return find.orElseGet(() -> ExternalIntegrationLoanSimulationDto.builder()
                    .bouwheerCode(jwtSimulasiModel.getBouwheerCode())
                    .alreadyAcceptImportantNotes(false)
                    .build());
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

    public ExternalIntegrationLoanSimulationDto saveImportantNotes(SaveImportantNotesRequest request) {
        try {
            JwtSimulasiModel jwtSimulasiModel = jwtLoanSubmissionService.extractToken(request.getToken());
            Optional<ExternalIntegrationLoanSimulationDto> find = findExternalIntegrationByBouwheerCode(jwtSimulasiModel.getBouwheerCode());
            if (find.isEmpty()) {
                jdbcTemplate.update(
                        "insert into _loan_important_notes (bouwheer_code, already_accept_important_notes, dtm_crt) values (?, ?, ?)",
                        jwtSimulasiModel.getBouwheerCode(),
                        true,
                        new Date()
                );

                return findExternalIntegrationByBouwheerCode(jwtSimulasiModel.getBouwheerCode()).get();
            } else {
                return find.get();
            }

        } catch (Exception e) {
            log.error("saveImportantNotes, error {}", e.getMessage());
            throw e;
        }
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
                                    bouwheer_code               varchar(255)                  not null,
                                    already_accept_important_notes boolean default false not null,
                                    dtm_crt                     timestamp             not null
                                );
                            """;

            jdbcTemplate.execute(createTable);
        }
    }

    private Optional<ExternalIntegrationLoanSimulationDto> findExternalIntegrationByBouwheerCode(String bouwheerCode) {
        try {
            initTblImportantNotes();
            ExternalIntegrationLoanSimulationDto result = jdbcTemplate.queryForObject(
                    "select bouwheer_code, already_accept_important_notes, dtm_crt from public._loan_important_notes where bouwheer_code = ? order by id desc limit 1",
                    (rs, rowNum) -> ExternalIntegrationLoanSimulationDto.builder()
                            .bouwheerCode(rs.getString("bouwheer_code"))
                            .alreadyAcceptImportantNotes(rs.getBoolean("already_accept_important_notes"))
                            .dtmCrt(rs.getTimestamp("dtm_crt"))
                            .build(),
                    bouwheerCode
            );
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("findExternalIntegrationByBouwheerCode, error {}", e.getMessage());
            throw e;
        }
    }
}
