package com.kmkbe.feign.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class ConfinsR3CwrRecordResponse implements Serializable {
  // System/Metadata fields per record
  @JsonProperty("rn")
  private Integer rn;
  @JsonProperty("RowVersion")
  private String rowVersion; // If this is specific to the record, keep it here.

  // Business Identification Fields
  @JsonProperty("CwrNo")
  private String cwrNo;
  @JsonProperty("CustName")// Unique reference number
  private String custName;
  @JsonProperty("CustNo")// Customer Name
  private String custNo;       // Customer Number (Key Identifier)
  @JsonProperty("GroupNo")
  private String groupNo;      // Group Number

  // Product/Facility Details
  @JsonProperty("ProdOfferingName")
  private String prodOfferingName;
  @JsonProperty("DebtorType")
  private String debtorType;
  @JsonProperty("CwrTypeDesc")
  private String cwrTypeDesc;
  @JsonProperty("Facility")
  private String facility;
  @JsonProperty("IsRevolving")
  private Boolean isRevolving;

  // Date and Status Fields
  @JsonProperty("StartDt")
  private LocalDateTime startDt; // Use LocalDateTime for timestamps
  @JsonProperty("EndDt")
  private LocalDateTime endDt;

  @JsonProperty("CurrStep")
  private String currStep;      // Current workflow step

  @JsonProperty("LastStep")
  private String lastStep;      // Last recorded step

  @JsonProperty("CwrStat")
  private String cwrStat;
  @JsonProperty("CwrStatDescr")
  private String cwrStatDescr;  // Description of the status

  // Financial and Office Details
  @JsonProperty("PlafondAmt")
  private BigDecimal plafondAmt; // Use BigDecimal for precise currency calculations
  @JsonProperty("OfficeCode")
  private String officeCode;
  @JsonProperty("OfficeName")
  private String officeName;

  // Other details
  @JsonProperty("IsSuspend")
  private Boolean isSuspend;
  @JsonProperty("ChangeCwrTrxNo")
  private String changeCwrTrxNo;
}

