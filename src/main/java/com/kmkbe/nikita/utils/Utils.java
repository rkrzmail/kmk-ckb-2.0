package com.kmkbe.nikita.utils;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {
    public static long formatNoExponent(double d){
        return BigDecimal.valueOf(d).longValue();
    }
    public static double scale(double value, int scale) {
        BigDecimal decimalValue = BigDecimal.valueOf(value);
        return decimalValue.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
    public static String valueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }
    public static int getIntCurr(String s) {
        s = Utils.replace(s, ".", "");
        s = Utils.replace(s, ",", "");
        return getNumber(s).intValue();
    }
    public static List<String> splitList(String original, String separator) {
        List<String> nodes = new ArrayList<>();
        int index = original.indexOf(separator);
        while (index >= 0) {
            nodes.add(original.substring(0, index));
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }
        nodes.add(original);
        return nodes;
    }
    public static String replace(String _text, String _searchStr, String _replacementStr)     {
        return com.kmkbe.modules.user.utils.Utils.replace(_text, _searchStr, _replacementStr);
    }
    public static int getInt(String s) {
        return getNumber(s).intValue();
    }
    public static long getLong(String s) {
        return getNumber(s).longValue();
    }
    public static double getDouble(Object n) {
        return getNumber(n).doubleValue();
    }
    public static float getFloat(String s) {
        return getNumber(s).floatValue();
    }
    public static Number getNumber(Object n) {
        if (n instanceof Number) {
            return ((Number)n);
        }else if (isDecimalNumber(String.valueOf(n))){
            return Double.valueOf(String.valueOf(n));
        }
        return 0;
    }
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    private static boolean isDecimalNumber(String str) {
        return str.matches("^[-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?$");
    }
    public static boolean isLongIntegerNumber(String str) {
        return str.matches("-?\\d+");
    }


    public static void main(String[] args) {
        System.out.println(formatNoExponent(3.14159E6));
    }
    public static String Now() {
        Calendar calendar = Calendar.getInstance();

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }
    public static String NowDate() {
        Calendar calendar = Calendar.getInstance();

        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    public static LocalDateTime toInstant(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(Utils.formatDate(date)  , formatter) ;
    }

    public static Date fromInstant(LocalDateTime date) {
        return Date.from(date.toInstant( ZoneOffset.UTC)) ;
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
    }
    public static String formatDateView(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date.getTime());
    }
    public static Duration dateDuration(Date date1, Date date2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the input dates
        LocalDateTime startDate = LocalDateTime.parse(Utils.formatDate(date1)  , formatter);
        LocalDateTime endDate = LocalDateTime.parse(Utils.formatDate(date1), formatter);

        // Calculate the duration between the dates
        Duration duration = Duration.between(startDate, endDate);
        return duration;
    }
    public static Duration dateDuration(LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


        // Calculate the duration between the dates
        Duration duration = Duration.between(startDate, endDate);
        return duration;
    }
}
