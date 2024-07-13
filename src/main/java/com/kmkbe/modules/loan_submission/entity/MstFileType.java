package com.kmkbe.modules.loan_submission.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "mst_file_type")
public class MstFileType {
    @Id
    @Size(max = 20)
    @Column(name = "file_type_code", nullable = false, length = 20)
    private String fileTypeCode;

    @NotNull
    @ColumnDefault("nextval('mst_file_type_file_type_id_seq'::regclass)")
    @Column(name = "file_type_id", nullable = false)
    private Long fileTypeId;

    @Size(max = 100)
    @NotNull
    @Column(name = "file_type_name", nullable = false, length = 100)
    private String fileTypeName;

    @Size(max = 500)
    @NotNull
    @Column(name = "file_type_desc", nullable = false, length = 500)
    private String fileTypeDesc;

    @Size(max = 50)
    @NotNull
    @Column(name = "file_allocation", nullable = false, length = 50)
    private String fileAllocation;

    @NotNull
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    @NotNull
    @Column(name = "max_size_mb", nullable = false)
    private Long maxSizeMb;

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

    @OneToMany(mappedBy = "fileTypeCode")
    private Set<LegalFile> legalFiles = new LinkedHashSet<>();

}
