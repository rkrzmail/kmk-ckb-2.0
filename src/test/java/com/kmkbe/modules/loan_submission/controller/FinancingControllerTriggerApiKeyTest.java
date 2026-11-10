package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.loan_submission.service.FinancingDtlService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Regresi test untuk security issue #13 + keluarga #2/#9 (SECURITY.md):
 * trigger apiKey pada FinancingController tidak lagi hardcoded "123"
 * (env-overridable via FINANCING_TRIGGER_API_KEY), dan perbandingan
 * constant-time (MessageDigest.isEqual). Semantik: key benar memicu job,
 * key salah ditolak.
 */
@ExtendWith(MockitoExtension.class)
class FinancingControllerTriggerApiKeyTest {

  @Mock private FinancingHdrService financingHdrService;
  @Mock private FinancingService financingService;
  @Mock private FinancingDtlService financingDtlService;
  @Mock private CurrentUserService currentUserService;

  private FinancingController controller;

  @BeforeEach
  void setUp() {
    controller = new FinancingController(
      financingHdrService, financingService, financingDtlService, currentUserService);
    ReflectionTestUtils.setField(controller, "triggerApiKey", "trigger-secret");
  }

  @Test
  void correctApiKeyTriggersApprovalRecall() {
    var result = controller.updateApproval("trigger-secret");

    assertThat(result.isSuccess()).isTrue();
    verify(financingService).recallApprovalStatus();
  }

  @Test
  void correctApiKeyTriggersCwrRecall() {
    var result = controller.schCWR("trigger-secret");

    assertThat(result.isSuccess()).isTrue();
    verify(financingService).recallCWRStatus();
  }

  @Test
  void wrongApiKeyIsRejectedOnApprovalRecall() {
    assertThatThrownBy(() -> controller.updateApproval("wrong"))
      .isInstanceOf(IllegalApiKeyException.class);

    verify(financingService, never()).recallApprovalStatus();
  }

  @Test
  void wrongApiKeyIsRejectedOnCwrRecall() {
    assertThatThrownBy(() -> controller.schCWR("wrong"))
      .isInstanceOf(IllegalApiKeyException.class);

    verify(financingService, never()).recallCWRStatus();
  }

  @Test
  void nullApiKeyIsRejected() {
    assertThatThrownBy(() -> controller.updateApproval(null))
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  void legacyHardcodedKeyIsNoLongerAccepted() {
    // key lama hardcoded "123" tidak lagi lolos setelah env di-set
    assertThatThrownBy(() -> controller.updateApproval("123"))
      .isInstanceOf(IllegalApiKeyException.class);
  }
}
