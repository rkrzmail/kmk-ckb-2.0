package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class LegalFileDto {
    private Long fileId;
    private String fileUrl;
    private String fileName;
    private Instant uploadedDate;
}
