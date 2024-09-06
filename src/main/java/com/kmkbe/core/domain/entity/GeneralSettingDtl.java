package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "general_setting_dtl", schema = "public")
public class GeneralSettingDtl {

    @Column(
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long gsDtlId;

    @Id
    @Column(name = "gs_dtl_code", length = 100, nullable = false)
    private String gsDtlCode;

    @Column(name = "gs_hdr_code", nullable = false, length = 20)
    private String gsHdrCode;

    @Column(length = 250)
    private String gsDtlValue;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private Date dtmCrt;

    @Column(nullable = false, length = 50)
    private String usrUpd;

    @Column
    private Date dtmUpd;

    @ManyToOne
    @JoinColumn(
            name = "gs_hdr_code",
            referencedColumnName = "gs_hdr_code",
            insertable = false,
            updatable = false
    )
    private GeneralSettingHdr generalSettingHdr;
}
