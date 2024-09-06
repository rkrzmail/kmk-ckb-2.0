package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "simulation_hist", schema = "public")
public class SimulationHist {
    @Builder.Default
    @Id
    @Column(name = "simulation_hist_code", nullable = false)
    private UUID simulationHistCode = UUID.randomUUID();

    @Column(
            name = "simulation_hist_id",
            columnDefinition = "serial",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Long simulationHistId;

    @NotNull(message = "financingHdr null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "financing_hdr_code",
            referencedColumnName = "financing_hdr_code",
            nullable = false,
            updatable = false
    )
    private FinancingHdr financingHdr;

    @NotNull(message = "totalInvoiceAmt null")
    @Column(name = "total_invoice_amt", nullable = false)
    private Double totalInvoiceAmt;

    @NotNull(message = "retention null")
    @Column(name = "retention", nullable = false)
    private Double retention;

    @NotNull(message = "adminAmt null")
    @Column(name = "admin_amt", nullable = false)
    private Double adminAmt;

    @NotNull(message = "financingAmt null")
    @Column(name = "financing_amt", nullable = false)
    private Double financingAmt;

    @Builder.Default
    @NotNull(message = "isUsed null")
    @ColumnDefault("false")
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Size(max = 50)
    @NotNull(message = "usrCrt null")
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
