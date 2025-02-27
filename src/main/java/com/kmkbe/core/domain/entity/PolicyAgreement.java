package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "policy_agreement")
public class PolicyAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "policy_code", nullable = false)  // Pastikan kolom ini tidak null
    private String policyCode;

    @Column(name = "policy_name", nullable = false)
    private String policyName;

    @Column(name = "policy_description", nullable = false)
    private String policyDescription;

    @Column(name = "policy_content", nullable = false)
    private String policyContent;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "usr_crt", nullable = false)
    private String usrCrt;

    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;

    @Column(name = "usr_upd")
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;
}
