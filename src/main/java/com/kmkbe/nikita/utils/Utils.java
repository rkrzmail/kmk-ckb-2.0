package com.kmkbe.nikita.utils;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Utils {
    public static long formatNoExponent(double d){
        return (long)d;
    }

    public static void main(String[] args) {
        System.out.println(formatNoExponent(3.14159));
    }


    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
    }

    public static Duration dateDuration(Instant date1, Instant date2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the input dates
        LocalDateTime startDate = LocalDateTime.parse(Utils.formatDate(Date.from(date1))  , formatter);
        LocalDateTime endDate = LocalDateTime.parse(Utils.formatDate(Date.from(date2)), formatter);

        // Calculate the duration between the dates
        Duration duration = Duration.between(startDate, endDate);
        return duration;
    }
}
