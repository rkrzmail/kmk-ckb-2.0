package com.kmkbe.core.domain.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "api_integration_log")
public class ApiIntegrationLog {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "api_integration_log_id_gen"
    )
    @SequenceGenerator(
            name = "api_integration_log_id_gen",
            sequenceName = "api_integration_log_api_log_id_seq",
            allocationSize = 1
    )
    @Column(
            name = "api_log_id",
            columnDefinition = "serial"
    )
    private Long apiLogId;

    @Size(max = 500)
    @NotNull
    @Column(name = "endpoint_url", nullable = false, length = 500)
    private String endpointUrl;

    @NotNull
    @Column(name = "content_type", nullable = false)
    private String contentType;

    @NotNull
    @Column(name = "request_payload", nullable = false)
    private String requestPayload;

    @NotNull
    @Column(name = "response_json", nullable = false)
    private String responseJson;

    @Size(max = 30)
    @NotNull
    @Column(name = "response_status", nullable = false, length = 30)
    private String responseStatus;

    @Builder.Default
    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt = "system";

    @Builder.Default
    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt = DateTimeUtils.now();

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

}
