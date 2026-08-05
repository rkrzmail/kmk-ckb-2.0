package com.kmkbe.modules.bouwheer.model.response;

import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BouwheerResponse extends BaseResponse {
  
  private UUID bouwheerCode;
  private String bouwheerName;
  private String legalAddress;
  private Boolean isActive;
  private String picName;
  private String picEmail;
  private String picMobilePhone;
}
