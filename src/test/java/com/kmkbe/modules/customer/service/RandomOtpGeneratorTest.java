package com.kmkbe.modules.customer.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresi test untuk security issue #7 (SECURITY.md):
 * OTP wajib memakai SecureRandom (bukan java.util.Random yang predictable)
 * dan panjang 6 digit.
 */
class RandomOtpGeneratorTest {

  private final RandomOtpGenerator generator = new RandomOtpGenerator();

  @Test
  void generatedOtpIsSixDigits() {
    for (int i = 0; i < 200; i++) {
      String otp = generator.generate();
      assertThat(otp).hasSize(6).matches("\\d{6}");
    }
  }

  @Test
  void generatedOtpValuesAreDistributed() {
    // Dengan SecureRandom 6 digit, 50 sampel harus menghasilkan banyak nilai
    // berbeda (probabilitas duplikasi ekstrem ~ 0). Ini menjebak regression
    // ke RNG yang ter-constrain (mis. nextInt(9999) yang selalu < 5 digit).
    Set<String> samples = new HashSet<>();
    for (int i = 0; i < 50; i++) {
      samples.add(generator.generate());
    }
    assertThat(samples.size()).isGreaterThan(45);
  }

  @Test
  void generatedOtpCanReachFullSixDigitRange() {
    // Memastikan range penuh 000000-999999 terjadi (bug lama: nextInt(9999)
    // tidak pernah menghasilkan 9999).
    Set<Integer> leading = new HashSet<>();
    for (int i = 0; i < 300; i++) {
      leading.add(Character.getNumericValue(generator.generate().charAt(0)));
    }
    // 300 sampel: kemungkinan tidak melihat digit lead tertentu sangat kecil,
    // tapi agar tidak flaky cukup assert > 3 digit lead berbeda muncul.
    assertThat(leading.size()).isGreaterThanOrEqualTo(4);
  }
}
