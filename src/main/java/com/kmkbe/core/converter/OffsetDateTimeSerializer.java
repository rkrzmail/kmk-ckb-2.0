package com.kmkbe.core.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {
    private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
            .withZone(ZoneId.of("UTC"));

    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Jakarta"));

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            throw new IOException("OffsetDateTime argument is null.");
        }

        //jsonGenerator.writeString(ISO_8601_FORMATTER.format(value));
        jsonGenerator.writeString(STANDARD_FORMATTER.format(value));
    }
}
