package com.kmkbe.core.domain.model;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class RefreshToken {
    private UUID userCode;
    private UUID refreshToken;
    private Date expiredDate;
    private Date issuedDate;
}
