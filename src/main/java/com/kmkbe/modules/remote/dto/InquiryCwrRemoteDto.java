package com.kmkbe.modules.remote.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCwrRemoteDto {
    @JsonAlias("rn")
    public Integer rn;

    @JsonAlias("CwrNo")
    public String cwrNo;

    @JsonAlias("DebtorType")
    public String debtorType;

    @JsonAlias("CustName")
    public String custName;

    @JsonAlias("CustNo")
    public String custNo;

    @JsonAlias("StartDt")
    public String startDt;

    @JsonAlias("EndDt")
    public String endDt;

    @JsonAlias("CurrStep")
    public String currStep;

    @JsonAlias("LastStep")
    public String lastStep;

    @JsonAlias("CwrTypeDesc")
    public String cwrTypeDesc;

    @JsonAlias("CwrType")
    public String cwrType;

    @JsonAlias("CwrStat")
    public String cwrStat;

    @JsonAlias("PlafondAmt")
    public Double plafondAmt;

    @JsonAlias("MrCwrTypeCode")
    public String mrCwrTypeCode;

    @JsonAlias("Version")
    public Integer version;

    @JsonAlias("AFVersion")
    public Object aFVersion;

    @JsonAlias("OfficeCode")
    public String officeCode;

    @JsonAlias("OfficeName")
    public String officeName;

    @JsonAlias("CwrStatDescr")
    public String cwrStatDescr;

    @JsonAlias("Facility")
    public String facility;

    @JsonAlias("IsRevolving")
    public Boolean isRevolving;

    @JsonAlias("Currency")
    public String currency;

    @JsonAlias("RealisationAmt")
    public Double realisationAmt;

    @JsonAlias("LastApprover")
    public String lastApprover;

    @JsonAlias("GroupName")
    public Object groupName;

    @JsonAlias("GroupNo")
    public Object groupNo;

    @JsonAlias("IsSuspend")
    public Boolean isSuspend;

    @JsonAlias("ChangeCwrTrxNo")
    public Object changeCwrTrxNo;
}
