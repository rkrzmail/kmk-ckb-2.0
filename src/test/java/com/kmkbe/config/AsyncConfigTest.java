package com.kmkbe.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test AsyncConfig: pool sizing, thread name prefix, dan
 * rejection handler auditTaskExecutor (queue penuh → error log,
 * tidak melempar exception ke caller).
 */
class AsyncConfigTest {

  private AsyncConfig asyncConfigWithDefaults() {
    AsyncConfig config = new AsyncConfig();
    ReflectionTestUtils.setField(config, "auditCorePoolSize", 2);
    ReflectionTestUtils.setField(config, "auditMaxPoolSize", 5);
    ReflectionTestUtils.setField(config, "auditQueueCapacity", 1000);
    return config;
  }

  @Test
  void taskExecutorHasHardcodedPoolSizingAndPrefix() {
    Executor executor = asyncConfigWithDefaults().taskExecutor();

    assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
    ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) executor;
    assertThat(pool.getCorePoolSize()).isEqualTo(10);
    assertThat(pool.getMaxPoolSize()).isEqualTo(25);
    assertThat(pool.getThreadNamePrefix()).isEqualTo("AsyncDebtorCheck-");
  }

  @Test
  void auditTaskExecutorUsesConfiguredPoolSizingAndPrefix() {
    Executor executor = asyncConfigWithDefaults().auditTaskExecutor();

    assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
    ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) executor;
    assertThat(pool.getCorePoolSize()).isEqualTo(2);
    assertThat(pool.getMaxPoolSize()).isEqualTo(5);
    assertThat(pool.getThreadNamePrefix()).isEqualTo("AuditTrail-");
  }

  @Test
  void taskExecutorIsAbleToRunTask() {
    Executor executor = asyncConfigWithDefaults().taskExecutor();
    AtomicBoolean ran = new AtomicBoolean(false);

    ((ThreadPoolTaskExecutor) executor).execute(() -> ran.set(true));

    assertThat(ran).isTrue();
    ((ThreadPoolTaskExecutor) executor).shutdown();
  }

  @Test
  void auditRejectionHandlerDoesNotThrowToCaller() {
    AsyncConfig config = asyncConfigWithDefaults();
    // queue capacity 1 supaya mudah penuh
    ReflectionTestUtils.setField(config, "auditQueueCapacity", 1);
    ReflectionTestUtils.setField(config, "auditCorePoolSize", 1);
    ReflectionTestUtils.setField(config, "auditMaxPoolSize", 1);
    ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.auditTaskExecutor();

    AtomicBoolean ran = new AtomicBoolean(false);
    try {
      // banjiri queue (core 1 + queue 1 + max 1) → task berikutnya ditolak
      executor.execute(() -> { try { Thread.sleep(300); } catch (InterruptedException ignored) {} });
      executor.execute(() -> { try { Thread.sleep(300); } catch (InterruptedException ignored) {} });
      executor.execute(() -> { try { Thread.sleep(300); } catch (InterruptedException ignored) {} });
      executor.execute(() -> { try { Thread.sleep(300); } catch (InterruptedException ignored) {} });
      // rejection handler tidak melempar — kalau ini sampai sini tanpa exception, OK
      executor.execute(() -> ran.set(true));
    } catch (RejectedExecutionException e) {
      // handler default melempar — tapi kita set handler custom, jadi ini seharusnya tak terjadi
      throw new AssertionError("Rejection harus ditangani handler custom tanpa melempar", e);
    } finally {
      executor.shutdown();
    }
  }
}
