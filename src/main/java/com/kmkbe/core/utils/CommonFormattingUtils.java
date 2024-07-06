package com.kmkbe.core.utils;

public class CommonFormattingUtils {
    public static String cleanBase64(String value) {
        if (value == null) {
            return null;
        }

        if (value.isEmpty()) {
            return value;
        }

        return value
                .replace('-', '+')
                .replace('_', '/');
    }
}
