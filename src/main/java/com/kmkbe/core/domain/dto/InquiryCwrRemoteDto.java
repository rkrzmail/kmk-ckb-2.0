package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCwrRemoteDto {
    @JsonAlias("rn")
    private Integer rn;

    @JsonAlias("CwrNo")
    private String cwrNo;

    @JsonAlias("DebtorType")
    private String debtorType;

    @JsonAlias("CustName")
    private String custName;

    @JsonAlias("CustNo")
    private String custNo;

    @JsonAlias("StartDt")
    private String startDt;

    @JsonAlias("EndDt")
    private String endDt;

    @JsonAlias("CurrStep")
    private String currStep;

    @JsonAlias("LastStep")
    private String lastStep;

    @JsonAlias("CwrTypeDesc")
    private String cwrTypeDesc;

    @JsonAlias("CwrType")
    private String cwrType;

    @JsonAlias("CwrStat")
    private String cwrStat;

    @JsonAlias("PlafondAmt")
    private Double plafondAmt;

    @JsonAlias("MrCwrTypeCode")
    private String mrCwrTypeCode;

    @JsonAlias("Version")
    private Integer version;

    @JsonAlias("AFVersion")
    private Object aFVersion;

    @JsonAlias("OfficeCode")
    private String officeCode;

    @JsonAlias("OfficeName")
    private String officeName;

    @JsonAlias("CwrStatDescr")
    private String cwrStatDescr;

    @JsonAlias("Facility")
    private String facility;

    @JsonAlias("IsRevolving")
    private Boolean isRevolving;

    @JsonAlias("Currency")
    private String currency;

    @JsonAlias("RealisationAmt")
    private Double realisationAmt;

    @JsonAlias("LastApprover")
    private String lastApprover;

    @JsonAlias("GroupName")
    private Object groupName;

    @JsonAlias("GroupNo")
    private Object groupNo;

    @JsonAlias("IsSuspend")
    private Boolean isSuspend;

    @JsonAlias("ChangeCwrTrxNo")
    private Object changeCwrTrxNo;
}
