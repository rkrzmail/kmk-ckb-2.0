package com.kmkbe.core.utils;

import com.kmkbe.core.model.CommonResult;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;

@Slf4j
public class ExceptionUtils {

    public static ResponseEntity<CommonResult<Object>> handleException(
            Exception exception,
            WebRequest request
    ) {
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

        if (exception instanceof IllegalArgumentException) {
            detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
            detail.setProperty("description", "Invalid Argument Provide, Try To Complete Field");
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

        CommonResult<Object> result = new CommonResult<>();
        result.setIsSuccess(false);
        result.setCode(detail.getStatus());
        result.setMessage(desc);
        result.setData(null);
        //result.setDetails(exception);

        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(result, null, HttpStatusCode.valueOf(detail.getStatus()));
    }
}
