package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAgreementCwrDto {
    @JsonProperty("AppId")
    public Integer appId;

    @JsonProperty("Cmo")
    public String cmo;

    @JsonProperty("Office")
    public String office;

    @JsonProperty("DebtorNo")
    public String debtorNo;

    @JsonProperty("DebtorName")
    public String debtorName;

    @JsonProperty("DebtorType")
    public String debtorType;

    @JsonProperty("CWRNo")
    public String cwrNo;

    @JsonProperty("AppNo")
    public String appNo;

    @JsonProperty("ProductOffering")
    public String productOffering;

    @JsonProperty("CurrStep")
    public String currStep;

    @JsonProperty("LastStep")
    public String lastStep;

    @JsonProperty("AgrmntNo")
    public String agrmntNo;

    @JsonProperty("Status")
    public String status;
    @JsonProperty("Facility")
    public String facility;

    @JsonProperty("Currency")
    public String currency;

    @JsonProperty("NtfAmt")
    public Double ntfAmt;

    @JsonProperty("LastApprover")
    public String lastApprover;
}
