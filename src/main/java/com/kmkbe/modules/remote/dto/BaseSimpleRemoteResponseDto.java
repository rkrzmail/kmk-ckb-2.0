package com.kmkbe.modules.remote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class BaseSimpleRemoteResponseDto<T> {
    private String status;

    @JsonProperty("status_code")
    private Integer statusCode;

    private String message;

    private T data;
}
