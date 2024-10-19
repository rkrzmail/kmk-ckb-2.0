package com.kmkbe.modules.user.entity;

import com.kmkbe.core.utils.DateTimeUtils;
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
@Table(name = "mst_app_role_form_user", schema = "users")
public class MstAppRoleFormUser {
    @Builder.Default
    @Id
    @Column(name = "app_role_form_user_code", nullable = false)
    private UUID appRoleFormUserCode = UUID.randomUUID();

    @ColumnDefault("nextval('users.mst_app_role_form_user_app_role_form_user_id_seq'::regclass)")
    @Column(
            name = "app_role_form_user_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long appRoleFormUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "app_role_form_code",
            referencedColumnName = "app_role_form_code"
    )
    private MstAppRoleForm appRoleForm;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "user_code",
            referencedColumnName = "user_code",
            nullable = false
    )
    private MstUser user;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Size(max = 50)
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt = "system";

    @Builder.Default
    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt = DateTimeUtils.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
