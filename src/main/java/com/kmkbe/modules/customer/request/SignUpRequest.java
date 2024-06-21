package com.kmkbe.modules.customer.request;

import com.kmkbe.modules.customer.constant.CompanyModel;
import com.kmkbe.modules.customer.constant.CustomerModel;
import com.kmkbe.modules.customer.constant.CustomerType;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.OffsetDateTime;

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

    @Nullable
    private Company company;

    @Nullable
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
        private OffsetDateTime staySince;
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
        private OffsetDateTime identityIssuedDate;
        private OffsetDateTime identityExpiredDate;
        private String companyAddress;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Personal extends AddressDetail {
        private String birthPlace;
        private OffsetDateTime birthDate;
        private String gender;
        private String identityType;
        private String identityNo;
        private OffsetDateTime expiredDate;
        private String motherMaidenName;
        private String maritalStatus;
        private CustomerModel customerModel;
        private String legalAddress;
    }
}
