package com.kmkbe.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class ExecutionTimer {
    private static final Logger log = LoggerFactory.getLogger(ExecutionTimer.class);

    // Versi dengan return (untuk method yang menghasilkan nilai)
    public static <T> T logExecutionTime(String stepName, Callable<T> callable) {
        long start = System.currentTimeMillis();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException("Error on step: " + stepName, e);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("Step [{}] selesai dalam {} ms", stepName, duration);
        }
    }

    // Versi tanpa return (untuk method void)
    public static void logExecutionTime(String stepName, Runnable runnable) {
        long start = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException("Error on step: " + stepName, e);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("Step [{}] selesai dalam {} ms", stepName, duration);
        }
    }
}
