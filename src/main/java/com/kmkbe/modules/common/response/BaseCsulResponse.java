package com.kmkbe.modules.common.response;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Data
public class BaseCsulResponse<T> {
    private CsulHeaderResult header;
    private T data;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class CsulHeaderResult {
        private Long processTime;
        private CsulSuccessResult success;
        private Object errors;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class CsulSuccessResult {
        private Boolean isSuccess;
        private String message;
    }
}
