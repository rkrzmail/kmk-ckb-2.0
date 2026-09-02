package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.dto.SimulationHistDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.AssignmentSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
public class AssignmentSubmissionController {
  private final AssignmentSubmissionService assignmentSubmissionService;

  public AssignmentSubmissionController(AssignmentSubmissionService assignmentSubmissionService) {
    this.assignmentSubmissionService = assignmentSubmissionService;
  }

  @GetMapping("/list")
  public CommonResult<PaginationResult<AssignmentDto>> getAssignmentList(
    HttpServletRequest httpServletRequest,
    PaginationRequest request
  ) throws SignatureException {
    return new CommonResult<PaginationResult<AssignmentDto>>()
      .success(
        assignmentSubmissionService.assignmentList(
          httpServletRequest,
          request
        )
      );
  }

  @GetMapping("/toc/list/{financingHdrCode}")
  public CommonResult<PaginationResult<SimulationHistDto>> getTocList(
    @PathVariable String financingHdrCode,
    PaginationRequest request
  ) {
    return new CommonResult<PaginationResult<SimulationHistDto>>()
      .success(
        assignmentSubmissionService.tocList(financingHdrCode, request)
      );
  }

}
