package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
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
    @Column(
            name = "cwr_id",
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long cwrId;

    @NotNull(message = "Customer cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            nullable = false
    )
    private Customer customer;

    @NotNull(message = "Bouwheer cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bouwheer_code",
            referencedColumnName = "bouwheer_code",
            nullable = false
    )
    private Bouwheer bouwheer;

    @Size(max = 5)
    @NotNull(message = "Branch code cannot be null")
    @Column(name = "branch_code", nullable = false, length = 5)
    private String branchCode;

    @Size(max = 100)
    @NotNull(message = "Cwr type cannot be null")
    @Column(name = "cwr_type", nullable = false, length = 100)
    private String cwrType;

    @Size(max = 300)
    @NotNull(message = "Cwr type description cannot be null")
    @Column(name = "cwr_type_desc", nullable = false, length = 300)
    private String cwrTypeDesc;

    @Size(max = 100)
    @NotNull(message = "Facility cannot be null")
    @Column(name = "facility", nullable = false, length = 100)
    private String facility;

    @NotNull(message = "Is revolving cannot be null")
    @Column(name = "is_revolving", nullable = false)
    private Boolean isRevolving = false;

    @Size(max = 5)
    @NotNull(message = "Currency cannot be null")
    @Column(name = "currency", nullable = false, length = 5)
    private String currency;

    @NotNull(message = "Cwr start date cannot be null")
    @Column(name = "cwr_start_date", nullable = false)
    private LocalDateTime cwrStartDate;

    @NotNull(message = "Cwr end date cannot be null")
    @Column(name = "cwr_end_date", nullable = false)
    private LocalDateTime cwrEndDate;

    @NotNull(message = "Plafond amount cannot be null")
    @Column(name = "plafond_amt", nullable = false)
    private Double plafondAmt;

    @NotNull(message = "Realisation amount cannot be null")
    @Column(name = "realisation_amt", nullable = false)
    private Double realisationAmt;

    @Size(max = 20)
    @NotNull(message = "Status cannot be null")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 50)
    @NotNull(message = "UsrCrt cannot be null")
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull(message = "DtmCrt cannot be null")
    @ColumnDefault("now()")
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

    @OneToMany(mappedBy = "cwr")
    private Set<Agreement> agreements = new LinkedHashSet<>();

}
