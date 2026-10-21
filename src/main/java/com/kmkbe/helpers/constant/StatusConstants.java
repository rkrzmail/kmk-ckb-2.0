package com.kmkbe.helpers.constant;

import java.util.List;

public class StatusConstants {
  private StatusConstants() {
    /* This utility class should not be instantiated */
  }

  public static final String FINANCING_STEP_NEW = "NEW";
  public static final String FINANCING_STEP_ASSIGNMENT = "ASSIGNMENT";
  public static final String FINANCING_STEP_INPROCESS = "INPROCESS";
  public static final String FINANCING_STEP_SIGNING = "SIGNING";
  public static final String FINANCING_STEP_SIGNED = "SIGNED";
  public static final String FINANCING_STEP_GOLIVE = "GOLIVE";
  public static final String FINANCING_STEP_PAID = "PAID";
  public static final List<String> FINANCING_FINAL_STEPS = List.of(FINANCING_STEP_GOLIVE, FINANCING_STEP_PAID);

  public static final String FINANCING_STATUS_NEW = "NEW";
  public static final String FINANCING_STATUS_INPROCESS = "INPROCESS";
  public static final String FINANCING_STATUS_LIVE = "LIVE";
  public static final String FINANCING_STATUS_PAID = "PAID";
  public static final List<String> FINANCING_FINAL_STATUS = List.of(FINANCING_STATUS_LIVE,FINANCING_STATUS_PAID);
}
