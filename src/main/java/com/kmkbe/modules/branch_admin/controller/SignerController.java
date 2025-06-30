package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.AssignmentSubmissionService;
import com.kmkbe.modules.branch_admin.service.SignerService;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.util.List;

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

    @GetMapping("/person/list")
    public CommonResult<List<DebtorDto>> getSignerPersonList() {
        List<DebtorDto> signerPersonList  = signerService.signerPersonList();
        return new CommonResult<List<DebtorDto>>().success(signerPersonList);
    }

    @PostMapping("/person")
    public CommonResult<DebtorDto> createDebtor(
            Authentication authentication,
            @RequestBody DebtorDto debtorDto
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);

        DebtorDto createdDebtor = signerService.createDebtor(debtorDto);

        return new CommonResult<DebtorDto>().success(createdDebtor);
    }

    @GetMapping("/getSigners")  // Ganti dari @PostMapping
    public CommonResult<PersonDto> getSigners(Authentication authentication
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        PersonDto getSigner = signerService.getSignersFromExternalApi(); // Tanpa parameter
        return new CommonResult<PersonDto>().success(getSigner);
    }
}
