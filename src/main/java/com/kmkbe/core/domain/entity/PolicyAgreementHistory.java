package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "policy_agreement_history")
public class PolicyAgreementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_history_id", nullable = false)
    private Long policyId;

    @Column(name = "policy_code", nullable = false)  // Pastikan kolom ini tidak null
    private String policyCode;

    @Column(name = "policy_content", nullable = false)
    private String policyContent;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "usr_crt", nullable = false)
    private String usrCrt;

    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;

}
