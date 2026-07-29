package com.kmkbe.core.domain.entity;

import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstBranch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "financing_hdr", schema = "public")
public class FinancingHdr implements Serializable {
    @Id
    @Column(name = "financing_hdr_code", nullable = false)
    private UUID financingHdrCode;

    @Column(
            name = "financing_hdr_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long financingHdrId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            nullable = false,
            updatable = false
    )
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bouwheer_code",
            referencedColumnName = "bouwheer_code",
            nullable = false,
            updatable = false
    )
    private Bouwheer bouwheer;

    @NotNull
    @Column(name = "financing_date", nullable = false)
    private LocalDateTime financingDate;

    @Size(max = 5)
    @NotNull
    @Column(name = "currency_code", nullable = false, length = 5)
    private String currencyCode;

    @NotNull
    @Column(name = "invoice_qty", nullable = false)
    private Long invoiceQty;

    @Size(max = 20)
    @NotNull
    @Column(name = "interest_type", nullable = false, length = 20)
    private String interestType;

    @NotNull
    @Column(name = "tenor", nullable = false)
    private Long tenor;

    @NotNull
    @Column(name = "effective_rate", nullable = false) // , precision = 5, scale = 2
    private Double effectiveRate;

    @NotNull
    @Column(name = "interest_amt", nullable = false) // , precision = 17, scale = 2
    private Double interestAmt;

    @NotNull
    @Column(name = "term_of_payment", nullable = false)
    private Long termOfPayment;

    @NotNull
    @Column(name = "grace_period", nullable = false)
    private Long gracePeriod;

    @NotNull
    @Column(name = "retention", nullable = false) // , precision = 5, scale = 2
    private Double retention;

    @NotNull
    @Column(name = "total_invoice_amt", nullable = false) // , precision = 17, scale = 2
    private Double totalInvoiceAmt;

    @NotNull
    @Column(name = "provision_fee_percentage", nullable = false) // , precision = 5, scale = 2
    private Double provisionFeePercentage;

    @NotNull
    @Column(name = "provision_fee_amt", nullable = false) // , precision = 17, scale = 2
    private Double provisionFeeAmt;

    @NotNull
    @Column(name = "survey_fee_amt", nullable = false) // , precision = 17, scale = 2
    private Double surveyFeeAmt;

    @NotNull
    @Column(name = "survey_fee_amt_nett", nullable = false) // , precision = 17, scale = 2
    private Double surveyFeeAmtNett;

    @NotNull
    @Column(name = "legal_fee_amt", nullable = false) // , precision = 17, scale = 2
    private Double legalFeeAmt;

    @NotNull
    @Column(name = "legal_fee_amt_nett", nullable = false) //  precision = 17, scale = 2
    private Double legalFeeAmtNett;

    @NotNull
    @Column(name = "admin_limit_amt", nullable = false) // , precision = 17, scale = 2
    private Double adminLimitAmt = 0.0;

    @NotNull
    @Column(name = "admin_fee_percentage", nullable = false) // , precision = 5, scale = 2
    private Double adminFeePercentage = 0.0;

    @NotNull
    @Column(name = "admin_fee_amt", nullable = false) // , precision = 17, scale = 2
    private Double adminFeeAmt = 0.0;

    @NotNull
    @Column(name = "insurance_fee_percentage", nullable = false) // , precision = 5, scale = 2
    private Double insuranceFeePercentage = 0.0;

    @NotNull
    @Column(name = "insurance_fee_amt", nullable = false) // , precision = 17, scale = 2
    private Double insuranceFeeAmt = 0.0;

    @NotNull
    @Column(name = "others_fee_amt", nullable = false) // , precision = 17, scale = 2
    private Double othersFeeAmt;

    @NotNull
    @Column(name = "financing_amt", nullable = false) // , precision = 17, scale = 2
    private Double financingAmt;

    @NotNull
    @Column(name = "disburse_amt", nullable = false) // , precision = 17, scale = 2
    private Double disburseAmt;

    @NotNull
    @Column(name = "disburse_date", nullable = false) // , precision = 17, scale = 2
    private LocalDateTime disburseDate;

    @NotNull
    @Column(name = "financing_due_date", nullable = false)
    private LocalDateTime financingDueDate;

    @NotNull
    @Column(name = "financing_status", nullable = false)
    private String financingStatus;

    @NotNull
    @Column(name = "financing_step", nullable = false)
    private String financingStep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_code",
            referencedColumnName = "branch_code",
            nullable = false
    )
    private MstBranch mstBranch;

    @OneToMany(mappedBy = "financingHdr")
    private Set<FinancingDtl> financingDtls;

    @OneToMany(mappedBy = "financingHdr")
    private Set<SimulationHist> simulationHistories;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

    @OneToMany(mappedBy = "financingHdr")
    private Set<Agreement> agreement;

    @Column(name = "fap_date")
    private LocalDateTime fapDate;

    @Column(name = "fap_status")
    private String fapStatus;

    @Column(name = "vendor_id")
    private String vendorId;
}
