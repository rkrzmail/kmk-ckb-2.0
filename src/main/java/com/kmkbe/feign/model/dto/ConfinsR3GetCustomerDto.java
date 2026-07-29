package com.kmkbe.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfinsR3GetCustomerDto {
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
  private LocalDateTime idExpiredDt;
  @JsonProperty("TaxIdNo")
  private String taxIdNo;
  @JsonProperty("IsVip")
  private Boolean isVip;
  @JsonProperty("VipNotes")
  private String vipNotes;
  @JsonProperty("IsCustomer")
  private Boolean isCustomer;
  @JsonProperty("Flag")
  private String flag;
  @JsonProperty("AO")
  private String ao;
  @JsonProperty("Addr")
  private String addr;
  @JsonProperty("Branch")
  private String branch;
  @JsonProperty("OriOfficeCode")
  private String oriOfficeCode;
  @JsonProperty("IsAffiliateWithMf")
  private Boolean isAffiliateWithMf;
  @JsonProperty("IsGuarantor")
  private Boolean isGuarantor;
  @JsonProperty("IsFamily")
  private Boolean isFamily;
  @JsonProperty("IsShareholder")
  private Boolean isShareholder;
  @JsonProperty("ThirdPartyTrxNo")
  private String ThirdPartyTrxNo;
  @JsonProperty("IsCustGrp")
  private Boolean isCustGrp;
  @JsonProperty("ThirdPartyGroupTrxNo")
  private String thirdPartyGroupTrxNo;
}
