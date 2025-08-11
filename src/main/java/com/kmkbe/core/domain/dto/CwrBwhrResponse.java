package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CwrBwhrResponse {
    @JsonProperty("ReturnObject")
    private List<ListCwrBwhr> cwrBouwheerCustNos;

    @JsonProperty("HeaderObj")
    private RekDebiturResponse.Header header;

    @Data
    public static class ListCwrBwhr {
        @JsonProperty("CwrBouwheerCustNo")
        private String CwrBouwheerCustNo;
    }

    @Data
    public static class Header {
        @JsonProperty("StatusCode")
        private String statusCode;
        @JsonProperty("Message")
        private String message;
    }
}

