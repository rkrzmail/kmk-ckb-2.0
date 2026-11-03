package com.kmkbe.core.utils;

import jakarta.xml.bind.DatatypeConverter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @deprecated Hash lemah (MD5) - JANGAN dipakai untuk keperluan keamanan.
 * Saat ini hanya dipakai test fixture {@code MD5Test} untuk meng-generate
 * contoh bypass token loan-submission. Verifikasi security issue #12 (SECURITY.md);
 * migrasi HMAC-SHA256 bersama rotasi secret dibahas di fase 2 (terkait issue #9).
 */
@Deprecated
public class MD5Utils {

    /**
     * @deprecated lihat javadoc class.
     */
    @Deprecated
    public static String hash(String value) throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(value.getBytes(StandardCharsets.UTF_8));
        byte[] digest = messageDigest.digest();
        return DatatypeConverter.printHexBinary(digest);
    }
}
