package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebtorResponse {
    private boolean success;
    private String message;
    private DebtorDto data;
    private String statusCode;
}