package com.kmkbe.modules.loan_submission.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.loan_submission.request.*;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import com.kmkbe.modules.loan_submission.service.SessionLoanSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
import java.text.ParseException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/loan-submissions")
@Tag(
  name = "Loan Submission",
  description = "Berisi endpoints data pengajuan kredit customer"
)
@RequiredArgsConstructor
public class LoanSubmissionController {
  private final LoanSubmissionService loanSubmissionService;
  private final DocumentService documentService;
  private final SessionLoanSubmissionService sessionLoanSubmissionService;
  private final CurrentUserService currentUserService;

  @GetMapping
  public CommonResult<Object> getExternalIntegration(
    String token
  ) throws JsonProcessingException, SignatureException {
    if (token != null && token.isEmpty()) {
      return new CommonResult<>().success(null);
    }

    return new CommonResult<>()
      .success(
        loanSubmissionService.externalIntegrationSimulation(currentUserService.customerOrNull(), token)
      );
  }

  @GetMapping("/importance-notes")
  public CommonResult<ImportantNotesDto> getImportantNotes() {
    return new CommonResult<ImportantNotesDto>()
      .success(loanSubmissionService.importanceNotes());
  }

  @PostMapping("/importance-notes")
  public CommonResult<Object> saveImportantNotes(
    @Valid @RequestBody SaveImportantNotesRequest request
  ) throws SignatureException, JsonProcessingException {
    return new CommonResult<>().success(loanSubmissionService.saveImportantNotes(request));
  }

  @PostMapping("/create")
  public CommonResult<Object> submitLoanSubmission(
    @Valid @RequestBody CreateLoanApplicationRequest request
  ) throws Exception {
    loanSubmissionService.createLoanSubmission(
      currentUserService.customer(),
      request
    );

    return new CommonResult<>()
      .success(null);
  }

  @GetMapping("/invoices")
  public CommonResult<List<PostedInvoiceDto>> getActiveInvoices(
    String token
  ) throws Exception {
    return new CommonResult<List<PostedInvoiceDto>>().success(
      loanSubmissionService.fetchActiveInvoice(currentUserService.customer(), token)
    );
  }

  @GetMapping("/simulations/percentage")
  public CommonResult<List<DisbursePercentageDto>> getPercentage() {
    return new CommonResult<List<DisbursePercentageDto>>().success(
      loanSubmissionService.fetchDisbursePercentage()
    );
  }

  @GetMapping("/simulations/calculate")
  public CommonResult<EstimatedDisburseDto> getCalculateDisburse(
    CalculateSimulationRequest request
  ) throws SignatureException, JsonProcessingException, ParseException {
    return new CommonResult<EstimatedDisburseDto>().success(
      loanSubmissionService.calculateDisburse(currentUserService.customer(), request)
    );
  }

  @GetMapping("/simulations/recalculate")
  public CommonResult<EstimatedDisburseDto> getReCalculateDisburse(
    HttpServletRequest request
  ) throws Exception {
    return new CommonResult<EstimatedDisburseDto>().success(
      loanSubmissionService.recalculateDisburse(request)
    );
  }


  @GetMapping("/simulations/viewcalculate/{financeCode}/{histCode}")
  public CommonResult<FinancingHdrDto> getViewCalculateDisburse(
    @PathVariable("financeCode") String financeCode,
    @PathVariable("histCode") String histCode
  ) throws SignatureException, JsonProcessingException, ParseException {
    return new CommonResult<FinancingHdrDto>().success(
      loanSubmissionService.viewCulateDisburse(financeCode, histCode)
    );
  }

  @GetMapping("/simulations/update")
  public CommonResult<CreatedSimulationDto> updateSimulation(
    HttpServletRequest request
  ) throws Exception {
    CreatedSimulationDto result = loanSubmissionService.updateSimulation(request);
    if (result != null) {
      int i = 0;
    }
    return new CommonResult<CreatedSimulationDto>().success(
      result
    );
  }


  @PostMapping("/simulations/create")
  public CommonResult<CreatedSimulationDto> createSimulation(
    @Valid @RequestBody CreateSimulationRequest request
  ) throws Exception {
    var result = loanSubmissionService.createSimulation(currentUserService.customer(), request);
    return new CommonResult<CreatedSimulationDto>().success(
      result
    );
  }

  @GetMapping("/simulations/history")
  public CommonResult<SimulationHistDto> getSimulationHistory() throws Exception {
    //loanSubmissionService.lastSimulationHistory(authentication)
    return new CommonResult<SimulationHistDto>().success(
      null
    );
  }

  @GetMapping("/session/history")
  public CommonResult<LoanSubmissionSessionDto> getSimulationSession() throws SignatureException {
    return new CommonResult<LoanSubmissionSessionDto>().success(
      sessionLoanSubmissionService.findLastByCust(
        currentUserService.customer()
      ).orElse(null)
    );
  }

  @PostMapping("/session/history")
  public CommonResult<LoanSubmissionSessionDto> createSimulationSession(
    @Valid @RequestBody CreateSessionLoanSubmissionRequest createSessionLoanSubmissionRequest
  ) throws SignatureException, JsonProcessingException {
    return new CommonResult<LoanSubmissionSessionDto>().success(
      sessionLoanSubmissionService.create(
        currentUserService.customer(),
        createSessionLoanSubmissionRequest.getLastStep(),
        createSessionLoanSubmissionRequest.getSession()
      )
    );
  }

  @GetMapping("/documents/template-financing")
  public CommonResult<List<DocumentTemplateFinancingDto>> getDocumentTemplateFinancing() throws Exception {
    return new CommonResult<List<DocumentTemplateFinancingDto>>().success(
      documentService.fetchDocumentTemplateFinancing(currentUserService.customer())
    );
  }

  @PostMapping(
    path = "/documents/requirement/upload",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public CommonResult<LegalFileDto> uploadDocument(
    HttpServletRequest httpServletRequest,
    @Valid @RequestPart MultipartFile file,
    @Valid @RequestParam("fileTypeCode") String fileTypeCode
  ) throws Exception {
    return new CommonResult<LegalFileDto>().success(
      documentService.uploadLoanDocument(
        httpServletRequest,
        currentUserService.customer(),
        file,
        fileTypeCode
      )
    );
  }

  @GetMapping("/documents/requirement")
  public CommonResult<PaginationResult<MstFileTypeDto>> getDocumentRequirement(
    HttpServletRequest httpServletRequest,
    PaginationRequest request,
    Boolean isFirst
  ) throws Exception {
    return new CommonResult<PaginationResult<MstFileTypeDto>>().success(
      documentService.fetchAllLoanDocumentRequirement(
        httpServletRequest,
        currentUserService.customer(),
        request,
        isFirst
      )
    );
  }


  @GetMapping("/documents/debitur")
  public CommonResult<PaginationResult<MstFileTypeDto>> getDocumentDebitur(
    HttpServletRequest httpServletRequest,
    PaginationRequest request,
    Boolean isFirst,
    @RequestParam("custCode") String custCode,
    @RequestParam("financingHdrCode") String financingHdrCode
  ) throws Exception {
    return new CommonResult<PaginationResult<MstFileTypeDto>>().success(
      documentService.fetchAllLoanDocumentDebitur(
        httpServletRequest,
        request,
        isFirst,
        custCode,
        financingHdrCode
      )
    );
  }

  @DeleteMapping(path = "/documents/requirement/{id}")
  public CommonResult<Object> deleteDocument(
    @PathVariable("id") Long id
  ) throws Exception {
    currentUserService.customer();
    documentService.delete(id);
    return new CommonResult<>().success(null);
  }
}
