package com.kmkbe.core.domain.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitPaymentReceiveRequestDto {
    @JsonProperty("PayRcvNo")
    public String payRcvNo;
    @JsonProperty("ReceiptResponse")
    public Object receiptResponse;
    @JsonProperty("IsSuccessPrintReceipt")
    public int isSuccessPrintReceipt;
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

    public  static    class HeaderObj{
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
