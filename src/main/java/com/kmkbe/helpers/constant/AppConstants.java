package com.kmkbe.helpers.constant;

public class AppConstants {
  private AppConstants() {
  }

  public static final String CREATOR = "system";
  public static final String LANG_ID = "id-ID";
  public static final String LANG_EN = "en-ID";

  public static final Integer CODE_OK = 200;
  public static final Integer CODE_FAILED = 500;
  public static final Integer CODE_CONFLICT = 409;
  public static final Integer CODE_NOT_FOUND = 404;
  public static final Integer CODE_CREATED =201;

  public static final String PROCESS_SUCCESSFULLY = "Success";
  public static final String  PROCESS_CREATED="Created";
  public static final String PROCESS_FAILED = "Failed";
  public static final String PROCESS_CONFLICT = "Failed";
  public static final String PROCESS_NOT_FOUND = "Not Found";



  public static final String NOT_FOUND_CODE = "404";
  public static final String NOT_FOUND_MSG = "Could not found requested data";

  public static final String SPECIAL_CHARACTERS = "^[^<>'\"/;`%]*$";
  public static final String PASS_PATTERN_CHARACTERS = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{6,30}$";
  public static final String EMAIL_PATTERN = ".+@.+\\.[a-z]+";
  public static final String IMAGE_FORMAT = "png";
  public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
  public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
  public static final String TIME_FORMAT_HHMMSS = "HHmmss";
  public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
  public static final String DATE_FORMAT_YYYYMMDDT_HHMMSSSSSZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

}
