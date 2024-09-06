package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSubmissionSessionDto {
    @JsonIgnore
    private Integer id;
    private Integer lastStep;
    private Instant dtmCrt;
    private Instant dtmUpd;
    private Object session;
}
