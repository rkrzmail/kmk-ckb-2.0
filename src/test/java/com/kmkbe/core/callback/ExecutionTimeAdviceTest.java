package com.kmkbe.core.callback;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExecutionTimeAdviceTest {
  @Test void executionTimeProceeds() throws Throwable {
    var advice = new ExecutionTimeAdvice();
    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.proceed()).thenReturn("ok");
    assertThat(advice.executionTime(pjp)).isEqualTo("ok");
    verify(pjp).proceed();
  }
}
