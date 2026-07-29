package com.kmkbe.modules.confinsr3.model.response;

import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetZipCodeResponse extends BaseResponse {
  private Long refZipcodeId;
  private String areaCode1;
  private String areaCode2;
  private String city;
  private String zipcode;
  private Integer refProvDistrictId;
  private String provDistrictName;
  private String subZipcode;
  private String phoneArea;
}
