package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.Visitor;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.major_account.service.MstBranchService;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

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

    private String jwtToken;

    private void ensureJwtToken() {
        jwtToken = authRemoteService.fetchAuthJwt().getData();
    }

    public PaginationResult<VisitorDto>getVisitorReport(
            PaginationRequest request
    ) {
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

            pageSize = 1000;
            Page<Visitor> pagination = visitorRepository.findAll(PageRequest.of(pageNo, pageSize));

            List<VisitorDto> result = pagination.stream()
                    .map((e) -> VisitorDto.builder()
                            .visitorId(e.getVisitorId())
                            .vendorCode(e.getVendorCode())
                            .debtorName(e.getDebtorName())
                            .debtorStatus(e.getDebtorStatus())
                            .bouwheerName(e.getBouwheerName())
                            .visitDate(e.getVisitDate())
                            .build())
                    .toList();

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

            // Use the Pageable object to manage pagination
            Page<ProyeksiReportDto> pagination = customerRepository.findActiveCustomersWithInvoiceDetails(PageRequest.of(pageNo, pageSize));

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

            Page<SummaryByBranchDto> pagination = customerRepository.findSummaryByCustCode(PageRequest.of(pageNo, pageSize));

            List<SummaryByBranchDto> result = pagination.stream()
                    .map(e -> new SummaryByBranchDto(
                            e.getDebtorName(),
                            e.getNpwp(),
                            e.getBouwheerName(),
                            e.getTotalPencairan(),
                            e.getJumlahPlafonAmount(),
                            e.getTotalUtilizationAmount(),
                            e.getTotalNilaiRetensi()
                    ))
                    .collect(Collectors.toList());

            return PaginationResult.<SummaryByBranchDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(pagination.getTotalElements())
                    .totalPage(pagination.getTotalPages())
                    .list(result)
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    public PaginationResult<SummaryByAODto> getAllReportBranchByAO(PaginationRequest request) {
        try {
            // Setup pagination
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

            ensureJwtToken();

            Page<Object[]> dataPage = financingHdrRepository.findFinancingDataByFinancingHdrCode(PageRequest.of(pageNo, pageSize));

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
                        employeeName,
                        branchName,
                        customerName,
                        bouwheerName,
                        totalDisbursement,
                        plafondAmount,
                        totalUtilizationAmount,
                        retentionAmount
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
}

