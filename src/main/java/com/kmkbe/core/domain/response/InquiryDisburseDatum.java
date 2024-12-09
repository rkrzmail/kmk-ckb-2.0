package com.kmkbe.core.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.core.domain.dto.InquiryDataAgreementDtoAgrmntObj;
import com.kmkbe.core.domain.dto.InquiryHeaderObj;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryDisburseDatum {
    @JsonProperty("APNo")
    public String aPNo;
    @JsonProperty("APDescr")
    public String aPDescr;
    @JsonProperty("APDest")
    public String aPDest;
    @JsonProperty("ApAmt")
    public double apAmt;
    @JsonProperty("APDueDt")
    public LocalDateTime aPDueDt;
    @JsonProperty("APTypeCode")
    public String aPTypeCode;
    @JsonProperty("APTypeName")
    public String aPTypeName;
    @JsonProperty("CurrCode")
    public String currCode;
    @JsonProperty("InvoiceDt")
    public Object invoiceDt;
    @JsonProperty("InvoiceNo")
    public String invoiceNo;
    @JsonProperty("ApPaidLocCode")
    public String apPaidLocCode;
    @JsonProperty("ApPaidLocName")
    public String apPaidLocName;
    @JsonProperty("APStatCode")
    public String aPStatCode;
    @JsonProperty("ApStat")
    public String apStat;
    @JsonProperty("RefNo")
    public String refNo;
    @JsonProperty("AgreementNo")
    public String agreementNo;
    @JsonProperty("APAmtInProces")
    public double aPAmtInProces;
    @JsonProperty("APPaidAmt")
    public double aPPaidAmt;
    @JsonProperty("UnpaidAmt")
    public double unpaidAmt;
    @JsonProperty("OfficeCode")
    public String officeCode;
    @JsonProperty("RefAccPayableTypeId")
    public int refAccPayableTypeId;
    @JsonProperty("MrApSourceCode")
    public String mrApSourceCode;
    @JsonProperty("ReferenceNo")
    public String referenceNo;
    @JsonProperty("RefAccPayableTypeCode")
    public String refAccPayableTypeCode;
}


