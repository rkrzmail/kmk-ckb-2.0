package com.kmkbe.core.domain.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "_existing_customer")
public class ExistingCustomer {

    @Id
    private Long id;  // id sebagai primary key

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "is_existing")
    private Boolean isExisting;

    @Column(name = "identity_type")
    private String identityType;

    @Column(name = "identity_no")
    private String identityNo;

    @Column(name = "dtm_crt")
    private String createdDate;

    @Column(name = "dtm_upd")
    private String updatedDate;

    // Getter dan Setter untuk masing-masing field
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public Boolean getIsExisting() {
        return isExisting;
    }

    public void setIsExisting(Boolean isExisting) {
        this.isExisting = isExisting;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }
}
