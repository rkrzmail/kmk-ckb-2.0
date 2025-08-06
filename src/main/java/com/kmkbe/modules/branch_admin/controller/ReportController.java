package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.ReportService;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    private final Map<String, byte[]> reportCache = new ConcurrentHashMap<>();

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

    @GetMapping("/summary/detail")
    public CommonResult<PaginationResult<SummaryDetailDto>> getAllReportSummaryDetail(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<SummaryDetailDto>>().success(
                reportService.getSummaryDetail(request)
        );
    }

    @GetMapping("/duedate")
    public CommonResult<PaginationResult<ReportDueDateDto>> getAllContractDueDate(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<ReportDueDateDto>>().success(
                reportService.getDueDateDetail(request)
        );
    }

    @GetMapping("/preview/{financingHdrCode}")
    public ResponseEntity<byte[]> previewReport(
            @PathVariable String financingHdrCode
    ) {

        try {
            byte[] pdfBytes = reportService.generateReport(financingHdrCode);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=preview.pdf")
                    .body(pdfBytes);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((e.getMessage()).getBytes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/download-pdf")
    public void downloadPdf(HttpServletResponse response, @PathVariable String financingHdrCode) {
        try {
            byte[] pdfBytes = reportService.generateReport(financingHdrCode);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"");
            response.setContentLength(pdfBytes.length);

            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(pdfBytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}