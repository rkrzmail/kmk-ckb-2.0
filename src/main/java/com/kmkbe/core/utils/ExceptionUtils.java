package com.kmkbe.core.utils;

import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.exception.LoanDocMandatoryException;
import com.kmkbe.core.model.CommonResult;
import io.jsonwebtoken.ExpiredJwtException;
import io.netty.util.internal.StringUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PropertyValueException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
public class ExceptionUtils {

    public static ResponseEntity<CommonResult<Object>> handleException(
            Exception exception,
            WebRequest request
    ) {
        ProblemDetail detail = null;

        if (exception instanceof LoanDocMandatoryException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
            //detail.setDetail(exception.getMessage());
        }

        if (exception instanceof BadCredentialsException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
            detail.setProperty("description", "The email or pin is incorrect");
            detail.setDetail(exception.getMessage());
        }

        if (exception instanceof AccountStatusException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            detail.setProperty("description", "The account is inactive");
            detail.setDetail(exception.getMessage());
        }

        if (exception instanceof AccessDeniedException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            detail.setProperty("description", "The account is not permit to access this resource");
            detail.setDetail(exception.getMessage());
        }

        if (exception instanceof SignatureException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            //detail.setProperty("description", "Invalid Credentials");
            detail.setDetail(exception.getMessage());
        }

        if (exception instanceof ExpiredJwtException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            detail.setProperty("description", "Expired Session");
            detail.setDetail(exception.getMessage());
        }

        if (exception instanceof IllegalArgumentException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
            detail.setProperty("description", "Invalid Argument Provide, Try To Complete Field");
            detail.setDetail(exception.getMessage());
        }

        if (exception instanceof IllegalStateException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
            //detail.setDetail(exception.getMessage());
        }

        if (
                exception instanceof EntityNotFoundException
                        || exception instanceof PropertyValueException
        ) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
            //detail.setDetail(exception.getMessage());
        }

        if (exception instanceof NoHandlerFoundException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
            //detail.setDetail(exception.getMessage());
        }

        if (exception instanceof IllegalApiKeyException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
            //detail.setDetail(exception.getMessage());
        }

        if (exception instanceof DataIntegrityViolationException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
            if (exception.getCause() instanceof PropertyValueException) {
                detail.setProperty("description", "Non null value invoke null, property: " + ((PropertyValueException) exception.getCause()).getPropertyName());
                //detail.setDetail(exception.getMessage());
            } else {
                detail.setProperty("description", exception.getMessage());
            }
        }

        if (exception instanceof NoSuchElementException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
        }

        if (exception instanceof HttpMessageNotReadableException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Required request body is missing");
            detail.setProperty("description", "Required request body is missing");
        }

        if (detail == null) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            detail.setProperty("description", exception.getMessage());
        }

        String desc = exception.getMessage();
        if (detail.getProperties() != null && detail.getProperties().get("description") != null) {
            desc = detail.getProperties().get("description").toString();
        }

        CommonResult<Object> result = new CommonResult<>();
        result.setIsSuccess(false);
        result.setCode(detail.getStatus());
        result.setMessage(desc);
        if (!StringUtil.isNullOrEmpty(detail.getDetail())) {
            result.setData(Map.of("details", detail.getDetail()));
        } else {
            //result.setData(Map.of("details", exception.getStackTrace()));
        }


        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(result, null, HttpStatusCode.valueOf(detail.getStatus()));
    }
}
