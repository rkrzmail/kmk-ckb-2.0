package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceIssueRegressionTest {

  @Mock private OtpRepository otpRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private EmailService emailService;
  @Mock private CustomerService customerService;
  @Mock private BCryptPasswordEncoder bcryptEncoder;
  @Mock private OtpGenerator otpGenerator;

  private OtpService service;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(Instant.parse("2026-08-18T03:00:00Z"), ZoneId.of("Asia/Jakarta"));
    service = new OtpService(
      otpRepository,
      customerRepository,
      emailService,
      customerService,
      bcryptEncoder,
      clock,
      otpGenerator
    );
  }

  @Test
  void verifySignUpDelegatesToVerifyEmailInsteadOfActivatingCustomer() throws Exception {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custEmail("user@example.com")
      .isEmailValid(false)
      .isActive(false)
      .build();
    OtpLog otpLog = new OtpLog();
    otpLog.setOtpLogId(1L);
    otpLog.setOtpCode("123456");
    otpLog.setEmail("user@example.com");
    otpLog.setExpiredDate(LocalDateTime.ofInstant(Instant.parse("2026-08-18T03:05:00Z"), ZoneId.of("Asia/Jakarta")));
    otpLog.setIsUsed(false);

    when(customerRepository.findByCustEmailOrderByCustIdDesc("user@example.com")).thenReturn(Optional.of(customer));
    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("user@example.com", "123456")).thenReturn(Optional.of(otpLog));

    String result = service.verifySignUp(new VerifyOtpRequest(null, "user@example.com", "123456", null));

    assertThat(result).isEqualTo("Sign up successfully");
    assertThat(otpLog.getIsUsed()).isTrue();
    verify(customerService).verifyEmail(customer);
    verify(otpRepository).save(otpLog);
  }
}
