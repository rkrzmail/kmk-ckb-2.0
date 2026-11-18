package com.kmkbe.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfinsR3GetZipCodeDto extends BaseResponse {
  @JsonAlias("refZipcodeId")
  @JsonProperty("RefZipcodeId")
  private Long refZipcodeId;
  @JsonAlias("kecamatan")
  @JsonProperty("AreaCode1")
  private String areaCode1;
  @JsonAlias("kelurahan")
  @JsonProperty("AreaCode2")
  private String areaCode2;
  @JsonAlias("city")
  @JsonProperty("City")
  private String city;
  @JsonAlias("zipcode")
  @JsonProperty("Zipcode")
  private String zipcode;
  @JsonAlias("refProvDistrictId")
  @JsonProperty("RefProvDistrictId")
  private Integer refProvDistrictId;
  @JsonAlias("provDistrictName")
  @JsonProperty("ProvDistrictName")
  private String provDistrictName;
  @JsonAlias("province")
  @JsonProperty("Province")
  private String province;
  @JsonAlias("subZipcode")
  @JsonProperty("SubZipcode")
  private String subZipcode;
  @JsonAlias("phnArea")
  @JsonProperty("PhnArea")
  private String phoneArea;
}
