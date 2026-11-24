package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.BranchAreaMappingDto;
import com.kmkbe.core.domain.dto.BranchDto;
import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.dto.email.MailDataDto;
import com.kmkbe.core.domain.dto.MjrAccDashboardDto;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.major_account.request.DistributionSubmissionListRequest;
import com.kmkbe.modules.major_account.request.MjrDashboardRequest;
import com.kmkbe.modules.major_account.service.BranchAreaMappingService;
import com.kmkbe.modules.major_account.service.DistributionSubmissionService;
import com.kmkbe.modules.major_account.service.MjrDashboardService;
import com.kmkbe.modules.major_account.service.MstBranchService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MajorAccountControllersCoverageTest {
  @Mock MstBranchService mstBranchService;
  @Mock BranchAreaMappingService branchAreaMappingService;
  @Mock CurrentUserService currentUserService;
  @Mock InvoiceService invoiceService;
  @Mock FinancingHdrService financingHdrService;
  @Mock DistributionSubmissionService distributionSubmissionService;
  @Mock MjrDashboardService mjrDashboardService;

  @Test
  void branchControllerDelegates() throws Exception {
    BranchController controller = new BranchController(mstBranchService, branchAreaMappingService, currentUserService);
    when(mstBranchService.branchList("")).thenReturn(List.of(BranchDto.builder().branchCode("B1").build()));
    when(branchAreaMappingService.listBranch(any(BasePaginationRequest.class)))
      .thenReturn(PaginationResult.empty(1));
    when(branchAreaMappingService.placementBranch(any(PaginationRequest.class)))
      .thenReturn(PaginationResult.empty(1));

    assertThat(controller.getBranchListA().isSuccess()).isTrue();
    assertThat(controller.getList(new BasePaginationRequest()).isSuccess()).isTrue();
    assertThat(controller.getBranchList(new PaginationRequest()).isSuccess()).isTrue();
    assertThat(controller.postUploadPlacementBranch(mock(HttpServletRequest.class),
      mock(MultipartFile.class)).isSuccess()).isTrue();
    verify(branchAreaMappingService).updateBranch(any(), any());
  }

  @Test
  void distributionControllerDelegates() throws Exception {
    DistributionSubmissionController controller = new DistributionSubmissionController(invoiceService,
      financingHdrService, distributionSubmissionService, currentUserService);
    when(distributionSubmissionService.submissionDistribution(any(DistributionSubmissionListRequest.class)))
      .thenReturn(PaginationResult.empty(1));
    when(financingHdrService.findByCode("F1")).thenReturn(null);
    when(invoiceService.invoiceSubmissionByFinancingHdr(any(), any(BasePaginationRequest.class)))
      .thenReturn(PaginationResult.empty(1));
    when(distributionSubmissionService.assignSubmission(any())).thenReturn(new BaseResponseBuilder<Object>(true, 200, "OK"));
    when(distributionSubmissionService.aoAmByBranch("B1")).thenReturn(List.of(MailDataDto.builder().build()));

    assertThat(controller.getDistributionList(new DistributionSubmissionListRequest()).isSuccess()).isTrue();
    assertThat(controller.getDetailInvoiceDistributionList("F1", new BasePaginationRequest()).isSuccess()).isTrue();
    assertThat(controller.createAssign(new AssignInvoiceToBranchRequest())).isNotNull();
    assertThat(controller.aoAmByBranch("B1").isSuccess()).isTrue();
  }

  @Test
  void dashboardControllerDelegates() throws Exception {
    MjrDashboardController controller = new MjrDashboardController(mjrDashboardService, currentUserService);
    when(mjrDashboardService.calculate(any(MjrDashboardRequest.class))).thenReturn(new MjrAccDashboardDto());
    assertThat(controller.getDashboard(new MjrDashboardRequest()).isSuccess()).isTrue();
  }
}
