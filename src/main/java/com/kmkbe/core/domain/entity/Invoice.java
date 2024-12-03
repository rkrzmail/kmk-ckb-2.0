package com.kmkbe.core.domain.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "invoice", schema = "public")
public class Invoice {
    @Builder.Default
    @Id
    @Column(name = "invoice_code", nullable = false)
    private UUID invoiceCode = UUID.randomUUID();

    @Column(
            name = "invoice_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long invoiceId;

    @NotNull(message = "custCode cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            nullable = false,
            updatable = false
    )
    private Customer customer;

    @NotNull(message = "bouwheerCode cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bouwheer_code",
            referencedColumnName = "bouwheer_code",
            nullable = false,
            updatable = false
    )
    private Bouwheer bouwheer;

    @OneToOne(mappedBy = "invoice")
    private FinancingDtl financingDtl;

    @Size(max = 50)
    @NotNull(message = "bouwheerInvNo cannot be null")
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
    private LocalDateTime invoiceDate;

    @NotNull
    @Column(name = "invoice_due_date", nullable = false)
    private LocalDateTime invoiceDueDate;

    @NotNull
    @Column(name = "invoice_amt", nullable = false) // , precision = 17, scale = 2
    private Double invoiceAmt;

    @Column(name = "status")
    private String status;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt") // , nullable = false, length = 50
    private String usrCrt;

    @Builder.Default
    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt = DateTimeUtils.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "posting_date")
    private Date postingDate;
}
