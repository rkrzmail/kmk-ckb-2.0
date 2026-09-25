package com.kmkbe.modules.common.service;

import com.kmkbe.core.domain.entity.AuditTrail;
import com.kmkbe.core.domain.event.AuditTrailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditTrailEventListener {
  private final AuditTrailWriterService auditTrailWriterService;

  @Value("${audit.retry.max-attempts:3}")
  private int maxAttempts;

  @Value("${audit.retry.initial-delay-ms:200}")
  private long initialDelayMs;

  @Async("auditTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void handle(AuditTrailEvent event) {
    AuditTrail auditTrail = buildAuditTrail(event);
    saveWithRetry(auditTrail, event);
  }

  private AuditTrail buildAuditTrail(AuditTrailEvent event) {
    return AuditTrail.builder()
      .traceId(event.traceId())
      .actorType(event.actorType())
      .actorUsername(event.actorUsername())
      .actorId(event.actorId())
      .sourceIp(event.sourceIp())
      .userAgent(event.userAgent())
      .moduleName(event.moduleName())
      .action(event.action())
      .entityName(event.entityName())
      .entityId(event.entityId())
      .beforeData(event.beforeData())
      .afterData(event.afterData())
      .requestPath(event.requestPath())
      .httpMethod(event.httpMethod())
      .responseStatus(event.responseStatus())
      .success(event.success())
      .errorMessage(event.errorMessage())
      .createdAt(event.createdAt())
      .build();
  }

  private void saveWithRetry(AuditTrail auditTrail, AuditTrailEvent event) {
    int attempts = Math.max(1, maxAttempts);

    for (int attempt = 1; attempt <= attempts; attempt++) {
      try {
        auditTrailWriterService.save(auditTrail);
        return;
      } catch (Exception e) {
        if (attempt == attempts) {
          log.error(
            "Failed to write audit trail after {} attempts. traceId={}, module={}, action={}, entityName={}, entityId={}, actor={}, path={}, method={}, error={}",
            attempts,
            event.traceId(),
            event.moduleName(),
            event.action(),
            event.entityName(),
            event.entityId(),
            event.actorUsername(),
            event.requestPath(),
            event.httpMethod(),
            e.getMessage(),
            e
          );
          return;
        }

        log.warn(
          "Retrying audit trail write. attempt={}, nextAttempt={}, traceId={}, module={}, action={}, entityName={}, entityId={}, error={}",
          attempt,
          attempt + 1,
          event.traceId(),
          event.moduleName(),
          event.action(),
          event.entityName(),
          event.entityId(),
          e.getMessage()
        );
        sleepBeforeRetry(attempt);
      }
    }
  }

  private void sleepBeforeRetry(int attempt) {
    long delay = Math.max(0, initialDelayMs) * attempt;
    if (delay == 0) {
      return;
    }

    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Audit trail retry interrupted");
    }
  }
}
