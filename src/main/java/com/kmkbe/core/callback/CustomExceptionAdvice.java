package com.kmkbe.core.callback;

import com.kmkbe.core.exception.LoanDocMandatoryException;
import com.kmkbe.core.exception.AuthenticationException;
import com.kmkbe.core.model.CommonResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionAdvice {

    @ExceptionHandler(LoanDocMandatoryException.class)
    public ResponseEntity<CommonResult<LoanDocMandatoryException>> handleLoanDocMandatoryException(
            LoanDocMandatoryException exception
    ) {
        return new ResponseEntity<>(
                new CommonResult<LoanDocMandatoryException>().fail(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage()
                ),
                null,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonResult<Object>> handleLoginException(
            AuthenticationException exception
    ) {
        return new ResponseEntity<>(
                new CommonResult<>().fail(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getHeaderMessage(),
                        exception.getPayload()
                ),
                null,
                HttpStatus.BAD_REQUEST
        );
    }
}
