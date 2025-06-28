package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.AssignmentSubmissionService;
import com.kmkbe.modules.branch_admin.service.SignerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public CommonResult<List<SignerPersonDto>> getSignerPersonList() {
        List<SignerPersonDto> signerPersonList  = signerService.signerPersonList();
        return new CommonResult<List<SignerPersonDto>>().success(signerPersonList);
    }

}
