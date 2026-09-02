package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class CustomerCompanyDto {
  private String custCompanyType;
  private String companyModel;
  private String identityType;
  private String identityNo;
  private Date identityIssuedDate;
  private Date identityExpiredDate;
  private String companyAddress;
  private String phone;
  private String ownershipStatus;
  private Date staySince;
  private Double stayLength;
  private String directorName;
  private String directorPhone;

}
