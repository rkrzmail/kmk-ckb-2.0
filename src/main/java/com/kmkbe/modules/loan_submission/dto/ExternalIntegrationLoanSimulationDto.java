package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Builder
public class ExternalIntegrationLoanSimulationDto {
    private String vendorCode;
    private String bouwheerCode;
    private Boolean alreadyAcceptImportantNotes;
    private Date dtmCrt;
}
