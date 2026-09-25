package com.kmkbe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${audit.executor.core-pool-size:2}")
    private int auditCorePoolSize;

    @Value("${audit.executor.max-pool-size:5}")
    private int auditMaxPoolSize;

    @Value("${audit.executor.queue-capacity:1000}")
    private int auditQueueCapacity;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(25);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncDebtorCheck-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(auditCorePoolSize);
        executor.setMaxPoolSize(auditMaxPoolSize);
        executor.setQueueCapacity(auditQueueCapacity);
        executor.setThreadNamePrefix("AuditTrail-");
        executor.setRejectedExecutionHandler((task, executorService) -> {
            int queuedTasks = executorService instanceof ThreadPoolExecutor threadPoolExecutor
                    ? threadPoolExecutor.getQueue().size()
                    : -1;
            log.error(
                    "Audit trail task rejected because executor queue is full. activeThreads={}, queuedTasks={}",
                    executor.getActiveCount(),
                    queuedTasks
            );
        });
        executor.initialize();
        return executor;
    }
}
