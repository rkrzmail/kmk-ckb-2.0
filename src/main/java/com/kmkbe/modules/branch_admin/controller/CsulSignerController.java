package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.CsulSignerService;
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
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/csul-signer")
@Tag(
        name = "List table data debtor untuk signer person",
        description = "Berisi endpoints data debtor"
)
@RequiredArgsConstructor
public class CsulSignerController {
    private final CsulSignerService csulSignerService;

    @GetMapping("/list")
    public CommonResult<List<SignerCsulDto>> getSignerCsulList(
            Authentication authentication) {
        List<SignerCsulDto> signerCsulList = csulSignerService.signerCsulList(authentication);
        return new CommonResult<List<SignerCsulDto>>().success(signerCsulList);
    }

    @GetMapping("/list/{id}")
    public CommonResult<SignerCsulDto> getSignerDetail(
            @PathVariable("id") Long id
    ) {
        SignerCsulDto dto = csulSignerService.detailSigner(id);
        return new CommonResult<SignerCsulDto>().success(dto);
    }

    // sementara static get signer csul
    @GetMapping("/get-signer")
    public CommonResult<ExternalSignerResponse> getExternalSigners() {
        ExternalSignerResponse response = csulSignerService.getStaticSigners();
        return new CommonResult<ExternalSignerResponse>().success(response);
    }

    @PostMapping("/signer")
    public CommonResult<String> createSigner(
            @RequestBody SignerCsulRequest request,
            Authentication authentication) {

        try {
            csulSignerService.createSigner(request, authentication);
            return new CommonResult<String>().success("Data berhasil disimpan");
        } catch (RuntimeException e) {
            return new CommonResult<String>().fail(400, "Gagal menyimpan: " + e.getMessage());
        }
    }

    @PutMapping("/signer/{id}")
    public CommonResult<String> updateSigner(
            @PathVariable("id") Long id,
            @RequestBody SignerCsulRequest request,
            Authentication authentication) {
        try {
            csulSignerService.updateSigner(id, request, authentication);
            return new CommonResult<String>().success("Data berhasil diperbarui");
        } catch (RuntimeException e) {
            return new CommonResult<String>().fail(400,"Gagal update: " + e.getMessage());
        }
    }

}
