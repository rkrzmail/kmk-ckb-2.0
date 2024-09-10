package com.kmkbe.core.callback;

import com.kmkbe.core.domain.entity.ErrorLog;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <h4>Catch all uncaught exception</h2>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final ErrorLogRepository errorLogRepository;

    @PostConstruct
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("UNCAUGHTEXCEPTION " + e.getMessage());
        log.error("uncaughtException: error {}", e.getMessage());

        ErrorLog errorLog = ErrorLog.builder()
                .errorType(e.getClass().getCanonicalName())
                .errorLine(String.valueOf(e.getStackTrace()[0].getLineNumber()))
                .errorMsg(e.getMessage())
                .pageUrl("UNCAUGHTEXCEPTION")
                .methodName(e.getStackTrace()[0].getMethodName())
                .requestParam("")
                .build();

        errorLogRepository.save(errorLog);
    }
}
