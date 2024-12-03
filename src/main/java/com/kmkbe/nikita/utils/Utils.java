package com.kmkbe.nikita.utils;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static long formatNoExponent(double d){
        return BigDecimal.valueOf(d).longValue();
    }

    public static void main(String[] args) {
        System.out.println(formatNoExponent(3.14159E6));
    }
    public static String Now() {
        Calendar calendar = Calendar.getInstance();

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
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
