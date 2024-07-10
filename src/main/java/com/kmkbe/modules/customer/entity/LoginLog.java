package com.kmkbe.modules.customer.entity;

import com.kmkbe.modules.common.entity.ErrorLog;
import com.kmkbe.modules.common.entity.FormVisitLog;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "login_log")
public class LoginLog {
    @Id
    @Column(name = "login_log_code", nullable = false)
    private UUID loginLogCode;

    @ColumnDefault("nextval('login_log_login_log_id_seq'::regclass)")
    @Column(
            name = "login_log_id",
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long loginLogId;

    @Size(max = 50)
    @NotNull
    @Column(name = "login_role", nullable = false, length = 50)
    private String loginRole;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cust_code", nullable = false)
    private Customer custCode;

    @NotNull
    @Column(name = "login_date", nullable = false)
    private Instant loginDate;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_logout", nullable = false)
    private Boolean isLogout = false;

    @Column(name = "logout_date")
    private Instant logoutDate;

    @Column(name = "usr_logout")
    private Instant usrLogout;

    @OneToMany(mappedBy = "loginLogCode")
    private Set<ErrorLog> errorLogs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "loginLogCode")
    private Set<FormVisitLog> formVisitLogs = new LinkedHashSet<>();

}
