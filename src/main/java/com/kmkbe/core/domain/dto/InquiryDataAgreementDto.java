package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryDataAgreementDto {
    @JsonProperty("AgrmntObj")
    public AgrmntObj agrmntObj;
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



    public static class AgrmntObj{
        @JsonProperty("CwrNo")
        public String cwrNo;
        @JsonProperty("CurrCode")
        public String currCode;
        @JsonProperty("OfficeCode")
        public String officeCode;
        @JsonProperty("AgrmntNo")
        public String agrmntNo;
        @JsonProperty("CustNo")
        public String custNo;
        @JsonProperty("SpouseNo")
        public String spouseNo;
        @JsonProperty("NumOfAsset")
        public int numOfAsset;
        @JsonProperty("Tenor")
        public int tenor;
        @JsonProperty("PayFreqCode")
        public String payFreqCode;
        @JsonProperty("NumOfInst")
        public int numOfInst;
        @JsonProperty("CumulativeTenor")
        public int cumulativeTenor;
        @JsonProperty("InterestType")
        public String interestType;
        @JsonProperty("InstScheme")
        public String instScheme;
        @JsonProperty("StepUpDownType")
        public String stepUpDownType;
        @JsonProperty("GracePeriodLc")
        public int gracePeriodLc;
        @JsonProperty("MrLcCalcMethodCode")
        public String mrLcCalcMethodCode;
        @JsonProperty("TotalAssetPrice")
        public double totalAssetPrice;
        @JsonProperty("TotalDownPaymentGrossAmt")
        public double totalDownPaymentGrossAmt;
        @JsonProperty("NtfAmt")
        public double ntfAmt;
        @JsonProperty("InsCapitalizedAmt")
        public double insCapitalizedAmt;
        @JsonProperty("OsPrincipalUndueAmt")
        public double osPrincipalUndueAmt;
        @JsonProperty("OsInterestUndueAmt")
        public double osInterestUndueAmt;
        @JsonProperty("OsPrincipalAmt")
        public double osPrincipalAmt;
        @JsonProperty("OsInterestAmt")
        public double osInterestAmt;
        @JsonProperty("TotalIncomeAmt")
        public double totalIncomeAmt;
        @JsonProperty("TdpPaidCoyAmt")
        public double tdpPaidCoyAmt;
        @JsonProperty("DiffRateAmt")
        public double diffRateAmt;
        @JsonProperty("NextInstDueNum")
        public int nextInstDueNum;
        @JsonProperty("NextInstNum")
        public int nextInstNum;
        @JsonProperty("NextInstDueDt")
        public Instant nextInstDueDt;
        @JsonProperty("NextInstDt")
        public Instant nextInstDt;
        @JsonProperty("NextInstAmt")
        public double nextInstAmt;
        @JsonProperty("ContractStatCode")
        public String contractStatCode;
        @JsonProperty("DefaultStatCode")
        public String defaultStatCode;
        @JsonProperty("AgrmntDt")
        public Instant agrmntDt;
        @JsonProperty("GoLiveDt")
        public Instant goLiveDt;
        @JsonProperty("GoLiveBy")
        public String goLiveBy;
        @JsonProperty("PreGoLiveDt")
        public Instant preGoLiveDt;
        @JsonProperty("EffectiveDt")
        public Instant effectiveDt;
        @JsonProperty("MaturityDt")
        public Instant maturityDt;
        @JsonProperty("PrepaidHoldStatCode")
        public String prepaidHoldStatCode;
        @JsonProperty("LcInstRatePrml")
        public double lcInstRatePrml;
        @JsonProperty("LcInsRatePrml")
        public double lcInsRatePrml;
        @JsonProperty("EffectiveRatePrcnt")
        public double effectiveRatePrcnt;
        @JsonProperty("FlatRatePrcnt")
        public double flatRatePrcnt;
        @JsonProperty("SupplEffectiveRatePrcnt")
        public double supplEffectiveRatePrcnt;
        @JsonProperty("SupplFlatRatePrcnt")
        public double supplFlatRatePrcnt;
        @JsonProperty("GracePeriod")
        public int gracePeriod;
        @JsonProperty("GracePeriodType")
        public String gracePeriodType;
        @JsonProperty("MrFirstInstTypeCode")
        public String mrFirstInstTypeCode;
        @JsonProperty("Notes")
        public String notes;
        @JsonProperty("CollectibilityStatId")
        public String collectibilityStatId;
        @JsonProperty("AppNo")
        public String appNo;
        @JsonProperty("ProdOfferingCode")
        public String prodOfferingCode;
        @JsonProperty("ProdOfferingVersion")
        public String prodOfferingVersion;
        @JsonProperty("VirtualAccNo")
        public String virtualAccNo;
        @JsonProperty("MrWopCode")
        public String mrWopCode;
        @JsonProperty("InstAmt")
        public double instAmt;
        @JsonProperty("FloatingNextReviewDt")
        public Object floatingNextReviewDt;
        @JsonProperty("RefProdTypeCode")
        public String refProdTypeCode;
        @JsonProperty("LobCode")
        public String lobCode;
        @JsonProperty("IsCollBlock")
        public boolean isCollBlock;
        @JsonProperty("BaloonValueAmt")
        public double baloonValueAmt;
        @JsonProperty("LcInstAdminFeeAmt")
        public double lcInstAdminFeeAmt;
        @JsonProperty("StandardGrossYieldPrcnt")
        public double standardGrossYieldPrcnt;
        @JsonProperty("GrossYeildPrcnt")
        public double grossYeildPrcnt;
        @JsonProperty("FloatingPeriodCode")
        public Object floatingPeriodCode;
        @JsonProperty("JournalNo")
        public String journalNo;
        @JsonProperty("BizTemplateCode")
        public String bizTemplateCode;
        @JsonProperty("LmsSchmHId")
        public int lmsSchmHId;
        @JsonProperty("LmsSchmCode")
        public Object lmsSchmCode;
        @JsonProperty("InstRounding")
        public double instRounding;
        @JsonProperty("PrepaidAmt")
        public double prepaidAmt;



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


