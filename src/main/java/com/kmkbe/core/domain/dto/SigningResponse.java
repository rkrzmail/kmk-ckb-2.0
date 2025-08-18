package com.kmkbe.core.domain.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigningResponse {
    private boolean success;
    private String documentId;
    private String message;
}