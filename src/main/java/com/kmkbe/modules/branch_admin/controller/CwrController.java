package com.kmkbe.modules.branch_admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.request.CreateInquiryCwrRequest;
import com.kmkbe.modules.branch_admin.service.CwrService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.text.ParseException;
import java.util.List;

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
    private final InvoiceService invoiceService;
    private final FinancingHdrService financingHdrService;

    @GetMapping("/list/{custCode}")
    public CommonResult<PaginationResult<CwrListDto>> getCwrList(
            @PathVariable("custCode") String custCode,
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {

        UserInternalUtils.authenticated(authentication);

        return new CommonResult<PaginationResult<CwrListDto>>().success(
                cwrService.list(custCode, request)
        );
    }

    @GetMapping("/detail/{cwrCode}/{financingHdrCode}")
    public CommonResult<DetailCwrDto> getCwr(
            @PathVariable("cwrCode") String cwrCode,
            Authentication authentication,
            @PathVariable("financingHdrCode") String financingHdrCode
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<DetailCwrDto>().success(
                cwrService.detail(cwrCode, financingHdrCode)
        );
    }

    @GetMapping("/list-agreement/{cwrCode}/{financingHdrCode}")
    public CommonResult<List<String>> getListAggr(
            Authentication authentication,
            @PathVariable("cwrCode") String cwrCode,
            @PathVariable("financingHdrCode") String financingHdrCode

    ) throws SignatureException  {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<List<String>>().success(
                cwrService.inquiryListAggr (cwrCode)
        );
    }
    @GetMapping("/inquiry")
    public CommonResult<InquiryCwrDto> getInquiryCwr(
            Authentication authentication,
            @RequestParam("cwrNo") String cwrNo
    ) throws JsonProcessingException, ParseException, SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<InquiryCwrDto>().success(
                cwrService.inquiryCwr(cwrNo)
        );
    }

    @PostMapping("/inquiry/create")
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

    @GetMapping("/invoices/{financingHdrCode}")
    public CommonResult<PaginationResult<PostedInvoiceDto>> getCwrInvoices(
            Authentication authentication,
            @PathVariable("financingHdrCode") String financingHdrCode,
            PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        PaginationResult<PostedInvoiceDto> result = PaginationResult.empty(
                request.getPageNo()
        );

        FinancingHdr financingHdr = financingHdrService.findByCode(financingHdrCode);
        if (financingHdr != null) {


            result = invoiceService.invoiceSubmissionByFinancingHdr(
                    financingHdr,
                    request
            );
        }

        return new CommonResult<PaginationResult<PostedInvoiceDto>>().success(
                result
        );
    }
}
