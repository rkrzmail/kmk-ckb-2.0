package com.kmkbe.modules.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "general_setting_dtl", schema = "public")
public class GeneralSettingDtl {
    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1
            //initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long gsDtlId;

    @Column(length = 100)
    private String gsDtlCode;

    @Column(length = 250)
    private String gsDtlValue;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private OffsetDateTime dtmCrt;

    @Column(nullable = false, length = 50)
    private String usrUpd;

    @Column
    private OffsetDateTime dtmUpd;

}
