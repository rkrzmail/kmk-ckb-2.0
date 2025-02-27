package com.kmkbe.core.domain.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
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

    @Builder.Default
    @Id
    @Column(name = "cust_company_code", nullable = false)
    private UUID custCompanyCode = UUID.randomUUID();

    @NotNull
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "cust_code",
            referencedColumnName = "cust_code",
            nullable = false,
            updatable = false
    )
    private Customer customer;

    @Column(length = 50)
    private String custCompanyType = "";

    @Column(length = 200)
    private String companyModel = "";

    @Column(length = 50)
    private String identityType = "";

    @Column(length = 50)
    private String identityNo = "";

    @Column
    private LocalDateTime identityIssuedDate = DateTimeUtils.now();

    @Column
    private LocalDateTime identityExpiredDate = DateTimeUtils.now();

    @Column(length = 1000)
    private String companyAddress = "";

    @Column(length = 5)
    private String rt = "";

    @Column(length = 5)
    private String rw = "";

    @Column(length = 50)
    private String kelurahan = "";

    @Column(length = 50)
    private String kecamatan = "";

    @Column(length = 50)
    private String city = "";

    @Column(length = 50)
    private String province = "";

    @Column(name = "zipcode", length = 10)
    private String zipCode = "";

    @Column(name = "area", length = 5)
    private String area = "";

    @Column(length = 20)
    private String phone = "";

    @Column(length = 50)
    private String ownershipStatus = "";

    @Column
    private LocalDateTime staySince = DateTimeUtils.now();

    @Column // precision = 7, scale = 2
    private Double stayLength = 0.0;

    @Column(length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private LocalDateTime dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    private LocalDateTime dtmUpd;

    @Column(length = 50)
    private String directorName;

}
