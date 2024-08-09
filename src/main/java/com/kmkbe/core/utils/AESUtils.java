package com.kmkbe.core.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class AESUtils {
    public static final String EXAMPLE_ENCRYPT_AES = "Q2gri3deVD5Uam0kFODrYB1KCO8QFhRJXZeWEaoIyWxZU8cXkllqPcU0lyKi3ED/4N-wb/oPL4UKv8/111TEhZYugkRLit-AW0FtnBOiLww=";
    public static final String EXAMPLE_ENCRYPT_SUPER_ADMIN = "iF03HU84UUOVW2kvtD/xrvBdzwfvyhDqfsguFslk7AOYgQIdHCwbHR716Lh4dHimHG1a0/33KNBSZ9kXhGPNCCs92C8rLtZGtSd2hEAXaww=";

    private static final String KEY = "aXty08csul99hjkl";

    public static String decrypt(String encryptedValue) {
        try {
            encryptedValue = CommonFormattingUtils.cleanBase64(encryptedValue);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedValue);
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("decrypt error: {}", e.getMessage());
            return null;
        }
    }

    public static String encrypt(String value) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("encrypt error: {}", e.getMessage());
            return null;
        }
    }
}
