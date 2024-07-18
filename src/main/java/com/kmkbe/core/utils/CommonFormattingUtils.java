package com.kmkbe.core.utils;

import java.text.DecimalFormat;

public class CommonFormattingUtils {
    public static final DecimalFormat DEFAULT_NUMBER_FORMATTER = new DecimalFormat("###,###,###.0#");

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

    public static String formatAmount(double value) {
        return DEFAULT_NUMBER_FORMATTER.format(value);
    }
}
