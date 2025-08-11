package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CwrListBwhrResponse {
    @JsonProperty("ReturnObject")
    private List<CwrListBwhrResponse.CwrListBouwheerCustNo> cwrListBouwheerCustNo;

    @JsonProperty("HeaderObj")
    private RekDebiturResponse.Header header;

    @Data
    public static class CwrListBouwheerCustNo {
        @JsonProperty("CooperationAgreementNo")
        private String CooperationAgreementNo;
    }

    @Data
    public static class Header {
        @JsonProperty("StatusCode")
        private String statusCode;
        @JsonProperty("Message")
        private String message;
    }
}

