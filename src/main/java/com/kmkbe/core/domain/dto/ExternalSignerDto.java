package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExternalSignerDto implements Serializable {
    private String cabang;
    private String departement;
    private String email;
    private String signerName;
    private String signerPosition;
}
