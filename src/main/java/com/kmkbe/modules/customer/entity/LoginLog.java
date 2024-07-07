package com.kmkbe.modules.customer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "login_log", schema = "public")
public class LoginLog {

    @Column(
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long loginLogId;

    @Id
    @Column(nullable = false)
    private UUID loginLogCode;

    @Column(name = "cust_code")
    private UUID custCode;

    @Column(nullable = false, length = 50)
    private String loginRole;

    @Column(nullable = false)
    private OffsetDateTime loginDate;

    @Column
    private Boolean isLogout;

    @Column
    private OffsetDateTime logoutDate;

    @Column
    private OffsetDateTime usrLogout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            insertable = false,
            updatable = false
    )
    private Customer customer;
}
