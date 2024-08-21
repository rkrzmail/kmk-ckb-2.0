package com.kmkbe.core.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <h4>Catch all uncaught exception</h2>
 */
//@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class UncaughtExceptionAdvice {//extends ResponseEntityExceptionHandler
    /*@Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        CommonResult<Object> result = new CommonResult<>();
        result.setIsSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setMessage(ex.getMessage());

        //return super.handleMethodArgumentNotValid(ex, headers, status, request);
        return new ResponseEntity<>(
                result,
                null,
                HttpStatusCode.valueOf(result.getCode())
        );
    }

    @Override
    public ResponseEntity<Object> handleMethodValidationException(
            @NonNull MethodValidationException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request
    ) {
        CommonResult<Object> result = new CommonResult<>();
        result.setIsSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setMessage(ex.getMessage());

        //return super.handleMethodArgumentNotValid(ex, headers, status, request);
        return new ResponseEntity<>(
                result,
                null,
                HttpStatusCode.valueOf(result.getCode())
        );
    }


    @Override
    public ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        CommonResult<Object> result = new CommonResult<>();
        result.setIsSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setMessage(ex.getMessage());

        //return super.handleMethodArgumentNotValid(ex, headers, status, request);
        return new ResponseEntity<>(
                result,
                null,
                HttpStatusCode.valueOf(result.getCode())
        );
    }*/
}
