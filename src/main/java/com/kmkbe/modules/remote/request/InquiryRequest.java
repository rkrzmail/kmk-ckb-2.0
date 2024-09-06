package com.kmkbe.modules.remote.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InquiryRequest {
    @JsonProperty("vendor_code")
    private String vendorCode;
}
