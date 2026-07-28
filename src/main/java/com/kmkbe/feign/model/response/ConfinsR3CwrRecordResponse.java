package com.kmkbe.feign.model.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class ConfinsR3CwrRecordResponse implements Serializable {
  // System/Metadata fields per record
  private Integer rn;
  private String rowVersion; // If this is specific to the record, keep it here.

  // Business Identification Fields
  private String cwrNo;        // Unique reference number
  private String custName;     // Customer Name
  private String custNo;       // Customer Number (Key Identifier)
  private String groupNo;      // Group Number

  // Product/Facility Details
  private String prodOfferingName;
  private String debtorType;
  private String cwrTypeDesc;
  private String facility;
  private Boolean isRevolving;

  // Date and Status Fields
  private LocalDateTime startDt; // Use LocalDateTime for timestamps
  private LocalDateTime endDt;
  private String currStep;      // Current workflow step
  private String lastStep;      // Last recorded step

  private String cwrStat;       // e.g., "NEW", "ACT"
  private String cwrStatDescr;  // Description of the status

  // Financial and Office Details
  private BigDecimal plafondAmt; // Use BigDecimal for precise currency calculations
  private String officeCode;
  private String officeName;

  // Other details
  private Boolean isSuspend;
  private String changeCwrTrxNo;
}

