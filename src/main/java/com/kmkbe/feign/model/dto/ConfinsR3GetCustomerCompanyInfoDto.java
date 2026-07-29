package com.kmkbe.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

/**
 * POJO model representing customer information based on the provided JSON structure.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfinsR3GetCustomerCompanyInfoDto {
  @JsonProperty("custCompanyId")
  private Long custCompanyId;
  @JsonProperty("custId")
  private Long custId;
  @JsonProperty("mrCompanyTypeCode")
  private String mrCompanyTypeCode;
  @JsonProperty("RefIndustryTypeId")
  private Long refIndustryTypeId;
  @JsonProperty("RegistrationNo")
  private String registrationNo;
  @JsonProperty("LicenseNo")
  private String licenseNo;
  @JsonProperty("NumOfEmp")
  private Integer numOfEmp;
  @JsonProperty("MrInvestmentTypeCode")
  private String mrInvestmentTypeCode;
  @JsonProperty("EstablishmentDt")
  private LocalDateTime establishmentDt;
  @JsonProperty("IsAffiliated")
  private Boolean isAffiliated;
  @JsonProperty("Website")
  private String website;
  @JsonProperty("PhnArea1")
  private String phnArea1;
  @JsonProperty("Phn1")
  private String phn1;
  @JsonProperty("PhnExt1")
  private String phnExt1;
  @JsonProperty("PhnArea2")
  private String phnArea2;
  @JsonProperty("Phn2")
  private String phn2;
  @JsonProperty("PhnExt2")
  private String phnExt2;
  @JsonProperty("Email1")
  private String email1;
  @JsonProperty("Email2")
  private String email2;
}
