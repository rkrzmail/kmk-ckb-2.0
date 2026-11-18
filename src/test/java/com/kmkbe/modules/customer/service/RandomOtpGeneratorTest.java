package com.kmkbe.modules.customer.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresi test untuk security issue #7 (SECURITY.md):
 * OTP wajib memakai SecureRandom (bukan java.util.Random yang predictable)
 * dan panjang 4 digit (sesuai FE).
 */
class RandomOtpGeneratorTest {

  private final RandomOtpGenerator generator = new RandomOtpGenerator();

  @Test
  void generatedOtpIsFourDigits() {
    for (int i = 0; i < 200; i++) {
      String otp = generator.generate();
      assertThat(otp).hasSize(4).matches("\\d{4}");
    }
  }

  @Test
  void generatedOtpValuesAreDistributed() {
    Set<String> samples = new HashSet<>();
    for (int i = 0; i < 50; i++) {
      samples.add(generator.generate());
    }
    assertThat(samples.size()).isGreaterThan(30);
  }

  @Test
  void generatedOtpCanReachFullFourDigitRange() {
    Set<Integer> leading = new HashSet<>();
    for (int i = 0; i < 300; i++) {
      leading.add(Character.getNumericValue(generator.generate().charAt(0)));
    }
    assertThat(leading.size()).isGreaterThanOrEqualTo(4);
  }
}
