package com.kmkbe.core.converter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.kmkbe.core.utils.DateTimeUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class DateDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String date = jsonParser.getText();
        try {
            Date format = DateTimeUtils.SDF_STANDARD_RESPONSE_DATE.parse(date);
            var a = DateTimeUtils.SDF_STANDARD_DATE.format(format);
            return DateTimeUtils.SDF_STANDARD_DATE.parse(a);
        } catch (Exception e) {
            try {
                Date format = DateTimeUtils.SDF_STANDARD_DATE.parse(date);
                var a = DateTimeUtils.SDF_STANDARD_DATE.format(format);
                return DateTimeUtils.SDF_STANDARD_DATE.parse(a);
            } catch (ParseException ex) {
                throw new RuntimeException("Please entry a date value in format in dd/MM/yyyy or yyyy-MM-dd");
            }
        }
    }
}
