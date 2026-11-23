package com.kmkbe.core.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class DateTimeUtilsCoverageTest {

  @Test void basics() {
    assertThat(DateTimeUtils.nowLocal()).isNotNull();
    assertThat(DateTimeUtils.now()).isNotNull();
    assertThat(DateTimeUtils.nowDate()).isNotNull();
    assertThat(DateTimeUtils.plusWIB()).isPositive();
    DateTimeUtils.envMode("pro");
    DateTimeUtils.envMode("dev");
    DateTimeUtils.envMode(null);
    assertThatCode(() -> DateTimeUtils.main(new String[0])).doesNotThrowAnyException();
  }

  @Test void conversions() {
    OffsetDateTime odt = OffsetDateTime.of(2026,11,23,10,11,12,0, ZoneOffset.UTC);
    assertThatThrownBy(() -> DateTimeUtils.toStandardDate(odt)).isInstanceOf(Exception.class);
    assertThat(DateTimeUtils.toStandardDateStr(odt)).isEqualTo("2026-11-23 10:11:12");
    assertThat(DateTimeUtils.formatToDate(LocalDateTime.of(2026,11,23,10,11))).startsWith("23/11/2026");
    assertThat(DateTimeUtils.formatToDateTime(LocalDateTime.of(2026,11,23,10,11,12))).isEqualTo("2026-11-23 10:11:12");
    assertThat(DateTimeUtils.cSharpTimeStampToDate(null)).isNull();
    assertThat(DateTimeUtils.cSharpTimeStampToDate("")).isNull();
    assertThat(DateTimeUtils.cSharpTimeStampToDate("2026-11-23T10:11:12")).isNotNull();
    assertThat(DateTimeUtils.cSharpTimeStampToDate("bad")).isNull();
  }

  @Test void parsers() {
    assertThat(DateTimeUtils.formatDateTimeWithNull("2026-11-23 10:11:12")).isNotNull();
    assertThat(DateTimeUtils.formatDateTimeWithNull("2026-11-23")).isNotNull();
    assertThat(DateTimeUtils.formatDateTimeWithNull("23/11/2026 10:11")).isNotNull();
    assertThat(DateTimeUtils.formatDateTimeWithNull("23/11/2026")).isNotNull();
    assertThat(DateTimeUtils.formatDateTimeWithNull("bad")).isNull();
    assertThat(DateTimeUtils.formatDateTime("2026-11-23 10:11:12")).isNotNull();
    assertThat(DateTimeUtils.formatDateTime("2026-11-23")).isNotNull();
    assertThat(DateTimeUtils.formatDateTime("23/11/2026 10:11")).isNotNull();
    assertThat(DateTimeUtils.formatDateTime("23/11/2026")).isNotNull();
    assertThat(DateTimeUtils.formatDateTime("bad")).isNotNull();
    assertThat(DateTimeUtils.getDateOnly(null)).isEmpty();
    assertThat(DateTimeUtils.getDateOnly("12345678901")).hasSize(10);
    assertThat(DateTimeUtils.getDateOnly("123")).isEqualTo("123");
  }

  @Test void numericAndDateDiff() throws Exception {
    assertThat(DateTimeUtils.isNumeric("12")).isTrue();
    assertThat(DateTimeUtils.isNumeric("a")).isFalse();
    assertThat(DateTimeUtils.isLongIntegerNumber("12")).isTrue();
    assertThat(DateTimeUtils.isLongIntegerNumber("1.2")).isFalse();

    Method isDecimal = DateTimeUtils.class.getDeclaredMethod("isDecimalNumber", String.class);
    isDecimal.setAccessible(true);
    assertThat((boolean) isDecimal.invoke(null, "1.2")).isTrue();
    assertThat((boolean) isDecimal.invoke(null, "bad")).isFalse();

    Method s = DateTimeUtils.class.getDeclaredMethod("s", String.class);
    s.setAccessible(true);
    assertThat((long) s.invoke(null, "2026-11-23")).isGreaterThanOrEqualTo(0L);
    assertThat((long) s.invoke(null, "23/11/2026")).isGreaterThanOrEqualTo(0L);
    assertThat((long) s.invoke(null, "2026-11-23T10:11:12")).isGreaterThanOrEqualTo(0L);
    assertThat((long) s.invoke(null, "bad")).isZero();

    Date d1 = Date.from(Instant.parse("2026-11-01T00:00:00Z"));
    Date d2 = Date.from(Instant.parse("2026-11-23T00:00:00Z"));
    assertThat(DateTimeUtils.computeDateDiff(d1, d2)).containsKey(TimeUnit.DAYS);
    assertThat(DateTimeUtils.dateDiffInDay(d1, d2)).isEqualTo(22);
    assertThat(DateTimeUtils.dateDiffInDay(null, d2)).isZero();
    assertThat(DateTimeUtils.setDateZeroTime(new Date()).before(new Date())).isTrue();
  }
}
