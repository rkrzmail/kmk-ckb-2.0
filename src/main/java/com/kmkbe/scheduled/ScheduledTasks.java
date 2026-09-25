package com.kmkbe.scheduled;

import com.kmkbe.modules.loan_submission.service.FinancingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {
  private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

  private final FinancingService financingService;
  private final SchedulerLogRepository logRepository;

  public ScheduledTasks(FinancingService financingService, SchedulerLogRepository logRepository) {
    this.financingService = financingService;
    this.logRepository = logRepository;
  }

  @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT1H")
  public void updateStatusAgreement() {
    String jobName = "UPDATE_STATUS_AGREEMENT";
    SchedulerLog history = new SchedulerLog();
    history.setJobName(jobName);
    history.setStartTime(LocalDateTime.now());
    history.setStatus("RUNNING");
    history = logRepository.save(history);

    log.info("Starting scheduled job: {}", jobName);

    try {

      financingService.recallApprovalStatus();
      history.setEndTime(LocalDateTime.now());
      history.setStatus("SUCCESS");
      logRepository.save(history);

      log.info("Job {} completed successfully.", jobName);

    } catch (Exception e) {
      history.setEndTime(LocalDateTime.now());
      history.setStatus("FAILED");
      history.setErrorMessage(e.getMessage());
      logRepository.save(history);
      log.error("Job {} failed! Error: {}", jobName, e.getMessage(), e);
    }
  }
}
