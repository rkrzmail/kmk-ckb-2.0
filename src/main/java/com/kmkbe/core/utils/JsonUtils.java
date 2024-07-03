package com.kmkbe.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    public static Map<String, Object> strToJson(String value) {
        try {
            if (value == null || value.isEmpty()) {
                return null;
            }

            return new ObjectMapper().readValue(
                    value,
                    new TypeReference<HashMap<String, Object>>() {
                    }
            );
        } catch (Exception e) {
            return null;
        }
    }
}
