package com.kmkbe.helpers.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UuidConverterTest {
  private final UuidConverter converter = new UuidConverter();

  @Test
  void convertsUuidToDatabaseStringAndBack() {
    UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
    String column = converter.convertToDatabaseColumn(id);
    assertThat(column).isEqualTo("11111111-1111-1111-1111-111111111111");
    assertThat(converter.convertToEntityAttribute(column)).isEqualTo(id);
  }
}
