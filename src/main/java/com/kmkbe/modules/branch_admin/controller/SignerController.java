package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.request.FileUploadRequest;
import com.kmkbe.modules.branch_admin.service.AssignmentSubmissionService;
import com.kmkbe.modules.branch_admin.service.SignerService;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;

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
    public CommonResult<PaginationResult<SignerDto>> getAssignmentList(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        return new CommonResult<PaginationResult<SignerDto>>()
                .success(
                        signerService.assignmentList(
                                httpServletRequest,
                                authentication,
                                request
                        )
                );
    }

    @GetMapping("/person/list/{financingHdrCode}")
    public CommonResult<List<DebtorDto>> getSignerPersonList(
            @PathVariable String financingHdrCode) {
        List<DebtorDto> signerPersonList = signerService.signerPersonList(financingHdrCode);
        return new CommonResult<List<DebtorDto>>().success(signerPersonList);
    }


    @PostMapping("/person")
    public CommonResult<String> createDebtor(@RequestBody DebtorDto debtorDto) {
        try {
            signerService.createDebtor(debtorDto);
            return new CommonResult<String>()
                    .success("NIK belum terdaftar. Link registrasi telah dikirim ke email " +
                            debtorDto.getEmailDebtor());
        } catch (Exception e) {
            return new CommonResult<String>()
                    .fail(400, e.getMessage());
        }
    }

    @GetMapping("/getSigners/{financingHdrCode}")
    public CommonResult<PersonDto> getSigners(
            @PathVariable String financingHdrCode) {
        PersonDto result = signerService.getSignersFromExternalApi(financingHdrCode);
        return new CommonResult<PersonDto>().success(result);
    }

    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResult<AgreementFileSigning>> uploadFile(
            @ModelAttribute FileUploadRequest request) {

        try {
            AgreementFileSigning result = signerService.uploadAgreementFile(request);
            return ResponseEntity.ok()
                    .body(new CommonResult<AgreementFileSigning>()
                            .success(result));
        } catch (IOException e) {
            log.error("Gagal upload file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new CommonResult<AgreementFileSigning>()
                            .fail(400, "Gagal upload file: " + e.getMessage()));
        }
    }
}
