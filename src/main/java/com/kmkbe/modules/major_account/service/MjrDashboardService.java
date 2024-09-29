package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.MjrAccDashboardDto;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.major_account.request.MjrDashboardRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class MjrDashboardService {
    private final EntityManager entityManager;

    public MjrAccDashboardDto calculate(
            MjrDashboardRequest request
    ) {
        try {
            final String sql = """
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
                                            COUNT(CASE WHEN lower(financing_status) = 'live' THEN 1 END) AS total_live
                                        FROM
                                            public.financing_hdr
                                        WHERE
                                            disburse_date::date BETWEEN :startDate AND :endDate
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
                        (
                            coalesce(counting.total_new, 0)
                                + coalesce(counting.total_assignment, 0)
                                + coalesce(counting.total_inprocess, 0)
                                + coalesce(counting.total_signing, 0)
                                + coalesce(counting.total_live, 0)
                            ) as total_all
                    from
                        users.branch bch
                            left join counting on bch.branch_code::text = counting.branch_code::text;
                    """;

            Query query = entityManager.createNativeQuery(sql);
            if (request.getStartDate() == null) {
                request.setStartDate(Date.from(Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS)));
            }

            if (request.getEndDate() == null) {
                request.setEndDate(new Date());
            }

            query.setParameter("startDate", DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()));
            query.setParameter("endDate", DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));

            MjrAccDashboardDto result = new MjrAccDashboardDto();

            @SuppressWarnings("unchecked")
            List<MjrAccDashboardDto.Chart> queryResultList = query.getResultList()
                    .stream()
                    .map((e) -> {
                        MjrAccDashboardDto.Chart chart = null;
                        if (e instanceof Object[] objects) {
                            chart = new MjrAccDashboardDto.Chart();
                            chart.setBranchCode(String.valueOf(objects[0]));
                            chart.setBranchName(String.valueOf(objects[1]));
                            chart.setTotalNew((Long) objects[2]);
                            chart.setTotalAssignment((Long) objects[3]);
                            chart.setTotalInProcess((Long) objects[4]);
                            chart.setTotalSigning((Long) objects[5]);
                            chart.setTotalLive((Long) objects[6]);
                        }

                        return chart;
                    })
                    .toList();

            Long totalAll = queryResultList.stream()
                    .mapToLong((e) -> e.getTotalNew()
                            + e.getTotalAssignment()
                            + e.getTotalInProcess()
                            + e.getTotalInProcess()
                            + e.getTotalSigning()
                            + e.getTotalLive())
                    .sum();

            result.setTotalAll(totalAll);
            result.setStartDate(request.getStartDate());
            result.setEndDate(request.getEndDate());
            result.setChart(queryResultList);

            return result;
        } catch (Exception e) {
            log.error("calculateBranchProgress, error {}", e.getMessage());
            throw e;
        }
    }
}
