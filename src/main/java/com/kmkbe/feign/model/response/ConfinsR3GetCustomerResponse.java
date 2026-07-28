package com.kmkbe.feign.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class ConfinsR3GetCustomerResponse implements Serializable {
  @JsonProperty("CustNo")
  private String custNo;
  @JsonProperty("CustName")
  private String custName;
  @JsonProperty("MrCustTypeCode")
  private String mrCustTypeCode;
  @JsonProperty("IdNo")
  private String idNo; // Using String for IDs to prevent leading zero issues
  @JsonProperty("CustId")
  private Long custId;
  @JsonProperty("Flag")
  private String flag;
  @JsonProperty("AO")
  private String ao; // Account Officer
  @JsonProperty("Addr")
  private String addr;
  @JsonProperty("Branch")
  private String branch;
  @JsonProperty("OriOfficeCode")
  private String oriOfficeCode;
}
