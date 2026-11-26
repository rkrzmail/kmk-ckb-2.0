package com.kmkbe.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class BaseLdapRemoteResponseDto<T> {
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
