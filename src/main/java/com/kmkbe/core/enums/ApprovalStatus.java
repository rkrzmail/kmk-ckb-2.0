package com.kmkbe.core.enums;


import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author hyvercode
 * @date 6/25/26
 */
public enum ApprovalStatus {
  OPEN,
  APPROVED,
  REJECTED;


  @JsonCreator
  public static ApprovalStatus fromString(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return ApprovalStatus.valueOf(value.toUpperCase().trim());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
