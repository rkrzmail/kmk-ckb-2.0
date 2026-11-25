package com.kmkbe.core.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Slf4j
public final class AESUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String KEY = "aXty08csul99hjkl";

    private AESUtils() {
        /* This utility class should not be instantiated */
    }

    public static final String EXAMPLE_ENCRYPT_AES = "Q2gri3deVD5Uam0kFODrYB1KCO8QFhRJXZeWEaoIyWxZU8cXkllqPcU0lyKi3ED/4N-wb/oPL4UKv8/111TEhZYugkRLit-AW0FtnBOiLww=";
    public static final String EXAMPLE_ENCRYPT_SUPER_ADMIN = "iF03HU84UUOVW2kvtD/xrvBdzwfvyhDqfsguFslk7AOYgQIdHCwbHR716Lh4dHimHG1a0/33KNBSZ9kXhGPNCCs92C8rLtZGtSd2hEAXaww=";

    public static String decrypt(String encryptedValue) {
        try {
            String cleanedValue = CommonFormattingUtils.cleanBase64(encryptedValue);
            byte[] decryptedBytes = createCipher(Cipher.DECRYPT_MODE)
                    .doFinal(Base64.getDecoder().decode(cleanedValue));

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("decrypt error: {}", e.getMessage());
            return null;
        }
    }

    public static String encrypt(String value) {
        try {
            byte[] encryptedBytes = createCipher(Cipher.ENCRYPT_MODE)
                    .doFinal(value.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("encrypt error: {}", e.getMessage());
            return null;
        }
    }

    private static Cipher createCipher(int mode) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM));
        return cipher;
    }
}
