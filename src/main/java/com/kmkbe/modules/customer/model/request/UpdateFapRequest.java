package com.kmkbe.modules.customer.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UpdateFapRequest {
    private String email;
    private LocalDateTime fapDate;
    private String fapStatus;

    // Getters and Setters
}
