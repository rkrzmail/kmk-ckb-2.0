package com.kmkbe.helpers.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

@Converter
  public class UuidConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID attribute) {
      return attribute.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
      return UUID.fromString(dbData);
    }
}
