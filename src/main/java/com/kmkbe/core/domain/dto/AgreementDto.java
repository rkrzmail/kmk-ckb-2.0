package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Builder
@Getter
public class AgreementDto implements Serializable {
    private Integer no;
    private String cwrCode;
    private String agreementNo;
    private UUID financingHdrCode;
    private UUID bouwheerCode;
    private UUID custCode;
    private String bouwheerName;
    private String custName;
    private String bankName;
    private String rekeningNo;
    private BigDecimal financingAmt;
    @JsonSerialize(using = DisburseDateSerializer.class)
    private LocalDate disburseDate;
    private BigDecimal disburseAmt;
    private String currency;

    private static class DisburseDateSerializer extends JsonSerializer<LocalDate> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(FORMATTER.format(value));
        }
    }
}
