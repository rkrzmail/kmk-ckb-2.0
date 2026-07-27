package com.kmkbe.modules.bouwheer.model.response;

import com.kmkbe.helpers.base.BaseRequest;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BouwheerResponse extends BaseResponse {
  
  private UUID bouwheerCode;
  private String bouwheerName;

  // --- Address Details ---
  private String legalAddress;
  private String rt;
  private String rw;
  private String kelurahan;
  private String kecamatan;
  private String city;
  private String province;
  private String zipcode;

  // --- Contact Details ---
  private String area;
  private String phone;
  private Boolean isSbu;

  // --- PIC Details ---
  private String picName;
  private String picEmail;
  private String picMobilePhone;
  private Boolean isWaActive;

  // --- API & Payment Details (The core business logic) ---
  private Long termOfPayment;
  private Long gracePeriod;
  private String aesKey;
  private String secretKey;
  private String apiKey;

  // Status/Activation flags the client can set upon creation/update.
  private Boolean isActive;
}
