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
import java.util.ArrayList;
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
                        (
                            coalesce(counting.total_new, 0)
                                + coalesce(counting.total_assignment, 0)
                                + coalesce(counting.total_inprocess, 0)
                                + coalesce(counting.total_signing, 0)
                                + coalesce(counting.total_live, 0)
                            ) as total_all
                    from
                        users.branch bch
                            left join counting on bch.branch_code::text = counting.branch_code::text
	                        WHERE bch.business_unit = 'CBU'
                            ;
                    """;

            Query query = entityManager.createNativeQuery(sql);
            if (request.getStartDate() == null) {
                request.setStartDate(Date.from(DateTimeUtils.now().minus(30, java.time.temporal.ChronoUnit.DAYS)));
            }

            if (request.getEndDate() == null) {
                request.setEndDate(new Date());
            }

            query.setParameter("startDate", DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()));
            query.setParameter("endDate", DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));

            MjrAccDashboardDto result = new MjrAccDashboardDto();


            List<String> chartLabel = new ArrayList<>();
            List<Long> chartNew  = new ArrayList<>();
            List<Long> chartAssignment  = new ArrayList<>();
            List<Long> chartInProcess  = new ArrayList<>();
            List<Long> chartSigning  = new ArrayList<>();
            List<Long> chartLive  = new ArrayList<>();


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

                            chartLabel.add(String.valueOf(objects[1]));
                            chartNew.add((Long) objects[2]);
                            chartAssignment.add((Long) objects[3]);
                            chartInProcess.add((Long) objects[4]);
                            chartSigning.add((Long) objects[5]);
                            chartLive.add((Long) objects[6]);
                        }

                        return chart;
                    })
                    .toList();

            final String sqlTotal = """
                        SELECT COUNT(*) as count_total 
                      
 
                    from
                        financing_hdr fh
                    where
                        fh.financing_status is not null and
                        fh.financing_step is not null and
												 fh.financing_status != '' and
                        fh.financing_step  != ''
												
                        and
												
							   dtm_crt::date BETWEEN :startDate AND :endDate ;
            
                        """;

            query = entityManager.createNativeQuery(sqlTotal);
            query.setParameter("startDate", DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()));
            query.setParameter("endDate", DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));
            List<MjrAccDashboardDto.Counting> queryList = query.getResultList()
                    .stream()
                    .map((e) -> {
                        MjrAccDashboardDto.Counting counting = new MjrAccDashboardDto.Counting();
                        if (e instanceof Object objects) {
                            counting.setCountTotal((Long) objects);
                        }
                        return counting;
                    })
                    .toList();



            Long totalAll  = queryList.isEmpty()? 0: queryList.get(0).getCountTotal();


            /*final String sqlT = """
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

            query = entityManager.createNativeQuery(sqlT);
            query.setParameter("startDate", DateTimeUtils.SDF_STANDARD_DATE.format(request.getStartDate()));
            query.setParameter("endDate", DateTimeUtils.SDF_STANDARD_DATE.format(request.getEndDate()));
            MjrAccDashboardDto resultT = new MjrAccDashboardDto();
            @SuppressWarnings("unchecked")
            List<MjrAccDashboardDto.Chart> queryResultListT = query.getResultList()
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
                Long totalAllT = queryResultListT.stream()
                    .mapToLong((e) -> e.getTotalNew()
                            + e.getTotalAssignment()
                            + e.getTotalInProcess()
                            + e.getTotalSigning()
                            + e.getTotalLive())
                    .sum();*/




            result.setTotalAll(totalAll);
            //result.setTotalAll(totalAllT );

            result.setStartDate(request.getStartDate());
            result.setEndDate(request.getEndDate());
            //result.setChart(queryResultList);

            result.setChartLabel(chartLabel);
            result.setChartNew(chartNew);
            result.setChartAssignment(chartAssignment);
            result.setChartInProcess(chartInProcess);
            result.setChartSigning(chartSigning);
            result.setChartLive(chartLive);

            return result;
        } catch (Exception e) {
            log.error("calculateBranchProgress, error {}", e.getMessage());
            throw e;
        }
    }
}
