package com.kmkbe.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class CustomerCompanyDto {
    //private UUID custCode;
    private String custCompanyType;
    private String companyModel;
    private String identityType;
    private String identityNo;
    private Instant identityIssuedDate;
    private Instant identityExpiredDate;
    private String companyAddress;
    private String rt;
    private String rw;
    private String kelurahan;
    private String kecamatan;
    private String city;
    private String province;
    private String zipcode;
    private String area;
    private String phone;
    private String ownershipStatus;
    private Instant staySince;
    private Double stayLength;
}
