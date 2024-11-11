package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryVendorRemoteDto {
    @JsonProperty("vendor_id")
    private Integer vendorId;

    @JsonProperty("sap_code")
    private String sapCode;

    @JsonProperty("vendor_name")
    private String vendorName;

    @JsonProperty("founded_date")
    private String foundedDate;

    @JsonProperty("npwp")
    private String npwp;

    @JsonProperty("npwp_link")
    private String npwpLink;

    @JsonProperty("nip_siup")
    private String nipSiup;

    @JsonProperty("nip_siup_link")
    private String nipSiupLink;

    @JsonProperty("pkp_number")
    private String pkpNumber;

    @JsonProperty("pkp_link")
    private String pkpLink;

    @JsonProperty("jenis_perusahaan")
    private String jenisPerusahaan;

    @JsonProperty("jenis_perusahaan_name")
    private String jenisPerusahaanName;

    @JsonProperty("jenis_perusahaan_description")
    private String jenisPerusahaanDescription;

    @JsonProperty("ktp_npwp_vendor_stock_id")
    private String ktpNpwpVendorStockId;

    @JsonProperty("ktp_npwp_vendor_stock_link")
    private String ktpNpwpVendorStockLink;

    @JsonProperty("akta_pendirian_link")
    private String aktaPendirianLink;

    @JsonProperty("akta_perubahan_link")
    private String aktaPerubahanLink;

    @JsonProperty("pengesahan_kemenkumham_link")
    private String pengesahanKemenkumhamLink;

    @JsonProperty("vendor_building")
    private List<VendorBuilding> vendorBuilding;

    @JsonProperty("laporan_keuangan_link")
    private String laporanKeuanganLink;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("website")
    private String website;

    @JsonProperty("fax")
    private String fax;

    @JsonProperty("ktp_directur")
    private String ktpDirectur;

    @JsonProperty("ktp_direktur_link")
    private String ktpDirekturLink;

    @JsonProperty("position_ref")
    private Integer positionRef;

    @JsonProperty("bank_detail")
    private List<BankDetail> bankDetail;

    @JsonProperty("vendor_registration_doc")
    private List<VendorRegistrationDoc> vendorRegistrationDoc;

    @JsonProperty("other_document")
    private List<OtherDocument> otherDocument;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorRegistrationDoc {
        @JsonProperty("document_url")
        private String documentUrl;

        @JsonProperty("vendor_registration_doc_type")
        private Integer vendorRegistrationDocType;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorBuilding {
        @JsonProperty("ownership_status")
        private String ownershipStatus;

        @JsonProperty("jenis")
        private String jenis;

        @JsonProperty("category")
        private String category;

        @JsonProperty("address_detail")
        private String addressDetail;

        @JsonProperty("address_info")
        private String addressInfo;

        @JsonProperty("state_name")
        private String stateName;

        @JsonProperty("city_name")
        private String cityName;

        @JsonProperty("district_name")
        private String districtName;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherDocument {
        @JsonProperty("document_name")
        private String documentName;

        @JsonProperty("valid_date")
        private String validDate;

        @JsonProperty("document_no")
        private String documentNo;

        @JsonProperty("document_url")
        private String documentUrl;

        @JsonProperty("vendor_id")
        private String vendorId;

        @JsonProperty("is_temporary")
        private Object isTemporary;

        @JsonProperty("ref_vendor_id")
        private Object refVendorId;

        @JsonProperty("action")
        private Object action;

        @JsonProperty("is_active")
        private Boolean isActive;

        @JsonProperty("doc_company")
        private Object docCompany;

        @JsonProperty("passing_value")
        private Object passingValue;

        @JsonProperty("is_change")
        private Object isChange;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankDetail {
        @JsonProperty("account_no")
        private String accountNo;

        @JsonProperty("account_name")
        private String accountName;

        @JsonProperty("bank_name")
        private String bankName;

        @JsonProperty("branch")
        private String branch;

        @JsonProperty("doc_link")
        private Object docLink;

    }
}
