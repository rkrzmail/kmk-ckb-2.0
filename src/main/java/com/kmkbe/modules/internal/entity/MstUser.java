package com.kmkbe.modules.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "mst_user", schema = "users")
public class MstUser {
    @Id
    @Column(name = "user_code", nullable = false)
    private UUID id;

    @NotNull
    @ColumnDefault("nextval('users.mst_user_user_id_seq'::regclass)")
    @Column(
            name = "user_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long userId;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 20)
    @NotNull
    @Column(name = "employee_code", nullable = false, length = 20)
    private String employeeCode;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_user_ad", nullable = false)
    private Boolean isUserAd = false;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_user_nonad", nullable = false)
    private Boolean isUserNonad = false;

    @Size(max = 250)
    @Column(name = "password", length = 250)
    private String password;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt = Instant.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
