package com.kmkbe.modules.branch_admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.AgreementDto;
import com.kmkbe.core.domain.dto.InquiryAgreementDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.request.CreateInquiryAgreementRequest;
import com.kmkbe.modules.branch_admin.service.AgreementService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;

@Validated
@RestController
@RequestMapping("/api/v1/cwr/agreement")
@Tag(
        name = "Persetujuan Kredit Endpoints",
        description = "Berisi endpoints data persetujuan/kelayakan kredit debitur"
)
@RequiredArgsConstructor
public class AgreementController {
    private final AgreementService agreementService;
    private final FinancingRemoteService financingRemoteService;
    private final FinancingHdrService financingHdrService;
    private final FinancingHdrRepository financingHdrRepository;

    @GetMapping("/list/{cwrCode}/{financingHdrCode}")
    public CommonResult<PaginationResult<AgreementDto>> getCwrDisbursement(
            @PathVariable("cwrCode") String cwrCode,
            @PathVariable("financingHdrCode") String financingHdrCode,
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<AgreementDto>>().success(
                agreementService.list(
                        cwrCode,
                        financingHdrCode,
                        request
                )
        );
    }

    @GetMapping("/inquiry")
    public CommonResult<InquiryAgreementDto> getInquiryAgreement(
            @RequestParam("agreementNo") String agreementNo
    ) throws JsonProcessingException {
        return new CommonResult<InquiryAgreementDto>().success(
                agreementService.inquiryAgreementCwr(agreementNo)
        );
    }

    @PostMapping("/inquiry/create")
    public CommonResult<Object> createInquiryAgreement(
            Authentication authentication,
            @Valid @RequestBody CreateInquiryAgreementRequest request
    ) throws SignatureException, JsonProcessingException {
        agreementService.createInquiryAgreement(authentication, request);
        return new CommonResult<>().success(
                null,
                "Inquiry Agreement created successfully"
        );
    }

    @PostMapping(
            value = "/upload/contract",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CommonResult<Object> uploadContact(
            Authentication authentication,
            @Valid @RequestParam("financingHdrCode") String financingHdrCode,
            @Valid @RequestPart MultipartFile file

    ) throws Exception {
        FinancingHdr financingHdr = financingHdrService.findByCode(financingHdrCode);
        Agreement agreement = agreementService.findByFinancingHdr(financingHdr);
        if (agreement == null) {
            throw new IllegalStateException("Agreement Not Found with given argument");
        }

        agreementService.upload(
                authentication,
                file,
                agreement.getAgreementCode()
        );

        financingRemoteService.updateFinancingStatus(
                UpdateFinancingStatusRequest.builder()
                        .financingCode(financingHdrCode)
                        .status(UpdateFinancingStatusRequest.Status.Approve)
                        .vendorCode(financingHdr.getCustomer().getCustExternalCode())
                        .build()
        );

        //Branch admin melakukan upload dokumen perjanjian kerjasama
        financingHdr.setFinancingStatus("SIGNING");
        financingHdr.setFinancingStep("SIGNING");
        financingHdrRepository.save(financingHdr);

        return new CommonResult<>().success(
                null,
                "Agreement upload successfully"
        );
    }
}
