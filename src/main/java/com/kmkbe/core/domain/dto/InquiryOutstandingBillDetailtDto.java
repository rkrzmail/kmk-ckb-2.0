package com.kmkbe.core.domain.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryOutstandingBillDetailtDto {
    @JsonProperty("VirtualAccount")
    public String virtualAccount;
    @JsonProperty("CustName")
    public String custName;
    @JsonProperty("TotalBilling")
    public double totalBilling;
    @JsonProperty("ListBillingDetail")
    public ArrayList<ListBillingDetail> listBillingDetail;
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


    public class HeaderObj{
        @JsonProperty("ResponseTime")
        public String responseTime;
        @JsonProperty("StatusCode")
        public String statusCode;
        @JsonProperty("Message")
        public String message;
        @JsonProperty("ErrorMessages")
        public Object errorMessages;
    }

    public static class ListBillingDetail{
        @JsonProperty("BillDetailName")
        public String billDetailName;
        @JsonProperty("BillDetailAmt")
        public double billDetailAmt;
    }


}
