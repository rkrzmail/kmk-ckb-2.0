package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.major_account.service.DistributionSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
  private final CurrentUserService currentUserService;

  @GetMapping("/list")
  public CommonResult<PaginationResult<DistributionSubmissionDto>> getDistributionList(
    PaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<DistributionSubmissionDto>>().success(
      distributionSubmissionService.submissionDistribution(request)
    );
  }

  @GetMapping("/detail/{financingHdrCode}/invoices")
  public CommonResult<PaginationResult<PostedInvoiceDto>> getDetailInvoiceDistributionList(
    @PathVariable String financingHdrCode,
    PaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<PostedInvoiceDto>>().success(
      invoiceService.invoiceSubmissionByFinancingHdr(
        financingHdrService.findByCode(financingHdrCode),
        request
      )
    );
  }

  @PutMapping("/assign")
  public BaseResponse createAssign(@Valid @RequestBody AssignInvoiceToBranchRequest request) throws SignatureException {
    return distributionSubmissionService.assignSubmission(request);
  }
}
