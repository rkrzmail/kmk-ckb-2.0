package com.kmkbe.modules.customer.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UpdateFapRequest {
    private UUID financingHdrCode;
    private String email;
    private LocalDateTime fapDate;
    @NotBlank(message = "fapStatus cannot be empty")
    private String fapStatus;
}
