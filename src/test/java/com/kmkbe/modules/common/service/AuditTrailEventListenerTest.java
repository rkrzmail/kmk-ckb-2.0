package com.kmkbe.modules.common.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.constant.AuditActorType;
import com.kmkbe.core.domain.event.AuditTrailEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditTrailEventListenerTest {
  @Mock AuditTrailWriterService writerService;

  AuditTrailEventListener listener;

  @BeforeEach
  void setUp() {
    listener = new AuditTrailEventListener(writerService);
    ReflectionTestUtils.setField(listener, "initialDelayMs", 0L);
  }

  @Test
  void handleMapsEventAndSavesOnce() {
    ReflectionTestUtils.setField(listener, "maxAttempts", 1);

    listener.handle(event());

    ArgumentCaptor<com.kmkbe.core.domain.entity.AuditTrail> captor =
      ArgumentCaptor.forClass(com.kmkbe.core.domain.entity.AuditTrail.class);
    verify(writerService).save(captor.capture());
    assertThat(captor.getValue().getTraceId()).isEqualTo("trace-1");
    assertThat(captor.getValue().getModuleName()).isEqualTo("AUTH");
    assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.LOGIN);
    assertThat(captor.getValue().getActorType()).isEqualTo(AuditActorType.SYSTEM);
    assertThat(captor.getValue().getSuccess()).isTrue();
  }

  @Test
  void nonPositiveMaxAttemptsStillAttemptsOnce() {
    ReflectionTestUtils.setField(listener, "maxAttempts", 0);

    listener.handle(event());

    verify(writerService, times(1)).save(any());
  }

  @Test
  void retriesUntilSuccess() {
    ReflectionTestUtils.setField(listener, "maxAttempts", 2);
    doThrow(new RuntimeException("temporary")).doNothing().when(writerService).save(any());

    listener.handle(event());

    verify(writerService, times(2)).save(any());
  }

  @Test
  void stopsAfterFinalAttemptFailure() {
    ReflectionTestUtils.setField(listener, "maxAttempts", 2);
    doThrow(new RuntimeException("permanent")).when(writerService).save(any());

    listener.handle(event());

    verify(writerService, times(2)).save(any());
  }

  @Test
  void interruptedSleepKeepsInterruptFlagAndContinues() {
    ReflectionTestUtils.setField(listener, "maxAttempts", 2);
    ReflectionTestUtils.setField(listener, "initialDelayMs", 10L);
    doThrow(new RuntimeException("temporary")).doNothing().when(writerService).save(any());
    Thread.currentThread().interrupt();

    try {
      listener.handle(event());
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
      verify(writerService, times(2)).save(any());
    } finally {
      Thread.interrupted();
    }
  }

  private AuditTrailEvent event() {
    return new AuditTrailEvent(
      "trace-1", AuditActorType.SYSTEM, "sys", "1", "127.0.0.1", "JUnit", "AUTH",
      AuditAction.LOGIN, "Authentication", "1", null, null, "/api/login", "POST",
      200, true, null, LocalDateTime.of(2026, 11, 24, 10, 0));
  }
}
