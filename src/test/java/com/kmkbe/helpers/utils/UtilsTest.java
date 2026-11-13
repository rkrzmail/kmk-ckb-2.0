package com.kmkbe.helpers.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

  @Test
  void numericConversions() {
    assertThat(Utils.formatNoExponent(3.14159E6)).isEqualTo(3141590L);
    assertThat(Utils.scale(2.345, 2)).isEqualTo(2.35);
    assertThat(Utils.scale(1.0, 0)).isEqualTo(1.0);
    assertThat(Utils.getIntCurr("1.234.567")).isEqualTo(1234567);
    assertThat(Utils.getIntCurr("1,234")).isEqualTo(1234);
    assertThat(Utils.getInt("42")).isEqualTo(42);
    assertThat(Utils.getLong("42")).isEqualTo(42L);
    assertThat(Utils.getDouble("12.5")).isEqualTo(12.5);
    assertThat(Utils.getFloat("2.5")).isEqualTo(2.5f);
  }

  @Test
  void getNumberFallsBackToZero() {
    assertThat(Utils.getNumber(5)).isEqualTo(5);
    assertThat(Utils.getNumber("12.5")).isEqualTo(12.5);
    assertThat(Utils.getNumber("abc")).isEqualTo(0);
  }

  @Test
  void numericPredicates() {
    assertThat(Utils.isNumeric("123")).isTrue();
    assertThat(Utils.isNumeric("12.5")).isTrue();
    assertThat(Utils.isNumeric("-7")).isTrue();
    assertThat(Utils.isNumeric("abc")).isFalse();
    assertThat(Utils.isLongIntegerNumber("123")).isTrue();
    assertThat(Utils.isLongIntegerNumber("12.5")).isFalse();
  }

  @Test
  void valueOfAndReplaceAndSplit() {
    assertThat(Utils.valueOf(null)).isEmpty();
    assertThat(Utils.valueOf(42)).isEqualTo("42");
    assertThat(Utils.replace("a-b-c", "-", "")).isEqualTo("abc");
    assertThat(Utils.splitList("a;b;c", ";")).containsExactly("a", "b", "c");
  }

  @Test
  void dateFormattingAndDuration() {
    LocalDateTime ldt = LocalDateTime.of(2026, 5, 12, 14, 30, 0);
    Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    assertThat(Utils.toInstant(date)).isEqualTo(ldt);
    assertThat(Utils.formatDateView(date)).isEqualTo("12/05/2026");

    assertThat(Utils.dateDuration(
      LocalDateTime.of(2026, 1, 1, 0, 0),
      LocalDateTime.of(2026, 1, 2, 0, 0)).toHours()).isEqualTo(24);

    assertThat(Utils.Now()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    assertThat(Utils.NowDate()).matches("\\d{4}-\\d{2}-\\d{2}");
  }

  @Test
  void fromInstantProducesFormattedDate() {
    Date date = Utils.fromInstant(LocalDateTime.of(2026, 5, 12, 14, 30, 0));
    assertThat(Utils.formatDate(date)).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
  }
}
