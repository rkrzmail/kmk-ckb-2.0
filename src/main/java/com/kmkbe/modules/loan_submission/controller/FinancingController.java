package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/financing")
@Tag(
        name = "Financing Endpoint",
        description = "Berisi endpoints data Financing"
)
@RequiredArgsConstructor
public class FinancingController {
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


            return new CommonResult<>().success(null);
        } catch (Exception e) {
            throw e;
        }
    }
}
