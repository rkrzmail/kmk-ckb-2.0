package com.kmkbe.modules.confinsr3.model.response;

import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCustomerResponse extends BaseResponse {
  private Long custId;
  private String custNo;
  private String custName;
  private String mrCustTypeCode;
  private String mrCustModelCode;
  private String mrIdTypeCode;
  private String idNo;
  private LocalDate idExpiredDt;
  private String taxIdNo;
  private Boolean isVip;
  private Boolean isCustomer;
  private String originalOfficeCode;
  private Boolean isAffiliateWithMf;
  private String vipNotes;
  private Boolean isGuarantor;
  private Boolean isFamily;
  private Boolean isShareholder;
  private String thirdPartyTrxNo;
  private Boolean isCustGrp;
  private String thirdPartyGroupTrxNo;
}
