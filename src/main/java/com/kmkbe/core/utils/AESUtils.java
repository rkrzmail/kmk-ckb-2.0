package com.kmkbe.core.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public final class AESUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_PROPERTY = "security.aes.key";
    private static final String KEY_ENV = "SECURITY_AES_KEY";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private AESUtils() {
        /* This utility class should not be instantiated */
    }

    public static String decrypt(String encryptedValue) {
        try {
            byte[] payload = Base64.getDecoder().decode(CommonFormattingUtils.cleanBase64(encryptedValue));
            byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_LENGTH_BYTES);
            byte[] cipherText = Arrays.copyOfRange(payload, GCM_IV_LENGTH_BYTES, payload.length);
            byte[] decryptedBytes = createCipher(Cipher.DECRYPT_MODE, iv).doFinal(cipherText);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("decrypt error: {}", e.getMessage());
            return null;
        }
    }

    public static String encrypt(String value) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);
            byte[] encryptedBytes = createCipher(Cipher.ENCRYPT_MODE, iv)
                    .doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + encryptedBytes.length)
                    .put(iv)
                    .put(encryptedBytes)
                    .array();

            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            log.error("encrypt error: {}", e.getMessage());
            return null;
        }
    }

    private static Cipher createCipher(int mode, byte[] iv) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        return cipher;
    }

    private static SecretKeySpec secretKey() {
        String key = System.getProperty(KEY_PROPERTY);
        if (key == null || key.isBlank()) {
            key = System.getenv(KEY_ENV);
        }
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("AES key is not configured");
        }

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalStateException("AES key must be 16, 24, or 32 bytes");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
