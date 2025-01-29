package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPlafondDto {
    private UUID financingHdrCode;
    private UUID bouwheerCode;
    private UUID custCode;
    private String bouwheerName;
    private String custName;
    private String custIdTypeCode;
    private String custIdNo;
    private String email;
    private String custTypeCode;
    private String address;
    private String phoneNo;
    private PlafondDto plafond;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlafondDto {
        private BigDecimal plafond;
        private BigDecimal totalPlafond;
        private BigDecimal availablePlafond;

        private String validityLimitData;
        private BigDecimal jumlahInvoice;
    }
}
