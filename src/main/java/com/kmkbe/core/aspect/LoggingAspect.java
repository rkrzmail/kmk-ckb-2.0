package com.kmkbe.core.aspect;

import com.kmkbe.core.domain.entity.ErrorLog;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {
    private final ErrorLogRepository errorLogRepository;

    @Around("@annotation(com.kmkbe.core.annotation.LogMethod)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = null;
        Throwable ex = null;
        String methodName = joinPoint.getSignature().getName();
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        String methodArgTypes = Arrays.stream(codeSignature.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "));
        String methodArgNames = String.join("", codeSignature.getParameterNames());

        try {
            proceed = joinPoint.proceed();
        } catch (Throwable e) {
            ex = e;
        }

        log.debug(
                "Class {}, Method {} was executed. Args {}",
                joinPoint.getTarget(),
                methodName,
                joinPoint.getArgs()
        );

        if (ex != null) {
            ErrorLog errorLog = ErrorLog.builder()
                    .errorType(methodName)
                    .errorLine(String.valueOf(joinPoint.getSourceLocation().getLine()))
                    .errorMsg(ex.getMessage())
                    .pageUrl(methodName)
                    .methodName(methodName)
                    .requestParam(methodArgTypes + "\n" + methodArgNames)
                    .build();
            errorLogRepository.save(errorLog);
            throw ex;
        }

        return proceed;
    }
}
