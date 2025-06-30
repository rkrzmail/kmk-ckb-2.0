package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ExternalApiResponse {
    @JsonProperty("ReturnObject")
    private List<SignerData> returnObject;

    @JsonProperty("HeaderObj")
    private HeaderData headerObj;

    @JsonProperty("StatusCode")
    private String statusCode;

    @JsonProperty("Message")
    private String message;

    @Data
    public static class SignerData {
        @JsonProperty("CwrSignerId")
        private Integer cwrSignerId;

        @JsonProperty("CwrCustId")
        private Integer cwrCustId;

        @JsonProperty("SignerType")
        private String signerType;

        @JsonProperty("SignerName")
        private String signerName;

        @JsonProperty("SignerPosition")
        private String signerPosition;

        // Field lainnya diabaikan
    }

    @Data
    public static class HeaderData {
        @JsonProperty("ResponseTime")
        private String responseTime;

        @JsonProperty("StatusCode")
        private String statusCode;

        @JsonProperty("Message")
        private String message;
    }
}
