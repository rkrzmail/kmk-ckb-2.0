package com.kmkbe.config;

import feign.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test FeignConfig: level logging Feign dari properti
 * (default BASIC — security issue #14; FULL hanya via env non-prod).
 */
class FeignConfigTest {

  private FeignConfig feignConfigWith(String level) {
    FeignConfig config = new FeignConfig();
    ReflectionTestUtils.setField(config, "loggerLevel", level);
    return config;
  }

  @Test
  void defaultPropertyIsBasic() {
    Logger.Level level = feignConfigWith("BASIC").feignLoggerLevel();

    assertThat(level).isEqualTo(Logger.Level.BASIC);
  }

  @ParameterizedTest
  @CsvSource({"BASIC, BASIC", "FULL, FULL", "HEADERS, HEADERS", "NONE, NONE"})
  void mapsPropertyToEnumCaseInsensitive(String configured, Logger.Level expected) {
    Logger.Level level = feignConfigWith(configured.toLowerCase()).feignLoggerLevel();

    assertThat(level).isEqualTo(expected);
  }

  @Test
  void invalidLevelThrowsIllegalArgumentException() {
    FeignConfig config = feignConfigWith("NOT_A_LEVEL");

    assertThatThrownBy(config::feignLoggerLevel)
      .isInstanceOf(IllegalArgumentException.class);
  }
}
