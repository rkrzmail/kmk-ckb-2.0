package com.kmkbe.helpers.constant;

public class ErrorConstant {
  private ErrorConstant() {
  }

  public static final String CREATOR = "SYSTEM";

  //Error Code
  public static final Integer ERROR_CODE_404 = 404;
  public static final Integer ERROR_CODE_409 = 409;
  public static final Integer ERROR_CODE_500 = 500;

  public static final Integer ERROR_CODE_80 = 80;
  public static final Integer ERROR_CODE_81 = 81;
  public static final Integer ERROR_CODE_82 = 82;
  public static final Integer ERROR_CODE_83 = 83;

  //Error Message
  public static final String ERROR_MESSAGE_404= "Not Found";
  public static final String ERROR_MESSAGE_409= "Conflict";
  public static final String ERROR_MESSAGE_500= "Internal Server Error";

  public static final String ERROR_MESSAGE_80 = "Error Business Exception";
  public static final String ERROR_MESSAGE_81 = "Record not found";
  public static final String ERROR_MESSAGE_82 = "Captcha is not valid";
  public static final String ERROR_MESSAGE_83 = "Record already exist";
}
