package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "agreement", schema = "public")
public class Agreement {
    @Id
    @Size(max = 20)
    @Column(name = "agreement_code", nullable = false, length = 20)
    private String agreementCode;

    @Column(
            name = "agreement_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false,
            nullable = false
    )
    private Long agreementId;

    @NotNull(message = "Cwr cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cwr_code",
            referencedColumnName = "cwr_code",
            nullable = false
    )
    private Cwr cwr;

    @Size(max = 20)
    @NotNull(message = "Application code cannot be null")
    @Column(name = "application_code", nullable = false, length = 20)
    private String applicationCode;

    @NotNull(message = "Financing hdr cannot be null")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "financing_hdr_code",
            referencedColumnName = "financing_hdr_code",
            nullable = false
    )
    private FinancingHdr financingHdr;

    @Size(max = 100)
    @NotNull(message = "Facility cannot be null")
    @Column(name = "facility", nullable = false, length = 100)
    private String facility;

    @Size(max = 5)
    @NotNull(message = "Currency cannot be null")
    @Column(name = "currency", nullable = false, length = 5)
    private String currency;

    @NotNull(message = "Financing amt cannot be null")
    @Column(name = "financing_amt", nullable = false)
    private Double financingAmt;

    @Size(max = 20)
    @NotNull(message = "Status cannot be null")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 100)
    @NotNull(message = "Product offering cannot be null")
    @Column(name = "product_offering", nullable = false, length = 100)
    private String productOffering;

    @Size(max = 50)
    @NotNull(message = "Usr crt cannot be null")
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull(message = "Dtm crt cannot be null")
    @ColumnDefault("now()")
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

    @Column(name = "approval_flag")
    private String approvalFlag;

}
