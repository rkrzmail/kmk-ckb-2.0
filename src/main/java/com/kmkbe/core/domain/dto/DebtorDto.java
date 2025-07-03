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
//@NoArgsConstructor
public class DebtorDto implements Serializable {
    private Long debtorId;
    private String debtorName;
    private String karyawanName;
    private String jabatan;
    private String identityNo;
    private String email;
    private String noTelp;
    private String tempatLahir;
    private String tanggalLahir;
    private String jenisKelamin;
    private String alamat;
    private String rt;
    private String rw;
    private String kodePos;
    private String kelurahan;
    private String kecamatan;
    private String kota;
    private Boolean isActive;
    private String signerStatus;
    private String signhubStatus;
    private String emailDebtor;
    private String financingHdrCode;
}
