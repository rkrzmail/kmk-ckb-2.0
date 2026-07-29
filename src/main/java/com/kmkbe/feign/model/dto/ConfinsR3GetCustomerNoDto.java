package com.kmkbe.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfinsR3GetCustomerNoDto {
  @JsonProperty("CustId")
  private Long custId;
  @JsonProperty("CustNo")
  private String custNo;
  @JsonProperty("CustName")
  private String custName;
  @JsonProperty("MrCustTypeCode")
  private String mrCustTypeCode;
  @JsonProperty("MrCustModelCode")
  private String mrCustModelCode;
  @JsonProperty("MrIdTypeCode")
  private String mrIdTypeCode;
  @JsonProperty("IdNo")
  private String idNo;
  @JsonProperty("IdExpiredDt")
  private LocalDate idExpiredDt;
  @JsonProperty("TaxIdNo")
  private String taxIdNo;
  @JsonProperty("IsVip")
  private Boolean isVip;
  @JsonProperty("IsCustomer")
  private Boolean isCustomer;
  @JsonProperty("OriginalOfficeCode")
  private String originalOfficeCode;
  @JsonProperty("IsAffiliateWithMf")
  private Boolean isAffiliateWithMf;
  @JsonProperty("VipNotes")
  private String vipNotes;
  @JsonProperty("IsGuarantor")
  private Boolean isGuarantor;
  @JsonProperty("IsFamily")
  private Boolean isFamily;
  @JsonProperty("IsShareholder")
  private Boolean isShareholder;
  @JsonProperty("ThirdPartyTrxNo")
  private String thirdPartyTrxNo;
  @JsonProperty("IsCustGrp")// Can be null
  private Boolean isCustGrp;
  @JsonProperty("ThirdPartyGroupTrxNo")
  private String thirdPartyGroupTrxNo;
}
