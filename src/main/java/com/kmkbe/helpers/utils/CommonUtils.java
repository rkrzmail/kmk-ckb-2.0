package com.kmkbe.helpers.utils;


import java.security.SecureRandom;
import java.text.SimpleDateFormat;

/**
 * @author hyvercode
 * @date 6/29/26
 */
public class CommonUtils {
  public static String generateOtp() {
    SecureRandom random = new SecureRandom();
    int number = random.nextInt(10000);
    return String.format("%04d", number);
  }

  public static String generateDate(String format) {
    return new SimpleDateFormat(format).format(new java.util.Date());
  }
}
