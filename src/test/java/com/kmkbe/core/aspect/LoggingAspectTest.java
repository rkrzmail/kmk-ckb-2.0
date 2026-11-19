package com.kmkbe.core.aspect;

import com.kmkbe.core.domain.entity.ErrorLog;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

  @Mock ErrorLogRepository errorLogRepository;
  @Mock ProceedingJoinPoint joinPoint;
  @Mock CodeSignature signature;
  @Mock SourceLocation sourceLocation;
  @InjectMocks LoggingAspect aspect;

  @Test
  void returnsProceedResultWhenNoException() throws Throwable {
    when(joinPoint.proceed()).thenReturn("ok");
    when(joinPoint.getSignature()).thenReturn(signature);
    when(joinPoint.getTarget()).thenReturn("target");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"a"});
    when(signature.getName()).thenReturn("doWork");
    when(signature.getParameterTypes()).thenReturn(new Class[]{String.class});
    when(signature.getParameterNames()).thenReturn(new String[]{"arg1"});

    Object result = aspect.log(joinPoint);

    assertThat(result).isEqualTo("ok");
    verify(errorLogRepository, never()).save(any(ErrorLog.class));
  }

  @Test
  void savesErrorLogAndRethrowsOnException() throws Throwable {
    RuntimeException boom = new RuntimeException("boom");
    when(joinPoint.proceed()).thenThrow(boom);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(joinPoint.getTarget()).thenReturn("target");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"x"});
    when(joinPoint.getSourceLocation()).thenReturn(sourceLocation);
    when(signature.getName()).thenReturn("doWork");
    when(signature.getParameterTypes()).thenReturn(new Class[]{String.class, Integer.class});
    when(signature.getParameterNames()).thenReturn(new String[]{"a", "b"});
    when(sourceLocation.getLine()).thenReturn(42);

    assertThatThrownBy(() -> aspect.log(joinPoint)).isSameAs(boom);

    ArgumentCaptor<ErrorLog> captor = ArgumentCaptor.forClass(ErrorLog.class);
    verify(errorLogRepository).save(captor.capture());
    ErrorLog saved = captor.getValue();
    assertThat(saved.getErrorType()).isEqualTo("doWork");
    assertThat(saved.getErrorLine()).isEqualTo("42");
    assertThat(saved.getErrorMsg()).isEqualTo("boom");
    assertThat(saved.getPageUrl()).isEqualTo("doWork");
    assertThat(saved.getMethodName()).isEqualTo("doWork");
    assertThat(saved.getRequestParam()).contains("String, Integer").contains("ab");
  }
}
