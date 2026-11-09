package com.kmkbe.modules.branch_admin.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.request.CallbackRequest;
import com.kmkbe.modules.branch_admin.service.CallbackService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Regresi test untuk security issue #13 (SECURITY.md) — residual:
 * CallbackController (x-api-key digital-sign callback) memakai
 * constant-time comparison (MessageDigest.isEqual); semantik tidak berubah:
 * key benar diproses, key salah ditolak 401.
 */
@ExtendWith(MockitoExtension.class)
class CallbackControllerApiKeyValidationTest {

  private static final String CONFIGURED_KEY = "YiByHB@CSUL_DEV";

  @Mock
  private CallbackService callbackService;

  private CallbackController controller;

  @BeforeEach
  void setUp() {
    controller = new CallbackController(callbackService);
    ReflectionTestUtils.setField(controller, "expectedApiKey", CONFIGURED_KEY);
  }

  @Test
  void correctApiKeyProcessesCallback() {
    ResponseEntity<CommonResult<String>> response =
      controller.handleCallback(CONFIGURED_KEY, new CallbackRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(callbackService).processCallback(any(CallbackRequest.class));
  }

  @Test
  void wrongApiKeyIsRejectedWith401() {
    ResponseEntity<CommonResult<String>> response =
      controller.handleCallback("wrong-key", new CallbackRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(callbackService, never()).processCallback(any());
  }

  @Test
  void sameLengthDifferentContentIsRejected() {
    String sameLength = "A".repeat(CONFIGURED_KEY.length());

    ResponseEntity<CommonResult<String>> response =
      controller.handleCallback(sameLength, new CallbackRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(callbackService, never()).processCallback(any());
  }
}
