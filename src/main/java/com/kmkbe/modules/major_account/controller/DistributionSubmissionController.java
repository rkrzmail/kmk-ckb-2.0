package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.major_account.service.DistributionSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;


@Validated
@RestController
@RequestMapping("/api/v1/distribution-submission")
@Tag(
        name = "Distribusi Pengajuan Kredit API",
        description = "Berisi endpoints data Distribusi Pengajuan Kredit"
)
@RequiredArgsConstructor
public class DistributionSubmissionController {
    private final InvoiceService invoiceService;
    private final FinancingHdrService financingHdrService;
    private final DistributionSubmissionService distributionSubmissionService;

    @GetMapping("/list")
    public CommonResult<PaginationResult<DistributionSubmissionDto>> getDistributionList(
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<DistributionSubmissionDto>>().success(
                distributionSubmissionService.submissionDistribution(request)
        );
    }

    @GetMapping("/detail/{financingHdrCode}/invoices")
    public CommonResult<PaginationResult<PostedInvoiceDto>> getDetailInvoiceDistributionList(
            @PathVariable String financingHdrCode,
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<PostedInvoiceDto>>().success(
                invoiceService.invoiceSubmissionByFinancingHdr(
                        financingHdrService.findByCode(financingHdrCode),
                        request
                )
        );
    }

    @PutMapping("/assign")
    public CommonResult<Object> createAssign(
            Authentication authentication,
            @Valid @RequestBody AssignInvoiceToBranchRequest request
    ) throws SignatureException {
        distributionSubmissionService.assignSubmission(authentication, request);
        return new CommonResult<>().success(null, "Assigned Successfully");
    }
}
