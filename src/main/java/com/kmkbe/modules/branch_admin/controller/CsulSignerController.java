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



}
