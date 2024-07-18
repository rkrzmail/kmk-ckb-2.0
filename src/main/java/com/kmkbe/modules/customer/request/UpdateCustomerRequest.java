package com.kmkbe.modules.customer.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UpdateCustomerRequest {
    @NotNull(message = "Jenis Debitur is required, key: custType")
    private String custType;

    @NotNull(message = "Nama is required, key: name")
    private String name;

    @NotNull(message = "Tipe Perusahaan is required, key: custCompanyType")
    private String custCompanyType;

    @NotNull(message = "Email is required, key: email")
    private String email;

    @NotNull(message = "Jenis Identitas is required, key: identityType")
    private String identityType;

    @NotNull(message = "No. Identitas is required, key: identityNo")
    private String identityNo;

    @NotNull(message = "Tanggal Terbit Identitas is required, key: identityIssuedDate")
    private Date identityIssuedDate;

    @NotNull(message = "Tanggal Kadaluarsa Identitas is required, key: identityExpiredDate")
    private Date identityExpiredDate;

    @NotNull(message = "NPWP is required, key: npwp")
    private String npwp;

    @NotNull(message = "Model Debitur is required, key: custModel")
    private String custModel;

    @NotNull(message = "Alamat Kantor is required, key: companyAddress")
    private String companyAddress;

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

    @NotNull(message = "Telepon is required, key: phone")
    private String phone;

    @NotNull(message = "Kepemilikan Rumah is required, key: ownershipStatus")
    private String ownershipStatus;

    @NotNull(message = "Tinggal Sejak is required, key: staySince")
    private Date staySince;
}
