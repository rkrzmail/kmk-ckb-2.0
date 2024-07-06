package com.kmkbe.modules.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "customer_company", schema = "public")
public class CustomerCompany implements Serializable {

    @Column(
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long custCompanyId;

    @Id
    @Column(name = "cust_company_code", nullable = false)
    private UUID custCompanyCode;

    @Column(name = "cust_code", nullable = false)
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

    @OneToOne
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            insertable = false,
            updatable = false
    )
    private Customer customer;
}
