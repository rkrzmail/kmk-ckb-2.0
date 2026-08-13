package com.kmkbe.helpers.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

@Converter(autoApply = false)
public class UuidConverter implements AttributeConverter<UUID, String> {

  @Override
  public String convertToDatabaseColumn(UUID attribute) {
    return attribute != null ? attribute.toString() : null;
  }

  @Override
  public UUID convertToEntityAttribute(String dbData) {
    return dbData != null ? UUID.fromString(dbData) : null;
  }
}
