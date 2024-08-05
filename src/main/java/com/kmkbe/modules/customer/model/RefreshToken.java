package com.kmkbe.modules.customer.model;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class RefreshToken {
    private UUID custCode;
    private UUID refreshToken;
    private Date expiredDate;
    private Date issuedDate;
}
