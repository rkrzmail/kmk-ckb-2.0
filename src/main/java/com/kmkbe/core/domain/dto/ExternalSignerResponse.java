package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExternalSignerResponse implements Serializable {
    private List<ExternalSignerDto> signers;
    private String statusCode;
    private String message;
}
