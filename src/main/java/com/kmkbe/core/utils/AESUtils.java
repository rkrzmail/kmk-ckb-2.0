package com.kmkbe.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitas enkripsi dan dekripsi AES (Issue H4).
 * Mendukung enkripsi modern terautentikasi (AES/GCM/NoPadding) dengan random IV 12 bytes
 * dan tetap menjaga kompatibilitas penuh (backward-compatible) dengan legacy AES/ECB/PKCS5Padding
 * untuk integrasi eksternal (mis. SISCA active directory validation).
 *
 * Kunci AES dapat dikonfigurasi melalui properti 'security.aes.key' / env var 'SECURITY_AES_KEY'
 * dengan fallback default yang aman.
 */
@Slf4j
@Component
public class AESUtils {

    public static final String GCM_PREFIX = "GCM:";
    public static final String DEFAULT_ECB_ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final String GCM_ALGORITHM = "AES/GCM/NoPadding";
    public static final int GCM_IV_LENGTH = 12; // 96-bit IV
    public static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag

    private static final String DEFAULT_FALLBACK_KEY = "aXty08csul99hjkl";
    private static volatile String configuredKey = System.getenv().getOrDefault("SECURITY_AES_KEY", DEFAULT_FALLBACK_KEY);
    private static final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.aes.key:aXty08csul99hjkl}")
    public void setConfiguredKey(String key) {
        if (key != null && !key.trim().isEmpty()) {
            AESUtils.configuredKey = key.trim();
        }
    }

    public static void setKey(String key) {
        if (key != null && !key.trim().isEmpty()) {
            AESUtils.configuredKey = key.trim();
        }
    }

    public static String getKey() {
        return configuredKey;
    }

    /**
     * Mendekripsi ciphertext. Secara otomatis mengenali format modern (prefix 'GCM:')
     * dan fallback ke legacy AES/ECB/PKCS5Padding agar alur integrasi eksisting tetap berjalan.
     *
     * @param encryptedValue string ciphertext (Base64)
     * @return plaintext hasil dekripsi atau null jika gagal
     */
    public static String decrypt(String encryptedValue) {
        return decrypt(encryptedValue, getKey());
    }

    public static String decrypt(String encryptedValue, String secretKey) {
        if (encryptedValue == null || encryptedValue.trim().isEmpty()) {
            return null;
        }

        try {
            if (encryptedValue.startsWith(GCM_PREFIX)) {
                return decryptGcm(encryptedValue, secretKey);
            }
            return decryptEcb(encryptedValue, secretKey);
        } catch (Exception e) {
            log.error("decrypt error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Enkripsi default menggunakan AES/ECB/PKCS5Padding demi menjaga kompatibilitas penuh
     * dengan server eksternal (SISCA LDAP Active Directory Validation).
     *
     * @param value plaintext yang akan dienkripsi
     * @return ciphertext (Base64)
     */
    public static String encrypt(String value) {
        return encrypt(value, getKey());
    }

    public static String encrypt(String value, String secretKey) {
        return encryptEcb(value, secretKey);
    }

    /**
     * Enkripsi modern terautentikasi (AEAD) menggunakan AES/GCM/NoPadding dengan random IV.
     * Hasilnya diberi prefix 'GCM:' sehingga method decrypt dapat mendeteksinya secara otomatis.
     *
     * @param value plaintext
     * @return ciphertext berformat 'GCM:' + Base64(IV + CiphertextWithTag)
     */
    public static String encryptGcm(String value) {
        return encryptGcm(value, getKey());
    }

    public static String encryptGcm(String value, String secretKey) {
        if (value == null) {
            return null;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            SecretKeySpec secretKeySpec = buildKeySpec(secretKey);
            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);

            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return GCM_PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("encryptGcm error: {}", e.getMessage());
            return null;
        }
    }

    public static String decryptGcm(String encryptedValue) {
        return decryptGcm(encryptedValue, getKey());
    }

    public static String decryptGcm(String encryptedValue, String secretKey) {
        if (encryptedValue == null) {
            return null;
        }

        try {
            String payload = encryptedValue;
            if (payload.startsWith(GCM_PREFIX)) {
                payload = payload.substring(GCM_PREFIX.length());
            }
            payload = CommonFormattingUtils.cleanBase64(payload);

            byte[] combined = Base64.getDecoder().decode(payload);
            if (combined.length < GCM_IV_LENGTH) {
                log.error("Invalid GCM payload length");
                return null;
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

            int cipherTextLength = combined.length - GCM_IV_LENGTH;
            byte[] cipherText = new byte[cipherTextLength];
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherTextLength);

            SecretKeySpec secretKeySpec = buildKeySpec(secretKey);
            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(cipherText);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("decryptGcm error: {}", e.getMessage());
            return null;
        }
    }

    public static String encryptEcb(String value, String secretKey) {
        if (value == null) {
            return null;
        }

        try {
            SecretKeySpec secretKeySpec = buildKeySpec(secretKey);
            Cipher cipher = Cipher.getInstance(DEFAULT_ECB_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("encryptEcb error: {}", e.getMessage());
            return null;
        }
    }

    public static String decryptEcb(String encryptedValue, String secretKey) {
        if (encryptedValue == null) {
            return null;
        }

        try {
            String cleaned = CommonFormattingUtils.cleanBase64(encryptedValue);
            byte[] encryptedBytes = Base64.getDecoder().decode(cleaned);
            SecretKeySpec secretKeySpec = buildKeySpec(secretKey);

            Cipher cipher = Cipher.getInstance(DEFAULT_ECB_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("decryptEcb error: {}", e.getMessage());
            return null;
        }
    }

    private static SecretKeySpec buildKeySpec(String secretKey) {
        String key = (secretKey != null && !secretKey.trim().isEmpty()) ? secretKey : DEFAULT_FALLBACK_KEY;
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
