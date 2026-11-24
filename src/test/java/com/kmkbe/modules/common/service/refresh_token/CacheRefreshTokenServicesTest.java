package com.kmkbe.modules.common.service.refresh_token;

import com.kmkbe.core.domain.model.RefreshToken;
import com.kmkbe.core.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class CacheRefreshTokenServicesTest {
  @Mock RedisUtils redisUtil;

  private final Clock clock = Clock.fixed(Instant.parse("2026-11-24T00:00:00Z"), ZoneOffset.UTC);
  CacheRefreshTokenServices service;

  @BeforeEach
  void setUp() {
    service = new CacheRefreshTokenServices(redisUtil, clock);
  }

  @Test
  void createStoresPayloadInRedis() {
    UUID userCode = UUID.randomUUID();

    RefreshToken token = service.create(user(userCode));

    assertThat(token.getUserCode()).isEqualTo(userCode);
    assertThat(token.getIssuedDate()).isEqualTo(Date.from(clock.instant()));
    assertThat(token.getExpiredDate()).isEqualTo(Date.from(clock.instant().plusMillis(86_400_000L)));
    verify(redisUtil).putValue(token.getRefreshToken().toString(), token, TimeUnit.DAYS.toMillis(2));
  }

  @Test
  void createRethrowsRedisFailure() {
    doThrow(new RuntimeException("redis down")).when(redisUtil).putValue(anyString(), any(), anyLong());

    assertThatThrownBy(() -> service.create(user(UUID.randomUUID())))
      .isInstanceOf(RuntimeException.class).hasMessage("redis down");
  }

  @Test
  void invalidateDelegatesAndRethrows() {
    when(redisUtil.deleteKey("token")).thenReturn(true);
    assertThat(service.invalidate("token")).isTrue();

    doThrow(new RuntimeException("delete failed")).when(redisUtil).deleteKey("token2");
    assertThatThrownBy(() -> service.invalidate("token2")).hasMessage("delete failed");
  }

  @Test
  void verifyReadsRedisMap() throws Exception {
    UUID userCode = UUID.randomUUID();
    UUID refreshToken = UUID.randomUUID();
    when(redisUtil.getValue("token")).thenReturn(Map.of(
      "userCode", userCode.toString(),
      "refreshToken", refreshToken.toString(),
      "issuedDate", clock.millis() - 1000,
      "expiredDate", clock.millis() + 1000));

    RefreshToken result = service.verify("token");

    assertThat(result.getUserCode()).isEqualTo(userCode);
    assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
    assertThat(result.getExpiredDate()).isAfter(Date.from(clock.instant()));
  }

  @Test
  void verifyRejectsExpiredMissingAndWrongTypes() {
    when(redisUtil.getValue("expired")).thenReturn(Map.of(
      "userCode", UUID.randomUUID().toString(),
      "refreshToken", UUID.randomUUID().toString(),
      "issuedDate", clock.millis() - 2000,
      "expiredDate", clock.millis() - 1000));
    assertThatThrownBy(() -> service.verify("expired"))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("expired");

    when(redisUtil.getValue("missing")).thenReturn("not-a-map");
    assertThatThrownBy(() -> service.verify("missing"))
      .isInstanceOf(RuntimeException.class).hasMessage("Refresh token not found");

    when(redisUtil.getValue("broken")).thenThrow(new RuntimeException("redis read failed"));
    assertThatThrownBy(() -> service.verify("broken")).hasMessage("redis read failed");
  }

  private IRefreshTokenServices.User user(UUID userCode) {
    return IRefreshTokenServices.User.builder().userCode(userCode).build();
  }
}
