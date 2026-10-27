package com.kmkbe.modules.common.service;

import com.kmkbe.core.domain.entity.EmailDeliveryLog;
import com.kmkbe.core.domain.repository.EmailDeliveryLogRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.http.HttpStatus;
import com.kmkbe.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailDeliveryService {
  private final EmailDeliveryLogRepository deliveryLogRepository;
  private final CustomerRepository customerRepository;
  private final EmailService emailService;

  @Transactional
  public EmailDeliveryLog sendApproval(Customer customer, String approvalStatus, String note) {
    EmailDeliveryLog delivery = new EmailDeliveryLog();
    delivery.setCustomerCode(customer.getCustCode());
    delivery.setRecipient(customer.getCustEmail());
    delivery.setTemplateCode("APPROVED".equals(approvalStatus) ? "M_CUST_ACTIVE" : "M_CUST_REJECTED");
    delivery.setStatus(EmailDeliveryLog.Status.PENDING);
    delivery.setRequestedAt(LocalDateTime.now());
    delivery.setUpdatedAt(delivery.getRequestedAt());
    delivery = deliveryLogRepository.save(delivery);
    return attempt(delivery, customer, approvalStatus, note);
  }

  @Transactional
  public EmailDeliveryLog retry(Long deliveryId) {
    EmailDeliveryLog delivery = deliveryLogRepository.findByIdForUpdate(deliveryId)
      .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 404, "Riwayat pengiriman email tidak ditemukan."));
    if (delivery.getStatus() != EmailDeliveryLog.Status.FAILED) {
      throw new BusinessException(HttpStatus.CONFLICT, 409, "Hanya email berstatus FAILED yang dapat dikirim ulang.");
    }
    Customer customer = customerRepository.findByCustCode(delivery.getCustomerCode())
      .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 404, "Customer tidak ditemukan."));
    String approvalStatus = "M_CUST_ACTIVE".equals(delivery.getTemplateCode()) ? "APPROVED" : "REJECTED";
    if (!approvalStatus.equals(customer.getApprovalStatus())
        || !delivery.getRecipient().equalsIgnoreCase(customer.getCustEmail())) {
      throw new BusinessException(HttpStatus.CONFLICT, 409,
        "Status atau alamat email customer telah berubah; pengiriman ulang dibatalkan.");
    }
    return attempt(delivery, customer, approvalStatus, customer.getApprovalNote());
  }

  @Transactional(readOnly = true)
  public List<EmailDeliveryLog> history(UUID customerCode) {
    return deliveryLogRepository.findByCustomerCodeOrderByRequestedAtDesc(customerCode);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public EmailDeliveryLog recordFinanceInvoicePaid(UUID customerCode, String recipients,
                                                     EmailService.DeliveryResult result) {
    LocalDateTime now = LocalDateTime.now();
    EmailDeliveryLog delivery = new EmailDeliveryLog();
    delivery.setCustomerCode(customerCode);
    delivery.setTemplateCode("M_FINANCE_INVOICE_PAID");
    delivery.setRecipient(recipients == null ? "" : recipients);
    delivery.setRequestedAt(now);
    delivery.setUpdatedAt(now);
    delivery.setAttemptCount(recipients == null || recipients.isBlank() ? 0 : 1);
    if (result.acceptedBySmtp()) {
      delivery.setStatus(EmailDeliveryLog.Status.SENT);
      delivery.setSentAt(now);
    } else {
      delivery.setStatus(EmailDeliveryLog.Status.FAILED);
      String reason = result.errorMessage() == null ? "Pengiriman email gagal tanpa detail dari SMTP." : result.errorMessage();
      delivery.setErrorMessage(reason.substring(0, Math.min(reason.length(), 2000)));
    }
    return deliveryLogRepository.save(delivery);
  }

  private EmailDeliveryLog attempt(
    EmailDeliveryLog delivery, Customer customer, String approvalStatus, String note
  ) {
    delivery.setAttemptCount(delivery.getAttemptCount() + 1);
    EmailService.DeliveryResult result = emailService.sendCustomerApprovalNotification(customer, approvalStatus, note);
    delivery.setUpdatedAt(LocalDateTime.now());
    if (result.acceptedBySmtp()) {
      delivery.setStatus(EmailDeliveryLog.Status.SENT);
      delivery.setSentAt(delivery.getUpdatedAt());
      delivery.setErrorMessage(null);
    } else {
      delivery.setStatus(EmailDeliveryLog.Status.FAILED);
      String reason = result.errorMessage() == null ? "Pengiriman email gagal tanpa detail dari SMTP." : result.errorMessage();
      delivery.setErrorMessage(reason.substring(0, Math.min(reason.length(), 2000)));
      log.error("Customer approval email failed. deliveryId={}, customerCode={}, templateCode={}, error={}",
        delivery.getEmailDeliveryId(), delivery.getCustomerCode(), delivery.getTemplateCode(), reason);
    }
    return deliveryLogRepository.save(delivery);
  }
}
