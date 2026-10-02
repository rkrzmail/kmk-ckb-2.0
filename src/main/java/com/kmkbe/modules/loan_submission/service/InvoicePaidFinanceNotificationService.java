package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.common.service.EmailDeliveryService;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePaidFinanceNotificationService {
  private final FinancingHdrRepository financingHdrRepository;
  private final FinancingDtlRepository financingDtlRepository;
  private final AgreementRepository agreementRepository;
  private final MstAppRoleFormUserRepository roleRepository;
  private final EmailService emailService;
  private final EmailDeliveryService emailDeliveryService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public void onInvoicePaid(InvoicePaidEvent event) {
    UUID customerCode = null;
    String recipient = "";
    boolean deliveryRecorded = false;
    boolean acceptedBySmtp = false;
    try {
      FinancingHdr financing = financingHdrRepository.findByFinancingHdrCode(event.financingHdrCode())
        .orElseThrow(() -> new IllegalStateException("Financing not found: " + event.financingHdrCode()));
      customerCode = financing.getCustomer().getCustCode();
      List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(event.financingHdrCode());
      if (agreements.isEmpty()) {
        log.warn("Finance invoice paid notification skipped: no agreement. financingHdrCode={}", event.financingHdrCode());
        emailDeliveryService.recordFinanceInvoicePaid(customerCode, recipient,
          new EmailService.DeliveryResult(false, "Agreement tidak ditemukan untuk pengajuan ini."));
        deliveryRecorded = true;
        return;
      }
      List<String> recipients = roleRepository.findActiveFinanceEmails();
      if (recipients == null || recipients.isEmpty()) {
        log.warn("Finance invoice paid notification skipped: no active Finance user with email. financingHdrCode={}",
          event.financingHdrCode());
        emailDeliveryService.recordFinanceInvoicePaid(customerCode, recipient,
          new EmailService.DeliveryResult(false, "Tidak ada pengguna Finance aktif dengan alamat email."));
        deliveryRecorded = true;
        return;
      }
      recipient = String.join(";", recipients);

      List<InvoiceEmailPayload> invoices = financingDtlRepository.findAllByFinancingHdr(financing)
        .orElse(List.of()).stream()
        .map(detail -> detail.getInvoice())
        .filter(invoice -> invoice != null && "PAID".equalsIgnoreCase(invoice.getStatus()))
        .map(invoice -> InvoiceEmailPayload.builder()
          .invoiceNo(invoice.getCustInvNo())
          .description(invoice.getInvoiceDescription())
          .bouwheerName(financing.getBouwheer().getBouwheerName())
          .invoiceDate(DateTimeUtils.formatToDate(invoice.getInvoiceDate()))
          .invoiceDueDate(DateTimeUtils.formatToDate(invoice.getInvoiceDueDate()))
          .invoiceAmt(String.format(Locale.forLanguageTag("id-ID"), "%,.0f", invoice.getInvoiceAmt()))
          .build())
        .toList();
      if (invoices.isEmpty()) {
        log.warn("Finance invoice paid notification skipped: no paid invoice. financingHdrCode={}", event.financingHdrCode());
        emailDeliveryService.recordFinanceInvoicePaid(customerCode, recipient,
          new EmailService.DeliveryResult(false, "Tidak ada invoice berstatus PAID untuk pengajuan ini."));
        deliveryRecorded = true;
        return;
      }

      Map<String, Object> fields = new HashMap<>();
      fields.put("agreementCode", escape(agreements.getFirst().getAgreementCode()));
      fields.put("bouwheerName", escape(financing.getBouwheer().getBouwheerName()));
      fields.put("vendorName", escape(financing.getCustomer().getCustName()));
      fields.put("vendorEmail", escape(financing.getCustomer().getCustEmail()));
      fields.put("vendorPhone", escape(financing.getCustomer().getCustMobilePhone()));
      fields.put("submissionDate", DateTimeUtils.formatToDate(financing.getFinancingDate()));
      fields.put("invoices", InvoiceEmailPayload.toHtmlListBody(invoices));

      EmailService.DeliveryResult result = emailService.sendFinanceInvoicePaidNotification(
        recipient, agreements.getFirst().getAgreementCode(), fields);
      acceptedBySmtp = result.acceptedBySmtp();
      emailDeliveryService.recordFinanceInvoicePaid(customerCode, recipient, result);
      deliveryRecorded = true;
      if (!result.acceptedBySmtp()) {
        log.error("Finance invoice paid email failed. financingHdrCode={}, error={}",
          event.financingHdrCode(), result.errorMessage());
      } else {
        log.info("Finance invoice paid email accepted. financingHdrCode={}, recipientCount={}",
          event.financingHdrCode(), recipients.size());
      }
    } catch (Exception e) {
      log.error("Finance invoice paid notification failed. financingHdrCode={}", event.financingHdrCode(), e);
      if (!deliveryRecorded && !acceptedBySmtp && customerCode != null) {
        try {
          emailDeliveryService.recordFinanceInvoicePaid(customerCode, recipient,
            new EmailService.DeliveryResult(false, e.getClass().getSimpleName() + ": " + e.getMessage()));
        } catch (Exception logError) {
          log.error("Finance invoice paid delivery log failed. financingHdrCode={}", event.financingHdrCode(), logError);
        }
      }
    }
  }

  private String escape(String value) {
    return HtmlUtils.htmlEscape(value == null ? "" : value);
  }
}
