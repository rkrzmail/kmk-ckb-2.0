package com.kmkbe.modules.remote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CustomerRemoteDto {
    @JsonProperty("CustId")
    public Integer custId;

    @JsonProperty("CustNo")
    public String custNo;

    @JsonProperty("CustName")
    public String custName;

    @JsonProperty("MrCustTypeCode")
    public String mrCustTypeCode;

    @JsonProperty("MrCustModelCode")
    public String mrCustModelCode;

    @JsonProperty("MrIdTypeCode")
    public String mrIdTypeCode;

    @JsonProperty("IdNo")
    public String idNo;

    @JsonProperty("IdExpiredDt")
    public String idExpiredDt;

    @JsonProperty("TaxIdNo")
    public String taxIdNo;

    @JsonProperty("IsVip")
    public Boolean isVip;

    @JsonProperty("OriginalOfficeCode")
    public Object originalOfficeCode;

    @JsonProperty("IsAffiliateWithMf")
    public Boolean isAffiliateWithMf;

    @JsonProperty("VipNotes")
    public String vipNotes;

    @JsonProperty("IsCustomer")
    public Boolean isCustomer;

    @JsonProperty("IsGuarantor")
    public Boolean isGuarantor;

    @JsonProperty("IsShareholder")
    public Boolean isShareholder;

    @JsonProperty("IsFamily")
    public Boolean isFamily;

    @JsonProperty("ThirdPartyTrxNo")
    public Object thirdPartyTrxNo;

    @JsonProperty("IsCustGrp")
    public Boolean isCustGrp;
}
