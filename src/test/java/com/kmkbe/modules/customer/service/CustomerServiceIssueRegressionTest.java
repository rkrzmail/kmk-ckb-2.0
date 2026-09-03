package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.enums.ApprovalStatus;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.ApprovalRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceIssueRegressionTest {

  @Mock private CustomerRepository customerRepository;
  @Mock private BCryptPasswordEncoder bcryptEncoder;
  @Mock private JdbcTemplate jdbcTemplate;
  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private EmailService emailService;
  @Mock private AuditTrailService auditTrailService;
  @Mock private BouwheerRepository bouwheerRepository;

  private CustomerService service;

  @BeforeEach
  void setUp() {
    service = new CustomerService(
      customerRepository,
      bcryptEncoder,
      jdbcTemplate,
      financingHdrRepository,
      emailService,
      auditTrailService,
      bouwheerRepository
    );
  }

  @Test
  void createAllowsReRegistrationForSameVendorBeforeEmailIsVerified() {
    Customer existing = Customer.builder()
      .custCode(UUID.randomUUID())
      .custExternalCode("VENDOR-001")
      .custEmail("old@example.com")
      .isEmailValid(false)
      .isActive(false)
      .approvalStatus(ApprovalStatus.OPEN.name())
      .build();
    SignUpRequest request = signUpRequest("VENDOR-001", "new@example.com");

    when(customerRepository.findFirstByCustExternalCode("VENDOR-001")).thenReturn(Optional.of(existing));
    when(customerRepository.findByCustEmail("new@example.com")).thenReturn(Optional.empty());
    when(bcryptEncoder.encode("123456")).thenReturn("encoded-pin");
    when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Customer saved = service.create(request, CustomerType.Company);

    assertThat(saved).isSameAs(existing);
    assertThat(saved.getCustEmail()).isEqualTo("new@example.com");
    assertThat(saved.isActive()).isFalse();
    assertThat(saved.getIsEmailValid()).isFalse();
    assertThat(saved.getApprovalStatus()).isEqualTo(ApprovalStatus.OPEN.name());
    verify(customerRepository).save(existing);
  }

  @Test
  void createRejectsEmailChangeWhenSameVendorEmailIsAlreadyVerified() {
    Customer existing = Customer.builder()
      .custCode(UUID.randomUUID())
      .custExternalCode("VENDOR-001")
      .custEmail("verified@example.com")
      .isEmailValid(true)
      .isActive(false)
      .approvalStatus(ApprovalStatus.OPEN.name())
      .build();

    when(customerRepository.findFirstByCustExternalCode("VENDOR-001")).thenReturn(Optional.of(existing));
    when(customerRepository.findByCustEmail("new@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(signUpRequest("VENDOR-001", "new@example.com"), CustomerType.Company))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Tidak bisa mengubah email yang sudah terverifikasi!");
  }

  @Test
  void createRejectsActiveEmailOwnedByDifferentVendor() {
    Customer existingEmailOwner = Customer.builder()
      .custCode(UUID.randomUUID())
      .custExternalCode("OTHER-VENDOR")
      .custEmail("used@example.com")
      .isActive(true)
      .build();

    when(customerRepository.findFirstByCustExternalCode("VENDOR-001")).thenReturn(Optional.empty());
    when(customerRepository.findByCustEmail("used@example.com")).thenReturn(Optional.of(existingEmailOwner));

    assertThatThrownBy(() -> service.create(signUpRequest("VENDOR-001", "used@example.com"), CustomerType.Company))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Email sudah terdaftar dengan vendor lain!");
  }

  @Test
  void createRequiresTermsAndConditionsAgreement() {
    SignUpRequest request = signUpRequest("VENDOR-001", "user@example.com");
    request.setAgreeTc(false);

    assertThatThrownBy(() -> service.create(request, CustomerType.Company))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Setujui Syarat dan Ketentuan for sign up");
  }

  @Test
  void verifyEmailMarksEmailValidButKeepsCustomerInactiveUntilApproval() {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custEmail("user@example.com")
      .isEmailValid(false)
      .isActive(false)
      .build();
    when(customerRepository.save(customer)).thenReturn(customer);

    service.verifyEmail(customer);

    assertThat(customer.getIsEmailValid()).isTrue();
    assertThat(customer.isActive()).isFalse();
    assertThat(customer.getUsrUpd()).isEqualTo("Debitur");
    assertThat(customer.getDtmUpd()).isNotNull();
    verify(customerRepository).save(customer);
  }

  @Test
  void rejectedCustomerReturnsToOpenOnlyAfterReRegistrationOtpIsVerified() {
    Customer rejected = Customer.builder()
      .custCode(UUID.randomUUID())
      .custExternalCode("VENDOR-001")
      .custName("Debitur Lama")
      .custEmail("user@example.com")
      .isEmailValid(true)
      .isActive(false)
      .approvalStatus(ApprovalStatus.REJECTED.name())
      .approvalNote("Dokumen tidak sesuai")
      .approvalBy("major.user")
      .approvalAt(LocalDateTime.of(2026, 8, 1, 10, 0))
      .build();

    when(customerRepository.findFirstByCustExternalCode("VENDOR-001")).thenReturn(Optional.of(rejected));
    when(customerRepository.findByCustEmail("user@example.com")).thenReturn(Optional.of(rejected));
    when(bcryptEncoder.encode("123456")).thenReturn("encoded-pin");
    when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Customer registered = service.create(signUpRequest("VENDOR-001", "user@example.com"), CustomerType.Company);

    assertThat(registered.getIsEmailValid()).isFalse();
    assertThat(registered.isActive()).isFalse();
    assertThat(registered.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED.name());

    service.verifyEmail(registered);

    assertThat(registered.getIsEmailValid()).isTrue();
    assertThat(registered.isActive()).isFalse();
    assertThat(registered.getApprovalStatus()).isEqualTo(ApprovalStatus.OPEN.name());
    assertThat(registered.getApprovalNote()).isNull();
    assertThat(registered.getApprovalBy()).isNull();
    assertThat(registered.getApprovalAt()).isNull();
  }

  @Test
  void verifyingExistingOpenCustomerDoesNotClearApprovalMetadata() {
    LocalDateTime approvalAt = LocalDateTime.of(2026, 8, 1, 10, 0);
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custEmail("user@example.com")
      .isEmailValid(false)
      .isActive(false)
      .approvalStatus(ApprovalStatus.OPEN.name())
      .approvalNote("Catatan yang masih berlaku")
      .approvalBy("major.user")
      .approvalAt(approvalAt)
      .build();
    when(customerRepository.save(customer)).thenReturn(customer);

    service.verifyEmail(customer);

    assertThat(customer.getApprovalStatus()).isEqualTo(ApprovalStatus.OPEN.name());
    assertThat(customer.getApprovalNote()).isEqualTo("Catatan yang masih berlaku");
    assertThat(customer.getApprovalBy()).isEqualTo("major.user");
    assertThat(customer.getApprovalAt()).isEqualTo(approvalAt);
  }

  @Test
  void approvalSendsAccountActiveEmailWhenCustomerIsApproved() {
    Customer customer = openCustomer();
    ApprovalRequest request = ApprovalRequest.builder()
      .custCode(customer.getCustCode())
      .approvalStatus(ApprovalStatus.APPROVED.name())
      .build();
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    when(customerRepository.save(customer)).thenReturn(customer);

    service.approval(request, "major.user");

    assertThat(customer.isActive()).isTrue();
    assertThat(customer.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED.name());
    verify(emailService).sendNotificationActive(customer);
    verify(emailService, never()).sendNotificationRejected(any(Customer.class), anyString());
    verify(emailService, never()).customerVerification(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void approvalSendsRejectionEmailWithApprovalNoteWhenCustomerIsRejected() {
    Customer customer = openCustomer();
    ApprovalRequest request = ApprovalRequest.builder()
      .custCode(customer.getCustCode())
      .approvalStatus(ApprovalStatus.REJECTED.name())
      .approvalNote("NPWP tidak sesuai")
      .build();
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    when(customerRepository.save(customer)).thenReturn(customer);

    service.approval(request, "major.user");

    assertThat(customer.isActive()).isFalse();
    assertThat(customer.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED.name());
    verify(emailService).sendNotificationRejected(customer, "NPWP tidak sesuai");
    verify(emailService, never()).sendNotificationActive(any(Customer.class));
    verify(emailService, never()).customerVerification(anyString(), anyString(), anyString(), anyString());
  }

  private static Customer openCustomer() {
    return Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custEmail("customer@example.com")
      .custIdNo("NPWP001")
      .isEmailValid(true)
      .isActive(false)
      .approvalStatus(ApprovalStatus.OPEN.name())
      .build();
  }

  private static SignUpRequest signUpRequest(String vendorCode, String email) {
    return SignUpRequest.builder()
      .vendorCode(vendorCode)
      .name("Debitur")
      .customerType("Company")
      .customerIdNo("1234567890123456")
      .email(email)
      .mobilePhone("08123456789")
      .bouwheerCode(UUID.randomUUID().toString())
      .pin("123456")
      .isAgreeTc(true)
      .build();
  }
}
