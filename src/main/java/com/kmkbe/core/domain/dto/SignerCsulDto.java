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
public class SignerCsulDto implements Serializable {
    private Long signerId;
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
    private String signhubStatus;
    private String registrationMessage;

    public SignerCsulDto(Long signerId, String karyawanName, String jabatan, String identityNo, String email, String noTelp, String tempatLahir, String tanggalLahir, String jenisKelamin, String alamat, String rt, String rw, String kodePos, String kelurahan, String kecamatan, String kota, Boolean isActive, String signhubStatus, String usrCrt, LocalDateTime dtmCrt) {
    }

    public String getRegistrationMessage() {
        return registrationMessage;
    }
    public void setRegistrationMessage(String registrationMessage) {
        this.registrationMessage = registrationMessage;
    }
}
