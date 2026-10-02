package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.PaymentReceiveHistoryRepository;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.common.service.EmailDeliveryService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoicePaidNotificationTest {
  private final UUID financingCode = UUID.randomUUID();

  @Test
  void publishesOneEventOnlyForFirstPaidTransition() {
    FinancingDtlRepository details = mock(FinancingDtlRepository.class);
    ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    FinancingDtlService service = new FinancingDtlService(details,
      mock(PaymentReceiveHistoryRepository.class), mock(FinancingHdrRepository.class),
      mock(BaseRemoteService.class), mock(ObjectMapper.class), mock(RestTemplate.class), publisher);
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingHdrCode(financingCode);
    Invoice invoice = Invoice.builder().custInvNo("INV-1").invoiceAmt(100.0).status("UNPAID").build();
    FinancingDtl detail = FinancingDtl.builder().invoice(invoice).build();
    when(details.findAllByFinancingHdrForUpdate(financing)).thenReturn(List.of(detail));

    FinancingInvoicePaidRequest.InvoicePaid paid = new FinancingInvoicePaidRequest.InvoicePaid();
    paid.setInvoiceNo("INV-1");
    paid.setInvoiceAmount(BigDecimal.valueOf(100));
    paid.setPostingDate(new Date());
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    request.setFinancingCode(financingCode.toString());
    request.setInvoicePaid(List.of(paid));

    service.updatePaid(request, financing);
    service.updatePaid(request, financing);

    assertThat(invoice.getStatus()).isEqualTo("PAID");
    verify(publisher).publishEvent(new InvoicePaidEvent(financingCode));
  }

  @Test
  void doesNotPublishWhenInvoiceAmountIsInvalid() {
    FinancingDtlRepository details = mock(FinancingDtlRepository.class);
    ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    FinancingDtlService service = new FinancingDtlService(details,
      mock(PaymentReceiveHistoryRepository.class), mock(FinancingHdrRepository.class),
      mock(BaseRemoteService.class), mock(ObjectMapper.class), mock(RestTemplate.class), publisher);
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingHdrCode(financingCode);
    Invoice invoice = Invoice.builder().custInvNo("INV-1").invoiceAmt(100.0).status("UNPAID").build();
    when(details.findAllByFinancingHdrForUpdate(financing))
      .thenReturn(List.of(FinancingDtl.builder().invoice(invoice).build()));
    FinancingInvoicePaidRequest.InvoicePaid paid = new FinancingInvoicePaidRequest.InvoicePaid();
    paid.setInvoiceNo("INV-1");
    paid.setInvoiceAmount(BigDecimal.valueOf(50));
    FinancingInvoicePaidRequest request = new FinancingInvoicePaidRequest();
    request.setFinancingCode(financingCode.toString());
    request.setInvoicePaid(List.of(paid));

    assertThatThrownBy(() -> service.updatePaid(request, financing)).isInstanceOf(RuntimeException.class);
    assertThat(invoice.getStatus()).isEqualTo("UNPAID");
    verify(publisher, never()).publishEvent(any());
  }

  @Test
  void sendsRenderedPaidInvoiceToActiveFinanceRecipients() {
    FinancingHdrRepository headers = mock(FinancingHdrRepository.class);
    FinancingDtlRepository details = mock(FinancingDtlRepository.class);
    AgreementRepository agreements = mock(AgreementRepository.class);
    MstAppRoleFormUserRepository roles = mock(MstAppRoleFormUserRepository.class);
    EmailService email = mock(EmailService.class);
    EmailDeliveryService delivery = mock(EmailDeliveryService.class);
    InvoicePaidFinanceNotificationService service = new InvoicePaidFinanceNotificationService(
      headers, details, agreements, roles, email, delivery);
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingHdrCode(financingCode);
    financing.setFinancingDate(LocalDateTime.of(2026, 9, 30, 10, 0));
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    customer.setCustName("Vendor <One>");
    customer.setCustEmail("vendor@example.com");
    customer.setCustMobilePhone("08123");
    financing.setCustomer(customer);
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerName("PT Trakindo");
    financing.setBouwheer(bouwheer);
    Agreement agreement = Agreement.builder().agreementCode("AGR-123").build();
    Invoice invoice = Invoice.builder().custInvNo("INV-1").invoiceDescription("Barang A")
      .invoiceDate(LocalDateTime.of(2026, 9, 1, 0, 0))
      .invoiceDueDate(LocalDateTime.of(2026, 10, 1, 0, 0))
      .invoiceAmt(100.0).status("PAID").build();
    when(headers.findByFinancingHdrCode(financingCode)).thenReturn(Optional.of(financing));
    when(agreements.findByFinancingHdr_FinancingHdrCode(financingCode)).thenReturn(List.of(agreement));
    when(roles.findActiveFinanceEmails()).thenReturn(List.of("finance@example.com"));
    when(details.findAllByFinancingHdr(financing)).thenReturn(Optional.of(List.of(FinancingDtl.builder().invoice(invoice).build())));
    when(email.sendFinanceInvoicePaidNotification(eq("finance@example.com"), eq("AGR-123"), any()))
      .thenReturn(new EmailService.DeliveryResult(true, null));

    service.onInvoicePaid(new InvoicePaidEvent(financingCode));

    @SuppressWarnings("unchecked")
    org.mockito.ArgumentCaptor<Map<String, Object>> fields = org.mockito.ArgumentCaptor.forClass(Map.class);
    verify(email).sendFinanceInvoicePaidNotification(eq("finance@example.com"), eq("AGR-123"), fields.capture());
    verify(delivery).recordFinanceInvoicePaid(eq(customer.getCustCode()), eq("finance@example.com"),
      eq(new EmailService.DeliveryResult(true, null)));
    assertThat(fields.getValue().get("vendorName")).isEqualTo("Vendor &lt;One&gt;");
    assertThat(fields.getValue().get("invoices").toString()).contains("INV-1", "Barang A");
  }

  @Test
  void skipsSendingWhenNoActiveFinanceUserHasEmail() {
    FinancingHdrRepository headers = mock(FinancingHdrRepository.class);
    AgreementRepository agreements = mock(AgreementRepository.class);
    MstAppRoleFormUserRepository roles = mock(MstAppRoleFormUserRepository.class);
    EmailService email = mock(EmailService.class);
    EmailDeliveryService delivery = mock(EmailDeliveryService.class);
    InvoicePaidFinanceNotificationService service = new InvoicePaidFinanceNotificationService(
      headers, mock(FinancingDtlRepository.class), agreements, roles, email, delivery);
    FinancingHdr financing = new FinancingHdr();
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    financing.setCustomer(customer);
    when(headers.findByFinancingHdrCode(financingCode)).thenReturn(Optional.of(financing));
    when(agreements.findByFinancingHdr_FinancingHdrCode(financingCode))
      .thenReturn(List.of(Agreement.builder().agreementCode("AGR-123").build()));
    when(roles.findActiveFinanceEmails()).thenReturn(List.of());

    service.onInvoicePaid(new InvoicePaidEvent(financingCode));

    verify(email, never()).sendFinanceInvoicePaidNotification(any(), any(), any());
    verify(delivery).recordFinanceInvoicePaid(eq(customer.getCustCode()), eq(""),
      eq(new EmailService.DeliveryResult(false, "Tidak ada pengguna Finance aktif dengan alamat email.")));
  }
}
