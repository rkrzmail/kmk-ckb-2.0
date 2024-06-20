package com.kmkbe.core.advice;

import com.kmkbe.core.annotation.IgnoreResponseBinding;
import com.kmkbe.core.config.ResponseProperties;
import com.kmkbe.core.model.CommonResult;
import com.kmkbe.core.service.ResponseService;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private final ResponseService responseService;
    private final ResponseProperties responseProperties;

    public ResponseAdvice(ResponseService responseService, ResponseProperties responseProperties) {
        this.responseService = responseService;
        this.responseProperties = responseProperties;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (returnType.getContainingClass().isAnnotationPresent(RestController.class)) {
            if (
                    returnType.getMethod() != null
                            && !returnType.getMethod().isAnnotationPresent(IgnoreResponseBinding.class)
            ) {
                if (body instanceof CommonResult commonResult) {
                    try {
                        HttpStatus status = HttpStatus.valueOf(commonResult.getCode());
                        response.setStatusCode(status);
                    } catch (IllegalArgumentException e) {
                        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    return commonResult;
                }
            }

            CommonResult result = responseService.getResult(body);
            result.setIsSuccess(true);
            result.setCode(responseProperties.getSuccessProperties().getCode());
            result.setMessage(responseProperties.getSuccessProperties().getMessage());

            return result;
        }

        return body;
    }
}
