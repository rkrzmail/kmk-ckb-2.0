package com.kmkbe.modules.branch_admin.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.AgreementFileSigningMapper;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.ExternalApiService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.major_account.service.MstBranchService;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private FinancingHdrRepository financingHdrRepository;

    @Autowired
    private CsulSignerRepository csulSignerRepository;

    @Autowired
    private AgreementCodeService agreementCodeService;

    @Autowired
    private AgreementFileSigningRepository agreementFileSigningRepository;

    @Autowired
    private NotifDebtorRepository notifDebtorRepository;

    private AgreementFileSigningMapper agreementFileSigningMapper = AgreementFileSigningMapper.INSTANCE;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private CwrRepository cwrRepository;

    @Autowired
    private MstBranchService mstBranchService;

    @Autowired
    private AuthRemoteService authRemoteService;

    @Autowired
    private EmailAo emailAo;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private AgreementRepository agreementRepo;

    @Autowired
    private ExternalApiService externalApiService;

    @Autowired
    private FinancingHdrService financingHdrService;

    private String jwtToken;
    @Autowired
    private DebtorRepository debtorRepository;

    private void ensureJwtToken() {
        jwtToken = authRemoteService.fetchAuthJwt().getData();
    }

    public PaginationResult<VisitorDto>getVisitorReport(
            PaginationRequest request
    ) {
        try {
            int pageNo = 0, pageSize = Integer.MAX_VALUE;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            Page<VisitorDto> pagination = visitorRepository.getDebtorVisitStats(PageRequest.of(pageNo, pageSize), DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()),DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));

            List<VisitorDto> result = pagination.stream()
                    .map(e -> new VisitorDto(
                            e.getDebtorName(),
                            e.getDebtorStatus(),
                            e.getBouwheerName(),
                            e.getPeriodStart(),
                            e.getPeriodEnd(),
                            e.getCountVisit()
                    ))
                    .collect(Collectors.toList());

            return PaginationResult.<VisitorDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(pagination.getTotalElements())
                    .totalPage(pagination.getTotalPages())
                    .list(result)
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    public PaginationResult<ProyeksiReportDto> getProyeksiReport(PaginationRequest request) {
        try {
            int pageNo = 0, pageSize = Integer.MAX_VALUE;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            Page<ProyeksiReportDto> pagination = financingHdrRepository.findActiveCustomersWithInvoiceDetails(PageRequest.of(pageNo, pageSize), DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()),DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()) );

            List<ProyeksiReportDto> result = pagination.stream()
                    .map(e -> new ProyeksiReportDto(
                            e.getDebtorName(),
                            e.getDebtorStatus(),
                            e.getBouwheerName(),
                            e.getInvoiceNo(),
                            e.getAmountInvoice(),
                            e.getAmountFinancing(),
                            e.getInvoiceDueDate(),
                            e.getEffectiveDate()
                    ))
                    .collect(Collectors.toList());

            return PaginationResult.<ProyeksiReportDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(pagination.getTotalElements())
                    .totalPage(pagination.getTotalPages())
                    .list(result)
                    .build();

        } catch (Exception e) {
            throw e;
        }
    }

    public PaginationResult<SummaryByBranchDto> getSummaryByBranch(PaginationRequest request) {
        try {
            int pageNo = 0, pageSize = Integer.MAX_VALUE;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            Page<SummaryByBranchDto> pagination = financingHdrRepository.findSummaryBranch(PageRequest.of(pageNo, pageSize), DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()),DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));

            List<SummaryByBranchDto> result = pagination.stream()
                    .map(e -> {
                        String branchName = getBranchNameByCode(e.getBranchCode());
                        return new SummaryByBranchDto(
                                e.getDebtorName(),
                                branchName,
                                e.getNpwp(),
                                e.getBouwheerName(),
                                e.getTotalPencairan(),
                                e.getPlafondAmount(),
                                e.getUtilizationAmount(),
                                e.getRetentionAmt()
                        );
                    })
                    .collect(Collectors.toList());

            return PaginationResult.<SummaryByBranchDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(pagination.getTotalElements())
                    .totalPage(pagination.getTotalPages())
                    .list(result)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching summary by branch", e);
        }
    }

        public PaginationResult<SummaryByAODto> getAllReportBranchByAO(PaginationRequest request) {
        try {
            int pageNo = 0, pageSize = Integer.MAX_VALUE;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            ensureJwtToken();

            Page<Object[]> dataPage = financingHdrRepository.findFinancingDataByFinancingHdrCode(PageRequest.of(pageNo, pageSize) , DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()),DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));
            List<SummaryByAODto> reportList = new ArrayList<>();
            for (Object[] result : dataPage) {
                double totalDisbursement = (Double) result[0];
                double totalUtilizationAmount = (Double) result[1];
                String customerName = (String) result[2];
                String bouwheerName = (String) result[3];
                double plafondAmount = (Double) result[4];
                double retentionAmount = (Double) result[5];
                String branchCode = (String) result[6];

                String branchName = getBranchNameByCode(branchCode);

                List<Map<String, String>> employeeList = emailAo.getEmailByPosition(branchCode, "AO/AM", jwtToken);
                String employeeName = employeeList.isEmpty() ? "N/A" : employeeList.get(0).get("employeeName");

                SummaryByAODto report = new SummaryByAODto(
                        totalDisbursement,
                        totalUtilizationAmount,
                        customerName,
                        bouwheerName,
                        plafondAmount,
                        retentionAmount,
                        branchName,
                        employeeName
                );

                reportList.add(report);
            }

            return PaginationResult.<SummaryByAODto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(dataPage.getTotalElements())
                    .totalPage(dataPage.getTotalPages())
                    .list(reportList)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching report for all branches by AO", e);
        }
    }

    private String getBranchNameByCode(String branchCode) {
        List<BranchDto> branches = mstBranchService.branchList(null);
        for (BranchDto branch : branches) {
            if (branch.getBranchCode().equals(branchCode)) {
                return branch.getBranchName();
            }
        }
        return "Unknown Branch";
    }

    public PaginationResult<SummaryDetailDto> getSummaryDetail(PaginationRequest request) {
        try {
            int pageNo = 0, pageSize = Integer.MAX_VALUE;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            ensureJwtToken();

            Page<Object[]> dataPage = financingHdrRepository.findSummaryByCustCode(PageRequest.of(pageNo, pageSize), DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()),DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));

            List<SummaryDetailDto> reportList = new ArrayList<>();
            for (Object[] result : dataPage) {
                String debtorName = (String) result[0];
                String npwp = (String) result[1];
                String debtorStatus = (String) result[2];
                String bouwheerName = (String) result[3];
                String branchCode = (String) result[4];
                String cwrCode = (String) result[5];
                String agreementCode = (String) result[6];
                long utilizationSeqNoCount = (long) result[7];
                double persenPencairan = (double) result[8];
                double jumlahPlafonAmount = (double) result[9];
                double totalUtilizationAmount = (double) result[10];
                double sisaPlafon = (double) result[11];
                double adminPencairanFee = (double) result[12];
                double factoringFee = (double) result[13];
                LocalDateTime utilizationDate = (LocalDateTime) result[14];
                String danaSaktiStatus = (String) result[15];
                LocalDateTime invoiceDueDate = (LocalDateTime) result[16];
                LocalDateTime tanggalAktivasi = (LocalDateTime) result[17];
                LocalDateTime tanggalPengajuan = (LocalDateTime) result[18];
                LocalDateTime goliveDate = (LocalDateTime) result[19];

                String branchName = getBranchNameByCode(branchCode);

                List<Map<String, String>> employeeList = emailAo.getEmailByPosition(branchCode, "AO/AM", jwtToken);
                String employeeName = employeeList.isEmpty() ? "N/A" : employeeList.get(0).get("employeeName");

               SummaryDetailDto report = new  SummaryDetailDto(
                        debtorName, npwp, debtorStatus, bouwheerName,
                        employeeName,
                        branchName,
                        cwrCode, agreementCode, utilizationSeqNoCount, persenPencairan,
                        jumlahPlafonAmount, totalUtilizationAmount, sisaPlafon,
                        adminPencairanFee, factoringFee, utilizationDate,
                        danaSaktiStatus, invoiceDueDate,
                        tanggalAktivasi, tanggalPengajuan, goliveDate
                );
                reportList.add(report);
            }

            return PaginationResult.<SummaryDetailDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(dataPage.getTotalElements())
                    .totalPage(dataPage.getTotalPages())
                    .list(reportList)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching summary details", e);
        }
    }

    public PaginationResult<ReportDueDateDto> getDueDateDetail(PaginationRequest request) {
        try {
            int pageNo = 0, pageSize = Integer.MAX_VALUE;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            ensureJwtToken();

            Page<Object[]> dataPage = financingHdrRepository.findDueDateReport(PageRequest.of(pageNo, pageSize), DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()),DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));

            List<ReportDueDateDto> reportList = new ArrayList<>();
            for (Object[] result : dataPage) {
                String debtorName = (String) result[0];
                String npwp = (String) result[1];
                String bouwheerName = (String) result[2];
                String branchCode = (String) result[3];
                String agreementNo = (String) result[4];
                LocalDateTime goliveDate = (LocalDateTime) result[5];
                long utilizationSeqNo = (long) result[6];
                double utilizationAmount = (double) result[7];
                double osAr = (double) result[8];
                double effectiveRate = (double) result[9];
                double retentionAmount = (double) result[10];
                double lcAmount = (double) result[11];
                LocalDateTime invoiceDueDate = (LocalDateTime) result[12];
                LocalDateTime settlementDate = (LocalDateTime) result[13];
                String financingStatus = (String) result[14];

                String branchName = getBranchNameByCode(branchCode);

                List<Map<String, String>> employeeList = emailAo.getEmailByPosition(branchCode, "AO/AM", jwtToken);
                String employeeName = employeeList.isEmpty() ? "N/A" : employeeList.get(0).get("employeeName");

                ReportDueDateDto report = new ReportDueDateDto(
                        debtorName, npwp, bouwheerName, employeeName, branchName,
                        agreementNo, goliveDate, utilizationSeqNo, utilizationAmount,
                        osAr, effectiveRate, retentionAmount, lcAmount, invoiceDueDate, settlementDate, financingStatus
                );
                reportList.add(report);
            }

            return PaginationResult.<ReportDueDateDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(dataPage.getTotalElements())
                    .totalPage(dataPage.getTotalPages())
                    .list(reportList)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching report due date", e);
        }
    }

    private <T> T safeApiCall(Supplier<T> apiCall, T fallback) {
        try {
            return apiCall.get();
        } catch (Exception e) {
            System.err.println("External API error: " + e.getMessage());
            return fallback;
        }
    }

    public byte[] generateReport(String financingHdrCode, String agreementCode, String branchManager, String areaSalesManager) throws JRException, IOException
    {

        UUID financingHdrUuid;
        try {
            financingHdrUuid = UUID.fromString(financingHdrCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid financingHdrCode format");
        }

        Agreement agreement = agreementRepo.findByFinancingHdrCode(financingHdrUuid, agreementCode)
                .orElseThrow(() -> new NoSuchElementException(
                        "Data Agreement tidak ditemukan untuk: " + financingHdrCode
                ));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
        String tanggalDokumen = LocalDate.now().format(formatter);

        // get csul signer
        CsulSigner branchManagerData = csulSignerRepository
                .findByKaryawanNameAndJabatan(branchManager, "Branch Manager")
                .orElseThrow(() -> new IllegalArgumentException("Branch Manager " + branchManager + " tidak ditemukan"));

        CsulSigner areaSalesManagerData = csulSignerRepository
                .findByKaryawanNameAndJabatan(areaSalesManager, "Area Sales Manager")
                .orElseThrow(() -> new IllegalArgumentException("Area Sales Manager " + areaSalesManager + " tidak ditemukan"));

        // 1. AppResponse
        AppResponse apiResponse = safeApiCall(
                () -> externalApiService.getAppByAppNo(agreement.getApplicationCode()),
                new AppResponse() {{
                    setAppNo("-");
                    setTenor("-");
                    setLobCode("-");
                    setProdOfferingName("-");
                    setAppId(0);
                }}
        );
        Integer appId = apiResponse.getAppId();

        // 2. Rek Debitur
        RekDebiturResponse.BankAccount dataRekening = safeApiCall(() -> {
            RekDebiturResponse bankResponse = externalApiService.getRekDebitur(agreement.getApplicationCode());
            if (bankResponse != null && bankResponse.getBankAccounts() != null && !bankResponse.getBankAccounts().isEmpty()) {
                return bankResponse.getBankAccounts().get(0);
            }
            return new RekDebiturResponse.BankAccount("-", "-", "-");
        }, new RekDebiturResponse.BankAccount("-", "-", "-"));

        // 3. Factoring data
        AppFactoringResponse factoringData = safeApiCall(
                () -> externalApiService.getAppFactoringData(appId),
                new AppFactoringResponse() {{
                    setDiskontoAmount("0");
                    setTotalRetentionAmount("0");
                    setTotalInvoiceAmount("0");
                }}
        );

        // 4. Agreement Code
        String agrmntCode = agreementRepo
                .findAgreementCodeByFinancingHdrCode(UUID.fromString(financingHdrCode), agreementCode)
                .orElse("-");

        FinancialDataResponse fallbackFinancialData = new FinancialDataResponse();

        FinancialDataResponse.FinancialData defaultFinData = new FinancialDataResponse.FinancialData();
        defaultFinData.setNtfAmount("0");
        defaultFinData.setEffectiveRate("0");
        defaultFinData.setInstallmentAmount("0");
        defaultFinData.setMaxRefundAmount("0");
        defaultFinData.setTotalFeeAmount("0");
        defaultFinData.setGracePeriod("0");
        fallbackFinancialData.setFinancialData(defaultFinData);

        fallbackFinancialData.setFeeList(Collections.emptyList());

        FinancialDataResponse financialData = safeApiCall(
                () -> externalApiService.getFinancialData(agrmntCode),
                fallbackFinancialData
        );

        FinancialDataResponse.FinancialData findata = financialData.getFinancialData();

        // 6. CWR
        String cwrCode = agreementRepo
                .findCwrCodeByFinancingHdrCode(UUID.fromString(financingHdrCode), agreementCode)
                .orElse("-");

        CwrBwhrResponse.ListCwrBwhr cwrBwhr = safeApiCall(() -> {
            CwrBwhrResponse cwrBwhrData = externalApiService.getCwrBwhr(cwrCode);
            return (cwrBwhrData != null && cwrBwhrData.getCwrBouwheerCustNos() != null
                    && !cwrBwhrData.getCwrBouwheerCustNos().isEmpty())
                    ? cwrBwhrData.getCwrBouwheerCustNos().get(0)
                    : null;
        }, null);

        Optional<Map<String, Object>> cwrData = agreementRepo.findCwrCodeAndDate(UUID.fromString(financingHdrCode), agreementCode);
        Map<String, Object> Cdata = cwrData.orElseGet(Collections::emptyMap);
        SimpleDateFormat Csdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String formattedCwrDate = cwrData
                .map(data -> {
                    Object rawDate = data.get("cwr_start_date");
                    if (rawDate instanceof Date) {
                        return Csdf.format((Date) rawDate);
                    }
                    return "-";
                })
                .orElse("-");

        // 7. Bouwheer List
        CwrListBwhrResponse bouwheerData = safeApiCall(
                () -> externalApiService.getListCwrBwhr(cwrCode, cwrBwhr != null ? cwrBwhr.getCwrBouwheerCustNo() : "-"),
                new CwrListBwhrResponse(Collections.emptyList())
        );

        // 8. Debtor Name
        String debtorName = agreementRepo.findCustNameByFinancingHdrCode(financingHdrUuid, agreementCode)
                .orElse("Debtor Name");

        // 9. Karyawan
        Optional<Map<String, Object>> karyawanData = debtorRepository.findKaryawanByDebtorName(debtorName);
        Map<String, Object> Kdata = karyawanData.orElseGet(Collections::emptyMap);

        // 10. Facility
        String facility = agreementRepo.findFaciltyByFinancingHdrCode(UUID.fromString(financingHdrCode), agreementCode);

        BigDecimal effectiveRate = new BigDecimal(findata.getEffectiveRate());
        effectiveRate = effectiveRate.setScale(2, RoundingMode.HALF_UP);
        String effectiveRateStr = effectiveRate.stripTrailingZeros().toPlainString() + "%";

        // 11. Debtor Data
        Optional<Map<String, Object>> debtorData = agreementRepo.finddetailDebtor(UUID.fromString(financingHdrCode), agreementCode);
        Map<String, Object> data = debtorData.orElseGet(Collections::emptyMap);

        Map<String, Object> params = new HashMap<>();
        params.put("SUBREPORT_DIR", getClass().getResource("/Reports/").toString());


        params.put("NamaBranchManager", branchManagerData.getKaryawanName());
        params.put("JabatanBranchManager", branchManagerData.getJabatan());
        params.put("NamaAreaSalesManager", areaSalesManagerData.getKaryawanName());
        params.put("AppNo", apiResponse.getAppNo());
        params.put("TglDokumen", tanggalDokumen);
        params.put("DebtorName", debtorName);
        params.put("Cwr", Cdata.getOrDefault("cwr_code", "-").toString());
        params.put("Cwr-Date", formattedCwrDate);
        params.put("BankName", dataRekening.getBankName());
        params.put("BankAccNo", dataRekening.getAccNo());
        params.put("BankAccName", dataRekening.getAccName());
        params.put("NamaKaryawan", Kdata.getOrDefault("Karyawan_name", "-").toString());
        params.put("Jabatan", Kdata.getOrDefault("Jabatan", "-").toString());
        params.put("JabatanSigner", Kdata.getOrDefault("Jabatan", "-").toString());
        params.put("IdentitySigner", Kdata.getOrDefault("identity_no", "-").toString());
        params.put("AlamatSigner", Kdata.getOrDefault("alamat", "-").toString());
        params.put("NamaPerusahaan", debtorName);
        params.put("AgrmntNo", agrmntCode);
        params.put("Facility", facility);
        params.put("Tenor", apiResponse.getTenor());
        params.put("NtfAmt", findata.getNtfAmount());
        params.put("DiskontoAmt", factoringData.getDiskontoAmount());
        params.put("MaxAllocatedRefundAmt", findata.getMaxRefundAmount());
        params.put("TotalRetentionAmt", factoringData.getTotalRetentionAmount());
        params.put("LobCode", apiResponse.getLobCode());
        params.put("ProdOfferingName", apiResponse.getProdOfferingName());
        params.put("EffectiveRatePrcnt", effectiveRateStr);
        params.put("TotalFeeAmt", findata.getTotalFeeAmount());
        params.put("TotalInvcAmt", factoringData.getTotalInvoiceAmount());
        params.put("GracePeriodLc", findata.getGracePeriod());
        params.put("InstAmt", findata.getInstallmentAmount());
        params.put("AgmtNo", agrmntCode);
        params.put("JenisDebitur", "Badan Usaha");
        params.put("TipePerusahaan", data.getOrDefault("cust_company_type", "-").toString());
        params.put("NPWP", data.getOrDefault("cust_id_no", "-").toString());
        params.put("Alamat", data.getOrDefault("company_address", "-").toString());
        params.put("Email", data.getOrDefault("cust_email", "-").toString());
        params.put("Telepon", data.getOrDefault("phone", "-").toString());

        List<CwrListBwhrResponse.ListData> bwList =
                Optional.ofNullable(bouwheerData)
                        .map(CwrListBwhrResponse::getCwrListBouwheerCustNo)
                        .orElse(Collections.emptyList());

        CwrListBwhrResponse.ListData bouwheer = !bwList.isEmpty() ? bwList.get(0) : null;

        BigDecimal totalPiutang = BigDecimal.ZERO;
        BigDecimal appFeeFactoring = BigDecimal.ZERO;
        BigDecimal appFeeAdministration = BigDecimal.ZERO;
        BigDecimal appFeeInsurance = BigDecimal.ZERO;
        BigDecimal appFeeCreditInsurance = BigDecimal.ZERO;

        BigDecimal ntfAmt = new BigDecimal(findata.getNtfAmount());

        params.put("AppFeeAmtAdministration", fmtAmount(BigDecimal.ZERO));
        params.put("AppFeeAmtFactoring", fmtAmount(BigDecimal.ZERO));
        params.put("AppFeeInsurance", fmtAmount(BigDecimal.ZERO));
        params.put("AppFeeCreditInsurance", fmtAmount(BigDecimal.ZERO));


        for (var fee : financialData.getFeeList()) {
            if (fee.getFeeTypeName() != null) {
                if (fee.getFeeTypeName().equalsIgnoreCase("BIAYA FACTORING")) {
                    appFeeFactoring = new BigDecimal(fee.getFeeAmount());
                    params.put("AppFeeAmtFactoring", fmtAmount(appFeeFactoring));
                } else if (fee.getFeeTypeName().equalsIgnoreCase("BIAYA ADMINISTRASI PENCAIRAN")) {
                    appFeeAdministration = new BigDecimal(fee.getFeeAmount());
                    params.put("AppFeeAmtAdministration", fmtAmount(appFeeAdministration));
                } else if (fee.getFeeTypeName().equalsIgnoreCase("Total CWR Insurance Fee")) {
                    appFeeInsurance = new BigDecimal(fee.getFeeAmount());
                    params.put("AppFeeInsurance", fmtAmount(appFeeInsurance));
                } else if (fee.getFeeTypeName().equalsIgnoreCase("Total CWR Credit Insurance Fee")) {
                    appFeeCreditInsurance = new BigDecimal(fee.getFeeAmount());
                    params.put("AppFeeCreditInsurance", fmtAmount(appFeeCreditInsurance));
                }
            }
        }

        BigDecimal totalInsuranceVal = appFeeInsurance.add(appFeeCreditInsurance);
        StringBuilder totalInsuranceText = new StringBuilder(fmtAmount(totalInsuranceVal));

        if (appFeeInsurance.compareTo(BigDecimal.ZERO) > 0) {
            totalInsuranceText.append(", All Risk + SRCC");
        }
        if (appFeeCreditInsurance.compareTo(BigDecimal.ZERO) > 0) {
            if (appFeeInsurance.compareTo(BigDecimal.ZERO) > 0) {
                totalInsuranceText.append(", ");
            } else {
                totalInsuranceText.append(", ");
            }
            totalInsuranceText.append("Asuransi Kredit");
        }
        BigDecimal administrationFactoring = appFeeFactoring.add(appFeeAdministration);
        BigDecimal ntfAmtTotal = ntfAmt.subtract(administrationFactoring);

        params.put("Administration+Factoring", fmtAmount(administrationFactoring.toString()));
        params.put("NtfAmt-Total", ntfAmtTotal.toString());
        params.put("TotalInsurance", totalInsuranceText.toString());
        params.put("Limit", "IDR 0.00");
        params.put("Notaris","IDR 0.00");
        params.put("PengikatanJaminan","IDR 0.00");
        params.put("Provisi","IDR 0.00");
        params.put("Survey","IDR 0.00");

        CommonResult<SitDto> sitData = agreementCodeService.getAgreementsByFinancingHdrCode(UUID.fromString(financingHdrCode));
        if (sitData.getCode() == 200 && sitData.getData() != null) {
            SitDto sitDto = sitData.getData();

            DecimalFormat currency = new DecimalFormat("#,##0.00");
            String totalPembayaran = currency.format(sitDto.getTotalInvoiceAmt());

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
            String periodeBerlaku = sitDto.getFinancingDueDate() != null
                    ? sitDto.getFinancingDueDate().format(dateFormat)
                    : "-";

        params.put("NoDokumen", sitDto.getAgreementCode());
        params.put("TempatTanggal", tanggalDokumen);
        params.put("PeriodeBerlaku", periodeBerlaku);
        params.put("NamaGMFinance", "General Manager Finance PT. Trakindo Utama");
        params.put("NamaDirektur", sitDto.getDirectorName());
        params.put("NamaGMFinanceCSUL", "");
        params.put("TotalPembayaran", totalPembayaran);
        } else {
            throw new RuntimeException("Failed to get agreement data: " + (sitData.getMessage() != null ? sitData.getMessage() : ""));
        }

        // tabel 3
        List<Map<String, String>> tableData3 = new ArrayList<>();

        FinancingHdr financingHdr = financingHdrService.findByCode(financingHdrCode);
        PaginationResult<PostedInvoiceDto> invoiceResult = invoiceService.invoiceSubmissionByFinancingHdr(
                financingHdr,
                new PaginationRequest()
        );

        String invoiceDueDateParam = "-";
        if (invoiceResult != null && invoiceResult.getList() != null && !invoiceResult.getList().isEmpty()) {
            for (PostedInvoiceDto invoice : invoiceResult.getList()) {
                Map<String, String> row = new HashMap<>();
                row.put("nomor_invoice", invoice.getCustomerInvoiceNo() != null ? invoice.getCustomerInvoiceNo() : "-");
                row.put("tanggal_invoice", fmtDateObj(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : "-"));
                row.put("invoice_amt", fmtRupiah(invoice.getInvoiceAmount() != null ? invoice.getInvoiceAmount().toString() : "-"));
                row.put("description", invoice.getInvoiceDescription() != null ? invoice.getInvoiceDescription() : "-");
                row.put("bouwheer", invoice.getBouwheerName() != null ? invoice.getBouwheerName() : "-");
                row.put("invoice_duedate", fmtDateObj(invoice.getInvoiceDueDate() != null ? invoice.getInvoiceDueDate() : "-"));
                tableData3.add(row);
            }

            PostedInvoiceDto firstInvoice = invoiceResult.getList().get(0);
            if (firstInvoice.getInvoiceDueDate() != null) {
                invoiceDueDateParam = fmtDateObj(firstInvoice.getInvoiceDueDate());
            }
        } else {
            Map<String, String> emptyRow = new HashMap<>();
            emptyRow.put("nomor_invoice", "No Data Available");
            emptyRow.put("tanggal_invoice", "No Data Available");
            emptyRow.put("invoice_amt", "No Data Available");
            emptyRow.put("description", "No Data Available");
            emptyRow.put("bouwheer", "No Data Available");
            emptyRow.put("invoice_duedate", "No Data Available");
            tableData3.add(emptyRow);
        }
        params.put("tableDataSource3", new JRBeanCollectionDataSource(tableData3));

        params.put("InvoiceDueDate", invoiceDueDateParam);

        // tabel 2
        List<Map<String, String>> tableData2 = new ArrayList<>();

        for (PostedInvoiceDto invoice : invoiceResult.getList()) {
            Map<String, String> row = new HashMap<>();
            row.put("nomor_invoice", invoice.getCustomerInvoiceNo() != null ? invoice.getCustomerInvoiceNo() : "-");
            row.put("tanggal_invoice", fmtDateObj(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : "-"));
            row.put("invoice_duedate", fmtDateObj(invoice.getInvoiceDueDate() != null ? invoice.getInvoiceDueDate() : "-"));
            tableData2.add(row);
        }

        if (tableData2.isEmpty()) {
            Map<String, String> emptyRow = new HashMap<>();
            emptyRow.put("nomor_invoice", "No Data Available");
            emptyRow.put("tanggal_invoice", "No Data Available");
            emptyRow.put("invoice_duedate", "No Data Available");
            tableData2.add(emptyRow);
        }

        params.put("tableDataSource2", new JRBeanCollectionDataSource(tableData2));

        // tabel 1
        int counter = 1;
        List<Map<String, String>> tableData = new ArrayList<>();
        for (PostedInvoiceDto invoice : invoiceResult.getList()) {
            BigDecimal amt = invoice.getInvoiceAmount() != null ? new BigDecimal(invoice.getInvoiceAmount().toString()) : BigDecimal.ZERO;
            totalPiutang = totalPiutang.add(amt);
        }

        for (PostedInvoiceDto invoice : invoiceResult.getList()) {

            Map<String, String> row = new HashMap<>();
            row.put("no", String.valueOf(counter++));
            row.put("customer", debtorName);

            row.put("nomor_perjanjian", bouwheer != null ? Objects.toString(bouwheer.getCooperationAgreementNo(), "-") : "-");
            row.put("tanggal_perjanjian", bouwheer != null ? fmtDateObj(bouwheer.getStartPeriod()) : "-");

            row.put("nomor_invoice", invoice.getCustomerInvoiceNo() != null ? invoice.getCustomerInvoiceNo() : "-");
            row.put("tanggal_invoice", fmtDateObj(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : "-"));
            row.put("jumlah_piutang", fmtRupiah(invoice.getInvoiceAmount() != null ? invoice.getInvoiceAmount().toString() : "-"));
            row.put("total_piutang", fmtRupiah(totalPiutang));

            tableData.add(row);
        }
        if (tableData.isEmpty()) {
            Map<String, String> emptyRow = new HashMap<>();
            emptyRow.put("no", "1");
            emptyRow.put("customer", "No Data Available");
            emptyRow.put("nomor_perjanjian", "No Data Available");
            emptyRow.put("tanggal_perjanjian", "No Data Available");
            emptyRow.put("nomor_invoice", "No Data Available");
            emptyRow.put("tanggal_invoice", "No Data Available");
            emptyRow.put("jumlah_piutang", "No Data Available");
            emptyRow.put("total_piutang", "No Data Available");
            tableData.add(emptyRow);
        }

        params.put("tableDataSource", new JRBeanCollectionDataSource(tableData));

        InputStream reportStream = getClass().getResourceAsStream("/Reports/main_report.jrxml");
        if (reportStream == null) {
            throw new FileNotFoundException("main_report.jrxml tidak ditemukan di /Reports");
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        JRDataSource dataSource = new JREmptyDataSource();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private static String fmtDateObj(Object val) {
        if (val == null) return "-";

        final String targetFormat = "dd-MM-yyyy";
        java.time.format.DateTimeFormatter outFmt = java.time.format.DateTimeFormatter.ofPattern(targetFormat);

        try {
            if (val instanceof java.util.Date) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(targetFormat);
                return sdf.format((java.util.Date) val);
            }

            if (val instanceof java.time.LocalDate) {
                return ((java.time.LocalDate) val).format(outFmt);
            }

            if (val instanceof java.time.LocalDateTime) {
                return ((java.time.LocalDateTime) val).toLocalDate().format(outFmt);
            }

            if (val instanceof String) {
                String s = ((String) val).trim();
                if (s.isEmpty()) return "-";

                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(
                            s,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    );
                    return ldt.toLocalDate().format(outFmt);
                } catch (Exception ignore) {}

                try {
                    java.time.LocalDate ld = java.time.LocalDate.parse(s);
                    return ld.format(outFmt);
                } catch (Exception ignore) {}

                try {
                    java.text.SimpleDateFormat inSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date d = inSdf.parse(s);
                    java.text.SimpleDateFormat outSdf = new java.text.SimpleDateFormat(targetFormat);
                    return outSdf.format(d);
                } catch (Exception ignore) {}
            }

            return val.toString();
        } catch (Exception e) {
            return "-";
        }
    }


    private static String fmtAmount(Object val) {
        if (val == null) return "IDR 0.00";
        try {
            java.math.BigDecimal bd = (val instanceof java.math.BigDecimal)
                    ? (java.math.BigDecimal) val
                    : new java.math.BigDecimal(val.toString().replace(",", ""));
            return "IDR " + bd.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            return "IDR 0.00";
        }
    }

    private String fmtRupiah(Object amount) {
        if (amount == null) return "-";
        try {
            BigDecimal value = new BigDecimal(amount.toString());
            NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));
            numberFormat.setMaximumFractionDigits(0);
            return "Rp" + numberFormat.format(value);
        } catch (Exception e) {
            return "-";
        }
    }

    public SigningResponse sendDocumentForSigning(
            String financingHdrCode,
            String agreementCode,
            String branchManager,
            String areaSalesManager,
            Authentication authentication) {

        try {

            List<AgreementFileSigning> existingFiles = agreementFileSigningRepository.findByAgreementCode(agreementCode);

            boolean alreadySigned = existingFiles.stream()
                    .anyMatch(file -> "signed".equalsIgnoreCase(file.getStamp()));

            if (alreadySigned) {
                return SigningResponse.builder()
                        .success(false)
                        .documentId(null)
                        .message("Dokumen dengan agreementCode " + agreementCode + " sudah ditandatangani, tidak bisa dikirim ulang.")
                        .build();
            }

            byte[] pdfBytes = generateReport(financingHdrCode, agreementCode, branchManager, areaSalesManager);

            String username = authentication != null ? authentication.getName() : "SYSTEM";

            ExternalSigningRequest request = prepareSigningRequest(
                    financingHdrCode,
                    agreementCode,
                    pdfBytes,
                    username,
                    branchManager,
                    areaSalesManager
            );

            try {
                String requestJson = new ObjectMapper().writeValueAsString(request);
                log.info("E-Sign Request Payload: {}", requestJson);
            } catch (Exception e) {
                log.error("Error serializing request", e);
            }

            ExternalSigningResponse esignResponse = externalApiService.callEsignApi(request);

            if (esignResponse.getStatus().getCode() == 0) {
                saveToDatabase(
                        agreementCode,
                        esignResponse.getDocuments().get(0).getDocumentId(),
                        username,
                        financingHdrCode
                );

                return SigningResponse.builder()
                        .success(true)
                        .documentId(esignResponse.getDocuments().get(0).getDocumentId())
                        .message("Document sent for signing successfully")
                        .build();
            } else {
                String errorMessage = esignResponse.getStatus().getMessage();
                throw new RuntimeException("E-sign API error: " + errorMessage);
            }
        } catch (Exception e) {
            return SigningResponse.builder()
                    .success(false)
                    .documentId(null)
                    .message(e.getMessage())
                    .build();
        }
    }

    private ExternalSigningRequest prepareSigningRequest(
            String financingHdrCode,
            String agreementCode,
            byte[] pdfBytes,
            String username,
            String branchManager,
            String areaSalesManager
    ) {
        String branchCode = getBranchCodeFromAgreement(agreementCode);

        Agreement agreement = agreementRepo.findByAgreementCode(agreementCode)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

        return ExternalSigningRequest.builder()
                .tenantCode("CSUL_DEV")
                .psreCode("VIDA")
                .audit(new ExternalSigningRequest.Audit(username))
                .requests(List.of(
                        ExternalSigningRequest.DocumentRequest.builder()
                                .referenceNo(agreementCode)
                                .documentTemplateCode("DATA TEST DANA SAKTI")
                                .documentName("DOKUMEN SAKTI")
                                .officeCode(branchCode)
                                .officeName(username)
                                .regionCode(branchCode)
                                .regionName("JAKARTA")
                                .businessLineCode("CBU")
                                .businessLineName("Corporate Business Unit")
                                .signers(prepareSigners(agreement, financingHdrCode, branchManager, areaSalesManager))
                                .documentFile(base64Pdf)
                                .build()
                ))
                .build();
    }

    private List<ExternalSigningRequest.Signer> prepareSigners(Agreement agreement, String financingHdrCode, String branchManager, String areaSalesManager) {
        List<ExternalSigningRequest.Signer> signers = new ArrayList<>();

        String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(UUID.fromString(financingHdrCode));

        Debtor debtor = debtorRepository
                .findActiveSignerByDebtorName(debtorName)
                .orElseThrow(() -> new RuntimeException("Tidak ada data signer active dari financingHdr = " + financingHdrCode));

        signers.add(ExternalSigningRequest.Signer.builder()
                        .signAction("mt")
                        .signerType("CUST")
                        .idKtp(debtor.getIdentityNo())
                        .tlp(debtor.getNoTelp())
                        .email(debtor.getEmail())
                        .seqNo("0")
                        .build());

        CsulSigner bm = csulSignerRepository.findByKaryawanName(branchManager)
                .orElseThrow(() -> new RuntimeException("Branch Manager " + branchManager + " tidak ditemukan di csul_signer"));

        signers.add(ExternalSigningRequest.Signer.builder()
                .signAction("mt")
                .signerType("MF")
                .idKtp(bm.getIdentityNo())
                .tlp(bm.getNoTelp())
                .email(bm.getEmail())
                .seqNo("1")
                .build());

//        CsulSigner asm = csulSignerRepository.findByKaryawanName(areaSalesManager)
//                .orElseThrow(() -> new RuntimeException("Area Sales Manager " + areaSalesManager + " tidak ditemukan di csul_signer"));
//
//        signers.add(ExternalSigningRequest.Signer.builder()
//                .signAction("mt")
//                .signerType("SPV")
//                .idKtp(asm.getIdentityNo())
//                .tlp(asm.getNoTelp())
//                .email(asm.getEmail())
//                .seqNo("2")
//                .build());
        return signers;
    }

    private AgreementFileSigningDto saveToDatabase(String agreementCode, String documentId, String username, String financingHdrCode) {

        String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(UUID.fromString(financingHdrCode));
        Debtor debtor = debtorRepository
                .findActiveSignerByDebtorName(debtorName)
                .orElseThrow(() -> new RuntimeException("Tidak ada data signer active dari financingHdr = " + financingHdrCode));

//        AgreementFileSigning entity = AgreementFileSigning.builder()
//                .agreementCode(agreementCode)
//                .fileTypeCode("E_SIGN_DOC")
//                .fileName("PERJANJIAN_1A_" + agreementCode + ".pdf")
//                .stamp("Not Signed")
//                .usrCrt(username)
//                .dtmCrt(LocalDateTime.now())
//                .signer(debtor.getKaryawanName())
//                .emailSigner(debtor.getEmail())
//                .identityNo(debtor.getIdentityNo())
//                .documentId(documentId)
//                .financingHdrCode(financingHdrCode)
//                .build();

        List<AgreementFileSigning> existingList = agreementFileSigningRepository.findByAgreementCode(agreementCode);

        AgreementFileSigning entity;
        if (!existingList.isEmpty()) {
            entity = existingList.get(0);

            if (existingList.size() > 1) {
                agreementFileSigningRepository.deleteAll(existingList.subList(1, existingList.size()));
            }

        } else {
            entity = AgreementFileSigning.builder()
                    .agreementCode(agreementCode)
                    .fileTypeCode("E_SIGN_DOC")
                    .fileName("PERJANJIAN_1A_" + agreementCode + ".pdf")
                    .usrCrt(username)
                    .dtmCrt(LocalDateTime.now())
                    .build();
        }

        entity.setStamp("Not Signed");
        entity.setSigner(debtor.getKaryawanName());
        entity.setEmailSigner(debtor.getEmail());
        entity.setIdentityNo(debtor.getIdentityNo());
        entity.setDocumentId(documentId);
        entity.setFinancingHdrCode(financingHdrCode);
        entity.setUsrUpd(username);
        entity.setDtmUpd(LocalDateTime.now());

        AgreementFileSigning saveDoc =  agreementFileSigningRepository.save(entity);

        String custCode = String.valueOf(financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode))
                .map(finHdr -> finHdr.getCustomer().getCustCode())
                .orElseThrow(() -> new RuntimeException("FinancingHdr dengan code "
                        + financingHdrCode + " tidak ditemukan")));

        notifDebtorRepository.save(NotifDebtor.builder()
                .notification("Permintaan Tanda Tangan Dokumen")
                .description("Dokumen yang memerlukan tanda tangan " + debtor.getKaryawanName() + ", telah tersedia. Mohon segera mendandatangani dokumen tersebut atau menghubungi pihak terkait.")
                .financingHdrCode(financingHdrCode)
                .custCode(custCode)
                .usrCrt(username)
                .dtmCrt(LocalDateTime.now())
                .build());

        return agreementFileSigningMapper.entityToDto(saveDoc);

    }

    private String getBranchCodeFromAgreement(String agreementCode) {
        return agreementRepo.findByAgreementCode(agreementCode)
                .map(agreement -> {
                    if (agreement.getFinancingHdr() == null) {
                        throw new RuntimeException("FinancingHdr tidak ditemukan untuk agreement " + agreementCode);
                    }
                    String cwrCode = agreement.getCwr().getCwrCode();

                    return cwrRepository.findByCwrCode(cwrCode)
                            .map(cwr -> {
                                if (cwr.getBranchCode() == null || cwr.getBranchCode().isEmpty()) {
                                    throw new RuntimeException("Branch code kosong untuk cwr " + cwrCode);
                                }
                                return cwr.getBranchCode();
                            })
                            .orElseThrow(() -> new RuntimeException("Cwr dengan code " + cwrCode + " tidak ditemukan"));
                })
                .orElseThrow(() -> new RuntimeException("Agreement dengan code " + agreementCode + " tidak ditemukan"));
    }
}
