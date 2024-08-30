package com.kmkbe.modules.branch_admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.CwrDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.request.CreateInquiryAgreementRequest;
import com.kmkbe.modules.branch_admin.request.CreateInquiryCwrRequest;
import com.kmkbe.modules.branch_admin.service.AgreementService;
import com.kmkbe.modules.branch_admin.service.CwrService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final AgreementService agreementService;

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

    @PostMapping("/create/inquiry")
    public CommonResult<Object> createInquiryCwr(
            Authentication authentication,
            @Valid @RequestBody CreateInquiryCwrRequest request
    ) throws SignatureException, ParseException, JsonProcessingException {
        cwrService.createInquiryCwr(authentication, request);
        return new CommonResult<>().success(
                null,
                "Inquiry CWR created successfully"
        );
    }

    @PostMapping("/create/inquiry/agreement/{financingHdrCode}/{cwrCode}")
    public CommonResult<Object> createInquiryAgreement(
            @PathVariable("financingHdrCode") String financingHdrCode,
            @PathVariable("cwrCode") String cwrCode,
            Authentication authentication,
            @Valid @RequestBody CreateInquiryAgreementRequest request
    ) throws SignatureException, ParseException, JsonProcessingException {
        cwrService.createInquiryAgreement(authentication, financingHdrCode, cwrCode, request);
        return new CommonResult<>().success(
                null,
                "Inquiry Agreement created successfully"
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

    @PostMapping(
            value = "/upload/contract/{agreementCode}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CommonResult<Object> uploadContact(
            Authentication authentication,
            @PathVariable("agreementCode") String agreementCode,
            @Valid @RequestPart MultipartFile file

    ) throws Exception {
        agreementService.upload(
                authentication,
                file,
                agreementCode
        );

        return new CommonResult<>().success(
                null,
                "Agreement upload successfully"
        );
    }
}
