package com.kmkbe.modules.confinsr3.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCwrRecordResponse extends BaseResponse {
  private Integer rn;
  private String rowVersion;
  private String cwrNo;
  private String custName;
  private String custNo;
  private String groupNo;
  private String prodOfferingName;
  private String debtorType;
  private String cwrTypeDesc;
  private String facility;
  private Boolean isRevolving;
  private LocalDateTime startDt;
  private LocalDateTime endDt;
  private String currStep;
  private String lastStep;
  private String cwrStat;
  private String cwrStatDescr;
  private BigDecimal plafondAmt;
  private String officeCode;
  private String officeName;
  private Boolean isSuspend;
  private String changeCwrTrxNo;
}

