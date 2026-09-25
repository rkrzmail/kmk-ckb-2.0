package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.entity.EmailDeliveryLog;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.common.service.EmailDeliveryService;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.kmkbe.exception.BusinessException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalEmailDeliveryControllerTest {
  @Mock private EmailDeliveryService deliveryService;
  @Mock private CurrentUserService currentUserService;
  @Mock private MstAppRoleFormUserRepository roleRepository;

  @Test
  void rejectsInternalUserWithoutMajorAccountRole() throws Exception {
    UUID userCode = UUID.randomUUID();
    when(currentUserService.internalUser()).thenReturn(MstUser.builder().userCode(userCode).build());
    var controller = new ApprovalEmailDeliveryController(deliveryService, currentUserService, roleRepository);

    assertThatThrownBy(() -> controller.history(UUID.randomUUID()))
      .isInstanceOf(BusinessException.class).hasMessageContaining("Akses hanya");
    assertThatThrownBy(() -> controller.retry(1L))
      .isInstanceOf(BusinessException.class).hasMessageContaining("Akses hanya");
    verifyNoInteractions(deliveryService);
  }

  @Test
  void majorAccountCanReadHistoryAndRetryOnlyFailedDelivery() throws Exception {
    UUID userCode = UUID.randomUUID();
    UUID customerCode = UUID.randomUUID();
    when(currentUserService.internalUser()).thenReturn(MstUser.builder().userCode(userCode).build());
    when(roleRepository.hasActiveMajorAccountRole(userCode)).thenReturn(true);
    var delivery = new EmailDeliveryLog();
    delivery.setEmailDeliveryId(7L);
    delivery.setTemplateCode("M_CUST_ACTIVE");
    delivery.setStatus(EmailDeliveryLog.Status.SENT);
    when(deliveryService.history(customerCode)).thenReturn(List.of(delivery));
    when(deliveryService.retry(7L)).thenReturn(delivery);
    var controller = new ApprovalEmailDeliveryController(deliveryService, currentUserService, roleRepository);

    assertThat(controller.history(customerCode)).containsExactly(delivery);
    var response = (BaseResponseBuilder<?>) controller.retry(7L);
    assertThat(response.getMessage()).contains("SMTP");
    verify(deliveryService).retry(7L);
  }
}
