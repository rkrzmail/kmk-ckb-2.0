package com.kmkbe.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "job_log")
public class JobLog {
    @Id
    @ColumnDefault("nextval('job_log_job_log_id_seq'::regclass)")
    @Column(name = "job_log_id", nullable = false)
    private Long jobLogId;

    @Size(max = 100)
    @NotNull
    @Column(name = "server_name", nullable = false, length = 100)
    private String serverName;

    @Size(max = 100)
    @NotNull
    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Size(max = 50)
    @NotNull
    @Column(name = "job_type", nullable = false, length = 50)
    private String jobType;

    @Size(max = 200)
    @Column(name = "job_description", length = 200)
    private String jobDescription;

    @Size(max = 20)
    @NotNull
    @Column(name = "frequency", nullable = false, length = 20)
    private String frequency;

    @Size(max = 1000)
    @Column(name = "custom_config", length = 1000)
    private String customConfig;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Size(max = 8000)
    @NotNull
    @Column(name = "job_script", nullable = false, length = 8000)
    private String jobScript;

    @NotNull
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = false;

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
