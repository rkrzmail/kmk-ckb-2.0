package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.entity.EmailDeliveryLog;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.common.service.EmailDeliveryService;
import com.kmkbe.modules.customer.service.CustomerService;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SignatureException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class ApprovalEmailDeliveryController {
  private final EmailDeliveryService emailDeliveryService;
  private final CurrentUserService currentUserService;
  private final MstAppRoleFormUserRepository roleRepository;

  @GetMapping("/{custCode}/approval-email-deliveries")
  public List<EmailDeliveryLog> history(@PathVariable UUID custCode) throws SignatureException {
    requireMajorAccount();
    return emailDeliveryService.history(custCode);
  }

  @PostMapping("/approval-email-deliveries/{deliveryId}/retry")
  public BaseResponse retry(@PathVariable Long deliveryId) throws SignatureException {
    requireMajorAccount();
    EmailDeliveryLog delivery = emailDeliveryService.retry(deliveryId);
    boolean sent = delivery.getStatus() == EmailDeliveryLog.Status.SENT;
    return new BaseResponseBuilder<>(true, 200,
      sent ? "Email berhasil diterima server SMTP." : "Email belum berhasil dikirim. Silakan periksa riwayat pengiriman.",
      new CustomerService.ApprovalEmailResult(
        "M_CUST_ACTIVE".equals(delivery.getTemplateCode()) ? "APPROVED" : "REJECTED",
        delivery.getEmailDeliveryId(), sent));
  }

  private void requireMajorAccount() throws SignatureException {
    UUID userCode = currentUserService.internalUser().getUserCode();
    if (!roleRepository.hasActiveMajorAccountRole(userCode)) {
      throw new BusinessException(HttpStatus.FORBIDDEN, 403, "Akses hanya untuk Major Account.");
    }
  }
}
