package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cwr", schema = "public")
public class Cwr {
    @Id
    @Size(max = 20)
    @Column(name = "cwr_code", nullable = false, length = 20)
    private String cwrCode;

    @ColumnDefault("nextval('cwr_cwr_id_seq'::regclass)")
    @Column(name = "cwr_id", nullable = false)
    private Long cwrId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            nullable = false
    )
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bouwheer_code",
            referencedColumnName = "bouwheer_code",
            nullable = false
    )
    private Bouwheer bouwheer;

    @Size(max = 5)
    @NotNull
    @Column(name = "branch_code", nullable = false, length = 5)
    private String branchCode;

    @Size(max = 100)
    @NotNull
    @Column(name = "cwr_type", nullable = false, length = 100)
    private String cwrType;

    @Size(max = 300)
    @NotNull
    @Column(name = "cwr_type_desc", nullable = false, length = 300)
    private String cwrTypeDesc;

    @Size(max = 100)
    @NotNull
    @Column(name = "facility", nullable = false, length = 100)
    private String facility;

    @NotNull
    @Column(name = "is_revolving", nullable = false)
    private Boolean isRevolving = false;

    @Size(max = 5)
    @NotNull
    @Column(name = "currency", nullable = false, length = 5)
    private String currency;

    @NotNull
    @Column(name = "cwr_start_date", nullable = false)
    private Instant cwrStartDate;

    @NotNull
    @Column(name = "cwr_end_date", nullable = false)
    private Instant cwrEndDate;

    @NotNull
    @Column(name = "plafond_amt", nullable = false)
    private Double plafondAmt;

    @NotNull
    @Column(name = "realisation_amt", nullable = false)
    private Double realisationAmt;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

    @OneToMany(mappedBy = "cwr")
    private Set<Agreement> agreements = new LinkedHashSet<>();

}
