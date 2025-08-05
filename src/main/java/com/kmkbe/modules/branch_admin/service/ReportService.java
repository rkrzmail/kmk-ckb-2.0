package com.kmkbe.modules.branch_admin.service;


import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.Visitor;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.major_account.service.MstBranchService;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
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
    private CustomerRepository customerRepository;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private MstBranchService mstBranchService;

    @Autowired
    private AuthRemoteService authRemoteService;

    @Autowired
    private EmailAo emailAo;

    private JasperReport cachedReport;

    private String jwtToken;

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

    public byte[] generateReport() throws JRException, IOException {
        InputStream reportStream = getClass().getResourceAsStream("/Reports/main_report.jrxml");
        if (reportStream == null) {
            throw new FileNotFoundException("main_report.jrxml tidak ditemukan di /Reports");
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        Map<String, Object> params = new HashMap<>();
        params.put("SUBREPORT_DIR", getClass().getResource("/Reports/").toString());

        params.put("AppNo", "APP-2023-00567");
        params.put("TglDokumen", "15/07/2023");
        params.put("NoDokumen", "DOC/2023/00789");
        params.put("TempatTanggal", "Jakarta, 15 Juli 2023");
        params.put("DebtorName", "PT. Maju Jaya Abadi");
        params.put("JenisDebitur", "Perusahaan");
        params.put("TipePerusahaan", "PT");
        params.put("NPWP", "01.234.567.8-912.345");
        params.put("Alamat", "Jl. Sudirman No. 123, Jakarta Selatan");
        params.put("Email", "finance@majujaya.com");
        params.put("Telepon", "021-12345678");
        params.put("BankName", "Bank Central Asia");
        params.put("BankAccNo", "1234567890");
        params.put("BankAccName", "PT. Maju Jaya Abadi");
        params.put("NamaKaryawan", "Budi Santoso");
        params.put("Jabatan", "Account Manager");
        params.put("AgrmntNo", "AGR/2023/00567");
        params.put("AgmtNo", "AGR/2023/00567"); // Duplikat dengan nama berbeda
        params.put("Facility", "Factoring dengan Recourse");
        params.put("Tenor", "90 Hari");
        params.put("PeriodeBerlaku", "15 Juli 2023 - 15 Oktober 2023");
        params.put("NtfAmt", "500000000");
        params.put("DiskontoAmt", "25000000");
        params.put("MaxAllocatedRefundAmt", "475000000");
        params.put("TotalRetentionAmt", "15000000");
        params.put("TotalInvcAmt", "500000000");
        params.put("NtfAmt-Total", "500000000");
        params.put("AppFeeAmtFactoring", "5000000");
        params.put("AppFeeAmtAdministration", "3000000");
        params.put("TotalFeeAmt", "8000000");
        params.put("AppFeeAmt", "8000000");
        params.put("Administration+Factoring", "8000000");
        params.put("TotalPembayaran", "492000000");
        params.put("InvoiceDueDate", "15/10/2023");
        params.put("LobCode", "FCT");
        params.put("ProdOfferingName", "Factoring Reguler");
        params.put("EffectiveRatePrcnt", "12.5");
        params.put("InstAmt", "164000000");
        params.put("GracePeriodLc", "5 Hari");
        params.put("NamaGMFinance", "Dewi Kartini");
        params.put("NamaDirektur", "Hendrawan Susilo");
        params.put("NamaGMFinanceCSUL", "Ahmad Fauzi");
        params.put("NamaAreaSalesManager", "Rina Permata");
        params.put("no","1");
        params.put("customer","Debtor Name");
        params.put("nomor_perjanjian","FINE0016496");
        params.put("tanggal_perjanjian","25/10/2024");
        params.put("nomor_invoice","0082498008270");
        params.put("tanggal_invoice","02/09/2024");
        params.put("jumlah_piutang","1,084,154.0");
        params.put("description","Invoice By Trakindo");
        params.put("bouwheer","PT. Trakindo Utama");
        params.put("invoice_duedate","25/10/2024");
        params.put("invoice_amt","12,986,340.0");

        // <parameter name="no" class="java.lang.String"/>
        //	<parameter name="customer" class="java.lang.String"/>
        //	<parameter name="nomor_perjanjian" class="java.lang.String"/>
        //	<parameter name="tanggal_perjanjian" class="java.lang.String"/>
        //	<parameter name="nomor_invoice" class="java.lang.String"/>
        //	<parameter name="tanggal_invoice" class="java.lang.String"/>
        //	<parameter name="jumlah_piutang" class="java.lang.String"/>
        //	<parameter name="description" class="java.lang.String"/>
        //	<parameter name="bouwheer" class="java.lang.String"/>
        //	<parameter name="invoice_duedate" class="java.lang.String"/>
        //	<parameter name="invoice_amt" class="java.lang.String"/>

        JRDataSource dataSource = new JREmptyDataSource();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}

