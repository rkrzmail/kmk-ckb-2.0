package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.AssignmentSubmissionService;
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

@Validated
@RestController
@RequestMapping("/api/v1/assignment-submission")
@Tag(
        name = "Penyerahan Tugas Pengajuan Kredit API",
        description = "Berisi endpoints data Penyerahan Tugas Pengajuan Kredit API"
)
@RequiredArgsConstructor
public class AssignmentSubmissionController {
    private final AssignmentSubmissionService assignmentSubmissionService;

    @GetMapping("/list")
    public CommonResult<PaginationResult<AssignmentDto>> getAssignmentList(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        return new CommonResult<PaginationResult<AssignmentDto>>()
                .success(
                        assignmentSubmissionService.assignmentList(
                                httpServletRequest,
                                authentication,
                                request
                        )
                );
    }

    @GetMapping("/toc/list/{financingHdrCode}")
    public CommonResult<PaginationResult<Object>> getTocList(
            @PathVariable String financingHdrCode,
            PaginationRequest request
    ) {
        return new CommonResult<PaginationResult<Object>>()
                .success(
                        assignmentSubmissionService.tocList(financingHdrCode, request)
                );
    }

}
