package com.kmkbe.core.domain.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "legal_file")
public class LegalFile {
    @Id
    //@ColumnDefault("nextval('legal_file_file_id_seq'::regclass)")
    @SequenceGenerator(
            name = "legal_file_file_id_seq",
            sequenceName = "legal_file_file_id_seq",
            allocationSize = 1
            //initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "legal_file_file_id_seq"
    )
    @Column(
            name = "file_id",
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long fileId;

    @NotNull(message = "CustCode is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cust_code", nullable = false)
    private Customer custCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "file_type_code", nullable = false)
    private MstFileType fileTypeCode;

    @Size(max = 500)
    //@NotNull(message = "FileName is required")
    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Size(max = 8000)
    //@NotNull(message = "FilePath is required")
    @Column(name = "file_path", nullable = false, length = 8000)
    private String filePath;

    @Size(max = 500)
    //@NotNull(message = "ContentType is required")
    @Column(name = "content_type", nullable = false, length = 500)
    private String contentType;

    @Column(name = "file_no")
    private String fileNo;

    @Builder.Default
    @Size(max = 50)
    @NotNull(message = "UsrCrt is required")
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt = "system";

    @Builder.Default
    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt = DateTimeUtils.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

}
