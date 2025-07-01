package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "debtors")
public class Debtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "debtor_id", nullable = false)
    private Long debtorId;

    @Column(name = "debtor_name", nullable = false)  // Pastikan kolom ini tidak null
    private String debtorName;

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

    @Column(name = "tempat_lahir", nullable = false)
    private String tempatLahir;

    @Column(name = "tanggal_lahir", nullable = false)
    private String tanggalLahir;

    @Column(name = "jenis_kelamin", nullable = false)
    private String jenisKelamin;

    @Column(name = "alamat", nullable = false)
    private String alamat;

    @Column(name = "rt", nullable = false)
    private String rt;

    @Column(name = "rw", nullable = false)
    private String rw;

    @Column(name = "kode_pos", nullable = false)
    private String kodePos;

    @Column(name = "kelurahan", nullable = false)
    private String kelurahan;

    @Column(name = "kecamatan", nullable = false)
    private String kecamatan;

    @Column(name = "kota", nullable = false)
    private String kota;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "signer_status", nullable = false)
    private String signerStatus;

    @Column(name = "signhub_status", nullable = false)
    private String signhubStatus;

    @Column(name = "email_debtor", nullable = false)
    private String emailDebtor;

    @Column(name = "financing_hdr_code", nullable = true)
    private String financingHdrCode;

}
