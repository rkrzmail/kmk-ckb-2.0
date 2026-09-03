package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MajorAccountRegistrationNotificationServiceTest {

  @Mock private MstAppRoleFormUserRepository appRoleFormUserRepository;
  @Mock private EmailService emailService;

  private MajorAccountRegistrationNotificationService service;

  @BeforeEach
  void setUp() {
    service = new MajorAccountRegistrationNotificationService(appRoleFormUserRepository, emailService);
  }

  @Test
  void sendsNotificationToDistinctActiveMajorAccountRecipients() {
    Customer customer = customer();
    when(appRoleFormUserRepository.findActiveMajorAccountEmails()).thenReturn(List.of(
      "major1@csul.co.id",
      " major2@csul.co.id ",
      "major1@csul.co.id"
    ));

    service.notifyRegistrationCompleted(customer);

    verify(emailService).sendNotificationCustomerVerification(
      "major1@csul.co.id;major2@csul.co.id",
      customer
    );
  }

  @Test
  void skipsEmailWhenNoActiveMajorAccountRecipientExists() {
    when(appRoleFormUserRepository.findActiveMajorAccountEmails()).thenReturn(List.of());

    service.notifyRegistrationCompleted(customer());

    verify(emailService, never()).sendNotificationCustomerVerification(
      org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.any(Customer.class)
    );
  }

  @Test
  void recipientLookupFailureDoesNotRollbackRegistration() {
    when(appRoleFormUserRepository.findActiveMajorAccountEmails())
      .thenThrow(new IllegalStateException("user database unavailable"));

    assertThatCode(() -> service.notifyRegistrationCompleted(customer())).doesNotThrowAnyException();

    verify(emailService, never()).sendNotificationCustomerVerification(
      org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.any(Customer.class)
    );
  }

  private Customer customer() {
    return Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Vendor Baru")
      .custEmail("vendor@example.com")
      .build();
  }
}
