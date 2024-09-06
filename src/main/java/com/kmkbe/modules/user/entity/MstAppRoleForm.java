package com.kmkbe.modules.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mst_app_role_form", schema = "users")
public class MstAppRoleForm {
    @Builder.Default
    @Id
    @Column(name = "app_role_form_code", nullable = false)
    private UUID appRoleFormCode = UUID.randomUUID();

    @ColumnDefault("nextval('users.mst_app_role_form_app_role_form_id_seq'::regclass)")
    @Column(
            name = "app_role_form_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long appRoleFormId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_role_code",
            referencedColumnName = "application_role_code",
            nullable = false
    )
    private MstApplicationRole applicationRole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "form_code",
            referencedColumnName = "form_code",
            nullable = false
    )
    private MstForm form;

    @Builder.Default
    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt = "system";

    @Builder.Default
    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt = Instant.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
