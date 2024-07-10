package com.kmkbe.modules.kredit.entity;

import com.kmkbe.modules.customer.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @Column(name = "invoice_code", nullable = false)
    private UUID invoiceCode;

    @NotNull
    @ColumnDefault("nextval('invoice_invoice_id_seq'::regclass)")
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cust_code", nullable = false)
    private Customer custCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bouwheer_code", nullable = false)
    private Bouwheer bouwheerCode;

    @Size(max = 50)
    @NotNull
    @Column(name = "bouwheer_inv_no", nullable = false, length = 50)
    private String bouwheerInvNo;

    @Size(max = 50)
    @NotNull
    @Column(name = "cust_inv_no", nullable = false, length = 50)
    private String custInvNo;

    @Size(max = 250)
    @Column(name = "invoice_description", length = 250)
    private String invoiceDescription;

    @NotNull
    @Column(name = "invoice_date", nullable = false)
    private Instant invoiceDate;

    @NotNull
    @Column(name = "invoice_due_date", nullable = false)
    private Instant invoiceDueDate;

    @NotNull
    @Column(name = "invoice_amt", nullable = false, precision = 17, scale = 2)
    private BigDecimal invoiceAmt;

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
