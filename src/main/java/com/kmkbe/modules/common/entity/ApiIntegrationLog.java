package com.kmkbe.modules.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "api_integration_log")
public class ApiIntegrationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_integration_log_id_gen")
    @SequenceGenerator(name = "api_integration_log_id_gen", sequenceName = "api_integration_log_api_log_id_seq", allocationSize = 1)
    @Column(name = "api_log_id", nullable = false)
    private Long apiLogId;

    @Size(max = 500)
    @NotNull
    @Column(name = "endpoint_url", nullable = false, length = 500)
    private String endpointUrl;

    @Size(max = 50)
    @NotNull
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Size(max = 8000)
    @NotNull
    @Column(name = "request_payload", nullable = false, length = 8000)
    private String requestPayload;

    @Size(max = 8000)
    @NotNull
    @Column(name = "response_json", nullable = false, length = 8000)
    private String responseJson;

    @Size(max = 30)
    @NotNull
    @Column(name = "response_status", nullable = false, length = 30)
    private String responseStatus;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
