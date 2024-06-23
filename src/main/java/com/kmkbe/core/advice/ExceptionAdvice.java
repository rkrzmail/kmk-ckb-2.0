package com.kmkbe.core.advice;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.core.model.ExceptionResult;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;

@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(Exception.class)
    public CommonResult handleException(Exception exception) {
        ProblemDetail detail = null;
        exception.printStackTrace();

        if (exception instanceof BadCredentialsException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            detail.setProperty("description", "The username or pin is incorrect");
        }

        if (exception instanceof AccountStatusException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            detail.setProperty("description", "The account is inactive");
        }

        if (exception instanceof AccessDeniedException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            detail.setProperty("description", "The account is not permit to access this resource");
        }

        if (exception instanceof SignatureException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            detail.setProperty("description", "Invalid Login Session");
        }

        if (exception instanceof ExpiredJwtException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            detail.setProperty("description", "Expired Session");
        }

        if (
                exception instanceof IllegalStateException
                        || exception instanceof EntityNotFoundException
                        || exception instanceof PropertyValueException
        ) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
        }

        if (exception instanceof NoHandlerFoundException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
        }

        if (detail == null) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            //detail.setProperty("description", "Unknown Internal Server Error");
            detail.setProperty("description", exception.getMessage());
        }

        String desc = exception.getMessage();
        if (detail.getProperties() != null && detail.getProperties().get("description") != null) {
            desc = detail.getProperties().get("description").toString();
        }

        CommonResult result = new CommonResult();
        result.setIsSuccess(false);
        result.setCode(detail.getStatus());
        result.setMessage(desc);
        //result.setDetails(exception);

        return result;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public CommonResult handleBadCredentials(BadCredentialsException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(AccountStatusException.class)
    public CommonResult handleAccountStatus(AccountStatusException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public CommonResult handleAccessDenied(AccessDeniedException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(SignatureException.class)
    public CommonResult handleSignature(SignatureException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public CommonResult handleExpiredJwt(ExpiredJwtException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public CommonResult handleEntityNotFound(EntityNotFoundException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(IllegalStateException.class)
    public CommonResult handleIllegalState(IllegalStateException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(PropertyValueException.class)
    public CommonResult handlePropertyValue(PropertyValueException exception) {
        return handleException(exception);
    }
}
