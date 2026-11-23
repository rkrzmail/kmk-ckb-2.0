package com.kmkbe.core.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ClearingDateDeserializer extends JsonDeserializer<Date> {
  @Override
  public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    try {
      return Date.from(OffsetDateTime.parse(parser.getText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
    } catch (RuntimeException exception) {
      throw context.weirdStringException(parser.getText(), Date.class,
        "clearingDate must be an ISO-8601 timestamp with an offset");
    }
  }
}
