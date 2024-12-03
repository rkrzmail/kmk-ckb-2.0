package com.kmkbe.core.domain.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "error_log")
public class ErrorLog {
    @Id
    @SequenceGenerator(
            name = "error_log_error_log_id_seq",
            sequenceName = "error_log_error_log_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "error_log_error_log_id_seq"
    )
    @Column(name = "error_log_id", nullable = false)
    private Long errorLogId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "login_log_code")
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

    @Builder.Default
    @Size(max = 50)
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt = "system";

    @Builder.Default
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt = DateTimeUtils.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;
}
