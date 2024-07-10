package com.kmkbe.modules.common.entity;

import com.kmkbe.modules.customer.entity.LoginLog;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "error_log")
public class ErrorLog {
    @Id
    @ColumnDefault("nextval('error_log_error_log_id_seq'::regclass)")
    @Column(name = "error_log_id", nullable = false)
    private Long errorLogId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "login_log_code", nullable = false)
    private LoginLog loginLogCode;

    @Size(max = 100)
    @Column(name = "error_type", length = 100)
    private String errorType;

    @Size(max = 10)
    @Column(name = "error_line", length = 10)
    private String errorLine;

    @Size(max = 500)
    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Size(max = 500)
    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @Size(max = 100)
    @Column(name = "method_name", length = 100)
    private String methodName;

    @Size(max = 1000)
    @Column(name = "request_param", length = 1000)
    private String requestParam;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;
}
