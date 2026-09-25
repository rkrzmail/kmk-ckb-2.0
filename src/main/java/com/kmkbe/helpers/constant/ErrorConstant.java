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
  public static final Integer ERROR_CODE_84 = 84;

  //Error Message
  public static final String ERROR_MESSAGE_404= "Tidak ditemukan ";
  public static final String ERROR_MESSAGE_409= "Terjadi kesalahan ";
  public static final String ERROR_MESSAGE_500= "Terjadi error saat proses ";

  public static final String ERROR_MESSAGE_80 = "Error prosess bisnis ";
  public static final String ERROR_MESSAGE_81 = "Data tidak ditemukan ";
  public static final String ERROR_MESSAGE_82 = "Captcha tidak valid ";
  public static final String ERROR_MESSAGE_83 = "Invalid API-KEY ";
  public static final String ERROR_MESSAGE_84 = "Data sudah ada ";

}
