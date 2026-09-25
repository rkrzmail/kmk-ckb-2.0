package com.kmkbe.modules.common.service;

import com.kmkbe.core.domain.entity.ErrorLog;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorLogServiceTest {

  @Mock private ErrorLogRepository errorLogRepository;
  @Mock private HttpServletRequest servletRequest;

  @Test
  void createPersistsErrorLogFromThrowableAndRequest() throws Exception {
    ErrorLogService service = new ErrorLogService(errorLogRepository);
    RuntimeException throwable = new RuntimeException("boom");
    when(servletRequest.getRequestURI()).thenReturn("/api/test");
    when(servletRequest.getQueryString()).thenReturn("a=1");
    when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
    when(servletRequest.getRemoteUser()).thenReturn("user");
    when(servletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(servletRequest.getSession(false)).thenReturn(null);
    when(servletRequest.getContentType()).thenReturn(null);

    service.create(servletRequest, throwable);

    ArgumentCaptor<ErrorLog> captor = ArgumentCaptor.forClass(ErrorLog.class);
    verify(errorLogRepository).save(captor.capture());
    ErrorLog saved = captor.getValue();
    assertThat(saved.getErrorType()).isEqualTo(RuntimeException.class.getCanonicalName());
    assertThat(saved.getErrorMsg()).isEqualTo("boom");
    assertThat(saved.getPageUrl()).isEqualTo("/api/test");
    assertThat(saved.getMethodName()).isNotBlank();
    assertThat(saved.getUsrCrt()).isEqualTo("system");
    assertThat(saved.getDtmCrt()).isNotNull();
    assertThat(saved.getRequestParam()).contains("127.0.0.1");
  }

  @Test
  void createUsesUnknownWhenThrowableTypeHasNoCanonicalName() throws Exception {
    ErrorLogService service = new ErrorLogService(errorLogRepository);
    RuntimeException anonymousThrowable = new RuntimeException("anonymous") {
    };
    when(servletRequest.getRequestURI()).thenReturn("/api/test");
    when(servletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

    service.create(servletRequest, anonymousThrowable);

    ArgumentCaptor<ErrorLog> captor = ArgumentCaptor.forClass(ErrorLog.class);
    verify(errorLogRepository).save(captor.capture());
    assertThat(captor.getValue().getErrorType()).isEqualTo("Unknown");
  }

  @Test
  void createRethrowsWhenSaveFails() {
    ErrorLogService service = new ErrorLogService(errorLogRepository);
    RuntimeException throwable = new RuntimeException("boom");
    when(servletRequest.getRequestURI()).thenReturn("/api/test");
    when(servletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(errorLogRepository.save(any(ErrorLog.class))).thenThrow(new RuntimeException("db down"));

    assertThatThrownBy(() -> service.create(servletRequest, throwable))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("db down");
  }

  @Test
  void getAllAndGetByIdAreNoops() {
    ErrorLogService service = new ErrorLogService(errorLogRepository);

    service.getAll();
    service.getById(1L);
  }
}
