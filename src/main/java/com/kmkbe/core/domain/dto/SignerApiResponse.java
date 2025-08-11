package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SignerApiResponse {
    @JsonProperty("ReturnObject")
    private List<SignerData> returnObject;

    @JsonProperty("HeaderObj")
    private RekDebiturResponse.Header header;

    @Data
    public static class SignerData {
        @JsonProperty("SignerName")
        private String signerName;

        @JsonProperty("SignerPosition")
        private String signerPosition;
    }

    @Data
    public static class Header {
        @JsonProperty("StatusCode")
        private String statusCode;
        @JsonProperty("Message")
        private String message;
    }
}

