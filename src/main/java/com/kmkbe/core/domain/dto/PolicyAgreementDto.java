package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class PolicyAgreementDto implements Serializable {
    private Long policyId;

    @Size(max = 50)
    private String policyCode;

    @NotBlank(message = "Policy name is required")
    @Size(max = 255)
    private String policyName;

    @Size(max = 1000)
    private String policyDescription;

    private String policyContent;
    private Integer version;
    private Boolean isActive;
    private String usrCrt;
    private LocalDateTime dtmCrt;
    private String usrUpd;
    private LocalDateTime dtmUpd;
}
