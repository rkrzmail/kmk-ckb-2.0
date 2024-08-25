package com.kmkbe.modules.remote.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementRemoteDto {
    @JsonAlias("AppId")
    public Integer appId;

    @JsonAlias("Cmo")
    public String cmo;

    @JsonAlias("Office")
    public String office;

    @JsonAlias("DebtorNo")
    public String debtorNo;

    @JsonAlias("DebtorName")
    public String debtorName;

    @JsonAlias("DebtorType")
    public String debtorType;

    @JsonAlias("CWRNo")
    public String cWRNo;

    @JsonAlias("AppNo")
    public String appNo;

    @JsonAlias("ProductOffering")
    public String productOffering;

    @JsonAlias("CurrStep")
    public String currStep;

    @JsonAlias("LastStep")
    public String lastStep;

    @JsonAlias("AgrmntNo")
    public String agrmntNo;

    @JsonAlias("Status")
    public String status;

    @JsonAlias("Facility")
    public String facility;

    @JsonAlias("Currency")
    public String currency;

    @JsonAlias("NtfAmt")
    public Double ntfAmt;

    @JsonAlias("LastApprover")
    public String lastApprover;
}
