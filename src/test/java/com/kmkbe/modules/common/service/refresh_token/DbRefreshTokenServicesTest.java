package com.kmkbe.modules.common.service.refresh_token;

import com.kmkbe.core.domain.model.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DbRefreshTokenServicesTest {
  @Mock JdbcTemplate jdbcTemplate;

  private final Clock clock = Clock.fixed(Instant.parse("2026-11-24T00:00:00Z"), ZoneOffset.UTC);
  DbRefreshTokenServices service;

  @BeforeEach
  void setUp() {
    service = new DbRefreshTokenServices(jdbcTemplate, clock);
  }

  @Test
  void createReturnsPayload() {
    UUID userCode = UUID.randomUUID();

    RefreshToken token = service.create(user(userCode));

    assertThat(token.getUserCode()).isEqualTo(userCode);
    assertThat(token.getIssuedDate()).isEqualTo(Date.from(clock.instant()));
    assertThat(token.getExpiredDate()).isEqualTo(Date.from(clock.instant().plusMillis(86_400_000L)));
  }

  @Test
  void createRethrowsJdbcFailure() {
    doThrow(new RuntimeException("insert failed"))
      .when(jdbcTemplate).update(anyString(), any(), any(), any());

    assertThatThrownBy(() -> service.create(user(UUID.randomUUID()))).hasMessage("insert failed");
  }

  @Test
  void invalidateReflectsDeletedRows() {
    when(jdbcTemplate.update(anyString(), eq("token"))).thenReturn(1);
    assertThat(service.invalidate("token")).isTrue();

    when(jdbcTemplate.update(anyString(), eq("missing"))).thenReturn(0);
    assertThat(service.invalidate("missing")).isFalse();

    doThrow(new RuntimeException("delete failed")).when(jdbcTemplate).update(anyString(), eq("broken"));
    assertThatThrownBy(() -> service.invalidate("broken")).hasMessage("delete failed");
  }

  @Test
  void verifyReturnsActiveToken() {
    RefreshToken stored = token(Date.from(clock.instant().plusMillis(60_000)));
    stubQuery("active", stored);

    RefreshToken found = service.verify("active");
    assertThat(found.getUserCode()).isEqualTo(stored.getUserCode());
    assertThat(found.getRefreshToken()).isEqualTo(stored.getRefreshToken());
    assertThat(found.getExpiredDate().getTime()).isEqualTo(stored.getExpiredDate().getTime());
    assertThat(found.getIssuedDate().getTime()).isEqualTo(stored.getIssuedDate().getTime());
  }

  @Test
  void verifyRejectsExpiredMissingAndJdbcFailures() {
    stubQuery("expired", token(Date.from(clock.instant().minusMillis(1000))));
    assertThatThrownBy(() -> service.verify("expired"))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("expired");

    when(jdbcTemplate.queryForObject(anyString(), ArgumentMatchers.<RowMapper<RefreshToken>>any(), eq("missing")))
      .thenThrow(new EmptyResultDataAccessException(1));
    assertThatThrownBy(() -> service.verify("missing"))
      .isInstanceOf(RuntimeException.class).hasMessage("Refresh token not found");

    when(jdbcTemplate.queryForObject(anyString(), ArgumentMatchers.<RowMapper<RefreshToken>>any(), eq("broken")))
      .thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.verify("broken")).hasMessage("db down");
  }

  private void stubQuery(String refreshToken, RefreshToken stored) {
    when(jdbcTemplate.queryForObject(anyString(), ArgumentMatchers.<RowMapper<RefreshToken>>any(), eq(refreshToken)))
      .thenAnswer(call -> {
        RowMapper<RefreshToken> mapper = call.getArgument(1);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("user_code", UUID.class)).thenReturn(stored.getUserCode());
        when(rs.getObject("refresh_token", UUID.class)).thenReturn(stored.getRefreshToken());
        when(rs.getTimestamp("expired_date")).thenReturn(new java.sql.Timestamp(stored.getExpiredDate().getTime()));
        when(rs.getTimestamp("issued_date")).thenReturn(new java.sql.Timestamp(stored.getIssuedDate().getTime()));
        return mapper.mapRow(rs, 0);
      });
  }

  private RefreshToken token(Date expiredDate) {
    RefreshToken token = new RefreshToken();
    token.setUserCode(UUID.randomUUID());
    token.setRefreshToken(UUID.randomUUID());
    token.setIssuedDate(Date.from(clock.instant().minusMillis(1000)));
    token.setExpiredDate(expiredDate);
    return token;
  }

  private IRefreshTokenServices.User user(UUID userCode) {
    return IRefreshTokenServices.User.builder().userCode(userCode).build();
  }
}
