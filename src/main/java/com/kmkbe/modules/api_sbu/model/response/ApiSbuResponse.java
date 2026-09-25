package com.kmkbe.modules.api_sbu.model.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kmkbe.helpers.base.BaseRequest;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity mapping tabel public.api_sbu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class ApiSbuResponse extends BaseResponse {

  private Long sesId;
  private UUID bouwheerCode;
  private LocalDateTime expiredDate;
  private String sesStatus;
  private String appPath;
  private String appName;
  private String appKey;
  private String appSecret;
  private String tokenJwt;
  private String usrCrt;
  private LocalDateTime dtmCrt;
  private String usrUpd;
  private LocalDateTime dtmUpd;

}
