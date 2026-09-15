package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.DebtorDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.branch_admin.service.AssignmentSubmissionService;
import com.kmkbe.modules.branch_admin.service.SigningEligibilityService;
import com.kmkbe.modules.branch_admin.service.SignerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignerControllerTest {

  private static final String FINANCING_HDR_CODE = "11111111-1111-1111-1111-111111111111";
  private static final String AGREEMENT_CODE = "AGR001";

  @Mock private SignerService signerService;
  @Mock private AssignmentSubmissionService assignmentSubmissionService;
  @Mock private CurrentUserService currentUserService;
  @Mock private SigningEligibilityService signingEligibilityService;

  private SignerController controller;

  @BeforeEach
  void setUp() {
    controller = new SignerController(
      signerService,
      assignmentSubmissionService,
      currentUserService,
      signingEligibilityService
    );
  }

  @Test
  void checkSignerDanasaktiPreservesAuthenticationFailure() throws Exception {
    doThrow(new java.security.SignatureException("not authenticated"))
      .when(currentUserService).authenticatedInternalUser();
    assertThatThrownBy(() -> controller.checkSignerDanasakti(FINANCING_HDR_CODE, AGREEMENT_CODE))
      .isInstanceOf(java.security.SignatureException.class).hasMessage("not authenticated");
    verifyNoInteractions(signingEligibilityService, signerService);
  }

  @Test
  void checkSignerDanasaktiKeepsNotFoundResponseForEmptyResult() throws Exception {
    when(currentUserService.internalUsername()).thenReturn("maker");
    when(signerService.checkSignerDanasakti(FINANCING_HDR_CODE, "maker")).thenReturn(List.of());
    var result = controller.checkSignerDanasakti(FINANCING_HDR_CODE, AGREEMENT_CODE);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getCode()).isEqualTo(404);
    assertThat(result.getMessage()).isEqualTo("Signer tidak tersedia");
    assertThat(result.getData()).containsEntry("signerName", null);
  }

  @Test
  void checkSignerDanasaktiRejectsUnregisteredSignerBeforeExternalCheck() throws Exception {
    doThrow(new IllegalStateException(SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE))
      .when(signingEligibilityService)
      .validateDebtorSigner(FINANCING_HDR_CODE);

    CommonResult<Map<String, Object>> result = controller.checkSignerDanasakti(
      FINANCING_HDR_CODE,
      AGREEMENT_CODE
    );

    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getCode()).isEqualTo(400);
    assertThat(result.getMessage()).isEqualTo(SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE);
    assertThat(result.getData()).containsEntry("signerName", null);
    verify(signerService, never()).checkSignerDanasakti(FINANCING_HDR_CODE, "maker");
    verify(currentUserService, never()).internalUsername();
  }

  @Test
  void checkSignerDanasaktiReturnsSignerNamesWhenLatestStatusIsEligible() throws Exception {
    DebtorDto signer = new DebtorDto();
    signer.setKaryawanName("Signer One");
    doNothing().when(signingEligibilityService).validateDebtorSigner(FINANCING_HDR_CODE);
    when(currentUserService.internalUsername()).thenReturn("maker");
    when(signerService.checkSignerDanasakti(FINANCING_HDR_CODE, "maker")).thenReturn(List.of(signer));

    CommonResult<Map<String, Object>> result = controller.checkSignerDanasakti(
      FINANCING_HDR_CODE,
      AGREEMENT_CODE
    );

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getCode()).isEqualTo(200);
    assertThat(result.getData()).containsEntry("signerName", List.of("Signer One"));
    verify(signingEligibilityService, org.mockito.Mockito.times(2))
      .validateDebtorSigner(FINANCING_HDR_CODE);
  }

  @Test
  void checkSignerDanasaktiRejectsStatusThatBecomesIneligibleAfterRefresh() throws Exception {
    DebtorDto signer = new DebtorDto();
    signer.setKaryawanName("Signer One");
    doNothing()
      .doThrow(new IllegalStateException(SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE))
      .when(signingEligibilityService)
      .validateDebtorSigner(FINANCING_HDR_CODE);
    when(currentUserService.internalUsername()).thenReturn("maker");
    when(signerService.checkSignerDanasakti(FINANCING_HDR_CODE, "maker")).thenReturn(List.of(signer));

    CommonResult<Map<String, Object>> result = controller.checkSignerDanasakti(
      FINANCING_HDR_CODE,
      AGREEMENT_CODE
    );

    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getCode()).isEqualTo(400);
    assertThat(result.getMessage()).isEqualTo(SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE);
  }

  @Test
  void checkSignerDanasaktiReturnsControlledErrorWhenStatusRefreshFails() throws Exception {
    doNothing().when(signingEligibilityService).validateDebtorSigner(FINANCING_HDR_CODE);
    when(currentUserService.internalUsername()).thenReturn("maker");
    when(signerService.checkSignerDanasakti(FINANCING_HDR_CODE, "maker"))
      .thenThrow(new RuntimeException("E-Sign timeout"));

    CommonResult<Map<String, Object>> result = controller.checkSignerDanasakti(
      FINANCING_HDR_CODE,
      AGREEMENT_CODE
    );

    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getCode()).isEqualTo(500);
    assertThat(result.getMessage()).isEqualTo("Gagal memeriksa status E-Signer. Silakan coba kembali.");
    assertThat(result.getData()).containsEntry("signerName", null);
  }
}
