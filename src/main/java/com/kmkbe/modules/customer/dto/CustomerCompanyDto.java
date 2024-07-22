package com.kmkbe.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class CustomerCompanyDto {
    private String custCompanyType;
    private String companyModel;
    private String identityType;
    private String identityNo;
    private Date identityIssuedDate;
    private Date identityExpiredDate;
    private String companyAddress;
    /*private String rt;
    private String rw;
    private String kelurahan;
    private String kecamatan;
    private String city;
    private String province;
    private String zipCode;
    private String area;*/
    private String phone;
    private String ownershipStatus;
    private Date staySince;
    private Double stayLength;
}
