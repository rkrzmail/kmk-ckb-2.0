package com.kmkbe.modules.loan_submission.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.request.*;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import com.kmkbe.modules.loan_submission.service.SessionLoanSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
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

    @GetMapping
    public CommonResult<Object> getExternalIntegration(
            Authentication authentication,
            String token
    ) throws JsonProcessingException, SignatureException {
        if (token != null && token.isEmpty()) {
            return new CommonResult<>().success(null, "Successfully");
        }

        return new CommonResult<>()
                .success(
                        loanSubmissionService.externalIntegrationSimulation(authentication, token),
                        "Bouwheer has validated"
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
            Authentication authentication,
            @Valid @RequestBody CreateLoanApplicationRequest request
    ) throws Exception {
        loanSubmissionService.createLoanSubmission(
                authentication,
                request
        );

        return new CommonResult<>()
                .success(null, "Loan Application create successfully");
    }

    @GetMapping("/invoices")
    public CommonResult<List<PostedInvoiceDto>> getActiveInvoices(
            Authentication authentication,
            String token
    ) throws Exception {
        return new CommonResult<List<PostedInvoiceDto>>().success(
                loanSubmissionService.fetchActiveInvoice(authentication, token)
        );
    }

    @GetMapping("/simulations/percentage")
    public CommonResult<List<DisbursePercentageDto>> getPercentage(
            Authentication authentication
    ) {
        return new CommonResult<List<DisbursePercentageDto>>().success(
                loanSubmissionService.fetchDisbursePercentage()
        );
    }

    @GetMapping("/simulations/calculate")
    public CommonResult<EstimatedDisburseDto> getCalculateDisburse(
            CalculateSimulationRequest request
    ) {
        return new CommonResult<EstimatedDisburseDto>().success(
                loanSubmissionService.calculateDisburse(request)
        );
    }

    @PostMapping("/simulations/create")
    public CommonResult<CreatedSimulationDto> createSimulation(
            Authentication authentication,
            @Valid @RequestBody CreateSimulationRequest request
    ) throws Exception {
        return new CommonResult<CreatedSimulationDto>().success(
                loanSubmissionService.createSimulation(authentication, request),
                "Simulation Created Successfully"
        );
    }

    @GetMapping("/simulations/history")
    public CommonResult<SimulationHistDto> getSimulationHistory(
            Authentication authentication
    ) throws Exception {
        //loanSubmissionService.lastSimulationHistory(authentication)
        return new CommonResult<SimulationHistDto>().success(
                null
        );
    }

    @GetMapping("/session/history")
    public CommonResult<LoanSubmissionSessionDto> getSimulationSession(
            Authentication authentication
    ) throws SignatureException {
        return new CommonResult<LoanSubmissionSessionDto>().success(
                sessionLoanSubmissionService.findLastByCust(
                        CustomerUtils.authenticateCustomer(authentication)
                ).orElse(null)
        );
    }

    @PostMapping("/session/history")
    public CommonResult<LoanSubmissionSessionDto> createSimulationSession(
            Authentication authentication,
            @Valid @RequestBody CreateSessionLoanSubmissionRequest createSessionLoanSubmissionRequest
    ) throws SignatureException, JsonProcessingException {
        return new CommonResult<LoanSubmissionSessionDto>().success(
                sessionLoanSubmissionService.create(
                        authentication,
                        createSessionLoanSubmissionRequest.getLastStep(),
                        createSessionLoanSubmissionRequest.getSession()
                )
        );
    }

    @GetMapping("/documents/template-financing")
    public CommonResult<List<DocumentTemplateFinancingDto>> getDocumentTemplateFinancing(
            Authentication authentication
    ) throws Exception {
        return new CommonResult<List<DocumentTemplateFinancingDto>>().success(
                documentService.fetchDocumentTemplateFinancing()
        );
    }

    @PostMapping(
            path = "/documents/requirement/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CommonResult<LegalFileDto> uploadDocument(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            @Valid @RequestPart MultipartFile file,
            @Valid @RequestParam("fileTypeCode") String fileTypeCode
    ) throws Exception {
        return new CommonResult<LegalFileDto>().success(
                documentService.uploadLoanDocument(
                        httpServletRequest,
                        authentication,
                        file,
                        fileTypeCode
                ),
                "File Upload Successfully"
        );
    }

    @GetMapping("/documents/requirement")
    public CommonResult<List<MstFileTypeDto>> getDocumentRequirement(
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) throws Exception {
        return new CommonResult<List<MstFileTypeDto>>().success(
                documentService.fetchAllLoanDocumentRequirement(
                        httpServletRequest,
                        authentication
                )
        );
    }

    @DeleteMapping(path = "/documents/requirement/{id}")
    public CommonResult<Object> deleteDocument(
            Authentication authentication,
            @PathVariable("id") Long id
    ) throws Exception {
        documentService.delete(id);
        return new CommonResult<>().success(null, "Delete Successfully");
    }
}
