package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAgreementByNoCwrRemoteDto {
    @JsonProperty("CustNo")
    public String custNo;

    @JsonProperty("CustName")
    public String custName;

    @JsonProperty("MrCustTypeCode")
    public String mrCustTypeCode;

    @JsonProperty("CollStatus")
    public Object collStatus;

    @JsonProperty("CollDaysOverDue")
    public Integer collDaysOverDue;

    @JsonProperty("OverdueAmt")
    public Double overdueAmt;

    @JsonProperty("OsAr")
    public Double osAr;

    @JsonProperty("PledgeStat")
    public Object pledgeStat;

    @JsonProperty("AgrmntStat")
    public String agrmntStat;

    @JsonProperty("NtfAmt")
    public Double ntfAmt;

    @JsonProperty("AppId")
    public Integer appId;

    @JsonProperty("AgrmntId")
    public Integer agrmntId;

    @JsonProperty("AppNo")
    public String appNo;

    @JsonProperty("AgrmntNo")
    public String agrmntNo;

    @JsonProperty("AgrmntDate")
    public String agrmntDate;

    @JsonProperty("CurrCode")
    public String currCode;

    @JsonProperty("CurrName")
    public String currName;

    @JsonProperty("InstAmt")
    public Double instAmt;

    @JsonProperty("AgrmntCurrStep")
    public String agrmntCurrStep;

    @JsonProperty("OsPrincipalAmt")
    public Double osPrincipalAmt;

    @JsonProperty("OsInterestAmt")
    public Double osInterestAmt;

    @JsonProperty("CwrVersion")
    public Integer cwrVersion;
}
