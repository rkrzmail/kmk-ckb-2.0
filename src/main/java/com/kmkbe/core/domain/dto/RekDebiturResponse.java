package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RekDebiturResponse {

    @JsonProperty("ReturnObject")
    private List<BankAccount> bankAccounts;

    @JsonProperty("HeaderObj")
    private Header header;

    @Data
    public static class BankAccount {
        @JsonProperty("BankName")
        private String bankName;

        @JsonProperty("AccNo")
        private String accNo;

        @JsonProperty("AccName")
        private String accName;
    }

    @Data
    public static class Header {
        @JsonProperty("StatusCode")
        private String statusCode;
        @JsonProperty("Message")
        private String message;
    }
}