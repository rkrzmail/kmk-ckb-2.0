package com.kmkbe.modules.internal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mst_module", schema = "users")
public class MstModule {
    @Id
    @Size(max = 20)
    @Column(name = "module_code", nullable = false, length = 20)
    private String moduleCode;

    @NotNull
    @ColumnDefault("nextval('users.mst_module_module_id_seq'::regclass)")
    @Column(
            name = "module_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long moduleId;

    @Size(max = 50)
    @NotNull
    @Column(name = "module_name", nullable = false, length = 50)
    private String moduleName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_code", nullable = false)
    private MstApplication applicationCode;

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
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
