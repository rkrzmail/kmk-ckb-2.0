package com.kmkbe.modules.customer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_personal", schema = "public")
public class CustomerPersonal {

    @Column(
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long custPersonalId;

    @Id
    @Column(nullable = false)
    private UUID custPersonalCode;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            nullable = false,
            updatable = false
    )
    private Customer custCode;

    @Column(name = "birthplace", length = 50)
    private String birthPlace;

    @Column(name = "birthdate")
    private Instant birthDate;

    @Column(length = 10)
    private String gender;

    @Column(length = 50)
    private String identityType;

    @Column(length = 50)
    private String identityNo;

    @Column
    private Instant expiredDate;

    @Column(length = 50)
    private String motherMaidenName;

    @Column(length = 20)
    private String maritalStatus;

    @Column(length = 50)
    private String custModel;

    @Column(length = 1000)
    private String legalAddress;

    @Column(length = 5)
    private String rt;

    @Column(length = 5)
    private String rw;

    @Column(length = 50)
    private String kelurahan;

    @Column(length = 50)
    private String kecamatan;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String province;

    @Column(name = "zipcode", length = 10)
    private String zipCode;

    @Column(length = 5)
    private String area;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String ownershipStatus;

    @Column
    private Instant staySince;

    @Column // precision = 7, scale = 2
    private Double stayLength;

    @Column(length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private Instant dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column(nullable = false)
    private Instant dtmUpd;
}
