package com.kmkbe.modules.customer.utils;

import com.kmkbe.modules.customer.model.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CustomerUtilsCoverageTest {
  @Test
  void authenticateCustomerAcceptsOnlyCustomerPrincipal() throws Exception {
    Customer customer = new Customer();
    customer.setCustEmail("customer@example.com");

    assertThat(CustomerUtils.authenticateCustomer(
      new UsernamePasswordAuthenticationToken(customer, null, List.of(new SimpleGrantedAuthority("USER")))))
      .isSameAs(customer);
    assertThat(CustomerUtils.authenticateCustomerDto(
      new UsernamePasswordAuthenticationToken(customer, null, List.of(new SimpleGrantedAuthority("USER")))))
      .isNotNull();

    assertThatThrownBy(() -> CustomerUtils.authenticateCustomer(null)).isInstanceOf(SignatureException.class);
    assertThatThrownBy(() -> CustomerUtils.authenticateCustomer(new AnonymousAuthenticationToken(
      "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))))
      .isInstanceOf(SignatureException.class);
    assertThatThrownBy(() -> CustomerUtils.authenticateCustomer(
      new UsernamePasswordAuthenticationToken("other", null, List.of(new SimpleGrantedAuthority("USER")))))
      .isInstanceOf(SignatureException.class);
  }

  @Test
  void calculateStayLengthHandlesNullAndActualDuration() {
    assertThat(CustomerUtils.calculateStayLength(null)).isEqualTo(0.0);
    assertThat(CustomerUtils.calculateStayLength(LocalDateTime.now())).isEqualTo(0.0);
    assertThat(CustomerUtils.calculateStayLength(LocalDateTime.now().minusDays(60))).isGreaterThan(0.0);
  }

  @Test
  void clearCustomerInactiveDataDeletesAllTables() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());

    CustomerUtils.clearCustomerInactiveData(jdbcTemplate, customer);

    verify(jdbcTemplate, times(4)).update(anyString(), org.mockito.ArgumentMatchers.eq(customer.getCustCode()));
  }
}
