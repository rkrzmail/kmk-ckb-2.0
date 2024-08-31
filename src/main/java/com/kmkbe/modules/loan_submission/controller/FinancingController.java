package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.domain.dto.DisburseInvoiceDto;
import com.kmkbe.core.domain.dto.PaidInvoiceDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
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


    @PostMapping("/invoice-paid")
    public CommonResult<Object> invoicePaid(
            Authentication authentication,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody FinancingInvoicePaidRequest request
    ) throws Exception {
        try {
            String providedApiKey = httpServletRequest.getHeader("ApiKey");
            if (!providedApiKey.equals("$2b$10$YoLl0SFxCMlIWXfQ9RhixeU8Vxvj9Fi7RmF5j7zA9dhYwdplSGyWC")) {
                //throw new Exception("Invalid ApiKey");
                throw new IllegalApiKeyException();
            }

            financingHdrService.paidFinancing(
                    authentication,
                    request,
                    providedApiKey
            );

            return new CommonResult<>().success(null, "Success Submitted");
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/invoices/paid")
    public CommonResult<PaginationResult<PaidInvoiceDto>> getInvoicePaid() {
        return new CommonResult<PaginationResult<PaidInvoiceDto>>().success(
                financingHdrService.paidInvoice()
        );
    }

    @GetMapping("/invoices/disbursement")
    public CommonResult<PaginationResult<DisburseInvoiceDto>> getDisbursement() {
        return new CommonResult<PaginationResult<DisburseInvoiceDto>>().success(
                financingHdrService.disburseInvoice()
        );
    }

    @GetMapping("/approvals/status")
    public CommonResult<Object> updateApproval( @RequestParam("apiKey") String apiKey
    ) {
        //http://localhost:8443/api/v1/financing/approvals/status?apiKey=$2b$10$YoLl0SFxCMlIWXfQ9RhixeU8Vxvj9Fi7RmF5j7zA9dhYwdplSGyWC
        try {

            if (!apiKey.equals("$2b$10$YoLl0SFxCMlIWXfQ9RhixeU8Vxvj9Fi7RmF5j7zA9dhYwdplSGyWC")) {
                //throw new Exception("Invalid ApiKey");
                throw new IllegalApiKeyException();
            }



            financingService.recallApprovalStatus();



            return new CommonResult<>().success(null, "Success Submitted");
        } catch (Exception e) {
            throw e;
        }
    }
}
