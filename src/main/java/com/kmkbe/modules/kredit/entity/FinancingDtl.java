package com.kmkbe.modules.kredit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "financing_dtl")
public class FinancingDtl {
    @Id
    @Column(name = "financing_dtl_code", nullable = false)
    private UUID financingDtlCode;

    @NotNull
    @ColumnDefault("nextval('financing_dtl_financing_dtl_id_seq'::regclass)")
    @Column(name = "financing_dtl_id", nullable = false)
    private Long financingDtlId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financing_hdr_code", nullable = false)
    private FinancingHdr financingHdrCode;

    @Size(max = 50)
    @NotNull
    @Column(name = "bouwheer_inv_no", nullable = false, length = 50)
    private String bouwheerInvNo;

    @NotNull
    @Column(name = "invoice_seqno", nullable = false)
    private Long invoiceSeqno;

    @Column(name = "paid_to_cust_date")
    private Instant paidToCustDate;

    @Column(name = "bouwheer_paid_date")
    private Instant bouwheerPaidDate;

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
