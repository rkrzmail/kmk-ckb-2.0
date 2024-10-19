package com.kmkbe.core.domain.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MjrAccDashboardDto {
    private Date startDate;
    private Date endDate;
    private Long totalAll;
    private List<String> chartLabel;
    private List<Long> chartNew;
    private List<Long> chartAssignment;
    private List<Long> chartInProcess;
    private List<Long> chartSigning;
    private List<Long> chartLive;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Chart {
        private String branchCode;
        private String branchName;
        private Long totalNew;
        private Long totalAssignment;
        private Long totalInProcess;
        private Long totalSigning;
        private Long totalLive;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Counting {
        private Long countTotal;
    }
}
