package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "agreement_file", schema = "public")
public class AgreementFile {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "agreement_file_agreement_file_id_seq"
    )
    @SequenceGenerator(
            name = "agreement_file_agreement_file_id_seq",
            sequenceName = "agreement_file_agreement_file_id_seq",
            allocationSize = 1
    )
    @Column(
            name = "agreement_file_id",
            columnDefinition = "serial"
    )
    private Long agreementFileId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "agreement_code",
            referencedColumnName = "agreement_code",
            nullable = false
    )
    private Agreement agreement;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "file_type_code",
            referencedColumnName = "file_type_code",
            nullable = false
    )
    private MstFileType mstFileType;

    @Size(max = 500)
    @NotNull
    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Size(max = 8000)
    @Column(name = "file_path", length = 8000)
    private String filePath;

    @Size(max = 500)
    @Column(name = "content_type", length = 500)
    private String contentType;

    @Size(max = 50)
    @Column(name = "usr_crt", length = 50)
    private String usrCrt;

    @Column(name = "dtm_crt")
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
