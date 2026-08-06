package com.kmkbe.modules.bouwheer.model.response;

import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
  private String rt;
  private String rw;
  private String kelurahan;
  private String kecamatan;
  private String city;
  private String province;
  private String zipcode;
  private String area;
  private String phone;
  private Boolean isSbu;
  private Boolean isWaActive;
  private Long termOfPayment;
  private Long gracePeriod;
  private String usrCrt;
  private LocalDateTime dtmCrt;
  private String usrUpd;
  private LocalDateTime dtmUpd;
}
