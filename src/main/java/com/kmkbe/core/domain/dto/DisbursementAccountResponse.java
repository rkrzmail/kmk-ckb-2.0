package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisbursementAccountResponse {
    @JsonProperty("ListDisbInfo")
    private List<DisbInfo> listDisbInfo;

    @JsonProperty("StatusCode")
    private String statusCode;

    @JsonProperty("HeaderObj")
    private Header header;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DisbInfo {
        @JsonProperty("DisbTo")
        private String disbTo;

        @JsonProperty("BankName")
        private String bankName;

        @JsonProperty("AccNo")
        private String accNo;

        @JsonProperty("Nominal")
        private BigDecimal nominal;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("StatusCode")
        private String statusCode;
    }
}
