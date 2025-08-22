package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SignerCsulDto implements Serializable {
    private Long signerId;
    private String karyawanName;
    private String jabatan;
    private String identityNo;
    private String email;
    private String noTelp;
    private Boolean isActive;
    private String signhubStatus;
}
