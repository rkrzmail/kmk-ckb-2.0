package com.kmkbe.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class AuditMaskingUtils {
  private static final String MASKED_VALUE = "******";
  private static final Set<String> SENSITIVE_KEYS = Set.of(
    "password",
    "pin",
    "token",
    "refreshtoken",
    "accesstoken",
    "secretkey",
    "apikey",
    "aeskey",
    "authorization",
    "rtoken"
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

  private static boolean isSensitive(String key) {
    if (key == null) {
      return false;
    }

    String normalized = key.replace("_", "").replace("-", "").toLowerCase();
    return SENSITIVE_KEYS.contains(normalized);
  }
}
