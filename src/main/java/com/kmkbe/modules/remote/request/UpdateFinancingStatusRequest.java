package com.kmkbe.modules.remote.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateFinancingStatusRequest {
    @JsonProperty("vendor_code")
    private String vendorCode;

    @JsonProperty("financing_code")
    private String financingCode;

    @JsonProperty("status")
    private Status status;

    public enum Status {
        Approved,
        Reject,
        Disburse
    }
}

