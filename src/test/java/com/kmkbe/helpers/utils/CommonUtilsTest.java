package com.kmkbe.helpers.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommonUtilsTest {

  @Test
  void nullValueAndCleanString() {
    assertThat(CommonUtils.nullValue(null)).isEmpty();
    assertThat(CommonUtils.nullValue("x")).isEqualTo("x");

    assertThat(CommonUtils.getCleanString(null)).isNull();
    assertThat(CommonUtils.getCleanString("   ")).isNull();
    assertThat(CommonUtils.getCleanString("  x ")).isEqualTo("x");
    assertThat(CommonUtils.getCleanString(123)).isEqualTo("123");
  }

  @Test
  void toDateUsesIndonesianFormat() {
    assertThat(CommonUtils.toDate(null)).isEmpty();
    assertThat(CommonUtils.toDate(LocalDateTime.of(2026, 1, 5, 0, 0))).isEqualTo("05 Januari 2026");
  }

  @Test
  void capitalizeHandlesEdges() {
    assertThat(CommonUtils.capitalize("abc")).isEqualTo("Abc");
    assertThat(CommonUtils.capitalize("")).isEmpty();
    assertThat(CommonUtils.capitalize(null)).isNull();
  }

  @Test
  void safeRemoveAllAndReplaceEach() {
    assertThat(CommonUtils.safeRemoveAll("JAKARTA 1", new String[]{"1", " "})).isEqualTo("JAKARTA");
    assertThat(CommonUtils.safeRemoveAll(null, new String[]{" "})).isNull();
    assertThat(CommonUtils.safeRemoveAll("abc", null)).isEqualTo("abc");

    assertThat(CommonUtils.safeReplaceEach("a-b-c", new String[]{"-"}, new String[]{"_"})).isEqualTo("a_b_c");
    assertThat(CommonUtils.safeReplaceEach("abc", null, null)).isEqualTo("abc");
  }

  @Test
  void generatesOtpKeyAndUuid() {
    assertThat(CommonUtils.generateOtp()).matches("\\d{4}");
    assertThat(CommonUtils.generateAESKeyString()).hasSize(16);
    assertThat(UUID.fromString(CommonUtils.generateUUIDString())).isNotNull();
  }
}
