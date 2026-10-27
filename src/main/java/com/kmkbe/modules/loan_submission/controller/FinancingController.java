package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.domain.dto.DisburseInvoiceDto;
import com.kmkbe.core.domain.dto.PaidInvoiceDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaginationRequest;
import com.kmkbe.modules.loan_submission.service.FinancingDtlService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;

@Validated
@RestController
@RequestMapping("/api/v1/financing")
@Tag(
  name = "Financing Endpoint",
  description = "Berisi endpoints data Financing"
)
@RequiredArgsConstructor
public class FinancingController {
  private final FinancingHdrService financingHdrService;
  private final FinancingService financingService;
  private final FinancingDtlService financingDtlService;
  private final CurrentUserService currentUserService;

  @PostMapping("/invoice-paid")
  public CommonResult<Object> invoicePaid(
    @Valid @RequestBody FinancingInvoicePaidRequest request
  ) {
    try {
      FinancingHdr financingHdr = financingHdrService.paidFinancing(
        request
      );
        /**
         * Update Paid
         */
      financingDtlService.updatePaid(request, financingHdr);

      /**
       * Payment Recceive
       */
      financingDtlService.paymentReceive(request, financingHdr);

      return new CommonResult<>().success(null);
    } catch (Exception e) {
      return new CommonResult<>().fail(500, e.getMessage());
      //throw e;
    }
  }

  @GetMapping("/invoices/paid")
  public CommonResult<PaginationResult<PaidInvoiceDto>> getInvoicePaid2(
    FinancingInvoicePaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<PaidInvoiceDto>>().success(
      financingHdrService.listPaidUnpaidInvoice(request)
    );
  }

  @GetMapping("/invoices/paid/x")
  public CommonResult<PaginationResult<PaidInvoiceDto>> getInvoicePaid(
    PaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<PaidInvoiceDto>>().success(
      financingHdrService.paidInvoiceNew(request)
    );
  }

  @GetMapping("/invoices/disbursement")
  public CommonResult<PaginationResult<DisburseInvoiceDto>> getDisbursement(
    FinancingInvoicePaginationRequest request
  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<DisburseInvoiceDto>>().success(
      financingHdrService.listdisburseAggrement(request)
    );
  }

  @GetMapping("/approvals/status")
  public CommonResult<Object> updateApproval(
    @RequestParam("apiKey") String apiKey
  ) {
    if (!apiKey.equalsIgnoreCase("123")) {
      throw new IllegalApiKeyException();
    }
    try {
      financingService.recallApprovalStatus();
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }

    return new CommonResult<>().success(null);
  }

  @GetMapping("/sch/cwr/status")
  public CommonResult<Object> schCWR(
    @RequestParam("apiKey") String apiKey
  ) {
    if (!apiKey.equalsIgnoreCase("123")) {
      throw new IllegalApiKeyException();
    }
    try {
      financingService.recallCWRStatus();
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }

    return new CommonResult<>().success(null);
  }
}
