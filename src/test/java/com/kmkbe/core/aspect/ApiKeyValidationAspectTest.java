package com.kmkbe.core.aspect;

import com.kmkbe.core.exception.IllegalApiKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regresi test untuk security issue #13 (SECURITY.md):
 * perbandingan API key harus exact-match yang benar dan tidak memakai
 * String.equals raw (timing attack). Semantik: key benar lolos,
 * key salah / header null ditolak - perilaku tidak berubah bagi pemanggil.
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyValidationAspectTest {

  private static final String CONFIGURED_API_KEY =
    "$2b$10$YoLl0SFxCMlIWXfQ9RhixeU8Vxvj9Fi7RmF5j7zA9dhYwdplSGyWC";

  private ApiKeyValidationAspect aspect;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    aspect = new ApiKeyValidationAspect(request);
    ReflectionTestUtils.setField(aspect, "apiKey", CONFIGURED_API_KEY);
  }

  @Test
  void correctApiKeyPasses() {
    request.addHeader("API-Key", CONFIGURED_API_KEY);

    assertThatCode(() -> aspect.validateApiKey()).doesNotThrowAnyException();
  }

  @Test
  void wrongApiKeyIsRejected() {
    request.addHeader("API-Key", "wrong-key");

    assertThatThrownBy(() -> aspect.validateApiKey())
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  void missingApiKeyHeaderIsRejected() {
    assertThatThrownBy(() -> aspect.validateApiKey())
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  void emptyApiKeyHeaderIsRejected() {
    request.addHeader("API-Key", "");

    assertThatThrownBy(() -> aspect.validateApiKey())
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  void apiKeyIsCaseSensitive() {
    String upper = CONFIGURED_API_KEY.toUpperCase();

    assertThat(upper).isNotEqualTo(CONFIGURED_API_KEY);

    request.addHeader("API-Key", upper);

    assertThatThrownBy(() -> aspect.validateApiKey())
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  void keyWithSameLengthButDifferentContentIsRejected() {
    // string beda 1 karakter terakhir, panjang identik - harus tetap ditolak
    String tampered = CONFIGURED_API_KEY.substring(0, CONFIGURED_API_KEY.length() - 1) + 'X';

    request.addHeader("API-Key", tampered);

    assertThatThrownBy(() -> aspect.validateApiKey())
      .isInstanceOf(IllegalApiKeyException.class);
  }

  @Test
  void messageDigestIsEqualMatchesExactEquality() {
    byte[] a = CONFIGURED_API_KEY.getBytes(StandardCharsets.UTF_8);

    assertThat(MessageDigest.isEqual(a, a)).isTrue();
    assertThat(MessageDigest.isEqual(a, "wrong-key".getBytes(StandardCharsets.UTF_8))).isFalse();
    assertThat(MessageDigest.isEqual(a, new byte[a.length - 1])).isFalse();
    assertThat(MessageDigest.isEqual(new byte[0], new byte[0])).isTrue();
  }
}
