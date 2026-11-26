package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class BaseSimpleRemoteResponseDto<T> {
    private String status;

    @JsonAlias({"status_code", "statusCode"})
    private Integer statusCode;

    private String message;

    private T data;
}
