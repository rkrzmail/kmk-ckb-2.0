package com.kmkbe.feign.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.*;

import java.io.Serializable;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ZipCodeResponse extends BaseResponse {
  @JsonProperty("RefZipcodeId")
  private Long refZipcodeId;
  @JsonProperty("AreaCode1")
  private String areaCode1;
  @JsonProperty("AreaCode2")
  private String areaCode2;
  @JsonProperty("City")
  private String city;
  @JsonProperty("Zipcode")
  private String zipcode;
  @JsonProperty("RefProvDistrictId")
  private Integer refProvDistrictId;
  @JsonProperty("ProvDistrictName")
  private String provDistrictName;
  @JsonProperty("SubZipcode")
  private String subZipcode;
  @JsonProperty("PhnArea")
  private String phoneArea;
}
