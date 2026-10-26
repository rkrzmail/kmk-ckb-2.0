package com.kmkbe.core.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.config.ObjectMapperConfig;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.response.DocumentUploadResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TemporalDateTimeSerializerTest {

    @Test
    void serializesDocumentUploadResponseDates() throws Exception {
        DocumentUploadResponse upload = new DocumentUploadResponse(
            1L, UUID.randomUUID(), "DOC006", "sit.pdf",
            LocalDate.of(2027, 2, 18), "Signed", "admin",
            LocalDateTime.of(2026, 10, 26, 13, 23, 57));

        ObjectMapper mapper = new ObjectMapperConfig().objectMapper();
        JsonNode data = mapper.readTree(mapper.writeValueAsString(
            new CommonResult<DocumentUploadResponse>().success(upload))).path("data");

        assertThat(data.path("expiredDate").asText()).isEqualTo("2027-02-18");
        assertThat(data.path("uploadedAt").asText()).isEqualTo("2026-10-26 13:23:57");
    }
}
