package com.kmkbe.modules.branch_admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.CwrDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.request.AgreementCreditCwrRequest;
import com.kmkbe.modules.branch_admin.service.CwrService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.text.ParseException;

@Validated
@RestController
@RequestMapping("/api/v1/cwr")
@Tag(
        name = "Persetujuan Kredit Endpoints",
        description = "Berisi endpoints data persetujuan/kelayakan kredit debitur"
)
@RequiredArgsConstructor
public class CwrController {
    private final CwrService cwrService;

    @GetMapping("/{cwrCode}")
    public CommonResult<Object> getCwr(
            @PathVariable("cwrCode") String cwrCode
    ) {
        return new CommonResult<Object>().success(
                null
        );
    }

    @GetMapping("/list/{custCode}")
    public CommonResult<PaginationResult<CwrDto>> getCwrList(
            @PathVariable("custCode") String custCode,
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<CwrDto>>().success(
                cwrService.list(custCode, request)
        );
    }

    @PostMapping("/create/credit")
    public CommonResult<Object> createCredit(
            Authentication authentication,
            @Valid @RequestBody AgreementCreditCwrRequest request
    ) throws SignatureException, ParseException, JsonProcessingException {
        cwrService.createCredit(authentication, request);
        return new CommonResult<>().success(
                null,
                "Cwr created successfully"
        );
    }

    @PostMapping("/create/agreement/{cwrCode}")
    public CommonResult<Object> createAgreement(
            @PathVariable("cwrCode") String cwrCode,
            Authentication authentication
    ) throws SignatureException, ParseException, JsonProcessingException {
        cwrService.createAgreement(authentication);
        return new CommonResult<>().success(
                null,
                "Cwr created successfully"
        );
    }

    @GetMapping("/invoices/{cwrCode}")
    public CommonResult<PaginationResult<Object>> getCwrInvoices(
            @PathVariable("cwrCode") String cwrCode,
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<Object>>().success(
                null
        );
    }

    @GetMapping("/disbursement/{cwrCode}")
    public CommonResult<PaginationResult<Object>> getCwrDisbursement(
            @PathVariable("cwrCode") String cwrCode,
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<Object>>().success(
                null
        );
    }
}
