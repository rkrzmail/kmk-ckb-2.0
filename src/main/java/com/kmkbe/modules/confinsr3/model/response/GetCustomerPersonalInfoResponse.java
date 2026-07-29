package com.kmkbe.modules.confinsr3.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCustomerPersonalInfoResponse extends BaseResponse {
  private Long custPersonalId;
  private Long custId;
  private String fullName;
  private String prefixName;
  private String suffixName;
  private String nickname;
  private String birthPlace;
  private LocalDateTime birthDt;
  private String motherMaidenName;
  private String genderCode;
  private String religionCode;
  private String educationCode;
  private String nationalityCode;
  private String wnaCountryCode;
  private String maritalStatCode;
  private String mobilePhnNo1;
  private String mobilePhnNo2;
  private String email1;
  private String email2;
  private String email3;
  private String familyCardNo;
  private Integer noOfDependents;
  private String noOfResidence;
  private Boolean isRestInPeace;
  private String salutationCode;
}
