package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignerPersonDto {
    private Long karyawanId;
    private String karyawanName;
    private String jabatan ;
    private String signerStatus;
    private String signhubStatus;
    private String usrCrt;
    private LocalDateTime dtmCrt;
    private String usrUpd;
    private LocalDateTime dtmUpd;
}
