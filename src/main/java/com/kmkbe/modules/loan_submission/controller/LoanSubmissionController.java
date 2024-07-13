package com.kmkbe.modules.loan_submission.controller;


import com.kmkbe.core.model.CommonResult;
import com.kmkbe.core.model.PaginationResult;
import com.kmkbe.modules.loan_submission.dto.EstimatedDisburseDto;
import com.kmkbe.modules.loan_submission.dto.InvoiceDto;
import com.kmkbe.modules.loan_submission.dto.MstFileTypeDto;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.InvoiceListRequest;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-submissions")
@Tag(
        name = "Loan Submission",
        description = "Berisi endpoints data pengajuan kredit customer"
)
@RequiredArgsConstructor
public class LoanSubmissionController {
    private final InvoiceService invoiceService;

    @GetMapping("/invoices")
    public CommonResult<PaginationResult<InvoiceDto>> getActiveInvoices(
            Authentication authentication,
            InvoiceListRequest params
    ) throws Exception {
        return new CommonResult<PaginationResult<InvoiceDto>>()
                .success(invoiceService.fetchActiveInvoice(authentication, params));
    }

    @GetMapping("/simulation")
    public CommonResult<EstimatedDisburseDto> calculateDisburse(
            CalculateSimulationRequest request
    ) {
        return new CommonResult<EstimatedDisburseDto>().success(invoiceService.calculateDisburse(request));
    }

    @PostMapping("/simulation/create")
    public CommonResult<Object> createSimulation(
            Authentication authentication,
            CreateSimulationRequest request
    ) {
        return new CommonResult<>().success(null);
    }

    /*@GetMapping("/customer")
    public CommonResult<Object> getCustomer(
            Authentication authentication
    ) {
        return new CommonResult<>().success(null);
    }

    @PutMapping("/customer/{custCode}")
    public CommonResult<Object> updateCustomer(
            Authentication authentication,
            @PathVariable String custCode
    ) {
        return new CommonResult<>().success(null);
    }*/

    @GetMapping("/documents")
    public CommonResult<List<MstFileTypeDto>> getDocumentRequirement(
            Authentication authentication
    ) {
        return new CommonResult<List<MstFileTypeDto>>().success(null);
    }

    @PostMapping("/documents/upload")
    public CommonResult<Object> uploadDocument(
            Authentication authentication
    ) {
        return new CommonResult<>().success(null);
    }
}
