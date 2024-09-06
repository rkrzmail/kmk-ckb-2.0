package com.kmkbe.modules.customer.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UpdateCustomerRequest {
    @NotNull(message = "Nama is required, key: custName")
    private String custName;

    @NotNull(message = "Email is required, key: custEmail")
    private String custEmail;

    @NotNull(message = "Tipe Debitur is required, key: custTypeCode")
    private String custTypeCode;

    @NotNull(message = "KTP/NPWP is required, key: custIdNo")
    private String custIdNo;

    @NotNull(message = "Detail Alamat is required, key: address")
    private UpdateAddressRequest address;

    private UpdateCompanyRequest company;
    private UpdatePersonalRequest personal;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class UpdateCompanyRequest extends CommonField {
        @NotNull(message = "Tipe Perusahaan is required, key: custCompanyType")
        private String custCompanyType;

        @NotNull(message = "Model Debitur is required, key: companyModel")
        private String companyModel;

        @NotNull(message = "Jenis Identitas is required, key: identityType")
        private String identityType;

        @NotNull(message = "NPWP is required, key: identityType")
        private String identityNo;

        @NotNull(message = "Tanggal Terbit Identitas is required, key: identityIssuedDate")
        private Date identityIssuedDate;

        @NotNull(message = "Tanggal Kadaluarsa Identitas is required, key: identityExpiredDate")
        private Date identityExpiredDate;

        @NotNull(message = "Alamat Kantor is required, key: companyAddress")
        private String companyAddress;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class UpdatePersonalRequest extends CommonField {
        @NotNull(message = "Tempat Lahir is required, key: birthplace")
        private String birthPlace;

        @NotNull(message = "Tanggal Lahir is required, key: birthdate")
        private Date birthDate;

        @NotNull(message = "Jenis Kelamin is required, key: gender")
        private String gender;

        @NotNull(message = "Jenis Identitas is required, key: identityType")
        private String identityType;

        @NotNull(message = "Jenis Identitas is required, key: identityType")
        private String identityNo;

        private Date expiredDate;

        @NotNull(message = "Ibu Kandung is required, key: motherMaidenName")
        private String motherMaidenName;

        @NotNull(message = "Status Perkawinan is required, key: maritalStatus")
        private String maritalStatus;

        @NotNull(message = "Model Debitur is required, key: custModel")
        private String custModel;

        @NotNull(message = "Alamat Rumah is required, key: legalAddress")
        private String legalAddress;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    private static class CommonField {
        @NotNull(message = "Telepon is required, key: phone")
        private String phone;

        @NotNull(message = "Kepemilikan Rumah is required, key: ownershipStatus")
        private String ownershipStatus;

        @NotNull(message = "Tinggal Sejak is required, key: staySince")
        private Date staySince;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class UpdateAddressRequest {
        @NotNull(message = "RT is required, key: rt")
        private String rt;

        @NotNull(message = "RW is required, key: rw")
        private String rw;

        @NotNull(message = "Kode Pos is required, key: zipCode")
        private String zipCode;

        @NotNull(message = "Kelurahan is required, key: kelurahan")
        private String kelurahan;

        @NotNull(message = "Kecamatan is required, key: kecamatan")
        private String kecamatan;

        @NotNull(message = "Kota is required, key: city")
        private String city;

        @NotNull(message = "Area is required, key: area")
        private String area;

        @NotNull(message = "Provinsi is required, key: province")
        private String province;
    }
}
