package com.kmkbe.core.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


//@Component
@RequiredArgsConstructor
@Slf4j
public class BindingExceptionResolver {//implements HandlerExceptionResolver, Ordered
/*
    private final ObjectMapper objectMapper;

    @Override
    public ModelAndView resolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        try {
            if (ex instanceof org.springframework.validation.BindException) {
                BindException be = (BindException) ex;
                log.error("Binding exception in {} :: ({}) :: ({})=({})", be.getObjectName(), be.getBindingResult().getTarget().getClass(), be.getFieldError().getField(), be.getFieldError().getRejectedValue());
                MappingJackson2JsonView jsonView = new MappingJackson2JsonView(objectMapper);
                jsonView.setExtractValueFromSingleKeyModel(false);

                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setView(jsonView);
                modelAndView.setStatus(HttpStatus.BAD_REQUEST);

                return modelAndView;
            }
        } catch (Exception handlerException) {
            log.error("Could not handle exception", handlerException);
        }

        return null;
    }


    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    private ModelAndView handleException(ObjectError objectError, HttpServletResponse response) {
        if (objectError == null) return null;
        try {
            if (objectError.contains(Exception.class)) {
                Exception ex = objectError.unwrap(Exception.class);
                log.error(ex.getMessage(), ex);
                return handleCustomException(ex, response);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    protected ModelAndView handleCustomException(Exception ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());

        MappingJackson2JsonView jsonView = new MappingJackson2JsonView();
        jsonView.setExtractValueFromSingleKeyModel(false);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(jsonView);
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        return modelAndView;
    }*/

}
