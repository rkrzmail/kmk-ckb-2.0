package com.kmkbe.modules.remote.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
