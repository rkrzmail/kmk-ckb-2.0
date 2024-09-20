package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDashboardDto {
    @Builder.Default
    private String totalPlafond = "0";

    @Builder.Default
    private String totalPlafondUnit = "*Dalam jutaan rupiah";

    @Builder.Default
    private Date validityLimitDate = new Date();

    @Builder.Default
    private String totalPlafondUsed = "0";

    @Builder.Default
    private String totalPlafondUsedUnit = "*Dalam jutaan rupiah";

    @Builder.Default
    private Long totalInvoiceFounded = 0L;

    @Builder.Default
    private String totalAvailablePlafond = "0";

    @Builder.Default
    private String totalAvailablePlafondUnit = "*Dalam jutaan rupiah";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Info info;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Info {
        private UUID bouwheerCode;
        private UUID custCode;
        private String bouwheerName;
        private String custName;
        private String custIdTypeCode;
        private String custIdNo;
        private String email;
        private String custTypeCode;
        private String address;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Agreement {
        @Builder.Default
        private Long totalAgreement = 0L;

        @Builder.Default
        private Long totalWaitingSigning= 0L;

        @Builder.Default
        private Long totalSigned= 0L;
    }

}
