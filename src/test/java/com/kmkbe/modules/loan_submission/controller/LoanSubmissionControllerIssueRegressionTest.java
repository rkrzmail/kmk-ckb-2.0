package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import com.kmkbe.modules.loan_submission.service.SessionLoanSubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanSubmissionControllerIssueRegressionTest {

  @Mock private LoanSubmissionService loanSubmissionService;
  @Mock private DocumentService documentService;
  @Mock private SessionLoanSubmissionService sessionLoanSubmissionService;
  @Mock private CurrentUserService currentUserService;

  private LoanSubmissionController controller;

  @BeforeEach
  void setUp() {
    controller = new LoanSubmissionController(
      loanSubmissionService,
      documentService,
      sessionLoanSubmissionService,
      currentUserService
    );
  }

  @Test
  void oneParameterViewCalculateEndpointDelegatesWithNullHistoryCode() throws Exception {
    FinancingHdrDto dto = FinancingHdrDto.builder().build();
    when(loanSubmissionService.viewCulateDisburse("FIN-001", null)).thenReturn(dto);

    CommonResult<FinancingHdrDto> result = controller.getViewCalculateDisburse("FIN-001");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isSameAs(dto);
    verify(loanSubmissionService).viewCulateDisburse("FIN-001", null);
  }
}
