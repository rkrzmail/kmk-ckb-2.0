package com.kmkbe.modules.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;


@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "otp_log", schema = "public")
public class OtpLog {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1
            //initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long otpLogId;

    @Column(nullable = false, length = 10)
    private String otpCode;

    @Column(nullable = false, length = 20)
    private String mobilePhone;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private OffsetDateTime generatedDate;

    @Column(nullable = false)
    private OffsetDateTime expiredDate;

    @Column(nullable = false)
    private Boolean isUsed;

    @Column(nullable = false, length = 50)
    private String usrCrt;

    @Column
    private OffsetDateTime dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column
    private OffsetDateTime dtmUpd;
}
