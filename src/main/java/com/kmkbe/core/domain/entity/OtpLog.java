package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


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
            name = "otp_log_otp_log_id_seq",
            sequenceName = "otp_log_otp_log_id_seq",
            allocationSize = 1
            //initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "otp_log_otp_log_id_seq"
    )
    private Long otpLogId;

    @Column(nullable = false, length = 10)
    private String otpCode;

    @Column(nullable = false, length = 20)
    private String mobilePhone;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private Instant generatedDate;

    @Column(nullable = false)
    private Instant expiredDate;

    @Column(nullable = false)
    private Boolean isUsed;

    @Column(nullable = false, length = 50)
    private String usrCrt;

    @Column
    private Instant dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column
    private Instant dtmUpd;
}
