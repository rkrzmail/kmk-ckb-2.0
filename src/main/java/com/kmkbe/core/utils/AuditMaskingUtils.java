package com.kmkbe.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpHeaders;

import java.util.*;

public final class AuditMaskingUtils {
  private static final String MASKED_VALUE = "******";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final Set<String> SENSITIVE_KEYS = Set.of(
    "password",
    "pin",
    "custpin",
    "newpin",
    "oldpin",
    "token",
    "refreshtoken",
    "accesstoken",
    "secret",
    "secretkey",
    "apikey",
    "aeskey",
    "adinkey",
    "adinskey",
    "authorization",
    "rtoken",
    "otp",
    "otpcode"
  );

  private AuditMaskingUtils() {
  }

  public static JsonNode mask(JsonNode node) {
    if (node == null || node.isNull()) {
      return node;
    }

    JsonNode copy = node.deepCopy();
    maskInPlace(copy);
    return copy;
  }

  /**
   * Mensanitasi object apa pun (Map, List, DTO, atau String JSON) dan mengembalikan
   * hasil yang telah di-mask. Jika input null, return null.
   */
  public static Object maskObject(Object data) {
    if (data == null) {
      return null;
    }

    try {
      if (data instanceof JsonNode jsonNode) {
        return mask(jsonNode);
      }
      if (data instanceof String strData) {
        return maskJsonString(strData);
      }
      JsonNode node = OBJECT_MAPPER.valueToTree(data);
      maskInPlace(node);
      return OBJECT_MAPPER.convertValue(node, Object.class);
    } catch (Exception e) {
      return data;
    }
  }

  /**
   * Mensanitasi String JSON dan mengembalikan String JSON baru dengan nilai field sensitif tersamarkan.
   * Jika bukan format JSON valid, periksa substring pola sensitif.
   */
  public static String maskJsonString(String jsonStr) {
    if (jsonStr == null || jsonStr.trim().isEmpty()) {
      return jsonStr;
    }

    try {
      JsonNode node = OBJECT_MAPPER.readTree(jsonStr);
      maskInPlace(node);
      return OBJECT_MAPPER.writeValueAsString(node);
    } catch (Exception notJson) {
      return maskPlainTextOrQueryString(jsonStr);
    }
  }

  /**
   * Mensanitasi HttpHeaders agar header sensitif (Authorization, ApiKey, rtoken, dsb.) tidak bocor.
   */
  public static Map<String, List<String>> maskHeaders(HttpHeaders headers) {
    if (headers == null) {
      return Collections.emptyMap();
    }

    Map<String, List<String>> masked = new LinkedHashMap<>();
    headers.forEach((headerName, values) -> {
      if (isSensitive(headerName)) {
        masked.put(headerName, List.of(MASKED_VALUE));
      } else {
        masked.put(headerName, values);
      }
    });

    return masked;
  }

  private static void maskInPlace(JsonNode node) {
    if (node instanceof ObjectNode objectNode) {
      Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if (isSensitive(field.getKey())) {
          objectNode.put(field.getKey(), MASKED_VALUE);
        } else {
          maskInPlace(field.getValue());
        }
      }
      return;
    }

    if (node instanceof ArrayNode arrayNode) {
      arrayNode.forEach(AuditMaskingUtils::maskInPlace);
    }
  }

  public static boolean isSensitive(String key) {
    if (key == null) {
      return false;
    }

    String normalized = key.replace("_", "").replace("-", "").toLowerCase();
    if (SENSITIVE_KEYS.contains(normalized)) {
      return true;
    }
    // Tangani variasi gabungan seperti nestedPassword, userPin, tokenAuth, dsb.
    return normalized.contains("password")
      || normalized.contains("pin")
      || normalized.contains("secret")
      || normalized.contains("token")
      || normalized.contains("apikey")
      || normalized.contains("otp");
  }

  private static String maskPlainTextOrQueryString(String raw) {
    if (raw == null) {
      return null;
    }
    // Regex penggantian parameter query string sensitif misal ?token=xxx atau &pin=yyy
    return raw.replaceAll("(?i)(password|pin|custPin|newPin|token|secret|apiKey|otp)=([^&\\s]+)", "$1=" + MASKED_VALUE);
  }
}

