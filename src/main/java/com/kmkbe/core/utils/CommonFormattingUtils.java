package com.kmkbe.core.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CommonFormattingUtils {
    public static final String REGEX_EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
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

    public static String formatAmountWithTwoDecimals(double value) {
        return new DecimalFormat(
                "#,##0.00",
                DecimalFormatSymbols.getInstance(Locale.US)
        ).format(value);
    }

    public static boolean isEmailValid(String value) {
        try {
            return value.matches(REGEX_EMAIL);
        } catch (Exception e) {
            return false;
        }
    }
}
