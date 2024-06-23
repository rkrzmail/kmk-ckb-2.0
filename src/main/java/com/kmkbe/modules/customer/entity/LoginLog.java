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
    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long loginLogId;

    @Column(nullable = false)
    private UUID loginLogCode;

    @Column(nullable = false)
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

}
