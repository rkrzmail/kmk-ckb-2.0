package com.kmkbe.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Data
@Getter
@Setter
public class VendorDataPayload implements Serializable {
    @JsonProperty("vendor_id")
    private String vendorId;

    @JsonProperty("sap_code")
    private String sapCode;

    @JsonProperty("vendor_name")
    private String vendorName;

    @JsonProperty("founded_date")
    private String foundedDate;

    private String npwp;

    @JsonProperty("npwp_url")
    private String npwpUrl;

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

    @JsonProperty("ktp_npwp_vendor_stock_id")
    private String ktpNpwpVendorStockId;

    @JsonProperty("ktp_npwp_vendor_stock_link")
    private String ktpNpwpVendorStockLink;

    @JsonProperty("laporan_keuangan_link")
    private String laporanKeuanganLink;

    @JsonProperty("akta_pendirian_link")
    private String aktaPendirianLink;

    @JsonProperty("akta_perubahan_link")
    private String aktaPerubahanLink;

    @JsonProperty("pengesahan_kemenkumham_link")
    private String pengesahanKemenkumhamLink;

    private String email;
    private String phone;
    private String website;
    private String fax;

    @JsonProperty("ktp_directur")
    private String ktpDirectur;

    @JsonProperty("ktp_direktur_link")
    private String ktpDirekturLink;

    @JsonProperty("position_ref")
    private Integer positionRef;

    @JsonProperty("vendor_building")
    private BuildingDetail vendorBuilding;

    @JsonProperty("bank_detail")
    private List<InquiryVendorRemoteDto.BankDetail> bankDetail;

    @JsonProperty("vendor_registration_doc")
    private List<InquiryVendorRemoteDto.VendorRegistrationDoc> vendorRegistrationDoc;

    @JsonProperty("other_document_vendor")
    private List<InquiryVendorRemoteDto.OtherDocument> otherDocument;


  @Getter
    @Setter
    public static class BuildingDetail {
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
  }
