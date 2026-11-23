package com.kmkbe.core.utils;

import org.junit.jupiter.api.Test;

import java.math.RoundingMode;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.*;

class FormatingUtilsCoverageTest {

  @Test void formatRpAndAmounts() {
    assertThat(FormatingUtils.newFormatRp("")).isEqualTo("Rp. 0");
    assertThat(FormatingUtils.newFormatRp("1000")).contains("1");
    assertThat(FormatingUtils.newFormatRp("abc")).isEqualTo("abc");
    assertThat(FormatingUtils.newFormatRp(1000)).contains("1");
    assertThat(FormatingUtils.formatRpDecimal("")).isEqualTo("0");
    assertThat(FormatingUtils.formatRpDecimal("12.345")).contains("12");
    assertThat(FormatingUtils.formatRpDecimal("abc")).isEqualTo("abc");
    assertThat(FormatingUtils.formatRp(0)).isEqualTo("0");
    assertThat(FormatingUtils.formatRp(1000)).contains("1");
    assertThat(CommonFormattingUtils.formatAmount(1000)).contains("1");
    assertThat(CommonFormattingUtils.formatAmountWithTwoDecimals(1234.5)).isEqualTo("1,234.50");
  }

  @Test void percentAndClear() {
    assertThat(FormatingUtils.formatPercent(0)).isEqualTo("0.0");
    assertThat(FormatingUtils.formatPercent(12.5)).contains("%");
    assertThat(FormatingUtils.formatPercent2Values(0)).isEqualTo("0.0");
    assertThat(FormatingUtils.formatPercent2Values(12.5)).contains("%");
    assertThat(FormatingUtils.clearPercent(null)).isEqualTo("0");
    assertThat(FormatingUtils.clearPercent(" 12,5% ")).isEqualTo("12.5");
    assertThat(FormatingUtils.clearPercentDouble(null)).isEqualTo(0.0);
    assertThat(FormatingUtils.clearPercentDouble("12,5%")).isEqualTo(12.5);
    assertThat(FormatingUtils.clearPercentDouble("bad")).isEqualTo(0.0);
    assertThat(FormatingUtils.calculatePercentage(10, 100)).isEqualTo(10.0);
    assertThat(FormatingUtils.calculatePercentage(0, 100)).isEqualTo(0.0);
  }

  @Test void numberCleaning() {
    assertThat(FormatingUtils.formatOnlyNumber(null)).isEqualTo("0");
    assertThat(FormatingUtils.formatOnlyNumber("")).isEqualTo("0");
    assertThat(FormatingUtils.formatOnlyNumber("00")).isEqualTo("0");
    assertThat(FormatingUtils.formatOnlyNumber("a1b2")).isEqualTo("12");
    assertThat(FormatingUtils.formatOnlyNumberDouble(null)).isEqualTo("0");
    assertThat(FormatingUtils.formatOnlyNumberDouble("1,5a")).isEqualTo("1.5");
    assertThat(FormatingUtils.clearFormatDouble(null)).isEqualTo(0.0);
    assertThat(FormatingUtils.clearFormatDouble("1,5")).isEqualTo(1.5);
    assertThat(FormatingUtils.clearFormatDouble("bad")).isEqualTo(0.0);
    assertThat(FormatingUtils.formatOnlyNumberInt(null)).isZero();
    assertThat(FormatingUtils.formatOnlyNumberInt("12x")).isEqualTo(12);
    assertThat(FormatingUtils.formatOnlyNumberInt("bad")).isZero();
    assertThat(FormatingUtils.formatOnlyNumberInt(null, 7)).isEqualTo(7);
    assertThat(FormatingUtils.formatOnlyNumberInt("bad", 7)).isEqualTo(7);
  }

