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

  public ApprovalStatus safeValueOf(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null; // Or return a default value like ApprovalStatus.DRAFT
    }
    try {
      return ApprovalStatus.valueOf(value.toUpperCase().trim());
    } catch (IllegalArgumentException e) {
      // Log the warning instead of crashing
      System.err.println("Unknown ApprovalStatus: " + value);
      return null;
    }
  }

}
