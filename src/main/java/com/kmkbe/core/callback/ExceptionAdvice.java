package com.kmkbe.core.callback;

import com.kmkbe.core.domain.entity.ErrorLog;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.utils.ExceptionUtils;
import com.kmkbe.core.utils.ObjectUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.PropertyValueException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdvice {
    private final ErrorLogRepository errorLogRepository;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResult<Object>> handle(
            Exception exception,
            WebRequest request
    ) {
        String jsonRequest = "", pageUrl = "";
        if (request != null) {
            try {
                jsonRequest = ObjectUtils.jsonToStr(request.getParameterMap());
                pageUrl = request.getDescription(false);
            } catch (Exception ignored) {

            }
        }

        ErrorLog errorLog = ErrorLog.builder()
                .errorType(exception.getClass().getCanonicalName())
                .errorLine(String.valueOf(exception.getStackTrace()[0].getLineNumber()))
                .errorMsg(exception.getMessage())
                .pageUrl(pageUrl)
                .methodName(exception.getStackTrace()[0].getMethodName())
                .requestParam(jsonRequest)
                .build();

        errorLogRepository.save(errorLog);
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonResult<Object>> handleBadCredentials(
            BadCredentialsException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<CommonResult<Object>> handleAccountStatus(
            AccountStatusException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResult<Object>> handleAccessDenied(
            AccessDeniedException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<CommonResult<Object>> handleSignature(
            SignatureException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CommonResult<Object>> handleExpiredJwt(
            ExpiredJwtException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CommonResult<Object>> handleEntityNotFound(
            EntityNotFoundException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CommonResult<Object>> handleIllegalState(
            IllegalStateException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(PropertyValueException.class)
    public ResponseEntity<CommonResult<Object>> handlePropertyValue(
            PropertyValueException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResult<Object>> handleIllegalArgument(
            IllegalArgumentException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CommonResult<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, List<String>> body = new HashMap<>();

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        body.put("errors", errors);

        CommonResult<Object> result = new CommonResult<>();
        result.setIsSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setMessage("Validation Failed");
        result.setData(body);

        return new ResponseEntity<>(result, null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<CommonResult<Object>> handleHttpServerError(
            HttpServerErrorException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<CommonResult<Object>> handleNoHandlerFound(
            NoHandlerFoundException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<CommonResult<Object>> handleHttpClientError(
            HttpClientErrorException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<CommonResult<Object>> handleMessaging(
            MessagingException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }

    @ExceptionHandler(IllegalApiKeyException.class)
    public ResponseEntity<CommonResult<Object>> handleIllegalApi(
            IllegalApiKeyException exception,
            WebRequest request
    ) {
        return ExceptionUtils.handleException(exception, request);
    }
}
