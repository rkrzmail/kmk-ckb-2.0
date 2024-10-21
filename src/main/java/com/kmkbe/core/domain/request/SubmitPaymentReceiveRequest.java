package com.kmkbe.core.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitPaymentReceiveRequest {
    @JsonProperty("RefNo")
    public String refNo;
    @JsonProperty("RcvFrom")
    public String rcvFrom;
    @JsonProperty("ReceiptFormNo")
    public String receiptFormNo;
    @JsonProperty("OfficeBankAccCode")
    public String officeBankAccCode;
    @JsonProperty("ExchangeRateAmt")
    public int exchangeRateAmt;
    @JsonProperty("IsTempReceiptForm")
    public boolean isTempReceiptForm;
    @JsonProperty("WopCode")
    public String wopCode;
    @JsonProperty("ValueDt")
    public String valueDt;
    @JsonProperty("MrPayRecipientCode")
    public String mrPayRecipientCode;
    @JsonProperty("SuspdNo")
    public String suspdNo;
    @JsonProperty("ListPayRcvDApiObj")
    public ArrayList<ListPayRcvDApiObj> listPayRcvDApiObj;
    @JsonProperty("RequestDateTime")
    public String requestDateTime;


    public static class ListPayRcvDApiObj{
        @JsonProperty("AgrmntNo")
        public String agrmntNo;
        @JsonProperty("RcvAmt")
        public double rcvAmt;
        @JsonProperty("IsAutoAlloc")
        public boolean isAutoAlloc;
        @JsonProperty("RcvTrxType")
        public String rcvTrxType;
        @JsonProperty("RefNo")
        public String refNo;
        @JsonProperty("TotalAmtToBePaid")
        public double totalAmtToBePaid;
        @JsonProperty("AllocCurrCode")
        public String allocCurrCode;
        @JsonProperty("PayRcvDAllocAPIList")
        public ArrayList<PayRcvDAllocAPIList> payRcvDAllocAPIList;
        @JsonProperty("ExchangeRateAmt")
        public int exchangeRateAmt;
        @JsonProperty("MrOthTrxCategoryCode")
        public String mrOthTrxCategoryCode;
    }

    public static class PayRcvDAllocAPIList{
        @JsonProperty("RefPaymentAllocCode")
        public String refPaymentAllocCode;
        @JsonProperty("AllocAmt")
        public double allocAmt;
        @JsonProperty("Descr")
        public String descr;
        @JsonProperty("OfficeCode")
        public String officeCode;
        @JsonProperty("BizUnitCode")
        public String bizUnitCode;
    }



}
