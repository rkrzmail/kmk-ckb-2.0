package com.kmkbe.modules.user.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mst_application", schema = "users")
public class MstApplication {
    @Id
    @Size(max = 20)
    @Column(name = "application_code", nullable = false, length = 20)
    private String applicationCode;

    @NotNull
    @ColumnDefault("nextval('users.mst_application_application_id_seq'::regclass)")
    @Column(
            name = "application_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long applicationId;

    @Size(max = 50)
    @NotNull
    @Column(name = "application_name", nullable = false, length = 50)
    private String applicationName;

    @Size(max = 250)
    @NotNull
    @Column(name = "application_desc", nullable = false, length = 250)
    private String applicationDesc;

    @Size(max = 1000)
    @Column(name = "path_icon", length = 1000)
    private String pathIcon;

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
    private Instant dtmCrt = DateTimeUtils.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
