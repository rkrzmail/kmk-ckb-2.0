package com.kmkbe.core.domain.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryNewInfoAgreementDto {
    @JsonProperty("Tenor")
    public double tenor;
    @JsonProperty("InterestAmt")
    public double interestAmt;
    @JsonProperty("PrincipalAmt")
    public double principalAmt;
    @JsonProperty("OsArAmt")
    public double osArAmt;
    @JsonProperty("AdjustmentAmt")
    public double adjustmentAmt;
    @JsonProperty("DiskontoAmt")
    public double diskontoAmt;
    @JsonProperty("MrSingleInstCalcMthdCode")
    public String mrSingleInstCalcMthdCode;
}
