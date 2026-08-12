package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.MjrAccDashboardDto;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MjrDashboardServiceTest {

    @Test
    void buildDashboardMapsChartRowsIntoSeriesLists() {
        MjrDashboardService service = new MjrDashboardService(
                mock(jakarta.persistence.EntityManager.class),
                Clock.fixed(Instant.parse("2026-08-12T03:00:00Z"), ZoneId.of("Asia/Jakarta"))
        );
        Date startDate = Date.from(Instant.parse("2026-08-01T00:00:00Z"));
        Date endDate = Date.from(Instant.parse("2026-08-12T00:00:00Z"));

        MjrAccDashboardDto.Chart chart = MjrAccDashboardDto.Chart.builder()
                .branchCode("101")
                .branchName("Jakarta")
                .totalNew(1L)
                .totalAssignment(2L)
                .totalInProcess(3L)
                .totalSigning(4L)
                .totalLive(5L)
                .build();

        MjrAccDashboardDto result = service.buildDashboard(
                new MjrDashboardService.DateRange(startDate, endDate),
                List.of(chart),
                15L
        );

        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getTotalAll()).isEqualTo(15L);
        assertThat(result.getChartLabel()).containsExactly("Jakarta");
        assertThat(result.getChartNew()).containsExactly(1L);
        assertThat(result.getChartAssignment()).containsExactly(2L);
        assertThat(result.getChartInProcess()).containsExactly(3L);
        assertThat(result.getChartSigning()).containsExactly(4L);
        assertThat(result.getChartLive()).containsExactly(5L);
    }
}
