package com.kmkbe.modules.branch_admin.service;


import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Visitor;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.ExternalApiService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.major_account.service.MstBranchService;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private FinancingHdrRepository financingHdrRepository;

    @Autowired
    private AgreementCodeService agreementCodeService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private MstBranchService mstBranchService;

    @Autowired
    private AuthRemoteService authRemoteService;

    @Autowired
    private EmailAo emailAo;

    @Autowired
    private AgreementRepository agreementRepo;

    @Autowired
    private ExternalApiService externalApiService;

    private JasperReport cachedReport;

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

            // Use the Pageable object to manage pagination
            Page<ProyeksiReportDto> pagination = financingHdrRepository.findActiveCustomersWithInvoiceDetails(PageRequest.of(pageNo, pageSize), DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()),DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()) );

            // Collecting the results into a list
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
            // Setup pagination
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

    // Helper method to get the branch name using branchCode from mstBranchService
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

    public byte[] generateReport(String financingHdrCode, String agreementCode) throws JRException, IOException {

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

        // getAppbyAppNo
        AppResponse apiResponse = externalApiService.getAppByAppNo(agreement.getApplicationCode());
        Integer appId = apiResponse.getAppId();

        //getNomor Rek Debitur
        RekDebiturResponse BankResponse = externalApiService.getRekDebitur(agreement.getApplicationCode());
        RekDebiturResponse.BankAccount dataRekening = BankResponse.getBankAccounts().get(0);

        // getAppFctr
        AppFactoringResponse factoringData = externalApiService.getAppFactoringData(appId);

        // getFinancialData
        String agrmntCode = agreementRepo
                .findAgreementCodeByFinancingHdrCode(UUID.fromString(financingHdrCode), agreementCode)
                .orElseThrow(() -> new RuntimeException("Agreement tidak ditemukan untuk financingHdrCode: " + financingHdrCode));
        FinancialDataResponse financialData = externalApiService.getFinancialData(agrmntCode);

        //getcwrbouwheerNo
        String cwrCode = agreementRepo
                .findCwrCodeByFinancingHdrCode(UUID.fromString(financingHdrCode), agreementCode)
                .orElseThrow(() -> new RuntimeException("Agreement tidak ditemukan untuk financingHdrCode: " + financingHdrCode));
        CwrBwhrResponse cwrBwhrData = externalApiService.getCwrBwhr(cwrCode);
        CwrBwhrResponse.ListCwrBwhr cwrBwhr = cwrBwhrData.getCwrBouwheerCustNos().get(0);

        //getlisttable1
        CwrListBwhrResponse bouwheerData = externalApiService.getListCwrBwhr(cwrCode, cwrBwhr.getCwrBouwheerCustNo());

        // get bouwheer name
        String debtorName = agreementRepo.findCustNameByFinancingHdrCode(financingHdrUuid, agreementCode)
                .orElse("Debtor Name");

        //get karyawan
        Optional<Map<String, Object>> karyawanData = debtorRepository.findKaryawanByFinancingHdrCode(financingHdrCode);
        Map<String, Object> Kdata = karyawanData.orElseGet(Collections::emptyMap);

//        Map<String, Object> result = agreementRepo
//                .findCwrCodeAndCustNo(UUID.fromString(financingHdrCode))
//                .orElseThrow(() -> new RuntimeException("Agreement tidak ditemukan untuk financingHdrCode: " + financingHdrCode));
//        String cwrCodeResult = (String) result.get("cwrCode");
//        String custNoResult = (String) result.get("custNo");
//        SignerApiResponse signerData = externalApiService.getKaryawan(cwrCodeResult, custNoResult);
//        String namaKaryawan = Optional.ofNullable(signerData.getReturnObject())
//                .filter(list -> !list.isEmpty())
//                .map(list -> list.get(0))
//                .map(SignerApiResponse.SignerData::getSignerName)
//                .orElse("Tidak tersedia");
//
//        String jabatan = Optional.ofNullable(signerData.getReturnObject())
//                .filter(list -> !list.isEmpty())
//                .map(list -> list.get(0))
//                .map(SignerApiResponse.SignerData::getSignerPosition)
//                .orElse("Tidak tersedia");

        // get facility
        String facility = agreementRepo.findFaciltyByFinancingHdrCode(UUID.fromString(financingHdrCode), agreementCode);

        // get invoice due date
        Date invoiceDueDate = agreementRepo.findInvoiceDueDateByFinancingHdrCode(UUID.fromString(financingHdrCode), agreementCode)
                .orElseThrow(() -> new RuntimeException("Invoice due date tidak ditemukan"));

        // Format tanggal jika perlu
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = sdf.format(invoiceDueDate);

        // get fap
        Optional<Map<String, Object>> debtorData = agreementRepo.finddetailDebtor(UUID.fromString(financingHdrCode), agreementCode);
        Map<String, Object> data = debtorData.orElseGet(Collections::emptyMap);

        Map<String, Object> params = new HashMap<>();
        FinancialDataResponse.FinancialData findata = financialData.getFinancialData();
        params.put("SUBREPORT_DIR", getClass().getResource("/Reports/").toString());

        //lembar 1
        params.put("AppNo", apiResponse.getAppNo());
        params.put("TglDokumen", tanggalDokumen);
        params.put("DebtorName", debtorName);
        params.put("BankName", dataRekening.getBankName());
        params.put("BankAccNo", dataRekening.getAccNo());
        params.put("BankAccName", dataRekening.getAccName());
        params.put("NamaKaryawan", Kdata.getOrDefault("Karyawan_name", "-").toString());
        params.put("Jabatan", Kdata.getOrDefault("Jabatan", "-").toString());

        List<Map<String, String>> tableData = new ArrayList<>();
        Map<String, String> row1 = new HashMap<>();
        row1.put("no", "1");
        row1.put("customer", "Debtor Company");
        row1.put("nomor_perjanjian", "Y5SFS");
        row1.put("tanggal_perjanjian", "01-01-2024");
        row1.put("nomor_invoice", "MI-243655");
        row1.put("tanggal_invoice", "29-12-2024");
        row1.put("jumlah_piutang", "1265400000.00");
        tableData.add(row1);

        Map<String, String> row2 = new HashMap<>();
        row2.put("no", "2");
        row2.put("customer", "Debtor Company");
        row2.put("nomor_perjanjian", "Y5SFS");
        row2.put("tanggal_perjanjian", "01-01-2024");
        row2.put("nomor_invoice", "MI-243655");
        row2.put("tanggal_invoice", "29-12-2024");
        row2.put("jumlah_piutang", "1265400000.00");
        tableData.add(row2);

        params.put("tableDataSource", new JRBeanCollectionDataSource(tableData));

        // lembar 3
        params.put("AgrmntNo", agrmntCode);

        //lembar 4
        params.put("Facility", facility);
        params.put("Tenor", apiResponse.getTenor());
        params.put("NtfAmt", findata.getNtfAmount());
        params.put("DiskontoAmt", factoringData.getDiskontoAmount());
        params.put("MaxAllocatedRefundAmt", findata.getMaxRefundAmount());
        params.put("TotalRetentionAmt", factoringData.getTotalRetentionAmount());
        financialData.getFeeList().forEach(fee -> {
            if (fee.getFeeTypeName() != null) {
                if (fee.getFeeTypeName().equalsIgnoreCase("BIAYA FACTORING")) {
                    params.put("AppFeeAmtFactoring", fee.getFeeAmount());
                }
                else if (fee.getFeeTypeName().equalsIgnoreCase("BIAYA ADMINISTRASI PENCAIRAN")) {
                    params.put("AppFeeAmtAdministration", fee.getFeeAmount());
                }
            }
        });

        // lembar 5 (tabel)
        params.put("invoice_duedate", formattedDate);

        //lembar 6
        params.put("LobCode", apiResponse.getLobCode());
        params.put("ProdOfferingName", apiResponse.getProdOfferingName());
        params.put("EffectiveRatePrcnt", findata.getEffectiveRate());
        params.put("TotalFeeAmt", findata.getTotalFeeAmount());

        // lembar 7
        params.put("TotalInvcAmt", factoringData.getTotalInvoiceAmount());
        params.put("GracePeriodLc", findata.getGracePeriod());
        params.put("InstAmt", findata.getInstallmentAmount());

        // lembar 9
        params.put("AgmtNo", agrmntCode);
        params.put("Administration+Factoring", "10000");
        params.put("NtfAmt-Total", "400000");

        // lembar 11
        // fap1
        params.put("JenisDebitur", "Badan Usaha");
        params.put("TipePerusahaan", data.getOrDefault("cust_company_type", "-").toString());
        params.put("NPWP", data.getOrDefault("cust_id_no", "-").toString());
        params.put("Alamat", data.getOrDefault("company_address", "-").toString());
        params.put("Email", data.getOrDefault("cust_email", "-").toString());
        params.put("Telepon", data.getOrDefault("phone", "-").toString());

        //lembar12
        // sit
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
        params.put("NamaGMFinance", debtorName);
        params.put("NamaDirektur", sitDto.getDirectorName());
        params.put("NamaGMFinanceCSUL", debtorName);
        params.put("NamaAreaSalesManager", sitDto.getEmployeeName());
        params.put("TotalPembayaran", totalPembayaran);
        } else {
            throw new RuntimeException("Failed to get agreement data: " + (sitData.getMessage() != null ? sitData.getMessage() : ""));
        }

        // lembar 13 tabel
        params.put("description","Invoice By Trakindo");
        params.put("bouwheer","PT. Trakindo Utama");
        params.put("InvoiceDueDate", formattedDate);
        params.put("invoice_amt","1265400000.00");


//        hardcode
//        params.put("BankName", "Bank Central Asia");
//        params.put("BankAccNo", "7005592119");
//        params.put("BankAccName", "PT. Megah Utama");
//        params.put("Cwr","01920193311");
//        params.put("Cwr-Date","20-01-2025");
//        params.put("AgrmntNo", "0120100200");
//        params.put("NtfAmt", "7000000");
//        params.put("MaxAllocatedRefundAmt", "123000000");
//        params.put("EffectiveRatePrcnt", "130000");
//        params.put("TotalFeeAmt", "510000000");
//        params.put("AppFeeAmtFactoring", "10000");
//        params.put("AppFeeAmtAdministration", "120000");
//        params.put("GracePeriodLc", "0");
//        params.put("InstAmt", "10000");
//        params.put("AgmtNo", "01201021011");

        InputStream reportStream = getClass().getResourceAsStream("/Reports/main_report.jrxml");
        if (reportStream == null) {
            throw new FileNotFoundException("main_report.jrxml tidak ditemukan di /Reports");
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        JRDataSource dataSource = new JREmptyDataSource();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}

