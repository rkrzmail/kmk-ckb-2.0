package com.kmkbe.modules.api_sbu.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiSbuEntityTest {

  @Test
  @DisplayName("ApiSbu getters, setters, builder, and toString coverage")
  void testApiSbuCoverage() {
    UUID bouwheerCode = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    ApiSbu sbu = ApiSbu.builder()
      .sesId(1L)
      .bouwheerCode(bouwheerCode)
      .tokenJwt("jwt-sample")
      .usrCrt("admin")
      .dtmCrt(now)
      .usrUpd("editor")
      .dtmUpd(now)
      .expiredDate(now.plusDays(1))
      .sesStatus("ACTIVE")
      .appPath("/path")
      .appKey("key-123")
      .appSecret("secret-456")
      .appName("app-test")
      .build();

    assertThat(sbu.getSesId()).isEqualTo(1L);
    assertThat(sbu.getBouwheerCode()).isEqualTo(bouwheerCode);
    assertThat(sbu.getTokenJwt()).isEqualTo("jwt-sample");
    assertThat(sbu.getUsrCrt()).isEqualTo("admin");
    assertThat(sbu.getDtmCrt()).isEqualTo(now);
    assertThat(sbu.getUsrUpd()).isEqualTo("editor");
    assertThat(sbu.getDtmUpd()).isEqualTo(now);
    assertThat(sbu.getExpiredDate()).isEqualTo(now.plusDays(1));
    assertThat(sbu.getSesStatus()).isEqualTo("ACTIVE");
    assertThat(sbu.getAppPath()).isEqualTo("/path");
    assertThat(sbu.getAppKey()).isEqualTo("key-123");
    assertThat(sbu.getAppSecret()).isEqualTo("secret-456");
    assertThat(sbu.getAppName()).isEqualTo("app-test");

    String toString = sbu.toString();
    assertThat(toString).contains("sesId=1", "app-test", "ACTIVE");

    ApiSbu sbu2 = new ApiSbu();
    sbu2.setSesId(1L);
    assertThat(sbu.equals(sbu2)).isFalse();
    assertThat(sbu.hashCode()).isNotZero();
  }
}
