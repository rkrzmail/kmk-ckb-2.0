package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorDto implements Serializable {
    private String debtorName;
    private String debtorStatus;
    private String bouwheerName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Long countVisit;
}
