package com.kmkbe.core.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.kmkbe.core.utils.DateTimeUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class TemporalDateTimeSerializer extends JsonSerializer<TemporalAccessor> {
    @Override
    public void serialize(TemporalAccessor value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value != null) {
            if (value instanceof LocalDate date) {
                jsonGenerator.writeString(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
            } else {
                jsonGenerator.writeString(DateTimeUtils.DTF_DATE_TIME_STANDARD_FORMATTER.format(value));
            }
        }
    }
}
