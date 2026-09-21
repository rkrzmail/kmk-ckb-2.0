package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MajorAccountRegistrationNotificationService {

  private final MstAppRoleFormUserRepository appRoleFormUserRepository;
  private final EmailService emailService;
  private final BouwheerRepository bouwheerRepository;

  public void notifyRegistrationCompleted(Customer customer) {
    try {
      List<String> recipients = appRoleFormUserRepository.findActiveMajorAccountEmails()
        .stream()
        .filter(email -> email != null && !email.isBlank())
        .map(String::trim)
        .distinct()
        .toList();

      if (recipients.isEmpty()) {
        log.warn(
          "Major Account registration notification skipped: no active recipient. customerCode={}",
          customer == null ? null : customer.getCustCode()
        );
        return;
      }

      emailService.sendNotificationCustomerVerification(String.join(";", recipients), customer, bouwheerRepository.findByBouwheerCode(customer.getBouwheer() != null ? UUID.fromString(customer.getBouwheer()) : null)
        .map(Bouwheer::getBouwheerName)
        .orElse(null)
      );
    } catch (Exception exception) {
      log.error(
        "Major Account registration notification failed. customerCode={}",
        customer == null ? null : customer.getCustCode(),
        exception
      );
    }
  }
}
