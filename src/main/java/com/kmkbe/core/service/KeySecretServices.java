package com.kmkbe.core.service;

import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * Legacy key-secret verification untuk bypass token loan-submission
 * (format: {@code base64url(payload) + "." + hex(MD5(payload + "." + secret))}).
 *
 * PERINGATAN KEAMANAN (security issue #12, SECURITY.md):
 * - MD5 di sini dipakai sebagai MAC non-keyed (bukan HMAC) dan secret-nya
 *   hardcoded di yaml/test - kontrak protokol dengan generator token EKSTERNAL.
 * - JANGAN mengubah algoritma tanpa koordinasi dengan pemilik generator token:
   * token yang sudah beredar akan invalid (breaking change).
 * - Migrasi ke HMAC-SHA256 + rotasi secret + enforce expiry dibahas di fase 2
 *   (terkait issue #9) dengan dual-accept window.
 */
@Service
public class KeySecretServices {
    @Value("${security.secret-key}")
    private String secretKey;

    public boolean validate(@NonNull String fullKeyWithSecret) throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update((splitter(fullKeyWithSecret, 0) + "." + secretKey).getBytes());
        byte[] digest = messageDigest.digest();

        String myHash = DatatypeConverter.printHexBinary(digest);
        return myHash.equalsIgnoreCase(splitter(fullKeyWithSecret, 1));
    }

    /**
     * @deprecated Dead code: tidak memiliki pemanggil, dan cek expiry-nya kosong
     * ( {@code if (timeStamps.isAfter(now)) {}} tidak melakukan apa pun ).
     * Jangan dipakai; lihat javadoc class terkait migrasi HMAC-SHA256.
     */
    @Deprecated
    public boolean validateWithTimeStamps(@NonNull String fullKeyWithSecret) throws NoSuchAlgorithmException {
        final String payloadStr = decodePayloadAsString(fullKeyWithSecret);
        final Map<String, Object> payloadObj = ObjectUtils.strToJson(payloadStr);
        if (payloadObj == null) {
            return false;
        }

        if (payloadObj.get("TimeStamps") == null) {
            return false;
        }

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime timeStamps = LocalDateTime.parse(payloadObj.get("TimeStamps").toString(), DateTimeUtils.DTF_DATE_TIME_STANDARD_FORMATTER);

        if (timeStamps.isAfter(now)) {

        }

        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update((splitter(fullKeyWithSecret, 0) + "." + secretKey).getBytes());
        byte[] digest = messageDigest.digest();

        String myHash = DatatypeConverter.printHexBinary(digest);
        return myHash.equalsIgnoreCase(splitter(fullKeyWithSecret, 1));
    }

    public String splitter(@NonNull String fullKeyWithSecret, int index) {
        String[] splitter = fullKeyWithSecret.split("\\.");
        if (splitter.length <= index) {
            return null;
        }

        return splitter[index].trim();
    }

    public String decodePayloadAsString(@NonNull String fullKeyWithSecret) {
        return new String(decodePayload(fullKeyWithSecret), StandardCharsets.UTF_8);
    }

    public byte[] decodePayload(@NonNull String fullKeyWithSecret) {
        return Base64.getUrlDecoder().decode(splitter(fullKeyWithSecret, 0));
    }
}
