package com.kmkbe.modules.common.service.refresh_token;

import com.kmkbe.core.domain.model.RefreshToken;
import com.kmkbe.core.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service("DbRefreshTokenServices")
@Slf4j
public class DbRefreshTokenServices implements IRefreshTokenServices {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public DbRefreshTokenServices(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Override
    public RefreshToken create(IRefreshTokenServices.User user) {
        try {
            final RefreshToken payload = defaultPayload(user, clock);

            jdbcTemplate.update(
                    "insert into public._refresh_token (user_code, refresh_token, expired_date) values (?, ?, ?)",
                    payload.getUserCode(),
                    payload.getRefreshToken(),
                    payload.getExpiredDate()
            );

            return payload;
        } catch (Exception e) {
            log.error("DB create, error {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Boolean invalidate(String refreshToken) {
        try {
            int result = jdbcTemplate.update(
                    "delete from public._refresh_token where refresh_token = ?",
                    refreshToken
            );

            return result > 0;
        } catch (Exception e) {
            log.error("DB invalidate, error {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public RefreshToken verify(String refreshToken) {
        try {
            RefreshToken last = findBy("refresh_token", refreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            Calendar expiredDt = Calendar.getInstance();
            expiredDt.setTimeInMillis(last.getExpiredDate().getTime());
            Date now = Date.from(clock.instant());
            String expiredDtStr = DateTimeUtils.SDF_STANDARD_DATE_TIME.format(expiredDt.getTimeInMillis());
            String nowStr = DateTimeUtils.SDF_STANDARD_DATE_TIME.format(now.getTime());

            if (expiredDt.before(now)) {
                throw new IllegalStateException("Refresh token are expired or invalid, try to login again");
            }

            return last;
        } catch (Exception e) {
            log.error("DB verify, error {}", e.getMessage());
            throw e;
        }
    }

    private Optional<RefreshToken> findBy(String column, String value) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "select user_code, refresh_token, expired_date, issued_date from public._refresh_token where " + column + " = ? order by id desc limit 1",
                    (rs, rowNum) -> {
                        RefreshToken refreshToken = new RefreshToken();
                        refreshToken.setUserCode(rs.getObject("user_code", UUID.class));
                        refreshToken.setRefreshToken(rs.getObject("refresh_token", UUID.class));
                        refreshToken.setExpiredDate(rs.getTimestamp("expired_date"));
                        refreshToken.setIssuedDate(rs.getTimestamp("issued_date"));
                        return refreshToken;
                    },
                    value
            ));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("findBy, error {}", e.getMessage());
            throw e;
        }
    }
}
