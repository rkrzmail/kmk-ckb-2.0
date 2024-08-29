package com.kmkbe.modules.branch_admin.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AgreementCreditCwrRequest {
    @NotNull(message = "Customer Code is required, key: custCode")
    @NotEmpty(message = "Customer Code is required, key: custCode")
    private String bouwheerCode;

    @NotNull(message = "Customer Code is required, key: custCode")
    @NotEmpty(message = "Customer Code is required, key: custCode")
    private String custCode;

    @NotNull(message = "Cwr No is required, key: cwrNo")
    @NotEmpty(message = "Cwr No is required, key: cwrNo")
    private String cwrNo;
}
