package com.kmkbe.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class ExecutionTimer {
    private static final Logger log = LoggerFactory.getLogger(ExecutionTimer.class);

    public static <T> T logExecutionTime(String stepName, Callable<T> callable) {
        long start = System.currentTimeMillis();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException("Error on step: " + stepName, e);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} execute time {} ms", stepName, duration);
        }
    }
}
