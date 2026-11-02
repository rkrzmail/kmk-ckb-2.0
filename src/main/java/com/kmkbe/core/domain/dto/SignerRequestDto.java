package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SignerRequestDto {
    private String custNo;

    private String cwrNo;

    @JsonProperty("RequestDateTime")
    private String requestDateTime;

    public SignerRequestDto(String custNo, String cwrNo, String RequestDateTime) {
        this.custNo = custNo;
        this.cwrNo = cwrNo;
        this.requestDateTime = RequestDateTime;
    }
}
