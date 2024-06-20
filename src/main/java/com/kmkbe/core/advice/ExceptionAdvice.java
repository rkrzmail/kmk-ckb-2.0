package com.kmkbe.core.advice;

import com.kmkbe.core.config.ResponseProperties;
import com.kmkbe.core.model.SingleResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ExceptionAdvice {
    private final ResponseProperties responseProperties;

    public ExceptionAdvice(ResponseProperties responseProperties) {
        this.responseProperties = responseProperties;
    }

    @ExceptionHandler(RuntimeException.class)
    public Object handle(Exception e) {
        return getResult(e, getExceptionProperties(e, ResponseProperties.ExceptionProperties.UNHANDLED));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFound(Exception e) {
        return getResult(e, getExceptionProperties(e, ResponseProperties.ExceptionProperties.NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleRequestNotSupport(Exception e) {
        return getResult(e, getExceptionProperties(e, ResponseProperties.ExceptionProperties.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameter(Exception e) {
        return getResult(e, getExceptionProperties(e, ResponseProperties.ExceptionProperties.BAD_REQUEST));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidation(Exception e) {
        return getResult(e, getExceptionProperties(e, ResponseProperties.ExceptionProperties.BAD_REQUEST));
    }

    private ResponseProperties.ExceptionProperties getExceptionProperties(Exception e, ResponseProperties.ExceptionProperties unhandled) {
        ResponseProperties.ExceptionProperties exceptionModel = responseProperties
                .getExceptions()
                .values().stream()
                .filter(r -> r.getType().equals(e.getClass()))
                .findFirst()
                .orElse(unhandled);

        exceptionModel.setMessage(exceptionModel.getMessage());
        return exceptionModel;
    }

    private SingleResult<Object> getResult(Exception e, ResponseProperties.ExceptionProperties exceptionProperties) {
        SingleResult<Object> result = new SingleResult<>();
        result.setIsSuccess(false);
        result.setCode(exceptionProperties.getCode());
        result.setMessage(exceptionProperties.getMessage());
        result.setData(e.getMessage());
        return result;
    }
}
