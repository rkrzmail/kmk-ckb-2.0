package com.kmkbe.modules.user.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mst_application_role", schema = "users")
public class MstApplicationRole {
    @Builder.Default
    @Id
    @Column(name = "application_role_code", nullable = false)
    private UUID applicationRoleCode = UUID.randomUUID();

    @NotNull
    @ColumnDefault("nextval('users.mst_application_role_application_role_id_seq'::regclass)")
    @Column(
            name = "application_role_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long applicationRoleId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_code",
            referencedColumnName = "application_code",
            nullable = false
    )
    private MstApplication applicationCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_code",
            referencedColumnName = "role_code",
            nullable = false
    )
    private MstRole roleCode;

    @Builder.Default
    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Builder.Default
    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt = "by system";

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
