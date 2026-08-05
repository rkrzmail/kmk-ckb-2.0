package com.kmkbe.helpers.utils;


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.UUID;

/**
 * @author hyvercode
 * @date 6/29/26
 */
public class CommonUtils {


  // Alphanumeric characters for the key
  private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final SecureRandom random = new SecureRandom();

  public static String generateOtp() {
    SecureRandom random = new SecureRandom();
    int number = random.nextInt(10000);
    return String.format("%04d", number);
  }

  public static String generateUUIDString() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generates a random 16-character string compliant with @Size(max = 16)
   */
  public static String generateAESKeyString() {
    StringBuilder sb = new StringBuilder(16);
    for (int i = 0; i < 16; i++) {
      int index = random.nextInt(ALPHA_NUMERIC.length());
      sb.append(ALPHA_NUMERIC.charAt(index));
    }
    return sb.toString();
  }

  public static String generateDate(String format) {
    return new SimpleDateFormat(format).format(new java.util.Date());
  }
}
