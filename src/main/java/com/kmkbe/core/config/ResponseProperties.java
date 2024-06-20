package com.kmkbe.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("spring.response")
public class ResponseProperties {
    private SuccessProperties successProperties = new SuccessProperties();
    private Map<String, ExceptionProperties> exceptions = new HashMap<>();

    @Getter
    @Setter
    public static class SuccessProperties {
        private Integer code = 200;
        private String message = "Successfully";
    }

    @Getter
    @Setter
    public static class ExceptionProperties {
        public static final ExceptionProperties NOT_FOUND = new ExceptionProperties(HttpStatus.NOT_FOUND);
        public static final ExceptionProperties METHOD_NOT_ALLOWED = new ExceptionProperties(HttpStatus.METHOD_NOT_ALLOWED);
        public static final ExceptionProperties BAD_REQUEST = new ExceptionProperties(HttpStatus.BAD_REQUEST);
        public static final ExceptionProperties UNHANDLED = new ExceptionProperties(HttpStatus.NOT_IMPLEMENTED.value(), "Unhandled Exception");

        private Integer code;
        private String message;
        private Class<RuntimeException> type;

        public ExceptionProperties() {
            this(HttpStatus.BAD_REQUEST);
        }

        private ExceptionProperties(HttpStatus httpStatus) {
            this(httpStatus.value(), httpStatus.getReasonPhrase());
        }

        private ExceptionProperties(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
