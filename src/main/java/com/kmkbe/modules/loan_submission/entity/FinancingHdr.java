package com.kmkbe.modules.loan_submission.entity;

import com.kmkbe.modules.customer.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "financing_hdr")
public class FinancingHdr {
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
    @JoinColumn(name = "cust_code", nullable = false)
    private Customer custCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bouwheer_code", nullable = false)
    private Bouwheer bouwheerCode;

    @NotNull
    @Column(name = "financing_date", nullable = false)
    private Instant financingDate;

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
    private Instant disburseDate;

    @NotNull
    @Column(name = "financing_due_date", nullable = false)
    private Instant financingDueDate;

    @Size(max = 50)
    @NotNull
    @Column(name = "financing_status", nullable = false, length = 50)
    private String financingStatus;

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

    @OneToMany(mappedBy = "financingHdrCode")
    private Set<FinancingDtl> financingDtls = new LinkedHashSet<>();

}
