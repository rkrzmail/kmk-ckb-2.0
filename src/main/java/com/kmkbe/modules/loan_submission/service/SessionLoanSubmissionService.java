package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.dto.LoanSubmissionSessionDto;
import com.kmkbe.helpers.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

@Service
//@RequiredArgsConstructor
@Slf4j
public class SessionLoanSubmissionService {
    private final JdbcTemplate jdbcTemplate;

    public SessionLoanSubmissionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public LoanSubmissionSessionDto create(
            Customer customer,
            int lastStep,
            Object jsonSession
    ) throws SignatureException, JsonProcessingException {
        try {
            Optional<LoanSubmissionSessionDto> find = findLastByCust(customer);

            final LoanSubmissionSessionDto result;
            if (find.isEmpty()) {
                jdbcTemplate.update(
                        "insert into public._loan_submission_session (cust_code, last_step, session, dtm_crt) values (?, ?, ?, ?)",
                        customer.getCustCode(),
                        lastStep,
                        ObjectUtils.jsonToStr(jsonSession),
                        Timestamp.valueOf(DateTimeUtils.nowLocal())
                );
            } else {
                jdbcTemplate.update(
                        "update public._loan_submission_session set last_step = ?, session = ?, dtm_upd = ? where cust_code = ?",
                        lastStep,
                        ObjectUtils.jsonToStr(jsonSession),
                        Timestamp.valueOf(DateTimeUtils.nowLocal()),
                        customer.getCustCode()
                );
            }

            result = findLastByCust(customer).get();

            return result;
        } catch (Exception e) {
            log.error("create: error {}", e.getMessage());
            throw e;
        }
    }

    public Optional<LoanSubmissionSessionDto> findLastByCust(Customer customer) {
        try {
            LoanSubmissionSessionDto result = jdbcTemplate.queryForObject(
                    "select cust_code, last_step, session, dtm_crt, dtm_upd from public._loan_submission_session where cust_code = ? order by id desc limit 1",
                    (rs, rowNum) -> LoanSubmissionSessionDto.builder()
                            .lastStep(rs.getInt("last_step"))
                            .dtmCrt(Utils.toInstant( new Date( rs.getTimestamp("dtm_crt").getTime())))
                            .dtmUpd(rs.getTimestamp("dtm_upd") != null ? Utils.toInstant( new Date(rs.getTimestamp("dtm_upd").getTime())) : null)
                            .session(ObjectUtils.strToJson(rs.getString("session")))
                            .build(),
                    customer.getCustCode()
            );
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("findLastByCust, error {}", e.getMessage());
            throw e;
        }
    }
}
