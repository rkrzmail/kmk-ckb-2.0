package com.kmkbe.modules.remote.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateFinancingStatusRequest {
    @JsonProperty("VendorCode")
    private String vendorCode;

    @JsonProperty("FinancingCode")
    private String financingCode;

    @JsonProperty("Status")
    private Status status;

    public enum Status {
        Approve,
        Reject,
        Disburse
    }
}

