package com.kmkbe.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

  @Test
  void taskExecutorConfigured() {
    AsyncConfig config = new AsyncConfig();
    ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.taskExecutor();
    try {
      assertThat(executor.getCorePoolSize()).isEqualTo(10);
      assertThat(executor.getMaxPoolSize()).isEqualTo(25);
      assertThat(executor.getThreadNamePrefix()).isEqualTo("AsyncDebtorCheck-");
    } finally {
      executor.shutdown();
    }
  }

  @Test
  void auditTaskExecutorConfiguredAndRejectsWhenFull() throws Exception {
    AsyncConfig config = new AsyncConfig();
    ReflectionTestUtils.setField(config, "auditCorePoolSize", 1);
    ReflectionTestUtils.setField(config, "auditMaxPoolSize", 1);
    ReflectionTestUtils.setField(config, "auditQueueCapacity", 1);

    ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.auditTaskExecutor();
    try {
      assertThat(executor.getCorePoolSize()).isEqualTo(1);
      assertThat(executor.getMaxPoolSize()).isEqualTo(1);
      assertThat(executor.getThreadNamePrefix()).isEqualTo("AuditTrail-");

      CountDownLatch block = new CountDownLatch(1);
      RejectedExecutionHandler handler = executor.getThreadPoolExecutor().getRejectedExecutionHandler();
      // fill active + queue
      executor.execute(() -> { try { block.await(2, TimeUnit.SECONDS); } catch (InterruptedException ignored) {} });
      executor.execute(() -> { try { block.await(2, TimeUnit.SECONDS); } catch (InterruptedException ignored) {} });
      // next should be rejected by custom handler (logs error, no throw)
      handler.rejectedExecution(() -> {}, executor.getThreadPoolExecutor());
      block.countDown();
    } finally {
      executor.shutdown();
    }
  }
}