  @Test void characterTimeAndPhone() {
    assertThat(FormatingUtils.formatOnlyCharacter("")).isEqualTo("");
    assertThat(FormatingUtils.formatOnlyCharacter("a1b!")).isEqualTo("ab");
    assertThat(FormatingUtils.formatTime(0,0)).isEqualTo("00:00");
    assertThat(FormatingUtils.formatTime(1,2)).isEqualTo("01:02");
    assertThat(FormatingUtils.formatTime(1,0,0)).isEqualTo("00:00:00");
    assertThat(FormatingUtils.formatTime(1,2,3)).isEqualTo("01:02:03");
    assertThat(FormatingUtils.formatPhone("08123456789")).startsWith("62");
    assertThat(FormatingUtils.formatPhone("+628123456789")).contains("628");
    assertThat(FormatingUtils.formatPhone("8123456789")).startsWith("62");
    assertThat(FormatingUtils.formatXponsel("123456")).isEqualTo("3456");
    assertThat(FormatingUtils.formatXponsel("123")).isEqualTo("123");
    assertThat(FormatingUtils.formatNoPonselFromThird(null)).isEmpty();
    assertThat(FormatingUtils.formatNoPonselFromThird("010")).isEqualTo("01 ");
    assertThat(FormatingUtils.formatHideNoPonsel("")).isEmpty();
    assertThat(FormatingUtils.formatHideNoPonsel("123456")).endsWith("3456");
  }

  @Test void roundingAndFormatting() {
    assertThat(FormatingUtils.format2NumberDecimal(0)).isEqualTo(0);
    assertThat(FormatingUtils.format2NumberDecimal(1.23456)).isGreaterThan(0);
    assertThat(FormatingUtils.roundUp(null)).isEqualTo(0.0);
    assertThat(FormatingUtils.roundUp(1.234)).isEqualTo(1.23);
    assertThat(FormatingUtils.roundUpInt(0)).isZero();
    assertThat(FormatingUtils.roundUpInt(123)).isEqualTo(200);
    assertThat(FormatingUtils.roundDoubleToInt(12.34, RoundingMode.HALF_UP)).isEqualTo(12);
    assertThat(FormatingUtils.roundUpDoubleToInt(12.01)).isEqualTo(13);
    assertThat(FormatingUtils.removeZeroDecimal(0)).isEqualTo("0");
    assertThat(FormatingUtils.removeZeroDecimal(12.5)).contains("12");
    assertThat(FormatingUtils.capitalizeWords("")).isEmpty();
    assertThat(FormatingUtils.capitalizeWords("john doe")).isEqualTo("John Doe ");
    assertThat(FormatingUtils.capitalizeFirstWord("")).isEmpty();
    assertThat(FormatingUtils.capitalizeFirstWord("abc")).isEqualTo("Abc");
    assertThat(FormatingUtils.formatLegibleNumber(0)).isEqualTo("0");
    assertThat(FormatingUtils.formatLegibleNumber(500)).isEqualTo("500");
    assertThat(FormatingUtils.formatLegibleNumber(1500)).endsWith("K");
    assertThat(FormatingUtils.formatRb(0)).isEqualTo("0 Rb");
    assertThat(FormatingUtils.formatRb(500)).isEqualTo("500");
    assertThat(FormatingUtils.formatRb(2000)).endsWith("rb");
    assertThat(FormatingUtils.replaceAllWhiteSpaces(null)).isEmpty();
    assertThat(FormatingUtils.replaceAllWhiteSpaces("a b")).isEqualTo("ab");
    assertThat(new FormatingUtils.CurrencyFormatter(1_000_000).getValue()).isNotBlank();
  }

  @Test void kodePenempatan() {
    assertThat(FormatingUtils.kodePenempatan(null,"1","2","3")).isEmpty();
    assertThat(FormatingUtils.kodePenempatan("RAK","1","2","3")).isEqualTo("R.1.2.3");
    assertThat(FormatingUtils.kodePenempatan("OTHER","1","2","3")).isEqualTo("P.1.3");
  }

  @Test void commonFormattingEmailAndBase64() {
    assertThat(CommonFormattingUtils.cleanBase64(null)).isNull();
    assertThat(CommonFormattingUtils.cleanBase64("")).isEmpty();
    assertThat(CommonFormattingUtils.cleanBase64("a-b_c")).isEqualTo("a+b/c");
    assertThat(CommonFormattingUtils.isEmailValid("a@b.com")).isTrue();
    assertThat(CommonFormattingUtils.isEmailValid("bad")).isFalse();
    assertThat(CommonFormattingUtils.isEmailValid(null)).isFalse();
  }

  @Test void executionTimerAndMd5() throws Exception {
    assertThat(ExecutionTimer.logExecutionTime("ok", () -> 1)).isEqualTo(1);
    assertThatThrownBy(() -> ExecutionTimer.logExecutionTime("bad", (Callable<Object>) () -> { throw new Exception("x"); }))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("bad");
    assertThat(MD5Utils.hash("x")).hasSize(32);
  }
}
