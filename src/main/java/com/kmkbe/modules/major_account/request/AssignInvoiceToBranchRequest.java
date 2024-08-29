package com.kmkbe.modules.major_account.request;

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
public class AssignInvoiceToBranchRequest {
    @NotNull(message = "BranchCode is null, key: branchCode")
    @NotEmpty(message = "BranchCode is empty, key: branchCode")
    private String branchCode;

    @NotNull(message = "FinancingHdrCode is null, key: financingHdrCode")
    @NotEmpty(message = "FinancingHdrCode is empty, key: financingHdrCode")
    private String financingHdrCode;
}
