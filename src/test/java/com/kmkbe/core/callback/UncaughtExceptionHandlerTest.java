package com.kmkbe.core.callback;

import com.kmkbe.core.domain.entity.ErrorLog;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UncaughtExceptionHandlerTest {
  @Test void initSetsDefaultHandler() {
    ErrorLogRepository repo = mock(ErrorLogRepository.class);
    var handler = new UncaughtExceptionHandler(repo);
    var prev = Thread.getDefaultUncaughtExceptionHandler();
    try {
      handler.init();
      assertThat(Thread.getDefaultUncaughtExceptionHandler()).isSameAs(handler);
    } finally {
      Thread.setDefaultUncaughtExceptionHandler(prev);
    }
  }

  @Test void uncaughtExceptionSavesErrorLog() {
    ErrorLogRepository repo = mock(ErrorLogRepository.class);
    var handler = new UncaughtExceptionHandler(repo);
    Exception ex = new RuntimeException("boom");
    handler.uncaughtException(Thread.currentThread(), ex);
    ArgumentCaptor<ErrorLog> cap = ArgumentCaptor.forClass(ErrorLog.class);
    verify(repo).save(cap.capture());
    assertThat(cap.getValue().getErrorMsg()).isEqualTo("boom");
    assertThat(cap.getValue().getErrorType()).contains("RuntimeException");
    assertThat(cap.getValue().getPageUrl()).isEqualTo("UNCAUGHTEXCEPTION");
  }
}
