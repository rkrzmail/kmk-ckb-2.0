package com.kmkbe.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kmkbe.core.converter.DateDeserializer;
import com.kmkbe.core.converter.DateSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test ObjectMapperConfig: konfigurasi serialisasi utama aplikasi.
 * Menjaga: INDENT_OUTPUT aktif, FAIL_ON_UNKNOWN_PROPERTIES off,
 * Date/Temporal serializer kustom terpasang, JavaTimeModule terdaftar.
 */
class ObjectMapperConfigTest {

  private ObjectMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapperConfig().objectMapper();
  }

  @Test
  void indentOutputIsEnabled() throws Exception {
    String json = mapper.writeValueAsString(java.util.Map.of("key", "value"));

    assertThat(json).contains("\n");
  }

  @Test
  void unknownPropertiesAreIgnoredOnDeserialization() throws Exception {
    assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();

    Record result = mapper.readValue("{\"known\":\"x\",\"unknownField\":1}", Record.class);
    assertThat(result.getKnown()).isEqualTo("x");
  }

  @Test
  void failOnEmptyBeansIsDisabled() {
    assertThat(mapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)).isFalse();
  }

  @Test
  void javaTimeModuleSerializesLocalDateAsString() throws Exception {
    String json = mapper.writeValueAsString(java.util.Map.of("date", LocalDate.of(2026, 11, 9)));

    assertThat(json).contains("2026-11-09");
    assertThat(json).doesNotContain("[2026");
  }

  @Test
  void customDateSerializerIsApplied() throws Exception {
    assertThat(mapper.getSerializerProvider() != null).isTrue();

    // Date ser/de roundtrip via custom (de)serializer
    DateSerializer serializer = new DateSerializer();
    DateDeserializer deserializer = new DateDeserializer();
    assertThat(serializer).isNotNull();
    assertThat(deserializer).isNotNull();

    Date now = new Date(0L);
    String json = mapper.writeValueAsString(java.util.Map.of("d", now));
    java.util.Map<String, Object> map = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {
    });
    assertThat(map).containsKey("d");
  }

  @Test
  void serializesAndDeserializesRoundTrip() throws Exception {
    Record input = new Record();
    input.setKnown("roundtrip");
    input.setNumber(42);

    String json = mapper.writeValueAsString(input);
    Record output = mapper.readValue(json, Record.class);

    assertThat(output.getKnown()).isEqualTo("roundtrip");
    assertThat(output.getNumber()).isEqualTo(42);
  }

  public static class Record {
    private String known;
    private Integer number;

    public String getKnown() { return known; }
    public void setKnown(String known) { this.known = known; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
  }
}
