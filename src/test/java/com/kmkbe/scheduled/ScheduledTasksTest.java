package com.kmkbe.scheduled;

import com.kmkbe.modules.loan_submission.service.FinancingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledTasksTest {
  private final FinancingService financingService = mock(FinancingService.class);
  private final SchedulerLogRepository logRepository = mock(SchedulerLogRepository.class);
  private final ScheduledTasks tasks = new ScheduledTasks(financingService, logRepository);

  @Test
  void marksJobSuccessWhenRecallCompletes() {
    when(logRepository.save(any(SchedulerLog.class))).thenAnswer(inv -> inv.getArgument(0));

    tasks.updateStatusAgreement();

    ArgumentCaptor<SchedulerLog> captor = ArgumentCaptor.forClass(SchedulerLog.class);
    verify(logRepository, atLeast(2)).save(captor.capture());
    SchedulerLog last = captor.getAllValues().getLast();
    assertThat(last.getJobName()).isEqualTo("UPDATE_STATUS_AGREEMENT");
    assertThat(last.getStatus()).isEqualTo("SUCCESS");
    assertThat(last.getStartTime()).isNotNull();
    assertThat(last.getEndTime()).isNotNull();
    assertThat(last.getErrorMessage()).isNull();
    verify(financingService).recallApprovalStatus();
  }

  @Test
  void marksJobFailedAndSwallowsException() {
    when(logRepository.save(any(SchedulerLog.class))).thenAnswer(inv -> inv.getArgument(0));
    doThrow(new RuntimeException("confins down")).when(financingService).recallApprovalStatus();

    assertThatCode(tasks::updateStatusAgreement).doesNotThrowAnyException();

    ArgumentCaptor<SchedulerLog> captor = ArgumentCaptor.forClass(SchedulerLog.class);
    verify(logRepository, atLeast(2)).save(captor.capture());
    SchedulerLog last = captor.getAllValues().getLast();
    assertThat(last.getStatus()).isEqualTo("FAILED");
    assertThat(last.getErrorMessage()).isEqualTo("confins down");
    assertThat(last.getEndTime()).isNotNull();
  }
}
