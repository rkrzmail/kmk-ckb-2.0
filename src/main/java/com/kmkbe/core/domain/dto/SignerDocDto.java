package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@NoArgsConstructor
public class SignerDocDto implements Serializable {
    private Long agreementFileId;
    private String agreementCode;
    private String bowheerName;
    private String verifDate;
    private String status;
    private String documentId;
}
