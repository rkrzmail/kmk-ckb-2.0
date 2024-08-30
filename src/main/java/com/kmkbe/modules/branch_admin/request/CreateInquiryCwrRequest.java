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
public class CreateInquiryCwrRequest {
    @NotNull(message = "Customer Code is required, key: financingHdrCode")
    @NotEmpty(message = "Customer Code is required, key: financingHdrCode")
    private String financingHdrCode;

    @NotNull(message = "Cwr No is required, key: cwrNo")
    @NotEmpty(message = "Cwr No is required, key: cwrNo")
    private String cwrNo;
}
