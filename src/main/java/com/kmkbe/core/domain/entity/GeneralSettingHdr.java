package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "general_setting_hdr", schema = "public")
public class GeneralSettingHdr {

    @Column(
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long gsHdrId;

    @Id
    @Column(name = "gs_hdr_code", nullable = false, length = 20)
    private String gsHdrCode;

    @Column(nullable = false, length = 100)
    private String gsDescription;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "generalSettingHdr")
    private List<GeneralSettingDtl> generalSettingDtl;
}
