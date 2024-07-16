package com.kmkbe.core.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    public static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    public static final String STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final SimpleDateFormat SDF_STANDARD_DATE = new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat SDF_STANDARD_RESPONSE_DATE = new SimpleDateFormat("dd/MM/yyyy");

    public static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
            .withZone(ZoneId.of("UTC"));

    public static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Jakarta"));

    public static OffsetDateTime now() {
        return toZone(Instant.now(), JAKARTA_ZONE);
    }

    public static Long nowMilliSeconds() {
        return toMilliSeconds(now(), true);
    }

    public static OffsetDateTime toZone(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId).toOffsetDateTime();
    }

    public static OffsetDateTime toStandardDate(OffsetDateTime offsetDateTime) {
        return OffsetDateTime.parse(
                DateTimeFormatter.ofPattern(STANDARD_PATTERN).format(offsetDateTime),
                DateTimeFormatter.ofPattern(STANDARD_PATTERN)
        );
    }

    public static String toStandardDateStr(OffsetDateTime offsetDateTime) {
        return DateTimeFormatter.ofPattern(STANDARD_PATTERN).format(offsetDateTime);
    }

    public static Long toMilliSeconds(OffsetDateTime offsetDateTime, boolean is10Digits) {
        long milliseconds = offsetDateTime.toInstant().toEpochMilli();
        if (is10Digits) {
            return milliseconds / 1000; // Ensure 10 digits
        }

        return milliseconds;
    }
}
