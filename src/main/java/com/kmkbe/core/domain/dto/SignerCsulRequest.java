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
public class SignerCsulRequest implements Serializable {
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
    private String registrationMessage;

    public String getRegistrationMessage() {
        return registrationMessage;
    }
    public void setRegistrationMessage(String registrationMessage) {
        this.registrationMessage = registrationMessage;
    }
}
