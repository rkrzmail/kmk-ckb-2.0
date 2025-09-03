package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.SignerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/signer")
@Tag(
        name = "List table data debtor untuk signer person",
        description = "Berisi endpoints data debtor"
)
@RequiredArgsConstructor
public class SignerController {
    private final SignerService signerService;

    @GetMapping("/list")
    public CommonResult<PaginationResult<AssignmentDto>> getAssignmentList(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        return new CommonResult<PaginationResult<AssignmentDto>>()
                .success(
                        signerService.assignmentListGroupByCustomer(
                                httpServletRequest,
                                authentication,
                                request
                        )
                );
    }

    @GetMapping("/person/list/{financingHdrCode}")
    public CommonResult<List<DebtorDto>> getSignerPersonList(
            @PathVariable String financingHdrCode,
            Authentication authentication) {
        List<DebtorDto> signerPersonList = signerService.signerPersonList(financingHdrCode, authentication);
        return new CommonResult<List<DebtorDto>>().success(signerPersonList);
    }

    @GetMapping("/person/{id}")
    public ResponseEntity<CommonResult<DebtorDto>> getPersonDetail(
            @PathVariable Long id) {
        CommonResult<DebtorDto> result = signerService.detailSigner(id);

        if (result.getIsSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }


    @PostMapping("/person")
    public CommonResult<String> createDebtor(
            @RequestBody DebtorDto debtorDto,
            Authentication authentication) {
        try {
            signerService.createDebtor(debtorDto, authentication);
            return new CommonResult<String>()
                    .success("NIK belum terdaftar. Link registrasi telah dikirim ke email " +
                            debtorDto.getEmail());
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("NIK sudah terdaftar")) {
                return new CommonResult<String>()
                        .fail(409, errorMessage);
            }
            return new CommonResult<String>()
                    .fail(400, errorMessage);
        }
    }

    @GetMapping("/getSigners/{financingHdrCode}/{agreementNo}")
    public CommonResult<PersonDto> getSigners(
            @PathVariable String financingHdrCode,
            @PathVariable String agreementNo) {
        PersonDto result = signerService.getSignersFromExternalApi(financingHdrCode, agreementNo);
        return new CommonResult<PersonDto>().success(result);
    }

    @GetMapping("/signer-agreement/{financingHdrCode}")
    public CommonResult<List<SignerAgreementDto>> getAgreement(
            @PathVariable String financingHdrCode) {
        List<SignerAgreementDto> signerAgreement = signerService.signerAgreement(financingHdrCode);
        return new CommonResult<List<SignerAgreementDto>>().success(signerAgreement);
    }

    @GetMapping("/signer-doc/list/{financingHdrCode}")
    public CommonResult<List<SignerDocDto>> getSignerDocList(
            @PathVariable String financingHdrCode,
            Authentication authentication) {
        List<SignerDocDto> signerDocList = signerService.signerDocList(financingHdrCode, authentication);
        return new CommonResult<List<SignerDocDto>>().success(signerDocList);
    }

    @GetMapping("/check-signer/{financingHdrCode}/{agreementNo}")
    public CommonResult<SignerCheckResultDto> checkSigners(
            @PathVariable String financingHdrCode,
            @PathVariable String agreementNo) {
        SignerCheckResultDto result = signerService.compareSigners(financingHdrCode, agreementNo);

        if (result.getUnmatchedSigners().isEmpty()) {
            return new CommonResult<SignerCheckResultDto>()
                    .success(result, "Tidak ada perubahan signer pada confins");
        } else {
            return new CommonResult<SignerCheckResultDto>()
                    .fail(400, "Ada perubahan data signer pada confins", result);
        }
    }


    @GetMapping("/download-doc/{documentId}")
    public ResponseEntity<ApiResponse<?>> downloadDocument(
            @PathVariable String documentId,
            Authentication authentication) {

        return signerService.downloadDocument(documentId, authentication);
    }

    @GetMapping("/check-signer/danasakti/{financingHdrCode}/{agreementCode}")
    public CommonResult<Map<String, Object>> checkSignerDanasakti(
            @PathVariable String financingHdrCode,
            @PathVariable String agreementCode,
            Authentication authentication) {

        List<DebtorDto> signerPersonList = signerService.checkSignerDanasakti(financingHdrCode, authentication);

        Map<String, Object> responseData = new HashMap<>();

        if (signerPersonList == null || signerPersonList.isEmpty()) {
            responseData.put("signerName", null);
            return new CommonResult<Map<String, Object>>()
                    .fail(404, "Signer tidak tersedia", responseData);
        }
        List<String> signerNames = signerPersonList.stream()
                .map(DebtorDto::getKaryawanName)
                .collect(Collectors.toList());

        responseData.put("signerName", signerNames);

        return new CommonResult<Map<String, Object>>()
                .success(responseData, "Signer tersedia");
    }

}
