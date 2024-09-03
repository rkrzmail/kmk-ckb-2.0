package com.kmkbe.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

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
        return jsonToStr(object, true);
    }

    public static String jsonToStr(Object object, boolean includeNonNull) throws JsonProcessingException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(includeNonNull ? JsonInclude.Include.ALWAYS : JsonInclude.Include.NON_NULL);
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return "";
        }
    }

    public static <T> T castObjectFromMap(Map<String, Object> map, T object) throws IllegalAccessException {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            for (Field field : fields) {
                field.setAccessible(true);
                if (entry.getKey().equals(field.getName())) {
                    if (field.getType().isAssignableFrom(UUID.class)) {
                        field.set(object, UUID.fromString(entry.getValue().toString()));
                    } else if (field.getType().isAssignableFrom(Long.class)) {
                        field.set(object, new Date((Long) entry.getValue()));
                    } else {
                        field.set(object, entry.getValue());
                    }
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
