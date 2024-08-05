package com.kmkbe.modules.loan_submission.controller;


import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.remote.dto.PostedInvoiceDto;
import com.kmkbe.modules.loan_submission.dto.*;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateLoanApplicationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.RemoteBouwheerRequest;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping
    public CommonResult<Object> remoteHandler(@Valid RemoteBouwheerRequest request) {
        return new CommonResult<>()
                .success(null, "Bouwheer has validated");
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
            Authentication authentication
    ) throws Exception {
        return new CommonResult<List<PostedInvoiceDto>>()
                .success(loanSubmissionService.fetchActiveInvoice(authentication));
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

    @GetMapping("/documents/template-financing")
    public CommonResult<List<DocumentTemplateFinancingDto>> getDocumentTemplateFinancing(
            Authentication authentication
    ) throws Exception {
        return new CommonResult<List<DocumentTemplateFinancingDto>>().success(
                documentService.fetchDocumentTemplateFinancing()
        );
    }

    @GetMapping("/documents/requirement")
    public CommonResult<List<MstFileTypeDto>> getDocumentRequirement(
            HttpServletRequest httpServletRequest,
            Authentication authentication
    ) throws Exception {
        return new CommonResult<List<MstFileTypeDto>>().success(
                documentService.fetchAllLoanDocumentRequirement(httpServletRequest, authentication)
        );
    }

    @PostMapping(path = "/documents/requirement/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    @DeleteMapping(path = "/documents/requirement/{id}")
    public CommonResult<Object> deleteDocument(
            Authentication authentication,
            @PathVariable("id") Long id
    ) throws Exception {
        documentService.delete(id);
        return new CommonResult<>().success(null, "Delete Successfully");
    }
}
