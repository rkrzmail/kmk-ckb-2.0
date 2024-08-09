package com.kmkbe.core.callback;

import com.kmkbe.core.model.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * <h4>Catch all uncaught exception</h2>
 */
@RestControllerAdvice
@Order
@Slf4j
public class UncaughtExceptionAdvice {
    @ExceptionHandler
    ResponseEntity<CommonResult<Object>> handleUncaughtException(WebRequest request, RuntimeException e) {
        if (AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class) != null) throw e;
        log.warn("Handling uncaught controller exception for {}", request, e);

        CommonResult<Object> result = new CommonResult<>();
        result.setIsSuccess(false);
        result.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.setMessage(e.getMessage());
        result.setData(e.getCause());

        return new ResponseEntity<>(result, null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
