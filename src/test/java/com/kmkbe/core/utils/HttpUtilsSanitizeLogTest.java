package com.kmkbe.core.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresi test untuk security issue #14 (SECURITY.md):
 * log tidak boleh memuat kredensial (header Authorization/API-Key/Cookie,
 * field password/otp/token pada payload, maupun query string sensitif).
 */
class HttpUtilsSanitizeLogTest {

  @Test
  void authorizationHeaderIsMasked() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer rahasia.banget.token");
    headers.set("X-API-KEY", "kunci-api");
    headers.set("Cookie", "session=xyz");
    headers.set("Content-Type", "application/json");

    HttpHeaders sanitized = HttpUtils.sanitizeHeaders(headers);

    assertThat(sanitized.getFirst("Authorization")).isEqualTo(HttpUtils.MASKED_VALUE);
    assertThat(sanitized.getFirst("X-API-KEY")).isEqualTo(HttpUtils.MASKED_VALUE);
    assertThat(sanitized.getFirst("Cookie")).isEqualTo(HttpUtils.MASKED_VALUE);
    assertThat(sanitized.getFirst("Content-Type")).isEqualTo("application/json");
  }

  @Test
  void nullHeadersIsHandled() {
    assertThat(HttpUtils.sanitizeHeaders(null)).isNotNull();
  }

  @Test
  void passwordFieldInJsonPayloadIsMasked() {
    String payload = "{\"username\":\"user@mail.com\",\"password\":\"P@ssw0rd123\"}";

    String sanitized = HttpUtils.sanitizePayload(payload);
    Map<String, Object> json = ObjectUtils.strToJson(sanitized);

    assertThat(json).isNotNull();
    assertThat(json.get("username")).isEqualTo("user@mail.com");
    assertThat(String.valueOf(json.get("password"))).doesNotContain("P@ssw0rd123");
    assertThat(String.valueOf(json.get("password"))).isEqualTo(HttpUtils.MASKED_VALUE);
  }

  @Test
  void otpAndTokenFieldsInJsonPayloadAreMasked() {
    String payload = "{\"otp\":\"123456\",\"pin\":\"999999\",\"accessToken\":\"jwt.value.here\",\"nama\":\"Budi\"}";

    String sanitized = HttpUtils.sanitizePayload(payload);
    Map<String, Object> json = ObjectUtils.strToJson(sanitized);

    assertThat(String.valueOf(json.get("otp"))).isEqualTo(HttpUtils.MASKED_VALUE);
    assertThat(String.valueOf(json.get("pin"))).isEqualTo(HttpUtils.MASKED_VALUE);
    assertThat(String.valueOf(json.get("accessToken"))).isEqualTo(HttpUtils.MASKED_VALUE);
    assertThat(json.get("nama")).isEqualTo("Budi");
  }

  @Test
  void nestedJsonFieldsAreMasked() {
    String payload = "{\"user\":{\"email\":\"a@mail.com\",\"credentials\":{\"password\":\"topsecret\"}},\"items\":[{\"token\":\"tok123\"}]}";

    String sanitized = HttpUtils.sanitizePayload(payload);
    Map<String, Object> json = ObjectUtils.strToJson(sanitized);
    String asString = sanitized;

    assertThat(asString).doesNotContain("topsecret");
    assertThat(asString).doesNotContain("tok123");
    assertThat(asString).contains("a@mail.com");
  }

  @Test
  void formUrlEncodedPayloadIsMasked() {
    String payload = "username=user@mail.com&password=P@ssw0rd123&remember=true";

    String sanitized = HttpUtils.sanitizePayload(payload);

    assertThat(sanitized).doesNotContain("P@ssw0rd123");
    assertThat(sanitized).contains("username=user@mail.com");
    assertThat(sanitized).contains("password=" + HttpUtils.MASKED_VALUE);
  }

  @Test
  void nullAndEmptyPayloadIsReturnedAsIs() {
    assertThat(HttpUtils.sanitizePayload(null)).isNull();
    assertThat(HttpUtils.sanitizePayload("")).isEmpty();
  }

  @Test
  void sensitiveQueryStringIsMasked() {
    String query = "page=1&token=abc.def.ghi&size=10";

    String sanitized = HttpUtils.sanitizeQueryString(query);

    assertThat(sanitized).doesNotContain("abc.def.ghi");
    assertThat(sanitized).contains("token=" + HttpUtils.MASKED_VALUE);
    assertThat(sanitized).contains("page=1");
    assertThat(sanitized).contains("size=10");
  }

  @Test
  void nullQueryStringIsReturnedAsIs() {
    assertThat(HttpUtils.sanitizeQueryString(null)).isNull();
  }

  @Test
  void nonSensitivePayloadIsUntouched() {
    String payload = "{\"financingCode\":\"ABC-123\",\"amount\":5000000}";

    String sanitized = HttpUtils.sanitizePayload(payload);
    Map<String, Object> json = ObjectUtils.strToJson(sanitized);

    assertThat(json.get("financingCode")).isEqualTo("ABC-123");
    assertThat(json.get("amount")).isEqualTo(5000000);
  }

  @Test
  void caseInsensitiveSensitiveFieldsAreMasked() {
    String payload = "{\"PASSword\":\"x\",\"Api_Key\":\"y\",\"Refresh-Token\":\"z\"}";

    String sanitized = HttpUtils.sanitizePayload(payload);

    assertThat(sanitized).doesNotContain("\"x\"");
    assertThat(sanitized).doesNotContain("\"y\"");
    assertThat(sanitized).doesNotContain("\"z\"");
  }

  @Test
  void listOfMapsNestedIsMasked() {
    String payload = "{\"invoices\":[{\"no\":\"INV-1\",\"otp\":\"777777\"}]}";

    String sanitized = HttpUtils.sanitizePayload(payload);

    assertThat(sanitized).doesNotContain("777777");
    assertThat(sanitized).contains("INV-1");
  }
}
