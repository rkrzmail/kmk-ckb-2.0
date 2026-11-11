package com.kmkbe.helpers.utils;


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import static com.kmkbe.helpers.constant.AppConstants.DATE_FORMAT;

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

  public static String nullValue(String value) {
    return value == null ? "" : value;
  }

  public static String toDate(LocalDateTime value) {
    return value == null ? "" : DATE_FORMAT.format(value);
  }

  public static String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  public static String safeReplaceEach(String text, String[] searchList, String[] replacementList) {
    if (text == null || text.isEmpty() || searchList == null || replacementList == null) {
      return text;
    }

    int length = Math.min(searchList.length, replacementList.length);
    String result = text;

    for (int i = 0; i < length; i++) {
      String search = searchList[i];
      String replacement = replacementList[i];
      if (search != null && !search.isEmpty() && replacement != null) {
        result = result.replace(search, replacement);
      }
    }
    return result;
  }

  public static String safeRemoveAll(String text, String[] targets) {
    if (text == null || text.isEmpty() || targets == null) {
      return text;
    }

    String result = text;
    for (String target : targets) {
      if (target != null && !target.isEmpty()) {
        result = result.replace(target, "");
      }
    }
    return result;
  }

  public static String getCleanString(Object obj) {
    if (obj == null) {
      return null;
    }
    String str = obj.toString().trim();
    return str.isEmpty() ? null : str;
  }

}
