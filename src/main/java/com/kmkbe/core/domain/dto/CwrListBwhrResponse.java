package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CwrListBwhrResponse {
    @JsonProperty("ReturnObject")
    private List<ListData> cwrListBouwheerCustNo;

    @JsonProperty("HeaderObj")
    private RekDebiturResponse.Header header;

    public CwrListBwhrResponse(List<ListData> cwrListBouwheerCustNo) {
        this.cwrListBouwheerCustNo = cwrListBouwheerCustNo;
    }

    @Data
    public static class ListData {
        @JsonProperty("CooperationAgreementNo")
        private String CooperationAgreementNo;

        @JsonProperty("StartPeriod")
        private String StartPeriod;


    }

    @Data
    public static class Header {
        @JsonProperty("StatusCode")
        private String statusCode;
        @JsonProperty("Message")
        private String message;
    }
}

