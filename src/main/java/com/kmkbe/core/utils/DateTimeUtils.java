package com.kmkbe.core.utils;

import io.netty.util.internal.StringUtil;
import org.hibernate.query.sqm.TemporalUnit;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    public static LocalDateTime nowLocal() {
        return LocalDateTime.now();
    }
    public static Date nowDate() {
        return new Date();
    }
    public static long plusWIB() {
        return  LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(7));
    }
    public static LocalDateTime now() {
        //return toZone(LocalDateTime.now().plusSeconds( 7*60 * 60), JAKARTA_ZONE);
        return LocalDateTime.now();
    }

    private static int addTimeZoneCount = 0;
    public static void envMode(String env){
        if (env!=null && env.equalsIgnoreCase("pro")){
            addTimeZoneCount = 0;
        }else{
            addTimeZoneCount = 7*60 * 60 ;//7jam
        }
    }

    public static void main(String[] args) {
        System.out.println(nowDate().getTime());
        System.out.println(plusWIB());
    }

/*    public static Long nowMilliSeconds() {
        return toMilliSeconds(now(), true);
    }*/

   /* public static LocalDateTime toZone(LocalDateTime LocalDateTime, ZoneId zoneId) {
        return LocalDateTime.atZone(zoneId).toInstant();
    }*/

    public static OffsetDateTime toStandardDate(OffsetDateTime offsetDateTime) {
        return OffsetDateTime.parse(
                DateTimeFormatter.ofPattern(DATE_TIME_STANDARD_PATTERN).format(offsetDateTime),
                DateTimeFormatter.ofPattern(DATE_TIME_STANDARD_PATTERN)
        );
    }

    public static String toStandardDateStr(OffsetDateTime offsetDateTime) {
        return DateTimeFormatter.ofPattern(DATE_TIME_STANDARD_PATTERN).format(offsetDateTime);
    }

  /*  public static Long toMilliSeconds(LocalDateTime offsetDateTime, boolean is10Digits) {
        long milliseconds = offsetDateTime.toEpochMilli();
        if (is10Digits) {
            return milliseconds / 1000; // Ensure 10 digits
        }

        return milliseconds;
    }*/

    public static String formatToDate(LocalDateTime LocalDateTime) {
        return DTF_DATE_RESPONSE_STANDARD_FORMATTER.format(LocalDateTime);
    }

    public static Date cSharpTimeStampToDate(String v) {
        try {
            if (StringUtil.isNullOrEmpty(v)) {
                return null;
            }

            final String[] split = v
                    .replace("T", " ")
                    .trim()
                    .split(" ");

            if (split.length > 0) {
                return SDF_STANDARD_DATE.parse(split[0]);
            }

            return SDF_STANDARD_DATE.parse(v.replace("T", " ").trim());
        } catch (Exception e) {
            return null;
        }
    }
    private static long s(String date) {
        try {
            //dd/mm/yyyy|dd-mm-yyyy|yyyy-mm-dd
            String sd ="-";String time = "";
            if (date.contains(".")) {
                date=date.substring(0,date.indexOf("."));
            }
            if (date.contains(":")&& date.length()>=18) {
                time = " HH:mm:ss";
            }
            if (date.contains("-")) {
                sd = "-";
            }else if (date.contains("/")) {
                sd = "/";
            }
            if (date.length()>=10) {
                if (isNumeric(date.substring(0,4))) {
                    //yyyy-mm-dd
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy"+sd+"MM"+sd+"dd"+time);
                    return simpleDateFormat.parse(date).getTime() ;
                }else if (isNumeric(date.substring(6,10))) {
                    //dd/mm/yyyy
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd"+sd+"MM"+sd+"yyyy"+time);
                    return simpleDateFormat.parse(date).getTime() ;
                }
            }else{
                //???
            }
        } catch (Exception e) { }
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
    public static LocalDateTime formatDateTimeWithNull(String strDate) {
        try {
            return LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern(DATE_TIME_STANDARD_PATTERN));
        }catch (Exception e) {}
        try {
            return LocalDate.parse(strDate, DateTimeFormatter.ofPattern(DATE_STANDARD_PATTERN)).atStartOfDay();
        }catch (Exception e) {}
        try {
            return LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern(DATE_TIME_RESPONSE_STANDARD_PATTERN));
        }catch (Exception e) {}
        try {
            return LocalDate.parse(strDate, DateTimeFormatter.ofPattern(DATE_RESPONSE_STANDARD_PATTERN)).atStartOfDay();
        }catch (Exception e) {}


        return null;//kalo format slaha
    }
    public static LocalDateTime formatDateTime(String strDate) {
        try {
            return LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern(DATE_TIME_STANDARD_PATTERN));
        }catch (Exception e) {}
        try {
            return LocalDate.parse(strDate, DateTimeFormatter.ofPattern(DATE_STANDARD_PATTERN)).atStartOfDay();
        }catch (Exception e) {}
        try {
            return LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern(DATE_TIME_RESPONSE_STANDARD_PATTERN));
        }catch (Exception e) {}
        try {
            return LocalDate.parse(strDate, DateTimeFormatter.ofPattern(DATE_RESPONSE_STANDARD_PATTERN)).atStartOfDay();
        }catch (Exception e) {}
        return LocalDateTime.now();//kalo format slaha
    }
    public static String formatToDateTime(LocalDateTime LocalDateTime) {
        return DTF_DATE_TIME_STANDARD_FORMATTER.format(LocalDateTime);
    }

/*    static LocalDateTime getInstantFromMicros(long microsSinceEpoch) {
        return LocalDateTime.ofEpochSecond(
                TimeUnit.MICROSECONDS.toSeconds(microsSinceEpoch),
                TimeUnit.MICROSECONDS.toNanos(
                        Math.floorMod(microsSinceEpoch, TimeUnit.SECONDS.toMicros(1))
                )
        );
    }*/

   /* static LocalDateTime getInstantFromNanos(long nanosSinceEpoch) {
        return LocalDateTime.ofEpochSecond(0L, nanosSinceEpoch);
    }*/

    public static Date setDateZeroTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    public static String getDateOnly(String str){
        if (str == null){
            return "";
        }
        if (str.length()>=10){
            return str.substring(0,10);
        }else{
            return str;
        }
    }

    public static Map<TimeUnit, Long> computeDateDiff(Date date1, Date date2) {
        try {
            long diffInMillis = date2.getTime() - date1.getTime();

            //create the list
            List<TimeUnit> units = new ArrayList<>(EnumSet.allOf(TimeUnit.class));
            Collections.reverse(units);

            //create the result map of TimeUnit and difference
            Map<TimeUnit, Long> result = new LinkedHashMap<TimeUnit, Long>();
            long milliesRest = diffInMillis;

            for (TimeUnit unit : units) {

                //calculate difference in millisecond
                long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
                long diffInMilliesForUnit = unit.toMillis(diff);
                milliesRest = milliesRest - diffInMilliesForUnit;

                //put the result in the map
                result.put(unit, diff);
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static int dateDiffInDay(Date date1, Date date2) {
        return (int) ((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24)) + 2;
    }
}
