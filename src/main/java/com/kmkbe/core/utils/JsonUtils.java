package com.kmkbe.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonUtils {
    public static LinkedHashMap<String, Object> strToJson(String value) {
        try {
            if (value == null || value.isEmpty()) {
                return null;
            }

            return new ObjectMapper().readValue(
                    value,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static LinkedHashMap<String, Object> objectToJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                    jsonToStr(object),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            return null;
        }
    }

    public static String jsonToStr(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
