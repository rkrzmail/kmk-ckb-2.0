package com.kmkbe.modules.customer.request;

import com.kmkbe.modules.customer.constant.CompanyModel;
import com.kmkbe.modules.customer.constant.CustomerModel;
import com.kmkbe.modules.customer.constant.CustomerType;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SignUpRequest {

    private String name;
    private CustomerType customerType;
    private String customerIdNo;
    private String customerNo; // Kode Confins R3 (external req)
    private String email;
    private String mobilePhone;
    private String pin;
    private Boolean isAgreeTc;
    private Company company;
    private Personal personal;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressDetail {
        private String rt;
        private String rw;
        private String kelurahan;
        private String kecamatan;
        private String city;
        private String province;
        private String zipCode;
        private String area;
        private String phone;
        private String ownershipStatus;
        private Instant staySince;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Company extends AddressDetail {
        private String companyType;
        private CompanyModel companyModel;
        private String identityType;
        private String identityNo;
        private Instant identityIssuedDate;
        private Instant identityExpiredDate;
        private String companyAddress;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Personal extends AddressDetail {
        private String birthPlace;
        private Instant birthDate;
        private String gender;
        private String identityType;
        private String identityNo;
        private Instant expiredDate;
        private String motherMaidenName;
        private String maritalStatus;
        private CustomerModel customerModel;
        private String legalAddress;
    }
}
