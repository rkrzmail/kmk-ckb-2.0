package com.kmkbe.core.domain.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity mapping tabel public.api_sbu
 */
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

    // =========================================================
    // Getters & Setters
    // =========================================================

    public Long getSesId() { return sesId; }
    public void setSesId(Long sesId) { this.sesId = sesId; }

    public UUID getBouwheerCode() { return bouwheerCode; }
    public void setBouwheerCode(UUID bouwheerCode) { this.bouwheerCode = bouwheerCode; }

    public String getTokenJwt() { return tokenJwt; }
    public void setTokenJwt(String tokenJwt) { this.tokenJwt = tokenJwt; }

    public String getUsrCrt() { return usrCrt; }
    public void setUsrCrt(String usrCrt) { this.usrCrt = usrCrt; }

    public LocalDateTime getDtmCrt() { return dtmCrt; }
    public void setDtmCrt(LocalDateTime dtmCrt) { this.dtmCrt = dtmCrt; }

    public String getUsrUpd() { return usrUpd; }
    public void setUsrUpd(String usrUpd) { this.usrUpd = usrUpd; }

    public LocalDateTime getDtmUpd() { return dtmUpd; }
    public void setDtmUpd(LocalDateTime dtmUpd) { this.dtmUpd = dtmUpd; }

    public LocalDateTime getExpiredDate() { return expiredDate; }
    public void setExpiredDate(LocalDateTime expiredDate) { this.expiredDate = expiredDate; }

    public String getSesStatus() { return sesStatus; }
    public void setSesStatus(String sesStatus) { this.sesStatus = sesStatus; }

    public String getAppPath() { return appPath; }
    public void setAppPath(String appPath) { this.appPath = appPath; }

    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

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
