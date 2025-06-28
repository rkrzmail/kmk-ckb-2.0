package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPerjanjianDto {
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
    private PerjanjianDto perjanjian;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerjanjianDto {
        private Integer perjanjianBerjalan;
        private Integer perjanjianBerakhir;
        private Integer totalPerjanjian;
    }
}
