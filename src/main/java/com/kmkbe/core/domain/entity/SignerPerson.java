package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "signer_person")
public class SignerPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "karyawan_id", nullable = false)
    private Long karyawanId;

    @Column(name = "karyawan_name", nullable = false)  // Pastikan kolom ini tidak null
    private String karyawanName;

    @Column(name = "jabatan", nullable = false)
    private String jabatan;

    @Column(name = "signer_status", nullable = false)
    private String signerStatus;

    @Column(name = "signhub_status", nullable = false)
    private String signhubStatus;

    @Column(name = "usr_crt", nullable = false)
    private String usrCrt;

    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;

    @Column(name = "usr_upd")
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;
}
