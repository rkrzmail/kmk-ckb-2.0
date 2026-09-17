package com.kmkbe.modules.common.service;

import com.kmkbe.core.domain.entity.EmailDeliveryLog;
import com.kmkbe.core.domain.repository.EmailDeliveryLogRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.kmkbe.exception.BusinessException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailDeliveryServiceTest {
  @Mock private EmailDeliveryLogRepository repository;
  @Mock private CustomerRepository customerRepository;
  @Mock private EmailService emailService;
  private EmailDeliveryService service;

  @BeforeEach
  void setUp() {
    service = new EmailDeliveryService(repository, customerRepository, emailService);
  }

  @Test
  void sendApprovalRecordsAcceptedSmtpAndLeavesTemplateSelectionCorrect() {
    Customer customer = customer("APPROVED");
    when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
    when(emailService.sendCustomerApprovalNotification(customer, "APPROVED", "ok"))
      .thenReturn(new EmailService.DeliveryResult(true, null));

    EmailDeliveryLog delivery = service.sendApproval(customer, "APPROVED", "ok");

    assertThat(delivery.getStatus()).isEqualTo(EmailDeliveryLog.Status.SENT);
    assertThat(delivery.getTemplateCode()).isEqualTo("M_CUST_ACTIVE");
    assertThat(delivery.getRecipient()).isEqualTo(customer.getCustEmail());
    assertThat(delivery.getAttemptCount()).isEqualTo(1);
    assertThat(delivery.getSentAt()).isNotNull();
    verify(repository, times(2)).save(delivery);
  }

  @Test
  void failedRejectionIsRecordedWithReasonAndCanBeRetried() {
    Customer customer = customer("REJECTED");
    customer.setApprovalNote("NPWP blur");
    when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
    when(emailService.sendCustomerApprovalNotification(customer, "REJECTED", "NPWP blur"))
      .thenReturn(new EmailService.DeliveryResult(false, "SMTP rejected"))
      .thenReturn(new EmailService.DeliveryResult(true, null));

    EmailDeliveryLog delivery = service.sendApproval(customer, "REJECTED", "NPWP blur");
    assertThat(delivery.getStatus()).isEqualTo(EmailDeliveryLog.Status.FAILED);
    assertThat(delivery.getErrorMessage()).isEqualTo("SMTP rejected");
    assertThat(delivery.getTemplateCode()).isEqualTo("M_CUST_REJECTED");
    when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(delivery));
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));

    service.retry(1L);

    assertThat(delivery.getStatus()).isEqualTo(EmailDeliveryLog.Status.SENT);
    assertThat(delivery.getAttemptCount()).isEqualTo(2);
    assertThat(delivery.getErrorMessage()).isNull();
  }

  @Test
  void retryRefusesAlreadySentOrChangedRecipient() {
    Customer customer = customer("APPROVED");
    EmailDeliveryLog delivery = new EmailDeliveryLog();
    delivery.setCustomerCode(customer.getCustCode());
    delivery.setTemplateCode("M_CUST_ACTIVE");
    delivery.setRecipient("old@example.com");
    delivery.setStatus(EmailDeliveryLog.Status.SENT);
    when(repository.findByIdForUpdate(2L)).thenReturn(Optional.of(delivery));
    assertThatThrownBy(() -> service.retry(2L)).isInstanceOf(BusinessException.class);

    delivery.setStatus(EmailDeliveryLog.Status.FAILED);
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    assertThatThrownBy(() -> service.retry(2L)).isInstanceOf(BusinessException.class)
      .hasMessageContaining("alamat email");
    verifyNoInteractions(emailService);
  }

  @Test
  void historyReturnsRepositoryEntries() {
    UUID code = UUID.randomUUID();
    var delivery = new EmailDeliveryLog();
    when(repository.findByCustomerCodeOrderByRequestedAtDesc(code)).thenReturn(List.of(delivery));
    assertThat(service.history(code)).containsExactly(delivery);
  }

  private static Customer customer(String status) {
    return Customer.builder().custCode(UUID.randomUUID()).custEmail("customer@example.com")
      .approvalStatus(status).build();
  }
}
