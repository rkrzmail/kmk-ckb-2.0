package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.DetailDistributionSubmissionDto;
import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.InvoiceListRequest;
import com.kmkbe.modules.major_account.request.DistributionListRequest;
import com.kmkbe.modules.major_account.service.DistributionSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/distribution-submission")
@Tag(
        name = "Distribusi Pengajuan Kredit API",
        description = "Berisi endpoints data Distribusi Pengajuan Kredit"
)
@RequiredArgsConstructor
public class DistributionSubmissionController {
    private final DistributionSubmissionService distributionSubmissionService;

    @GetMapping("/submission/list")
    public CommonResult<PaginationResult<DistributionSubmissionDto>> getDistributionList(
            DistributionListRequest request
    ) {
        return new CommonResult<PaginationResult<DistributionSubmissionDto>>().success(
                distributionSubmissionService.submissionDistribution(request)
        );
    }

    @GetMapping("/submission/detail/{financingHdrCode}")
    public CommonResult<DetailDistributionSubmissionDto> getDistributionDetail(
            @PathVariable String financingHdrCode
    ) {
        return new CommonResult<DetailDistributionSubmissionDto>().success(
                distributionSubmissionService.detailSubmissionDistribution(financingHdrCode)
        );
    }

    @GetMapping("/submission/detail/{financingHdrCode}/invoices")
    public CommonResult<PaginationResult<PostedInvoiceDto>> getDetailInvoiceDistributionList(
            @PathVariable String financingHdrCode,
            InvoiceListRequest request
    ) {
        return new CommonResult<PaginationResult<PostedInvoiceDto>>().success(
                distributionSubmissionService.invoiceSubmission(
                        financingHdrCode,
                        request
                )
        );
    }

    @PostMapping("/submission/assign")
    public CommonResult<Object> createAssign(

    ) {
        distributionSubmissionService.assignSubmission();
        return new CommonResult<>().success(
                null
        );
    }
}
