package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.repository.CustomerCompanyRepository;
import com.kmkbe.core.domain.repository.CustomerPersonalRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerSeederServiceCoverageTest {
  @Mock CustomerRepository customerRepository;
  @Mock CustomerCompanyRepository companyRepository;
  @Mock CustomerPersonalRepository personalRepository;
  @Mock BCryptPasswordEncoder bcryptEncoder;
  @InjectMocks CustomerSeederService service;

  @BeforeEach
  void setUp() {
    lenient().when(bcryptEncoder.encode("123456")).thenReturn("encoded");
    lenient().when(customerRepository.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));
  }

  @Test
  void seedPersonalWhenMissing() {
    when(customerRepository.findByCustEmail("genzha112233@gmail.com")).thenReturn(Optional.empty());

    ReflectionTestUtils.invokeMethod(service, "seed");

    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "staySinceLength")).isNotNull();
  }

  @Test
  void seedSkippedWhenPersonalExists() {
    when(customerRepository.findByCustEmail("genzha112233@gmail.com"))
      .thenReturn(Optional.of(new Customer()));

    ReflectionTestUtils.invokeMethod(service, "seed");
  }

  @Test
  void privateSeedMethodsAndErrorPaths() {
    assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "seedCustomerPersonal", "p@x.com"))
      .doesNotThrowAnyException();
    assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "seedCustomerCompany", "c@x.com"))
      .doesNotThrowAnyException();

    when(customerRepository.save(any(Customer.class))).thenThrow(new RuntimeException("db down"));
    assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "seedCustomerPersonal", "p@x.com"))
      .isInstanceOf(RuntimeException.class);
    ReflectionTestUtils.invokeMethod(service, "seed");
  }
}
