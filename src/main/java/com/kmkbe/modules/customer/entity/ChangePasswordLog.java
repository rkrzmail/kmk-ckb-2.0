package com.kmkbe.modules.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "change_password_log", schema = "public")
public class ChangePasswordLog {

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
    private Long changePasswordId;

    @Column(name = "cust_code", nullable = false)
    private UUID custCode;

    @Column(nullable = false, length = 250)
    private String oldPin;

    @Column(nullable = false, length = 250)
    private String newPin;

    @Column(nullable = false, length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private OffsetDateTime dtmCrt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Customer customer;
}
