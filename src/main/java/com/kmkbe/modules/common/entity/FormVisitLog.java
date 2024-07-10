package com.kmkbe.modules.common.entity;

import com.kmkbe.modules.customer.entity.LoginLog;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "form_visit_log")
public class FormVisitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "form_visit_log_id_gen")
    @SequenceGenerator(name = "form_visit_log_id_gen", sequenceName = "form_visit_log_form_visit_id_seq", allocationSize = 1)
    @Column(name = "form_visit_id", nullable = false)
    private Long formVisitId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "login_log_code", nullable = false)
    private LoginLog loginLogCode;

    @Size(max = 20)
    @NotNull
    @Column(name = "module_code", nullable = false, length = 20)
    private String moduleCode;

    @Size(max = 20)
    @NotNull
    @Column(name = "form_code", nullable = false, length = 20)
    private String formCode;

    @Size(max = 1000)
    @NotNull
    @Column(name = "path_access", nullable = false, length = 1000)
    private String pathAccess;

    @NotNull
    @Column(name = "access_date", nullable = false)
    private Instant accessDate;

}
