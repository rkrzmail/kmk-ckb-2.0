package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "disbursement_log", schema = "public")
public class DisbursementLog {
    @Column(
            name = "disbursement_id",
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long disbursementId;

    @Id
    @Column(name = "disbursement_code", nullable = false)
    private UUID disbursementCode;

    @NotNull(message = "Agreement cannot be null")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "agreement_code",
            referencedColumnName = "agreement_code",
            nullable = false
    )
    private Agreement agreement;

    @Size(max = 20)
    @Column(name = "ap_no", length = 20)
    private String apNo;

    @Size(max = 500)
    @Column(name = "ap_desc", length = 500)
    private String apDesc;

    @Size(max = 20)
    @Column(name = "currency", length = 20)
    private String currency;

    @Column(name = "ap_amt")
    private Double apAmt;

    @Column(name = "ap_paid_amt")
    private Double apPaidAmt;

    @Column(name = "ap_amt_inprocess")
    private Double apAmtInprocess;

    @Column(name = "ap_unpaid_amt")
    private Double apUnpaidAmt;

    @Size(max = 10)
    @Column(name = "ap_type_code", length = 10)
    private String apTypeCode;

    @Size(max = 150)
    @Column(name = "ap_type_name", length = 150)
    private String apTypeName;

    @Column(name = "ap_due_date")
    private Instant apDueDate;

    @Size(max = 3)
    @Column(name = "branch_code", length = 3)
    private String branchCode;

    @Size(max = 3)
    @Column(name = "ap_paid_location", length = 3)
    private String apPaidLocation;

    @Size(max = 50)
    @Column(name = "usr_crt", length = 50)
    private String usrCrt;

    @ColumnDefault("now()")
    @Column(name = "dtm_crt")
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;
}
