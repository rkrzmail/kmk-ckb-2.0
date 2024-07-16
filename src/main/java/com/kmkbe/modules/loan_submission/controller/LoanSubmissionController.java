package com.kmkbe.modules.loan_submission.controller;


import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.external.dto.PostedInvoiceDto;
import com.kmkbe.modules.loan_submission.dto.DisbursePercentageDto;
import com.kmkbe.modules.loan_submission.dto.DocumentTemplateFinancingDto;
import com.kmkbe.modules.loan_submission.dto.EstimatedDisburseDto;
import com.kmkbe.modules.loan_submission.dto.MstFileTypeDto;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public CommonResult<Object> createSimulation(
            Authentication authentication,
            @RequestBody CreateSimulationRequest request
    ) throws Exception {
        loanSubmissionService.createSimulation(authentication, request);
        return new CommonResult<>().success(null, "Simulation Created Successfully");
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
            Authentication authentication
    ) throws Exception {
        return new CommonResult<List<MstFileTypeDto>>().success(
                documentService.fetchAllLoanDocumentRequirement(authentication)
        );
    }

    @PostMapping(path = "/documents/requirement/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult<Object> uploadDocument(
            Authentication authentication,
            @RequestPart("file") MultipartFile file,
            @RequestParam("fileTypeCode") String fileTypeCode
    ) throws Exception {
        documentService.uploadLoanDocument(
                authentication,
                file,
                fileTypeCode
        );
        return new CommonResult<>().success(null, "Uploaded Successfully");
    }
}
