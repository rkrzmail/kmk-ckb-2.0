package com.kmkbe.modules.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "customer_personal", schema = "public")
public class CustomerPersonal {
    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1
            //initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long custPersonalCode;

    @Column(nullable = false)
    private UUID custCode;

    @Column(length = 50)
    private String birthplace;

    @Column
    private OffsetDateTime birthdate;

    @Column(length = 10)
    private String gender;

    @Column(length = 50)
    private String identityType;

    @Column(length = 50)
    private String identityNo;

    @Column
    private OffsetDateTime expiredDate;

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

    @Column(length = 10)
    private String zipcode;

    @Column(length = 5)
    private String area;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String ownershipStatus;

    @Column
    private OffsetDateTime staySince;

    @Column(precision = 7, scale = 2)
    private BigDecimal stayLength;

    @Column(length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private OffsetDateTime dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column(nullable = false)
    private OffsetDateTime dtmUpd;
}
