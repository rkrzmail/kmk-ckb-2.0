package com.kmkbe.helpers.constant;

public class AppConstants {
  private AppConstants() {
  }

  public static final Integer CODE_OK=200;
  public static final String  PROCESS_SUCCESSFULLY="Success";
  public static final Integer CODE_CREATED =201;
  public static final String  PROCESS_CREATED="Created";
  public static final Integer CODE_NOT_FOUND=404;
  public static final String  PROCESS_NOT_FOUND="Not Found";

  public static final String SPECIAL_CHARACTERS = "^[^<>'\"/;`%]*$";
  public static final String PASS_PATTERN_CHARACTERS = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{6,30}$";
  public static final String EMAIL_PATTERN = ".+@.+\\.[a-z]+";
}
