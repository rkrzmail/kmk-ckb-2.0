package com.kmkbe.helpers.utils;


import java.security.SecureRandom;

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
}
