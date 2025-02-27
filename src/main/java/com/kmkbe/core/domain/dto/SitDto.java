package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class SitDto {
    private String agreementCode;
    private String bouwheerName;
    private String legalAddress;
    private String rt;
    private String rw;
    private String kelurahan;
    private String kecamatan;
    private String city;
    private String province;
    private String zipcode;
    private String area;
    private String picName;
    private LocalDateTime fapDate;
    private double totalInvoiceAmt;
    private LocalDateTime financingDueDate;
    private String DirectorName;
    private String bankName;  // Menambahkan bankName
    private String accountName;  // Menambahkan accountName
    private String accountNo;
    private String custName;
    //    private String branchCode;
    private String BranchCode;
//    private String MstBranch;


    public SitDto(String agreementCode, String bouwheerName, String legalAddress, String rt, String rw, String kelurahan, String kecamatan, String city, String province, String zipcode, String area, String picName, LocalDateTime fapDate, double totalInvoiceAmt, LocalDateTime financingDueDate, String DirectorName, String bankName, String accountName, String accountNo, String custName, String BranchCode) {
        this.agreementCode = agreementCode;
        this.bouwheerName = bouwheerName;
        this.legalAddress = legalAddress;
        this.rt = rt;
        this.rw = rw;
        this.kelurahan = kelurahan;
        this.kecamatan = kecamatan;
        this.city = city;
        this.province = province;
        this.zipcode = zipcode;
        this.area = area;
        this.picName = picName;
        this.fapDate = fapDate;
        this.totalInvoiceAmt = totalInvoiceAmt;
        this.financingDueDate = financingDueDate;
        this.DirectorName = DirectorName;
        this.bankName = bankName;
        this.accountName = accountName;
        this.accountNo = accountNo;
        this.custName = custName;
//        this.branchCode = branchCode;
        this.BranchCode = BranchCode;
//        this.MstBranch = mstBranch;

    }

    // Getter dan Setter
    public String getAgreementCode() {
        return agreementCode;
    }

    public void setAgreementCode(String agreementCode) {
        this.agreementCode = agreementCode;
    }

    public String getBouwheerName() {
        return bouwheerName;
    }

    public void setBouwheerName(String bouwheerName) {
        this.bouwheerName = bouwheerName;
    }

    public String getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(String legalAddress) {
        this.legalAddress = legalAddress;
    }

    public String getRt() {
        return rt;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public String getRw() {
        return rw;
    }

    public void setRw(String rw) {
        this.rw = rw;
    }

    public String getKelurahan() {
        return kelurahan;
    }

    public void setKelurahan(String kelurahan) {
        this.kelurahan = kelurahan;
    }

    public String getKecamatan() {
        return kecamatan;
    }

    public void setKecamatan(String kecamatan) {
        this.kecamatan = kecamatan;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public LocalDateTime getFapDate() {
        return fapDate;
    }

    public void setFapDate(LocalDateTime fapDate) {
        this.fapDate = fapDate;
    }

    public double getTotalInvoiceAmt() {
        return totalInvoiceAmt;
    }

    public void setTotalInvoiceAmt(double totalInvoiceAmt) {
        this.totalInvoiceAmt = totalInvoiceAmt;
    }

    public LocalDateTime getFinancingDueDate() {
        return financingDueDate;
    }

    public void setFinancingDueDate(LocalDateTime financingDueDate) {
        this.financingDueDate = financingDueDate;
    }

    public String getDirectorName() {
        return DirectorName;
    }

    public void setDirectorName(String directorName) {
        DirectorName = directorName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

//    public void getMstBranch() {
//        this.MstBranch = MstBranch;
//    }
//
//    public void setMstBranch(String MstBranch) {
//        this.MstBranch = MstBranch;
//    }

//    public void getBranchCode() {
//        this.branchCode = branchCode;
//    }
//
//    public void setBranchCode(String branchCode) {
//        this.branchCode = branchCode;
//    }

    public String getFindBranchCodeByFinancingHdrCode() {
        return BranchCode;
    }

    public void setFindBranchCodeByFinancingHdrCode(String findBranchCodeByFinancingHdrCode) {
        this.BranchCode = findBranchCodeByFinancingHdrCode;
    }
}

