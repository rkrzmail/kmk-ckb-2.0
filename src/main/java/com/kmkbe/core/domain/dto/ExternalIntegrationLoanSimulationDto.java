package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalIntegrationLoanSimulationDto {
    private String bouwheerCode;
    private Boolean alreadyAcceptImportantNotes;
    private Date dtmCrt;
    private String vendor;
    //private VendorDto vendor;

    @Getter
    @Builder
    public static class VendorDto {
        private String vendorCode;
        private String name;
        private String customerType;
        private String email;
        private String mobilePhone;
        private String customerIdNo;
    }


}
