package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.MjrAccDashboardDto;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.major_account.request.MjrDashboardRequest;
import com.kmkbe.helpers.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class MjrDashboardService {
    private static final String DASHBOARD_CHART_SQL = """
            with
                counting as (
                                SELECT
                                    branch_code,
                                    COUNT(CASE WHEN lower(financing_status) = 'new' THEN 1 END)  AS total_new,
                                    COUNT(CASE
                                              WHEN lower(financing_status) = 'inprocess'
                                                  and lower(financing_step) = 'assignment'
                                                  THEN 1 END)                                    AS total_assignment,
                                    COUNT(CASE
                                              WHEN lower(financing_status) = 'inprocess'
                                                  and lower(financing_step) = 'inprocess'
                                                  THEN 1 END)                                    AS total_inprocess,
                                    COUNT(CASE
                                              WHEN lower(financing_status) = 'inprocess'
                                                  and (lower(financing_step) = 'signing' or lower(financing_step) = 'signed')
                                                  THEN 1 END)                                    AS total_signing,
                                    COUNT(CASE
                                              WHEN lower(financing_status) = 'live'
                                                  and lower(financing_step) = 'golive'
                                                  THEN 1 END)                                    AS total_live,
                                    COUNT(CASE
                                              WHEN lower(financing_status) = 'live'
                                                  and lower(financing_step) = 'paid'
                                                  THEN 1 END)                                    AS total_paid,
                                    COUNT(CASE
                                              WHEN lower(financing_status) = 'completed'
                                                  and lower(financing_step) = 'refund'
                                                  THEN 1 END)                                    AS total_completed
                                FROM
                                    public.financing_hdr
                                WHERE
                                     Date(dtm_crt::date) BETWEEN :startDate AND :endDate
                                GROUP BY branch_code
                            )
            select
                bch.branch_code,
                bch.branch_name,
                coalesce(counting.total_new, 0) as total_new,
                coalesce(counting.total_assignment, 0) as total_assignment,
                coalesce(counting.total_inprocess, 0) as total_inprocess,
                coalesce(counting.total_signing, 0) as total_signing,
                coalesce(counting.total_live, 0) as total_live,
                coalesce(counting.total_paid, 0) as total_paid,
                coalesce(counting.total_completed, 0) as total_completed,
                (
                    coalesce(counting.total_new, 0)
                        + coalesce(counting.total_assignment, 0)
                        + coalesce(counting.total_inprocess, 0)
                        + coalesce(counting.total_signing, 0)
                        + coalesce(counting.total_live, 0)
                        + coalesce(counting.total_paid, 0)
                        + coalesce(counting.total_completed, 0)
                    ) as total_all
            from
                users.branch bch
                    left join counting on bch.branch_code::text = counting.branch_code::text
                    WHERE bch.business_unit = 'CBU'
                    ;
            """;

    private final EntityManager entityManager;
    private final Clock clock;

    public MjrAccDashboardDto calculate(
            MjrDashboardRequest request
    ) {
        try {
            DateRange dateRange = resolveDateRange(request);
            List<MjrAccDashboardDto.Chart> charts = fetchChartData(dateRange);

            return buildDashboard(dateRange, charts);
        } catch (Exception e) {
            log.error("calculateBranchProgress, error {}", e.getMessage());
            throw e;
        }
    }

    MjrAccDashboardDto buildDashboard(
            DateRange dateRange,
            List<MjrAccDashboardDto.Chart> charts
    ) {
        List<String> chartLabel = new ArrayList<>();
        List<Long> chartNew = new ArrayList<>();
        List<Long> chartAssignment = new ArrayList<>();
        List<Long> chartInProcess = new ArrayList<>();
        List<Long> chartSigning = new ArrayList<>();
        List<Long> chartLive = new ArrayList<>();
        List<Long> chartPaid = new ArrayList<>();
        List<Long> chartCompleted = new ArrayList<>();

        for (MjrAccDashboardDto.Chart chart : charts) {
            chartLabel.add(chart.getBranchName());
            chartNew.add(chart.getTotalNew());
            chartAssignment.add(chart.getTotalAssignment());
            chartInProcess.add(chart.getTotalInProcess());
            chartSigning.add(chart.getTotalSigning());
            chartLive.add(chart.getTotalLive());
            chartPaid.add(chart.getTotalPaid());
            chartCompleted.add(chart.getTotalCompleted());
        }

        MjrAccDashboardDto result = new MjrAccDashboardDto();
        result.setTotalAll(charts.stream().mapToLong(this::totalChart).sum());
        result.setStartDate(dateRange.startDate());
        result.setEndDate(dateRange.endDate());
        result.setChartLabel(chartLabel);
        result.setChartNew(chartNew);
        result.setChartAssignment(chartAssignment);
        result.setChartInProcess(chartInProcess);
        result.setChartSigning(chartSigning);
        result.setChartLive(chartLive);
        result.setChartPaid(chartPaid);
        result.setChartCompleted(chartCompleted);
        return result;
    }

    private DateRange resolveDateRange(MjrDashboardRequest request) {
        if (request.getStartDate() == null) {
            LocalDateTime startDate = LocalDateTime.now(clock).minus(30, ChronoUnit.DAYS);
            request.setStartDate(Utils.fromInstant(startDate));
        }

        if (request.getEndDate() == null) {
            request.setEndDate(Date.from(clock.instant()));
        }

        return new DateRange(request.getStartDate(), request.getEndDate());
    }

    private List<MjrAccDashboardDto.Chart> fetchChartData(DateRange dateRange) {
        Query query = entityManager.createNativeQuery(DASHBOARD_CHART_SQL);
        setDateRangeParameters(query, dateRange);

        List<?> rows = query.getResultList();
        return rows
                .stream()
                .filter(Object[].class::isInstance)
                .map(row -> (Object[]) row)
                .map(this::mapChartRow)
                .toList();
    }

    private void setDateRangeParameters(Query query, DateRange dateRange) {
        query.setParameter("startDate", DateTimeUtils.SDF_STANDARD_DATE.format(dateRange.startDate()));
        query.setParameter("endDate", DateTimeUtils.SDF_STANDARD_DATE.format(dateRange.endDate()));
    }

    private MjrAccDashboardDto.Chart mapChartRow(Object[] objects) {
        MjrAccDashboardDto.Chart chart = new MjrAccDashboardDto.Chart();
        chart.setBranchCode(String.valueOf(objects[0]));
        chart.setBranchName(String.valueOf(objects[1]));
        chart.setTotalNew(toLong(objects[2]));
        chart.setTotalAssignment(toLong(objects[3]));
        chart.setTotalInProcess(toLong(objects[4]));
        chart.setTotalSigning(toLong(objects[5]));
        chart.setTotalLive(toLong(objects[6]));
        chart.setTotalPaid(toLong(objects[7]));
        chart.setTotalCompleted(toLong(objects[8]));
        return chart;
    }

    private long totalChart(MjrAccDashboardDto.Chart chart) {
        return chart.getTotalNew()
                + chart.getTotalAssignment()
                + chart.getTotalInProcess()
                + chart.getTotalSigning()
                + chart.getTotalLive()
                + chart.getTotalPaid()
                + chart.getTotalCompleted();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    record DateRange(Date startDate, Date endDate) {
    }
}
