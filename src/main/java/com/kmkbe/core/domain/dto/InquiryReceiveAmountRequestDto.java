package com.kmkbe.core.domain.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryReceiveAmountRequestDto {
    @JsonProperty("AllocMapList")
    public ArrayList<AllocMapList> allocMapList;
    @JsonProperty("HeaderObj")
    public HeaderObj headerObj;
    @JsonProperty("StatusCode")
    public String statusCode;
    @JsonProperty("Message")
    public String message;
    @JsonProperty("ErrorMessages")
    public Object errorMessages;
    @JsonProperty("RowVersion")
    public Object rowVersion;

    public static class AllocList{
        @JsonProperty("PaymentAllocCode")
        public String paymentAllocCode;
        @JsonProperty("PaymentAllocName")
        public String paymentAllocName;
        @JsonProperty("OsAmt")
        public double osAmt;
        @JsonProperty("MaxValue")
        public double maxValue;
        @JsonProperty("RcvAmt")
        public double rcvAmt;
        @JsonProperty("Descr")
        public String descr;
    }

    public static class AllocMapList{
        @JsonProperty("AgrmntNo")
        public String agrmntNo;
        @JsonProperty("AllocList")
        public ArrayList<AllocList> allocList;
    }

    public static class HeaderObj{
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
