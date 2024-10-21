package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.domain.dto.DisburseInvoiceDto;
import com.kmkbe.core.domain.dto.PaidInvoiceDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.service.FinancingDtlService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/invoice-paid")
    public CommonResult<Object> invoicePaid(
            Authentication authentication,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody FinancingInvoicePaidRequest request
    ) throws Exception {
        try {
            String providedApiKey = httpServletRequest.getHeader("ApiKey");
            if (!providedApiKey.equals("$2b$10$YoLl0SFxCMlIWXfQ9RhixeU8Vxvj9Fi7RmF5j7zA9dhYwdplSGyWC")) {
                throw new IllegalApiKeyException();
            }

            FinancingHdr financingHdr = financingHdrService.paidFinancing(
                    authentication,
                    request,
                    providedApiKey
            );

            financingDtlService.updatePaid(request, financingHdr);


            try {
                financingDtlService.paymentReceive(financingHdr);
            }catch (Exception ignored){
                //akan ada proses skeduler
            }
            return new CommonResult<>().success(null, "Success Submitted");
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/invoices/paid")
    public CommonResult<PaginationResult<PaidInvoiceDto>> getInvoicePaid(
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<PaidInvoiceDto>>().success(
                financingHdrService.paidInvoice(request)
        );
    }

    @GetMapping("/invoices/disbursement")
    public CommonResult<PaginationResult<DisburseInvoiceDto>> getDisbursement(
            PaginationRequest request,
            FinancingHdr financingHdr
    ) {
        return new CommonResult<PaginationResult<DisburseInvoiceDto>>().success(
                financingHdrService.disburseInvoice(request, financingHdr)
        );
    }

    @GetMapping("/approvals/status")
    public CommonResult<Object> updateApproval(
            @RequestParam("apiKey") String apiKey
    ) {
        if (!apiKey.equalsIgnoreCase("123")) {
            throw new IllegalApiKeyException();
        }
        financingService.recallApprovalStatus();
        return new CommonResult<>().success(null, "Success Check Approval Status");
    }
}
