package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.ProyeksiReportDto;
import com.kmkbe.core.domain.dto.SummaryByAODto;
import com.kmkbe.core.domain.dto.SummaryByBranchDto;
import com.kmkbe.core.domain.dto.VisitorDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.ReportService;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SignatureException;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/visitor")
    public CommonResult<PaginationResult<VisitorDto>> getlistVisitor(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<VisitorDto>>().success(
                reportService.getVisitorReport(request)
        );
    }

    @GetMapping("/proyeksi")
    public CommonResult<PaginationResult<ProyeksiReportDto>> getlistProyeksi(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<ProyeksiReportDto>>().success(
                reportService.getProyeksiReport(request)
        );
    }

    @GetMapping("/summary/branch")
    public CommonResult<PaginationResult<SummaryByBranchDto>> getlistSummaryByBranch(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<SummaryByBranchDto>>().success(
                reportService.getSummaryByBranch(request)
        );
    }

    @GetMapping("/summary/ao")
    public CommonResult<PaginationResult<SummaryByAODto>> getAllReportBranchByAO(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<SummaryByAODto>>().success(
                reportService.getAllReportBranchByAO(request)
        );
    }
}