package com.kmkbe.modules.api_sbu.model;

import com.kmkbe.helpers.base.Pagination;
import com.kmkbe.modules.api_sbu.model.request.ApiSbuRequest;
import com.kmkbe.modules.api_sbu.model.response.ApiSbuResponse;
import com.kmkbe.modules.api_sbu.model.response.PageApiSbuResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiSbuDtoModelsTest {

  @Test
  @DisplayName("ApiSbuRequest getters, setters, builder, equals, and toString")
  void testApiSbuRequest() {
    Date expiry = new Date();
    ApiSbuRequest req1 = ApiSbuRequest.builder()
      .bouwheerCode("BOUW-001")
      .appName("MyApp")
      .appPath("/api/v1")
      .sesStatus("ACTIVE")
      .expiredDate(expiry)
      .build();

    assertThat(req1.getBouwheerCode()).isEqualTo("BOUW-001");
    assertThat(req1.getAppName()).isEqualTo("MyApp");
    assertThat(req1.getAppPath()).isEqualTo("/api/v1");
    assertThat(req1.getSesStatus()).isEqualTo("ACTIVE");
    assertThat(req1.getExpiredDate()).isEqualTo(expiry);
    assertThat(req1.toString()).contains("BOUW-001", "MyApp");

    ApiSbuRequest req2 = new ApiSbuRequest();
    req2.setBouwheerCode("BOUW-001");
    req2.setAppName("MyApp");
    req2.setAppPath("/api/v1");
    req2.setSesStatus("ACTIVE");
    req2.setExpiredDate(expiry);

    assertThat(req1).isEqualTo(req2);
    assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
  }

  @Test
  @DisplayName("ApiSbuResponse getters, setters, builder, equals, and toString")
  void testApiSbuResponse() {
    UUID bouwheerUuid = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    ApiSbuResponse resp1 = ApiSbuResponse.builder()
      .sesId(10L)
      .bouwheerCode(bouwheerUuid)
      .appName("MyApp")
      .appKey("key-1")
      .appSecret("secret-1")
      .appPath("/path")
      .sesStatus("ACTIVE")
      .tokenJwt("jwt-123")
      .usrCrt("admin")
      .dtmCrt(now)
      .usrUpd("editor")
      .dtmUpd(now)
      .expiredDate(now.plusDays(1))
      .build();

    assertThat(resp1.getSesId()).isEqualTo(10L);
    assertThat(resp1.getBouwheerCode()).isEqualTo(bouwheerUuid);
    assertThat(resp1.getAppName()).isEqualTo("MyApp");
    assertThat(resp1.getAppKey()).isEqualTo("key-1");
    assertThat(resp1.getAppSecret()).isEqualTo("secret-1");
    assertThat(resp1.getAppPath()).isEqualTo("/path");
    assertThat(resp1.getSesStatus()).isEqualTo("ACTIVE");
    assertThat(resp1.getTokenJwt()).isEqualTo("jwt-123");
    assertThat(resp1.getUsrCrt()).isEqualTo("admin");
    assertThat(resp1.getDtmCrt()).isEqualTo(now);
    assertThat(resp1.getUsrUpd()).isEqualTo("editor");
    assertThat(resp1.getDtmUpd()).isEqualTo(now);
    assertThat(resp1.getExpiredDate()).isEqualTo(now.plusDays(1));
    assertThat(resp1.toString()).contains("sesId=10", "MyApp");

    ApiSbuResponse resp2 = new ApiSbuResponse();
    resp2.setSesId(10L);
    resp2.setBouwheerCode(bouwheerUuid);
    resp2.setAppName("MyApp");
    resp2.setAppKey("key-1");
    resp2.setAppSecret("secret-1");
    resp2.setAppPath("/path");
    resp2.setSesStatus("ACTIVE");
    resp2.setTokenJwt("jwt-123");
    resp2.setUsrCrt("admin");
    resp2.setDtmCrt(now);
    resp2.setUsrUpd("editor");
    resp2.setDtmUpd(now);
    resp2.setExpiredDate(now.plusDays(1));

    assertThat(resp1).isEqualTo(resp2);
    assertThat(resp1.hashCode()).isEqualTo(resp2.hashCode());
  }

  @Test
  @DisplayName("PageApiSbuResponse getters, setters, builder, equals, and toString")
  void testPageApiSbuResponse() {
    Pagination pagination = Pagination.builder()
      .currentPage(1)
      .pageSize(10)
      .totalPages(1)
      .totalRecords(1L)
      .isFirstPage(true)
      .isLastPage(true)
      .build();

    ApiSbuResponse item = ApiSbuResponse.builder().sesId(1L).build();
    PageApiSbuResponse page1 = PageApiSbuResponse.builder()
      .content(List.of(item))
      .pagination(pagination)
      .build();

    assertThat(page1.getContent()).hasSize(1);
    assertThat(page1.getPagination()).isEqualTo(pagination);
    assertThat(page1.toString()).contains("content", "pagination");

    PageApiSbuResponse page2 = new PageApiSbuResponse();
    page2.setContent(List.of(item));
    page2.setPagination(pagination);

    assertThat(page1).isEqualTo(page2);
    assertThat(page1.hashCode()).isEqualTo(page2.hashCode());
  }
}
