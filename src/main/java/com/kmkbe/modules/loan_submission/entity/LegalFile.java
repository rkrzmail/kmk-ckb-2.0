package com.kmkbe.modules.loan_submission.entity;

import com.kmkbe.modules.customer.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "legal_file")
public class LegalFile {
    @Id
    @ColumnDefault("nextval('legal_file_file_id_seq'::regclass)")
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cust_code", nullable = false)
    private Customer custCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_type_code", nullable = false)
    private MstFileType fileTypeCode;

    @Size(max = 500)
    @NotNull
    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Size(max = 8000)
    @NotNull
    @Column(name = "file_path", nullable = false, length = 8000)
    private String filePath;

    @Size(max = 500)
    @NotNull
    @Column(name = "content_type", nullable = false, length = 500)
    private String contentType;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
