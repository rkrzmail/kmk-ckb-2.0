package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.MjrAccDashboardDto;
import com.kmkbe.modules.major_account.request.MjrDashboardRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MjrDashboardServiceCoverageTest {
  @Mock EntityManager entityManager;
  @Mock Query query;
  MjrDashboardService service;

  @BeforeEach
  void setUp() {
    service = new MjrDashboardService(entityManager,
      Clock.fixed(Instant.parse("2026-11-24T00:00:00Z"), ZoneId.of("Asia/Jakarta")));
  }

  @Test
  void calculateUsesDefaultsAndMapsQueryRows() {
    MjrDashboardRequest request = new MjrDashboardRequest();
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of(
      row("BR-1", "Jakarta", 1, 2L, "3", 0, 5, 6, 7),
      "ignored"));
    MjrAccDashboardDto result = service.calculate(request);
    assertThat(request.getStartDate()).isNotNull();
    assertThat(request.getEndDate()).isNotNull();
    assertThat(result.getChartLabel()).containsExactly("Jakarta");
    assertThat(result.getChartNew()).containsExactly(1L);
    assertThat(result.getTotalAll()).isEqualTo(24L);
    verify(query).setParameter(eq("startDate"), org.mockito.ArgumentMatchers.anyString());
    verify(query).setParameter(eq("endDate"), org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void calculateUsesProvidedRangeAndPropagatesErrors() {
    MjrDashboardRequest request = new MjrDashboardRequest();
    request.setStartDate(Date.from(Instant.parse("2026-11-01T00:00:00Z")));
    request.setEndDate(Date.from(Instant.parse("2026-11-24T00:00:00Z")));
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of());
    assertThat(service.calculate(request).getTotalAll()).isZero();

    when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.calculate(request)).hasMessage("db down");
  }

  @Test
  void privateHelpersConvertValues() {
    assertThat((Long) ReflectionTestUtils.invokeMethod(service, "toLong", 10)).isEqualTo(10L);
    assertThat((Long) ReflectionTestUtils.invokeMethod(service, "toLong", "12")).isEqualTo(12L);
    assertThat((MjrAccDashboardDto.Chart) ReflectionTestUtils.invokeMethod(service, "mapChartRow",
      (Object) row("BR-1", "Jakarta", 1, 2, 3, 4, 5, 6, 7))).isNotNull();
  }

  private static Object[] row(Object... values) {
    return values;
  }
}
