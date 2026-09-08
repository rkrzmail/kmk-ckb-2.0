package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

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
  @Column(name = "file_type_name", nullable = false, length = 100)
  private String fileTypeName;

  @Size(max = 500)
  @Column(name = "file_type_desc", nullable = false, length = 500)
  private String fileTypeDesc;

  @Size(max = 50)
  @Column(name = "file_allocation", nullable = false, length = 50)
  private String fileAllocation;

  @Builder.Default
  @Column(name = "is_mandatory", nullable = false)
  private Boolean isMandatory = false;

  @Column(name = "max_size_mb", nullable = false)
  private Long maxSizeMb;

  @Builder.Default
  @Column(name = "usr_crt", nullable = false, length = 50)
  private String usrCrt = "system";

  @Column(name = "dtm_crt", nullable = false)
  private LocalDateTime dtmCrt;

  @Column(name = "usr_upd", length = 50)
  private String usrUpd;

  @Column(name = "dtm_upd")
  private LocalDateTime dtmUpd;

  @OneToMany(mappedBy = "fileTypeCode")
  private Set<LegalFile> legalFiles;

  @Column(name = "bouwheer_Code", length = 36)
  private UUID bouwheerCode;
}
