package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class BaseMstRemoteResponseDto<T> {
    @JsonProperty("HeaderObj")
    private Header header;

    @JsonProperty("StatusCode")
    private String statusCode;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("ErrorMessages")
    private Object errorMessages;

    @JsonProperty("RowVersion")
    private Object rowVersion;

    @JsonProperty("Count")
    private Object count;

    @JsonProperty("ReturnObject")
    @JsonAlias("Data")
    private T data;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Header {
        @JsonProperty("ResponseTime")
        private String responseTime;

        @JsonProperty("StatusCode")
        private String statusCode;

        @JsonProperty("Message")
        private String message;

        @JsonProperty("ErrorMessages")
        private Object errorMessages;
    }
}
