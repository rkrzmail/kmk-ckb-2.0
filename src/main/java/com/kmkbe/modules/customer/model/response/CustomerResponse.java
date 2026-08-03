package com.kmkbe.modules.customer.model.response;

import com.kmkbe.core.domain.dto.AddressDto;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse extends BaseResponse {
  private UUID custCode;
  private String custNo;
  private String custName;
  private String custTypeCode;
  private String custIdTypeCode;
  private String custIdNo;
  private String custEmail;
  private Boolean isEmailValid;
  private String custMobilePhone;
  private Boolean isPhoneValid;
  private Boolean isWaActive;
  private Boolean agreeTc;
  private Boolean agreeLegalShare;
  private String custExternalCode;
  private Boolean isActive;
  private LocalDateTime dtmCrt;
  private AddressDto address;
  private Boolean forceLogout;
  private String vendorId;
  private UUID bouwheerCode;
  private String bouwheerName;
  private String approvalStatus;
  private String approvalNote;
  private String approvalBy;
  private LocalDateTime approvalAt;
  private String npwp;

}
