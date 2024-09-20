package com.kmkbe.core.utils;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatingUtils {
    public static final long SERIBU = 1_000;
    public static final long SERATUS_RIBU = 100_000;
    public static final long SEJUTA = 1_000_000;

    public static String newFormatRp(String currency) {
        if (!currency.isEmpty()) {
            try {
                DecimalFormat formatter = new DecimalFormat("###,###,###");
                return "Rp. " + formatter.format(Double.parseDouble(currency));
            } catch (Exception e) {
                return currency;
            }
        }
        return "Rp. " + "0";
    }

    public static String newFormatRp(int currency) {
        try {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            return "Rp. " + formatter.format(Double.parseDouble(String.valueOf(currency)));
        } catch (Exception e) {
            return "Rp. " + "0";
        }
    }

    public static String formatRpDecimal(String currency) {
        if (!currency.equals("")) {
            try {
                DecimalFormat formatter = new DecimalFormat("###,###,###.##");
                return formatter.format(Double.parseDouble(currency));
            } catch (Exception e) {
                return currency;
            }
        }
        return "0";
    }

    public static String formatRp(int values) {
        if (values > 0) {
            try {
                DecimalFormat formatter = new DecimalFormat("###,###,###");
                return formatter.format(values);
            } catch (Exception e) {
                return String.valueOf(values);
            }
        }
        return "0";
    }

    public static String formatPercent(double percentValue) {
        if (percentValue == 0) return "0.0";

        double result = percentValue / 100;
        NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMinimumFractionDigits(1);

        return percentageFormat.format(result);
    }

    public static String formatPercent2Values(double percentValue) {
        if (percentValue == 0) return "0.0";

        double result = percentValue / 100;
        NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMinimumFractionDigits(2);

        return percentageFormat.format(result);
    }

    public static String clearPercent(String value) {
        if (value == null || value.isEmpty())
            return "0";
        else
            return value.trim().replace("%", "").replace(",", ".");
    }


    public static Double clearPercentDouble(String value) {
        try {
            if (value == null || value.isEmpty())
                return 0.0;
            else
                return Double.valueOf(value.trim().replace("%", "").replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }


    public static String formatOnlyNumber(String text) {
        if (text == null || text.equals("") || text.equals("00"))
            return "0";
        else
            return text.replaceAll("[^0-9]+", "");
    }

    public static String formatOnlyNumberDouble(String text) {
        if (text == null || text.equals("") || text.equals("00"))
            return "0";

        text = text.replaceAll(",", ".");
        return text.replaceAll("[^\\d.]", "");
    }

    public static double clearFormatDouble(String text) {
        try {
            if (text == null || text.equals("") || text.equals("00"))
                return 0.0;

            text = text.replaceAll(",", ".");
            return Double.parseDouble(text.replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static int formatOnlyNumberInt(String text) {
        try {
            if (text == null || text.equals(""))
                return 0;
            else
                return Integer.parseInt(text.replaceAll("[^0-9]+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int formatOnlyNumberInt(String text, int defaultValue) {
        try {
            if (text == null || text.equals(""))
                return defaultValue;
            else
                return Integer.parseInt(text.replaceAll("[^0-9]+", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double calculatePercentage(double percent, int value) {
        if (percent > 0 && value > 0) {
            return (percent / 100) * value;
        }
        return 0;
    }

    public static String formatOnlyCharacter(String text) {
        if (text.isEmpty())
            return "";
        else
            return text.replaceAll("[^a-zA-Z]", "");
    }

    public static String formatTime(int hours, int minutes) {
        if (hours == 0 && minutes == 0) return String.format("%02d:%02d", 0, 0);
        else return String.format("%02d:%02d", hours, minutes);
    }

    public static String formatTime(int day, int hour, int minute) {
        if (hour == 0 && minute == 0) return String.format("%02d:%02d:%02d", 0, 0, 0);
        else return String.format("%02d:%02d:%02d", day, hour, minute);
    }

    public static double format2NumberDecimal(double value) {
        if (value == 0) return 0;
        try {
            return Double.parseDouble(new DecimalFormat("##.####").format(value));
        } catch (NumberFormatException ignored) {

        }
        return 0;
    }

    public static String formatPhone(String phone) {
        if (phone.startsWith("+62")) {
            phone = phone.substring(1);
        }
        if (phone.startsWith("0")) {
            phone = "62" + phone.substring(1);
        }
        if (phone.startsWith("8")) {
            phone = "62" + phone;
        }

        phone = formatNoPonselFromThird(formatOnlyNumber(phone));
        return phone.trim();
    }

    public static String formatXponsel(String noPonsel) {
        if (noPonsel.length() > 4) {
            noPonsel = noPonsel.substring(noPonsel.length() - 4);
        }
        return noPonsel;
    }

    //after string was clean from whitespace
    public static String formatNoPonselFromThird(String value) {
        try {
            if (value == null || value.isEmpty()) {
                return "";
            }

            char[] valueArr = value.toCharArray();
            if (valueArr.length >= 2 && valueArr[2] == '0') {
                valueArr[2] = ' ';
            }

            return String.valueOf(valueArr);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String kodePenempatan(String tempat, String no, String tingkat, String folder) {
        String kode;
        if (tempat == null || tempat.isEmpty()) {
            return "";
        }

        if (tempat.equals("RAK")) {
            kode = "R" + "." + no + "." + tingkat + "." + folder;
        } else {
            kode = "P" + "." + no + "." + folder;
        }
        return kode;
    }

    public static Double roundUp(Double value) {
        try {
            if (value == null || value == 0) {
                return 0.0;
            }
            if (String.valueOf(value).contains(",")) {
                value = Double.parseDouble(String.valueOf(value).replaceAll(",", "."));
            }
            final DecimalFormat df = new DecimalFormat("#.##");
            return Double.parseDouble(df.format(value).replaceAll(",", "."));
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
    }


    public static int roundUpInt(int src) {
        try {
            if (src == 0) {
                return 0;
            }

            int len = String.valueOf(src).length() - 1;
            if (len == 0) len = 1;
            int d = (int) Math.pow((double) 10, (double) len);
            return (src + (d - 1)) / d * d;
        } catch (Exception e) {
            e.printStackTrace();
            return src;
        }
    }

    public static Integer roundDoubleToInt(Double value, RoundingMode mode) {
        try {
            BigDecimal precision = new BigDecimal("1.00");
            BigDecimal r = new BigDecimal(value);
            return r.divide(precision, 0, mode).multiply(precision).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return value.intValue();
        }
    }

    public static Integer roundUpDoubleToInt(Double value) {
        try {
            BigDecimal precision = new BigDecimal("1.00");
            BigDecimal r = new BigDecimal(value);
            return r.divide(precision, 0, RoundingMode.UP).multiply(precision).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return value.intValue();
        }
    }


    public static String formatHideNoPonsel(String noPonsel) {
        if (noPonsel.isEmpty()) {
            return "";
        }

        if (noPonsel.length() > 4) {
            noPonsel = noPonsel.substring(noPonsel.length() - 4);
        }

        return "XXXXXXXX" + noPonsel;
    }

    public static String removeZeroDecimal(double value) {
        try {
            if (value == 0.0) {
                return "0";
            }

            DecimalFormat format = new DecimalFormat();
            format.setDecimalSeparatorAlwaysShown(false);

            return format.format(value);
        } catch (Exception e) {
            return "0";
        }
    }

    public static String capitalizeWords(String value) {
        try {
            if (value.isEmpty()) {
                return "";
            }

            value = value.toLowerCase();
            String[] split = value.split(" ");
            if (split.length > 1) {
                StringBuilder strBuffer = new StringBuilder();
                for (String text : split) {
                    char[] c = text.toCharArray();
                    c[0] = Character.toUpperCase(c[0]);
                    text = new String(c);

                    strBuffer.append(text).append(" ");
                }

                return strBuffer.toString();
            } else {
                return capitalizeFirstWord(value);
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String capitalizeFirstWord(String value) {
        try {
            if (value.isEmpty()) return "";

            return Character.toUpperCase(value.toCharArray()[0]) + value.substring(1);
        } catch (Exception e) {
            return "";
        }
    }

    public static String formatLegibleNumber(long value) {
        try {
            if (value == 0) {
                return "0";
            } else if (value < 1000) {
                return String.valueOf(value);
            }

            DecimalFormat format = new DecimalFormat();
            format.setDecimalSeparatorAlwaysShown(false);

            final int raise = (int) (Math.log(value) / Math.log(1_000));
            final String result = format.format(value / Math.pow(1000, raise));

            return String.format(
                    new Locale("in_ID"),
                    "%s%c", result, "KMBTPE".charAt(raise - 1)
            );

        } catch (Exception e) {
            return "0";
        }
    }

    public static String formatRb(long value) {
        try {
            if (value == 0) {
                return "0 Rb";
            } else if (value <= 1_000) {
                return String.valueOf(value);
            }

            final String suffix = "rb";

            if (value >= SEJUTA) {
                return (Math.round(value) / SEJUTA) + suffix;
            } else if (value >= SERATUS_RIBU) {
                return (Math.round(value) / SERATUS_RIBU) + suffix;
            } else {
                return (Math.round(value) / SERIBU) + suffix;
            }

        } catch (Exception e) {
            return "0rb";
        }
    }

    public static String replaceAllWhiteSpaces(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        return value.replaceAll(" ", "");
    }


    @Getter
    public static class CurrencyFormatter {
        private String unit;
        private String value;

        public CurrencyFormatter(double numb) {
            try {
                double dalamJutaan = numb / SEJUTA;
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                value = decimalFormat.format(dalamJutaan);
                unit = "*Dalam jutaan rupiah";
            } catch (Exception e) {
                value = String.valueOf(numb);
                unit = "*Dalam jutaan rupiah";
            }
        }
    }
}
