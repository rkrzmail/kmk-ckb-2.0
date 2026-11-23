package com.kmkbe.core.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConverterCoverageTest {

  @Test
  void dateDeserializer_parsesFormatsAndThrows() throws Exception {
    DateDeserializer d = new DateDeserializer();
    ObjectMapper m = new ObjectMapper();
    try (JsonParser p1 = m.getFactory().createParser("\"2026-11-23\"")) { p1.nextToken();
      assertThat(d.deserialize(p1, mock(DeserializationContext.class))).isNotNull(); }
    try (JsonParser p2 = m.getFactory().createParser("\"23/11/2026\"")) { p2.nextToken();
      assertThat(d.deserialize(p2, mock(DeserializationContext.class))).isNotNull(); }
    try (JsonParser p3 = m.getFactory().createParser("\"bad-date\"")) { p3.nextToken();
      assertThatThrownBy(() -> d.deserialize(p3, mock(DeserializationContext.class)))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("dd/MM/yyyy");
    }
  }

  @Test
  void dateSerializer_nullAndNonNull() throws Exception {
    DateSerializer s = new DateSerializer();
    var gen = mock(com.fasterxml.jackson.core.JsonGenerator.class);
    s.serialize(null, gen, null);
    verify(gen, never()).writeString(anyString());
    s.serialize(new Date(0), gen, null);
    verify(gen).writeString(anyString());
  }

  @Test
  void offsetDateTimeAttributeConverter_nullAndRoundTrip() {
    OffsetDateTimeAttributeConverter c = new OffsetDateTimeAttributeConverter();
    assertThat(c.convertToDatabaseColumn(null)).isNull();
    assertThat(c.convertToEntityAttribute(null)).isNull();
    OffsetDateTime odt = OffsetDateTime.now(ZoneId.of("Asia/Jakarta"));
    Timestamp ts = c.convertToDatabaseColumn(odt);
    assertThat(ts).isNotNull();
    OffsetDateTime back = c.convertToEntityAttribute(ts);
    assertThat(back).isNotNull();
  }

  @Test
  void toLowerCaseDeserializer() throws Exception {
    ObjectMapper m = new ObjectMapper();
    com.fasterxml.jackson.databind.module.SimpleModule mod = new com.fasterxml.jackson.databind.module.SimpleModule();
    mod.addDeserializer(String.class, new ToLowerCaseDeserializer());
    m.registerModule(mod);
    String val = m.readValue("\"HeLLo\"", String.class);
    assertThat(val).isEqualTo("hello");
  }

  @Test
  void objectToUrlEncodedConverter_behaviors() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectToUrlEncodedConverter c = new ObjectToUrlEncodedConverter(mapper);

    assertThat(c.canRead(Object.class, MediaType.APPLICATION_FORM_URLENCODED)).isFalse();
    assertThat(c.canWrite(Object.class, MediaType.APPLICATION_FORM_URLENCODED)).isTrue();
    assertThat(c.canWrite(Object.class, MediaType.APPLICATION_JSON)).isFalse();
    assertThat(c.getSupportedMediaTypes()).containsExactly(MediaType.APPLICATION_FORM_URLENCODED);
    assertThatThrownBy(() -> c.read(Object.class, mock(HttpInputMessage.class)))
      .isInstanceOf(org.apache.commons.lang3.NotImplementedException.class);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("a", "hello world");
    body.put("b", null);
    body.put("c", 123);
    MockHttpOutputMessage out = new MockHttpOutputMessage();
    c.write(body, MediaType.APPLICATION_FORM_URLENCODED, out);
    String written = out.getBodyAsString();
    assertThat(written).contains("a=hello+world");
    assertThat(written).contains("b=");
    assertThat(written).contains("c=123");
  }

  @Test
  void objectToUrlEncodedConverter_writeIOExceptionIgnored() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectToUrlEncodedConverter c = new ObjectToUrlEncodedConverter(mapper);
    org.springframework.http.HttpOutputMessage out = mock(org.springframework.http.HttpOutputMessage.class);
    java.io.OutputStream os = mock(java.io.OutputStream.class);
    when(out.getBody()).thenReturn(os);
    doThrow(new IOException("boom")).when(os).write(any(byte[].class));
    assertThatCode(() -> c.write(Map.of("a","b"), MediaType.APPLICATION_FORM_URLENCODED, out))
      .doesNotThrowAnyException();
  }

  @Test
  void objectToUrlEncodedConverter_nullChecks() {
    ObjectToUrlEncodedConverter c = new ObjectToUrlEncodedConverter(new ObjectMapper());
    MockHttpOutputMessage out = new MockHttpOutputMessage();
    assertThatThrownBy(() -> c.canRead(null, MediaType.APPLICATION_FORM_URLENCODED)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> c.canWrite(null, MediaType.APPLICATION_FORM_URLENCODED)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> c.read(null, mock(HttpInputMessage.class))).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> c.read(Object.class, null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> c.write(null, MediaType.APPLICATION_FORM_URLENCODED, out)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> c.write(Map.of("a","b"), MediaType.APPLICATION_FORM_URLENCODED, null)).isInstanceOf(NullPointerException.class);
  }
}
