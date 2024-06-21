package com.kmkbe.core.advice;

import com.kmkbe.core.annotation.IgnoreResponseBinding;
import com.kmkbe.core.model.CommonResult;
import com.kmkbe.core.service.ResponseService;
import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@AllArgsConstructor
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private final ResponseService responseService;

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
            result.setCode(200);
            result.setMessage("Succesfully");

            return result;
        }

        return body;
    }
}
