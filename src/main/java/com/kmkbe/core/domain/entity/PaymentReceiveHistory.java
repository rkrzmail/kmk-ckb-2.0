package com.kmkbe.core.domain.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payment_receive_history", schema = "public")
public class PaymentReceiveHistory {
    @Builder.Default
    @Id
    @Column(name = "payment_receive_hist_code", nullable = false)
    private UUID paymentReceiveHistCode = UUID.randomUUID();

    @Column(
            name = "payment_receive_hist_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long paymentReceiveHistId;


    /*@NotNull(message = "agreement_code cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "agreement_code",
            referencedColumnName = "agreement_code",
            nullable = false,
            updatable = false
    )
    private Agreement agreement;*/
    @Size(max = 50)
    @NotNull
    @Column(name = "agreement_code", nullable = false, length = 50)
    private String agreementCode;


    @NotNull(message = "bouwheerCode cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bouwheer_code",
            referencedColumnName = "bouwheer_code",
            nullable = false,
            updatable = false
    )
    private Bouwheer bouwheer;



    @Size(max = 50)
    @NotNull
    @Column(name = "currency", nullable = false, length = 50)
    private String currency;


    @Column(name = "golive_date", nullable = false)
    private Instant goliveDate;

    @NotNull
    @Column(name = "effective_date", nullable = false)
    private Instant effectiveDate;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private Instant dueDate;


    @Column(name = "settlement_date", nullable = false)
    private Instant settlementDte;

    @NotNull
    @Column(name = "settlement_amt", nullable = false)
    private double settlementAmt;


    @Column(name = "refund_amt", nullable = false)
    private double refundAmt;


    @Column(name = "real_tenor")
    private int realTenor;


    @NotNull
    @Column(name = "ntf_amt") // , nullable = false, length = 50
    private double ntfAmt;


    @NotNull
    @Column(name = "total_inv_amt", nullable = false)
    private double totalInvAmt  ;



    @NotNull
    @Column(name = "lc_rate", nullable = false)
    private double lcRate  ;

    @NotNull
    @Column(name = "lc_days", nullable = false)
    private int lcDays;


    @NotNull
    @Column(name = "lc_amt", nullable = false)
    private double lcAmt  ;

    @NotNull
    @Column(name = "interest_amt", nullable = false)
    private double interestAmt  ;



    @Column(name = "payment_receive_no" )
    private String paymentReceiveNo  ;

    @NotNull
    @Column(name = "retention", nullable = false)
    private double retention  ;

    @NotNull
    @Column(name = "retention_amt", nullable = false)
    private double retentionAmt  ;



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
