package com.kmkbe.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectUtils {
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

    public static <T> T castObjectFromMap(Map<String, Object> map, T object) throws IllegalAccessException {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            for (Field field : fields) {
                field.setAccessible(true);
                if (entry.getKey().equals(field.getName())) {
                    field.set(object, entry.getValue());
                    break;
                }
            }
        }
        return object;
    }

    public static Map<String, Object> castObjectToMap(Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(object));
        }

        return map;
    }
}
