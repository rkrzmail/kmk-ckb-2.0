package com.kmkbe.modules.api_sbu.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity mapping tabel public.api_sbu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_sbu", schema = "public")
public class ApiSbu {

    @Id
    @Column(name = "ses_id", nullable = false)
    private Long sesId;

    @Column(name = "bouwheer_code", nullable = false)
    private UUID bouwheerCode;

    @Column(name = "token_jwt", nullable = false, length = 255)
    private String tokenJwt;

    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;

    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

    @Column(name = "expired_date")
    private LocalDateTime expiredDate;

    @Column(name = "ses_status", length = 64)
    private String sesStatus;

    @Column(name = "app_path", length = 64)
    private String appPath;

    @Column(name = "app_key", length = 64)
    private String appKey;

    @Column(name = "app_secret", length = 64)
    private String appSecret;

    @Column(name = "app_name", length = 64)
    private String appName;

    @Override
    public String toString() {
        return "ApiSbu{" +
                "sesId=" + sesId +
                ", bouwheerCode=" + bouwheerCode +
                ", appName='" + appName + '\'' +
                ", appKey='" + appKey + '\'' +
                ", sesStatus='" + sesStatus + '\'' +
                ", expiredDate=" + expiredDate +
                '}';
    }
}
