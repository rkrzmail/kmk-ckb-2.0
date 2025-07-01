package com.kmkbe.core.exception;

import com.kmkbe.core.domain.model.CommonResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "isSuccess", false,
                "code", 400,
                "message", e.getMessage()
        ));
    }
}