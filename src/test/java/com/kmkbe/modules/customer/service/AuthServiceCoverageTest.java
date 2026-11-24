package com.kmkbe.modules.customer.service;

import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.constant.AuditActorType;
import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.entity.RedisLog;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.feign.model.dto.CsulGetVendorDto;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.LoginLogService;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.ForgotPinRequest;
import com.kmkbe.modules.customer.model.request.LoginRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.model.RefreshToken;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceCoverageTest {
  @Mock CustomerRepository customerRepository;
  @Mock OtpService otpService;
  @Mock JwtService jwtService;
  @Mock LoginLogService loginLogService;
  @Mock ChangePasswordLogService changePasswordLogService;
  @Mock BCryptPasswordEncoder bcryptEncoder;
  @Mock AuthenticationManager authenticationManager;
  @Mock OtpRepository otpRepository;
  @Mock RedisRepository redisRepository;
  @Mock RedisAttackRepository redisAttackRepository;
  @Mock AuditTrailService auditTrailService;
  @Mock ApiCsulAdapter apiCsulAdapter;
  @Mock BouwheerRepository bouwheerRepository;
  @Mock CustomerService customerService;
  @Mock CustomerCompanyService customerCompanyService;
  @Mock CustomerPersonalService customerPersonalService;
  @Mock DocumentService documentService;
  @Mock IRefreshTokenServices refreshTokenServices;
  @InjectMocks AuthService service;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(service, "refreshTokenServices", refreshTokenServices);
  }

  @Test
  void signUpCompanyWithSbuVendor() throws Exception {
    SignUpRequest request = SignUpRequest.builder().vendorCode("V001").bouwheerCode(UUID.randomUUID().toString())
      .customerType("perusahaan").customerIdNo("ID").mobilePhone("08123").build();
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerName("Bouwheer");
    bouwheer.setIsSbu(true);
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer));

    CsulGetVendorDto vendor = mock(CsulGetVendorDto.class);
    CsulGetVendorDto.BuildingDetail building = mock(CsulGetVendorDto.BuildingDetail.class);
    when(apiCsulAdapter.findByCode("V001")).thenReturn(vendor);
    when(vendor.getVendorBuilding()).thenReturn(building);
    when(vendor.getFoundedDate()).thenReturn("2020-01-01T00:00:00");
    when(customerRepository.findFirstByCustExternalCode("V001")).thenReturn(Optional.empty());
    Customer customer = customer();
    when(customerService.create(request, com.kmkbe.core.domain.constant.CustomerType.Company)).thenReturn(customer);
    OtpLog otp = new OtpLog(); otp.setExpiredDate(LocalDateTime.now().plusSeconds(60));
    when(otpService.create(customer, OtpService.OtpType.SIGNUP)).thenReturn(otp);
    when(otpService.genRequestId(any(Customer.class), any(OtpLog.class))).thenReturn("req-1");

    var result = service.signUp(request);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData().requestId()).isEqualTo("req-1");
    verify(customerCompanyService).create(any(), any());
    verify(documentService).mappingFromInquiryVendor(any(), any());
  }

  @Test
  void signUpValidationFailures() {
    SignUpRequest request = SignUpRequest.builder().vendorCode("V001").bouwheerCode(UUID.randomUUID().toString())
      .customerType("perusahaan").build();
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.signUp(request)).isInstanceOf(IllegalArgumentException.class);

    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setIsSbu(true);
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer));
    when(apiCsulAdapter.findByCode("V001")).thenReturn(null);
    assertThatThrownBy(() -> service.signUp(request)).isInstanceOf(RuntimeException.class);

    when(apiCsulAdapter.findByCode("V001")).thenReturn(mock(CsulGetVendorDto.class));
    Customer approved = customer();
    approved.setApprovalStatus("APPROVED");
    when(customerRepository.findFirstByCustExternalCode("V001")).thenReturn(Optional.of(approved));
    assertThatThrownBy(() -> service.signUp(request)).isInstanceOf(RuntimeException.class);

    request.setCustomerType("unknown");
    when(customerRepository.findFirstByCustExternalCode("V001")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.signUp(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Tipe Debitur");
  }

  @Test
  void signUpPersonalWithoutVendor() throws Exception {
    SignUpRequest request = SignUpRequest.builder().vendorCode("V001").bouwheerCode(UUID.randomUUID().toString())
      .customerType("perorangan").customerIdNo("ID").mobilePhone("08123").build();
    Bouwheer bouwheer = new Bouwheer(); bouwheer.setIsSbu(false);
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer));
    when(customerRepository.findFirstByCustExternalCode("V001")).thenReturn(Optional.empty());
    Customer customer = customer();
    when(customerService.create(request, com.kmkbe.core.domain.constant.CustomerType.Personal)).thenReturn(customer);
    OtpLog otp = new OtpLog(); otp.setExpiredDate(LocalDateTime.now().plusSeconds(60));
    when(otpService.create(customer, OtpService.OtpType.SIGNUP)).thenReturn(otp);
    when(otpService.genRequestId(any(Customer.class), any(OtpLog.class))).thenReturn("req");

    assertThat(service.signUp(request).isSuccess()).isTrue();
    verify(customerPersonalService).create(any(), any());
    verify(documentService, never()).mappingFromInquiryVendor(any(), any());
  }

  @Test
  void signInSuccessAndFailures() throws Exception {
    LoginRequest request = new LoginRequest("customer@example.com", "1234");
    when(redisAttackRepository.findTopByRedis(anyString())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.signIn(new LoginRequest("bad", "1234"))).isInstanceOf(RuntimeException.class);

    when(customerRepository.findByCustEmail("customer@example.com")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.signIn(request)).isInstanceOf(RuntimeException.class);

    Customer customer = customer();
    customer.setActive(false);
    when(customerRepository.findByCustEmail("customer@example.com")).thenReturn(Optional.of(customer));
    assertThatThrownBy(() -> service.signIn(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("not active");

    customer.setActive(true);
    when(bcryptEncoder.matches("1234", customer.getCustPin())).thenReturn(false);
    assertThatThrownBy(() -> service.signIn(request)).isInstanceOf(RuntimeException.class);

    when(bcryptEncoder.matches("1234", customer.getCustPin())).thenReturn(true);
    Authentication auth = new UsernamePasswordAuthenticationToken("customer@example.com", "1234");
    when(authenticationManager.authenticate(any())).thenReturn(auth);
    RefreshToken refresh = new RefreshToken(); refresh.setRefreshToken(UUID.randomUUID());
    when(refreshTokenServices.create(any())).thenReturn(refresh);
    when(jwtService.generateToken(customer)).thenReturn("jwt");
    when(jwtService.getExpirationTime()).thenReturn(1000L);

    assertThat(service.signIn(request).isSuccess()).isTrue();
    verify(loginLogService).create(customer, com.kmkbe.core.domain.constant.LoginRole.Customer);
    verify(auditTrailService).recordAuthentication(anyString(), any(AuditActorType.class), anyString(), any(), org.mockito.ArgumentMatchers.eq(true), any());
  }

  @Test
  void logoutForgotPinAndRefreshToken() throws Exception {
    assertThatThrownBy(() -> service.logout(null)).isInstanceOf(IllegalStateException.class);
    Customer customer = customer();
    assertThat(service.logout(customer)).isEqualTo("Logout Successfully");

    ForgotPinRequest forgot = new ForgotPinRequest("customer@example.com", "5678", "token");
    when(customerRepository.findByCustEmail("customer@example.com")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.forgotPin(forgot)).isInstanceOf(RuntimeException.class);

    when(customerRepository.findByCustEmail("customer@example.com")).thenReturn(Optional.of(customer));
    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("customer@example.com", "token"))
      .thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.forgotPin(forgot)).isInstanceOf(RuntimeException.class);

    OtpLog otp = new OtpLog();
    when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc("customer@example.com", "token"))
      .thenReturn(Optional.of(otp));
    when(bcryptEncoder.encode("5678")).thenReturn("encoded");
    when(customerRepository.save(customer)).thenReturn(customer);
    assertThat(service.forgotPin(forgot)).contains("Forgot pin successfully");

    RefreshTokenRequest refreshRequest = new RefreshTokenRequest("refresh");
    RefreshToken payload = new RefreshToken();
    payload.setUserCode(customer.getCustCode());
    payload.setRefreshToken(UUID.randomUUID());
    when(refreshTokenServices.verify("refresh")).thenReturn(payload);
    when(redisRepository.findFirstBySession(payload.getRefreshToken().toString()))
      .thenReturn(Optional.of(new RedisLog()));
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    RefreshToken replacement = new RefreshToken(); replacement.setRefreshToken(UUID.randomUUID());
    when(refreshTokenServices.create(any())).thenReturn(replacement);
    when(jwtService.generateToken(customer)).thenReturn("jwt");

    LoginDto login = assertDoesNotThrowRefresh(refreshRequest);
    assertThat(login.getRefreshToken()).isEqualTo(replacement.getRefreshToken().toString());

    when(refreshTokenServices.verify("refresh")).thenThrow(new RuntimeException("bad token"));
    assertThatThrownBy(() -> service.refreshToken(refreshRequest)).hasMessage("bad token");
  }

  private LoginDto assertDoesNotThrowRefresh(RefreshTokenRequest request) {
    try {
      return service.refreshToken(request);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    customer.setCustEmail("customer@example.com");
    customer.setCustName("Customer");
    customer.setCustPin("encoded");
    customer.setActive(true);
    return customer;
  }
}
