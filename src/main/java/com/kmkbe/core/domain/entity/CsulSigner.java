package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "csul_signer")
public class CsulSigner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signer_id", nullable = false)
    private Long signerId;

    @Column(name = "karyawan_name", nullable = false)  // Pastikan kolom ini tidak null
    private String karyawanName;

    @Column(name = "jabatan", nullable = false)
    private String jabatan;

    @Column(name = "identity_no", nullable = false)
    private String identityNo;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "no_telp", nullable = false)
    private String noTelp;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "signhub_status", nullable = false)
    private String signhubStatus;

    @Column(name = "usr_crt", nullable = true)
    private String usrCrt;

    @Column(name = "dtm_crt", nullable = true)
    private LocalDateTime dtmCrt;

    @Column(name = "usr_upd", nullable = true)
    private String usrUpd;

    @Column(name = "dtm_upd", nullable = true)
    private LocalDateTime dtmUpd;
}
