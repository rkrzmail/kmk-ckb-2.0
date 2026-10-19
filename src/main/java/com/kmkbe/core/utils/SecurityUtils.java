package com.kmkbe.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Utilitas keamanan untuk perbandingan string sensitif (seperti API Key, Shared Secret)
 * menggunakan perbandingan constant-time guna memitigasi serangan timing attack (Issue C3).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Membandingkan dua string secara aman tanpa membocorkan informasi panjang atau isi
     * melalui jeda waktu eksekusi (constant-time comparison).
     *
     * @param a string pertama
     * @param b string kedua
     * @return true jika kedua string tidak null dan identik, false sebaliknya
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] bytesA = a.getBytes(StandardCharsets.UTF_8);
        byte[] bytesB = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(bytesA, bytesB);
    }
}
