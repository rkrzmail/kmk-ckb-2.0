package com.kmkbe.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "customer_company", schema = "public")
public class CustomerCompany {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1,
            initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long custCompanyCode;

    @Column(nullable = false)
    private UUID custCode;

    @Column(length = 50)
    private String custCompanyType;

    @Column(length = 200)
    private String companyModel;

    @Column(length = 50)
    private String identityType;

    @Column(length = 50)
    private String identityNo;

    @Column
    private OffsetDateTime identityIssuedDate;

    @Column
    private OffsetDateTime identityExpiredDate;

    @Column(length = 1000)
    private String companyAddress;

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

    @Column(length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private OffsetDateTime dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column(nullable = false)
    private OffsetDateTime dtmUpd;
}
