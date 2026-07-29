package com.kmkbe.modules.confinsr3.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * POJO model representing customer information based on the provided JSON structure.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCustomerCompanyInfoResponse {
  private Long custCompanyId;
  private Long custId;
  private String mrCompanyTypeCode;
  private Long refIndustryTypeId;
  private String registrationNo;
  private String licenseNo;
  private Integer numOfEmp;
  private String mrInvestmentTypeCode;
  private LocalDateTime establishmentDt;
  private Boolean isAffiliated;
  private String website;
  private String phnArea1;
  private String phn1;
  private String phnExt1;
  private String phnArea2;
  private String phn2;
  private String phnExt2;
  private String email1;
  private String email2;
}
