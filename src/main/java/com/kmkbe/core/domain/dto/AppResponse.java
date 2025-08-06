package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppResponse {
    @JsonProperty("AppNo")
    private String appNo;

    @JsonProperty("ProdOfferingName")
    private String prodOfferingName;

    @JsonProperty("LobCode")
    private String lobCode;

    @JsonProperty("Tenor")
    private String tenor;

    @JsonProperty("HeaderObj")
    private HeaderObj headerObj;

    @Data
    public static class HeaderObj {
        @JsonProperty("StatusCode")
        private String statusCode;

        @JsonProperty("Message")
        private String message;
    }
}