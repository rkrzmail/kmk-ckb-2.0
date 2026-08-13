package com.kmkbe.modules.common.service;

import com.kmkbe.core.domain.constant.LoginRole;
import com.kmkbe.core.domain.entity.LoginLog;
import com.kmkbe.core.domain.repository.LoginLogRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginLogServiceTest {

  @Mock private LoginLogRepository loginLogRepository;

  @Test
  void createPersistsLoginLogWithCustomer() {
    LoginLogService service = new LoginLogService(loginLogRepository);
    Customer customer = new Customer();

    service.create(customer, LoginRole.Customer);

    ArgumentCaptor<LoginLog> captor = ArgumentCaptor.forClass(LoginLog.class);
    verify(loginLogRepository).save(captor.capture());
    LoginLog saved = captor.getValue();
    assertThat(saved.getCustCode()).isSameAs(customer);
    assertThat(saved.getLoginLogCode()).isNotNull();
    assertThat(saved.getLoginDate()).isNotNull();
    assertThat(saved.getLoginRole()).isEqualTo("Customer");
    assertThat(saved.getIsLogout()).isFalse();
  }

  @Test
  void createAllowsNullCustomer() {
    LoginLogService service = new LoginLogService(loginLogRepository);

    service.create(null, LoginRole.Internal);

    ArgumentCaptor<LoginLog> captor = ArgumentCaptor.forClass(LoginLog.class);
    verify(loginLogRepository).save(captor.capture());
    assertThat(captor.getValue().getCustCode()).isNull();
    assertThat(captor.getValue().getLoginRole()).isEqualTo("Internal");
  }

  @Test
  void logoutMarksLatestLoginLogAsLoggedOut() {
    LoginLogService service = new LoginLogService(loginLogRepository);
    Customer customer = new Customer();
    LoginLog loginLog = new LoginLog();
    loginLog.setIsLogout(false);
    when(loginLogRepository.findTopByCustCode(customer)).thenReturn(Optional.of(loginLog));

    service.logout(customer);

    assertThat(loginLog.getIsLogout()).isTrue();
    assertThat(loginLog.getLogoutDate()).isNotNull();
    assertThat(loginLog.getUsrLogout()).isNotNull();
    verify(loginLogRepository).save(loginLog);
  }

  @Test
  void logoutThrowsWhenLatestLoginLogMissing() {
    LoginLogService service = new LoginLogService(loginLogRepository);
    Customer customer = new Customer();
    when(loginLogRepository.findTopByCustCode(customer)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.logout(customer))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void logoutRethrowsRepositoryFailure() {
    LoginLogService service = new LoginLogService(loginLogRepository);
    Customer customer = new Customer();
    when(loginLogRepository.findTopByCustCode(customer)).thenThrow(new RuntimeException("db down"));

    assertThatThrownBy(() -> service.logout(customer))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("db down");
  }
}
