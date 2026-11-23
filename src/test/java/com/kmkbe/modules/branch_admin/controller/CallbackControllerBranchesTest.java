package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.modules.branch_admin.request.CallbackRequest;
import com.kmkbe.modules.branch_admin.service.CallbackService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CallbackControllerBranchesTest {
  @Test
  void processException() {
    CallbackService svc = mock(CallbackService.class);
    CallbackController c = new CallbackController(svc);
    ReflectionTestUtils.setField(c, "expectedApiKey", "k");
    doThrow(new RuntimeException("fail")).when(svc).processCallback(any());
    ResponseEntity<?> r = c.handleCallback("k", new CallbackRequest());
    assertThat(r.getStatusCode().value()).isEqualTo(400);
  }

  @Test
  void nullExpectedKeyRejects() {
    CallbackService svc = mock(CallbackService.class);
    CallbackController c = new CallbackController(svc);
    ReflectionTestUtils.setField(c, "expectedApiKey", null);
    ResponseEntity<?> r = c.handleCallback("k", new CallbackRequest());
    assertThat(r.getStatusCode().value()).isEqualTo(401);
    verifyNoInteractions(svc);
  }
}
