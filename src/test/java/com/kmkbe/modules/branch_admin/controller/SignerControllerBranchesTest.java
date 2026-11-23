package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.branch_admin.service.AssignmentSubmissionService;
import com.kmkbe.modules.branch_admin.service.SigningEligibilityService;
import com.kmkbe.modules.branch_admin.service.SignerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignerControllerBranchesTest {
  @Mock SignerService signerService;
  @Mock AssignmentSubmissionService assignmentSubmissionService;
  @Mock CurrentUserService currentUserService;
  @Mock SigningEligibilityService signingEligibilityService;
  SignerController c;

  @BeforeEach void setUp() { c = new SignerController(signerService, assignmentSubmissionService, currentUserService, signingEligibilityService); }

  @Test
  void getSigners500() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    when(assignmentSubmissionService.assignmentList(eq(req), any(PaginationRequest.class))).thenReturn(new PaginationResult<AssignmentDto>(1,1,0L,List.of()));
    PersonDto p = new PersonDto(); p.setStatusCode("500"); p.setMessage("fail");
    when(signerService.getSignersForGroup(eq("F"), any())).thenReturn(p);
    ResponseEntity<CommonResult<PersonDto>> r = c.getSigners("F","A",req);
    assertThat(r.getStatusCode().value()).isEqualTo(500);
    assertThat(r.getBody().getCode()).isEqualTo(500);
  }

  @Test
  void getSignersException() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    when(assignmentSubmissionService.assignmentList(eq(req), any(PaginationRequest.class))).thenThrow(new RuntimeException("boom"));
    ResponseEntity<CommonResult<PersonDto>> r = c.getSigners("F","A",req);
    assertThat(r.getStatusCode().value()).isEqualTo(500);
    assertThat(r.getBody().getData().getSigners()).isEmpty();
  }

  @Test
  void createDebtorSuccessAndErrors() throws Exception {
    DebtorDto dto = new DebtorDto(); dto.setEmail("e@x.com");
    when(currentUserService.internalUsername()).thenReturn("u");
    var ok = c.createDebtor(dto);
    assertThat(ok.isSuccess()).isTrue();
    assertThat(ok.getData()).contains("e@x.com");
    doThrow(new RuntimeException("NIK sudah terdaftar")).when(signerService).createDebtor(any(), eq("u"));
    assertThat(c.createDebtor(dto).getCode()).isEqualTo(409);
    doThrow(new RuntimeException("bad")).when(signerService).createDebtor(any(), eq("u"));
    assertThat(c.createDebtor(dto).getCode()).isEqualTo(400);
  }

  @Test
  void checkSignersUnmatchedAndErrors() {
    SignerCheckResultDto match = SignerCheckResultDto.builder().unmatchedSigners(List.of()).build();
    when(signerService.compareSigners("F","A")).thenReturn(match);
    assertThat(c.checkSigners("F","A").isSuccess()).isTrue();
    SignerCheckResultDto unmatched = SignerCheckResultDto.builder().unmatchedSigners(List.of("x")).build();
    when(signerService.compareSigners("F","A")).thenReturn(unmatched);
    assertThat(c.checkSigners("F","A").getCode()).isEqualTo(400);
    doThrow(new IllegalArgumentException("bad")).when(signerService).compareSigners("F","A");
    assertThat(c.checkSigners("F","A").getCode()).isEqualTo(400);
    doThrow(new RuntimeException("boom")).when(signerService).compareSigners("F","A");
    assertThat(c.checkSigners("F","A").getCode()).isEqualTo(500);
  }

  @Test
  void downloadDoc() throws Exception {
    when(currentUserService.internalUsername()).thenReturn("u");
    ResponseEntity<ApiResponse<?>> resp = ResponseEntity.ok(new ApiResponse<>());
    when(signerService.downloadDocument("D","u")).thenReturn(resp);
    assertThat(c.downloadDocument("D")).isSameAs(resp);
  }
}
