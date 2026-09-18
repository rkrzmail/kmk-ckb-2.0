package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.branch_admin.service.ReportService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

  private final ReportService reportService;
  private final CurrentUserService currentUserService;

  public ReportController(ReportService reportService,
                          CurrentUserService currentUserService) {
    this.reportService = reportService;
    this.currentUserService = currentUserService;
  }

  @GetMapping("/visitor")
  public CommonResult<PaginationResult<VisitorDto>> getlistVisitor(
    PaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<VisitorDto>>().success(
      reportService.getVisitorReport(request)
    );
  }

  @GetMapping("/proyeksi")
  public CommonResult<PaginationResult<ProyeksiReportDto>> getlistProyeksi(
    BasePaginationRequest request,
    @RequestParam(value = "startDate", required = false) String startDate,
    @RequestParam(value = "endDate", required = false) String endDate
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<ProyeksiReportDto>>().success(
      reportService.getProyeksiReport(
        request,
        parseReportDate(startDate, "startDate"),
        parseReportDate(endDate, "endDate")
      )
    );
  }

  // Accepts both "dd/MM/yyyy" (e.g. 01/01/2025) and "ddMMyyyy" (e.g. 01012025).
  private static Date parseReportDate(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    String raw = value.trim();
    for (String pattern : new String[]{"dd/MM/yyyy", "ddMMyyyy"}) {
      SimpleDateFormat sdf = new SimpleDateFormat(pattern);
      sdf.setLenient(false);
      try {
        return sdf.parse(raw);
      } catch (ParseException ignored) {
        // try next pattern
      }
    }
    throw new BusinessException(
      HttpStatus.BAD_REQUEST,
      400,
      "Format " + fieldName + " tidak valid: '" + value + "'. Gunakan dd/MM/yyyy atau ddMMyyyy."
    );
  }

  @GetMapping("/summary/branch")
  public CommonResult<PaginationResult<SummaryByBranchDto>> getlistSummaryByBranch(
    PaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<SummaryByBranchDto>>().success(
      reportService.getSummaryByBranch(request)
    );
  }

  @GetMapping("/summary/ao")
  public CommonResult<PaginationResult<SummaryByAODto>> getAllReportBranchByAO(
    PaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<SummaryByAODto>>().success(
      reportService.getAllReportBranchByAO(request)
    );
  }

  @GetMapping("/summary/detail")
  public CommonResult<PaginationResult<SummaryDetailDto>> getAllReportSummaryDetail(
    PaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<SummaryDetailDto>>().success(
      reportService.getSummaryDetail(request)
    );
  }

  @GetMapping("/duedate")
  public CommonResult<PaginationResult<ReportDueDateDto>> getAllContractDueDate(
    PaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<ReportDueDateDto>>().success(
      reportService.getDueDateDetail(request)
    );
  }

  @GetMapping("/preview/{financingHdrCode}/{agreementCode}/{branchManager}/{areaSalesManager}")
  public ResponseEntity<byte[]> previewReport(
    @PathVariable String financingHdrCode,
    @PathVariable String agreementCode,
    @PathVariable String branchManager,
    @PathVariable String areaSalesManager
  ) {

    try {
      byte[] pdfBytes = reportService.generateReport(financingHdrCode, agreementCode, branchManager, areaSalesManager);
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

  @GetMapping("/download-pdf/{financingHdrCode}/{agreementCode}/{branchManager}/{areaSalesManager}")
  public void downloadPdf(HttpServletResponse response,
                          @PathVariable String financingHdrCode,
                          @PathVariable String agreementCode,
                          @PathVariable String branchManager,
                          @PathVariable String areaSalesManager) {
    try {
      byte[] pdfBytes = reportService.generateReport(financingHdrCode, agreementCode, branchManager, areaSalesManager);
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

  @PostMapping("/send-doc/{financingHdrCode}/{agreementCode}/{branchManager}/{areaSalesManager}")
  public ResponseEntity<SigningResponse> sendForSigning(
    @PathVariable String financingHdrCode,
    @PathVariable String agreementCode,
    @PathVariable String branchManager,
    @PathVariable String areaSalesManager
  ) throws SignatureException {

    SigningResponse response = reportService.sendDocumentForSigning(
      financingHdrCode,
      agreementCode,
      branchManager,
      areaSalesManager,
      currentUserService.internalUsername()
    );

    return ResponseEntity.ok(response);
  }

}
