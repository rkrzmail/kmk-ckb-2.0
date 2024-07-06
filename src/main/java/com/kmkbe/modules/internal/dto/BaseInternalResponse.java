package com.kmkbe.modules.internal.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class BaseInternalResponse<T> {
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
