package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "mst_file_type")
public class MstFileType {
    @Id
    @Size(max = 20)
    @Column(name = "file_type_code", length = 20)
    private String fileTypeCode;

    @Column(
            name = "file_type_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long fileTypeId;

    @Size(max = 100)
    //@NotNull
    @Column(name = "file_type_name", nullable = false, length = 100)
    private String fileTypeName;

    @Size(max = 500)
    //@NotNull
    @Column(name = "file_type_desc", nullable = false, length = 500)
    private String fileTypeDesc;

    @Size(max = 50)
    //@NotNull
    @Column(name = "file_allocation", nullable = false, length = 50)
    private String fileAllocation;

    @Builder.Default
    //@NotNull
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    //@NotNull
    @Column(name = "max_size_mb", nullable = false)
    private Long maxSizeMb;

    @Builder.Default
    @Size(max = 50)
    //@NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt = "system";

    //@NotNull
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

    @OneToMany(mappedBy = "fileTypeCode")
    private Set<LegalFile> legalFiles;

}
