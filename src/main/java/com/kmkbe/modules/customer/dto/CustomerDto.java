package com.kmkbe.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class CustomerDto {
    private UUID custCode;
    private String custNo;
    private String custName;
    private String custTypeCode;
    private String custIdTypeCode;
    private String custIdNo;
    private String custEmail;
    private Boolean isEmailValid;
    private String custMobilePhone;
    private Boolean isPhoneValid;
    private Boolean isWaActive;
    private Boolean agreeTc;
    private Boolean agreeLegalShare;
    private String custExternalCode;
    private Boolean isActive;
    private Instant dtmCrt;

    private AddressDto address;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CustomerPersonalDto personal;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CustomerCompanyDto company;
}
