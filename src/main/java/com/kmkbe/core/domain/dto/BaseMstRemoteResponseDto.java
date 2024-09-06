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
    public Header header;

    @JsonProperty("StatusCode")
    public String statusCode;

    @JsonProperty("Message")
    public String message;

    @JsonProperty("ErrorMessages")
    public Object errorMessages;

    @JsonProperty("RowVersion")
    public Object rowVersion;

    @JsonProperty("ReturnObject")
    @JsonAlias("Data")
    public T data;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Header {
        @JsonProperty("ResponseTime")
        public String responseTime;

        @JsonProperty("StatusCode")
        public String statusCode;

        @JsonProperty("Message")
        public String message;

        @JsonProperty("ErrorMessages")
        public Object errorMessages;
    }
}
