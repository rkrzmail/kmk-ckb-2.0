package com.kmkbe.helpers.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PlainDoubleSerializerTest {
  private final PlainDoubleSerializer serializer = new PlainDoubleSerializer();
  private final JsonGenerator generator = mock(JsonGenerator.class);
  private final SerializerProvider provider = mock(SerializerProvider.class);

  @Test
  void writesPlainNumberWithoutScientificNotation() throws Exception {
    serializer.serialize(500000000.0, generator, provider);
    verify(generator).writeNumber("500000000");
  }

  @Test
  void writesDecimalNumber() throws Exception {
    serializer.serialize(3.14, generator, provider);
    verify(generator).writeNumber("3.14");
  }

  @Test
  void skipsNullValue() throws Exception {
    serializer.serialize(null, generator, provider);
    verifyNoInteractions(generator);
  }
}
