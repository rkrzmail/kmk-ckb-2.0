package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AgreementDtoSerializationTest {
  @Test
  void disburseDateKeepsResponseDateFormat() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    String json = objectMapper.writeValueAsString(AgreementDto.builder()
      .disburseDate(LocalDate.of(2026, 8, 12))
      .build());

    assertThat(json).contains("\"disburseDate\":\"12/08/2026\"");
  }
}
