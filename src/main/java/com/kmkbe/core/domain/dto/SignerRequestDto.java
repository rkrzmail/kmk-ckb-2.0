package com.kmkbe.core.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class SignerRequestDto {
    private String custNo;
    private String cwrNo;
    private String RequestDateTime;

    public SignerRequestDto(String custNo, String cwrNo, String requestDateTime) {
        this.custNo = custNo;
        this.cwrNo = cwrNo;
        this.RequestDateTime = requestDateTime;
    }
}
