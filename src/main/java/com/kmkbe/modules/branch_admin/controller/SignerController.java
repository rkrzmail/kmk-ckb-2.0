package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.branch_admin.service.AssignmentSubmissionService;
import com.kmkbe.modules.branch_admin.service.SignerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final AssignmentSubmissionService assignmentSubmissionService;
    private final CurrentUserService currentUserService;

    @GetMapping("/list")
    public CommonResult<PaginationResult<AssignmentDto>> getAssignmentList(
            HttpServletRequest httpServletRequest,
            PaginationRequest request
    ) throws SignatureException {
        return new CommonResult<PaginationResult<AssignmentDto>>()
                .success(
                        signerService.assignmentListGroupByCustomer(
                                httpServletRequest,
                                request
                        )
                );
    }

    @GetMapping("/person/list/{financingHdrCode}")
    public CommonResult<List<DebtorDto>> getSignerPersonList(
            @PathVariable String financingHdrCode
    ) throws SignatureException {
        List<DebtorDto> signerPersonList = signerService.signerPersonList(
                financingHdrCode,
                currentUserService.internalUsername()
        );
        return new CommonResult<List<DebtorDto>>().success(signerPersonList);
    }

    @GetMapping("/person/{id}")
    public ResponseEntity<CommonResult<DebtorDto>> getPersonDetail(
            @PathVariable Long id) {
        CommonResult<DebtorDto> result = signerService.detailSigner(id);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }


    @PostMapping("/person")
    public CommonResult<String> createDebtor(
            @RequestBody DebtorDto debtorDto
    ) {
        try {
            signerService.createDebtor(debtorDto, currentUserService.internalUsername());
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

    @GetMapping("/getSigners/{financingHdrCode}/{agreementCode}")

    public ResponseEntity<CommonResult<PersonDto>> getSigners(
            @PathVariable String financingHdrCode,
            @PathVariable String agreementCode,
            HttpServletRequest httpServletRequest
    ) throws SignatureException{
        try {
            PaginationResult<AssignmentDto> originalResult =
                    assignmentSubmissionService.assignmentList(httpServletRequest,new PaginationRequest());

            List<AssignmentDto> originalList = originalResult.getList();
            PersonDto allSigners = signerService.getSignersForGroup(financingHdrCode, originalList);

            if ("500".equals(allSigners.getStatusCode())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new CommonResult<PersonDto>().fail(500, allSigners.getMessage(), allSigners));
            }

            return ResponseEntity.ok(new CommonResult<PersonDto>().success(allSigners));
        } catch (Exception e) {
            log.error("getSigners failed. financingHdrCode={}, agreementCode={}, error={}",
                    financingHdrCode, agreementCode, e.getMessage(), e);
            PersonDto error = new PersonDto();
            error.setStatusCode("500");
            error.setMessage(e.getMessage());
            error.setSigners(List.of());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonResult<PersonDto>().fail(500, e.getMessage(), error));
        }
    }

    @GetMapping("/signer-agreement/{financingHdrCode}")
    public CommonResult<List<SignerAgreementDto>> getAgreement(
            @PathVariable String financingHdrCode) {
        List<SignerAgreementDto> signerAgreement = signerService.signerAgreement(financingHdrCode);
        return new CommonResult<List<SignerAgreementDto>>().success(signerAgreement);
    }

    @GetMapping("/signer-doc/list/{financingHdrCode}")
    public CommonResult<List<SignerDocDto>> getSignerDocList(
            @PathVariable String financingHdrCode
    ) throws SignatureException {
        List<SignerDocDto> signerDocList = signerService.signerDocList(
                financingHdrCode,
                currentUserService.internalUsername()
        );
        return new CommonResult<List<SignerDocDto>>().success(signerDocList);
    }

    // cek confins
    @GetMapping("/check-signer/{financingHdrCode}/{agreementNo}")
    public CommonResult<SignerCheckResultDto> checkSigners(
            @PathVariable String financingHdrCode,
            @PathVariable String agreementNo) {
        try {
            SignerCheckResultDto result = signerService.compareSigners(financingHdrCode, agreementNo);

            if (result.getUnmatchedSigners().isEmpty()) {
                return new CommonResult<SignerCheckResultDto>()
                        .success(result);
            } else {
                return new CommonResult<SignerCheckResultDto>()
                        .fail(400, "Ada perubahan data signer pada confins", result);
            }
        } catch (IllegalArgumentException e) {
            return new CommonResult<SignerCheckResultDto>()
                    .fail(400, e.getMessage(), null);
        } catch (Exception e) {
            return new CommonResult<SignerCheckResultDto>()
                    .fail(500, e.getMessage(), null);
        }
    }


    @GetMapping("/download-doc/{documentId}")
    public ResponseEntity<ApiResponse<?>> downloadDocument(
            @PathVariable String documentId
    ) throws SignatureException {

        return signerService.downloadDocument(documentId, currentUserService.internalUsername());
    }

    // cek db
    @GetMapping("/check-signer/danasakti/{financingHdrCode}/{agreementCode}")
    public CommonResult<Map<String, Object>> checkSignerDanasakti(
            @PathVariable String financingHdrCode,
            @PathVariable String agreementCode
    ) throws SignatureException {

        List<DebtorDto> signerPersonList = signerService.checkSignerDanasakti(
                financingHdrCode,
                currentUserService.internalUsername()
        );

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
                .success(responseData);
    }

    @GetMapping("/check-send-document/{financingHdrCode}/{agreementCode}")
    public CommonResult<Map<String, Object>> checkSendDocument(
            @PathVariable String financingHdrCode,
            @PathVariable String agreementCode) {

        Map<String, Object> responseData = signerService.checkSendDocument(financingHdrCode, agreementCode);

        return new CommonResult<Map<String, Object>>()
                .success(responseData);
    }

}
