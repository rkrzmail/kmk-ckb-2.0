package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.RequestOtpRequest;
import com.kmkbe.modules.customer.model.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceCoverageTest {
  private static final Instant NOW = Instant.parse("2026-11-24T00:00:00Z");
  @Mock OtpRepository otpRepository;
  @Mock CustomerRepository customerRepository;
  @Mock EmailService emailService;
  @Mock CustomerService customerService;
  @Mock BCryptPasswordEncoder bcryptEncoder;
  @Mock Clock clock;
  @Mock OtpGenerator otpGenerator;
  @Mock MajorAccountRegistrationNotificationService notificationService;
  @InjectMocks OtpService service;

  @BeforeEach
  void setUp() {
    lenient().when(clock.instant()).thenReturn(NOW);
    lenient().when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    lenient().when(otpGenerator.generate()).thenReturn("1234");
  }

  @Test
  void createSendsOtpByType() throws Exception {
    Customer customer = customer();

    OtpLog signup = service.create(customer, OtpService.OtpType.SIGNUP);

    assertThat(signup.getOtpCode()).isEqualTo("1234");
    assertThat(signup.getExpiredDate()).isAfter(signup.getGeneratedDate());
    verify(emailService).sendOtp(customer, "1234");

    OtpLog changePin = service.create(customer, OtpService.OtpType.CHANGE_PIN);
    assertThat(changePin.getEmail()).isEqualTo("customer@example.com");
    verify(emailService).sendOtpChangePin(customer, "1234");

    verify(otpRepository, org.mockito.Mockito.times(2)).save(any(OtpLog.class));
  }

  @Test
  void verifySignupSuccessAndFailures() throws Exception {
    VerifyOtpRequest request = new VerifyOtpRequest("r", "customer@example.com", "1234", null);
    Customer customer = customer();
    when(customerRepository.findByCustEmailOrderByCustIdDesc("customer@example.com"))
      .thenReturn(Optional.of(customer));

    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("customer@example.com", "1234"))
      .thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.verifySignUp(request)).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Otp not valid");

    OtpLog used = otpLog(false);
    used.setIsUsed(true);
    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("customer@example.com", "1234"))
      .thenReturn(Optional.of(used));
    assertThatThrownBy(() -> service.verifySignUp(request)).hasMessageContaining("already used");

    OtpLog expired = otpLog(false);
    expired.setExpiredDate(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).minusMinutes(1));
    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("customer@example.com", "1234"))
      .thenReturn(Optional.of(expired));
    assertThatThrownBy(() -> service.verifySignUp(request)).hasMessage("Otp is Expired");

    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("customer@example.com", "1234"))
      .thenReturn(Optional.of(otpLog(false)));
    assertThat(service.verifySignUp(request)).isEqualTo("Sign up successfully");
    verify(customerService).verifyEmail(customer);
    verify(notificationService).notifyRegistrationCompleted(customer);

    when(customerRepository.findByCustEmailOrderByCustIdDesc("customer@example.com")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.verifySignUp(request)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void sendForgotPinBranches() throws Exception {
    when(customerRepository.findByCustEmail("missing@example.com")).thenReturn(Optional.empty());
    assertThat(service.sendForgotPin("missing@example.com").email()).isEqualTo("missing@example.com");

    Customer customer = customer();
    when(customerRepository.findByCustEmail("customer@example.com")).thenReturn(Optional.of(customer));
    when(bcryptEncoder.encode(anyString())).thenReturn("hash");
    assertThat(service.sendForgotPin("customer@example.com").email()).isEqualTo("customer@example.com");
    verify(emailService).sendOtpChangePin(customer, "1234");
  }

  @Test
  void verifyForgotPinBranches() {
    VerifyOtpRequest request = new VerifyOtpRequest("r", "customer@example.com", "1234", null);
    Customer customer = customer();
    OtpLog otp = otpLog(false);
    when(customerRepository.findByCustEmailOrderByCustIdDesc("customer@example.com"))
      .thenReturn(Optional.of(customer));
    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("customer@example.com", "1234"))
      .thenReturn(Optional.of(otp));
    when(bcryptEncoder.encode(anyString())).thenReturn("hash");

    when(bcryptEncoder.matches(anyString(), anyString())).thenReturn(false);
    assertThatThrownBy(() -> service.verifyForgotPin(request)).hasMessageContaining("Request id not valid");

    when(bcryptEncoder.matches(anyString(), anyString())).thenReturn(true);
    otp.setExpiredDate(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).minusMinutes(1));
    assertThatThrownBy(() -> service.verifyForgotPin(request)).hasMessageContaining("expired");

    otp.setExpiredDate(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(5));
    assertThat(service.verifyForgotPin(request)).isEqualTo("Otp verified, try to enter new pin");
  }

  @Test
  void resendAndEmailHelpers() throws Exception {
    Customer customer = customer();
    when(customerRepository.findByCustEmailOrderByCustIdDesc("customer@example.com"))
      .thenReturn(Optional.of(customer));
    when(otpRepository.findTopByEmail("customer@example.com")).thenReturn(otpLog(false));
    when(bcryptEncoder.encode(anyString())).thenReturn("hash");

    assertThat(service.resend(new RequestOtpRequest("customer@example.com"), OtpService.OtpType.SIGNUP).email())
      .isEqualTo("customer@example.com");
    verify(emailService).sendOtp(customer, "1234");

    assertThat(service.resend(new RequestOtpRequest("customer@example.com"), OtpService.OtpType.CHANGE_PIN).email())
      .isEqualTo("customer@example.com");

    assertThat(service.genRequestId(customer, otpLog(false))).isEqualTo("hash");
    assertThat(service.genRequestId("customer@example.com", "otp")).isEqualTo("hash");
    assertThat(service.genRequestId2("customer@example.com", otpLog(false))).isEqualTo("hash");
    assertThat(OtpService.OtpType.SIGNUP.toString()).isEqualTo("Sign Up");
    assertThat(OtpService.OtpType.CHANGE_PIN.toString()).isEqualTo("Forgot Pin");
  }

  @Test
  void generateOtpForEmailAndVerifyOtp() throws Exception {
    when(bcryptEncoder.encode(anyString())).thenReturn("hash");

    assertThat(service.generateOtpForEmail("e@x.com", OtpService.OtpType.SIGNUP).getOtpCode()).isEqualTo("1234");
    verify(emailService).sendOtp2("e@x.com", "1234");
    service.generateOtpForEmail("e@x.com", OtpService.OtpType.CHANGE_PIN);
    verify(emailService).sendOtpChangePin2("e@x.com", "1234");
    service.generateOtpForEmail("e@x.com", null);
    assertThat(service.resendOtpForEmail("e@x.com", OtpService.OtpType.SIGNUP).requestId()).isEqualTo("hash");

    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("e@x.com", "1234"))
      .thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.verifyOtp("e@x.com", "1234")).hasMessage("No OTP found for this email.");

    OtpLog otp = otpLog(false);
    otp.setExpiredDate(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).minusMinutes(1));
    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("e@x.com", "1234")).thenReturn(Optional.of(otp));
    assertThatThrownBy(() -> service.verifyOtp("e@x.com", "1234")).hasMessage("OTP has expired.");

    otp.setExpiredDate(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(5));
    otp.setOtpCode("9999");
    assertThatThrownBy(() -> service.verifyOtp("e@x.com", "1234")).hasMessage("Incorrect OTP.");

    otp.setOtpCode("1234");
    assertThat(service.verifyOtp("e@x.com", "1234")).isEqualTo("OTP verification successful!");
    verify(emailService, never()).sendOtp(any(), any());
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustCode(java.util.UUID.randomUUID());
    customer.setCustEmail("customer@example.com");
    customer.setCustMobilePhone("08123");
    customer.setCustName("Customer");
    return customer;
  }

  private static OtpLog otpLog(boolean used) {
    OtpLog otp = new OtpLog();
    otp.setOtpLogId(1L);
    otp.setEmail("customer@example.com");
    otp.setOtpCode("1234");
    otp.setIsUsed(used);
    otp.setExpiredDate(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(5));
    return otp;
  }
}
