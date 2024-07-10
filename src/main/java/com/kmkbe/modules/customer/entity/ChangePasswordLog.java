package com.kmkbe.modules.customer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cust_code", nullable = false)
    private Customer custCode;

    @Column(nullable = false, length = 250)
    private String oldPin;

    @Column(nullable = false, length = 250)
    private String newPin;

    @Column(nullable = false, length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private OffsetDateTime dtmCrt;
}
