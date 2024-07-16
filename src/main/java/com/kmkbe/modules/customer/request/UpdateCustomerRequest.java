package com.kmkbe.modules.customer.request;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateCustomerRequest {
    private String custType;
    private String name;
    private String custCompanyType;
    private String email;
    private String identityType;
    private String identityNo;
    private Date identityIssuedDate;
    private Date identityExpiredDate;
    private String npwp;
    private String custModel;
    private String companyAddress;
    private String rt;
    private String rw;
    private String zipCode;
    private String kelurahan;
    private String kecamatan;
    private String city;
    private String area;
    private String province;
    private String phone;
    private String ownershipStatus;
    private Date staySince;
}
