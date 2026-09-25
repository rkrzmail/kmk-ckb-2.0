package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.dto.CustomerDashboardDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.customer.service.CustomerDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;

@RestController
@RequestMapping("/api/v1/customer/agreement")
@Tag(
        name = "Customer",
        description = "Customer Endpoints"
)
@RequiredArgsConstructor
public class CustomerAgreementController {
    private final CustomerDashboardService customerDashboardService;
    private final CurrentUserService currentUserService;

    @GetMapping("/dashboard")
    public CommonResult<CustomerDashboardDto.Agreement> getAgreementDashboard() throws SignatureException {
        currentUserService.customer();
        return new CommonResult<CustomerDashboardDto.Agreement>().success(
                customerDashboardService.agreementDashboard()
        );
    }

    @GetMapping("/signer")
    public CommonResult<PaginationResult<Object>> getSignerPersons(
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<Object>>().success(
                null
        );
    }

    @GetMapping("/financing")
    public CommonResult<PaginationResult<Object>> getFinancing(
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<Object>>().success(
                null
        );
    }

    @PostMapping("/signing/{agreementFileId}")
    public CommonResult<Object> postUploadSigning(
            @PathVariable("agreementFileId") String agreementFileId
    ){
        return new CommonResult<Object>().success(
                null
        );
    }
}
