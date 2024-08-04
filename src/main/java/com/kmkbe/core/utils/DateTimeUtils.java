package com.kmkbe.core.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {
    public static final String DATE_TIME_STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_STANDARD_PATTERN = "yyyy-MM-dd";
    public static final String DATE_RESPONSE_STANDARD_PATTERN = "dd/MM/yyyy";
    public static final String DATE_TIME_RESPONSE_STANDARD_PATTERN = "dd/MM/yyyy HH:mm";

    public static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    public static final SimpleDateFormat SDF_STANDARD_DATE_TIME = new SimpleDateFormat(DATE_TIME_STANDARD_PATTERN);

    public static final SimpleDateFormat SDF_STANDARD_DATE = new SimpleDateFormat(DATE_STANDARD_PATTERN);

    public static final SimpleDateFormat SDF_STANDARD_RESPONSE_DATE = new SimpleDateFormat(DATE_RESPONSE_STANDARD_PATTERN);

    public static final SimpleDateFormat SDF_STANDARD_RESPONSE_DATE_TIME = new SimpleDateFormat(DATE_TIME_RESPONSE_STANDARD_PATTERN);

    public static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
            .withZone(ZoneId.of("UTC"));

    public static final DateTimeFormatter DTF_DATE_TIME_STANDARD_FORMATTER = DateTimeFormatter
            .ofPattern(DATE_TIME_STANDARD_PATTERN)
            .withZone(ZoneId.of("Asia/Jakarta"));

    public static final DateTimeFormatter DTF_DATE_RESPONSE_STANDARD_FORMATTER = DateTimeFormatter
            .ofPattern(DATE_RESPONSE_STANDARD_PATTERN)
            .withZone(ZoneId.of("Asia/Jakarta"));

    public static Instant now() {
        return toZone(Instant.now(), JAKARTA_ZONE);
    }

    public static Long nowMilliSeconds() {
        return toMilliSeconds(now(), true);
    }

    public static Instant toZone(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId).toInstant();
    }

    public static OffsetDateTime toStandardDate(OffsetDateTime offsetDateTime) {
        return OffsetDateTime.parse(
                DateTimeFormatter.ofPattern(DATE_TIME_STANDARD_PATTERN).format(offsetDateTime),
                DateTimeFormatter.ofPattern(DATE_TIME_STANDARD_PATTERN)
        );
    }

    public static String toStandardDateStr(OffsetDateTime offsetDateTime) {
        return DateTimeFormatter.ofPattern(DATE_TIME_STANDARD_PATTERN).format(offsetDateTime);
    }

    public static Long toMilliSeconds(Instant offsetDateTime, boolean is10Digits) {
        long milliseconds = offsetDateTime.toEpochMilli();
        if (is10Digits) {
            return milliseconds / 1000; // Ensure 10 digits
        }

        return milliseconds;
    }

    public static String formatToDate(Instant instant) {
        return DTF_DATE_RESPONSE_STANDARD_FORMATTER.format(instant);
    }

    public static String formatToDateTime(Instant instant) {
        return DTF_DATE_TIME_STANDARD_FORMATTER.format(instant);
    }

    static Instant getInstantFromMicros(long microsSinceEpoch) {
        return Instant.ofEpochSecond(
                TimeUnit.MICROSECONDS.toSeconds(microsSinceEpoch),
                TimeUnit.MICROSECONDS.toNanos(
                        Math.floorMod(microsSinceEpoch, TimeUnit.SECONDS.toMicros(1))
                )
        );
    }

    static Instant getInstantFromNanos(long nanosSinceEpoch) {
        return Instant.ofEpochSecond(0L, nanosSinceEpoch);
    }
}
