package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.LoginLog;
import com.kmkbe.modules.customer.repository.LoginLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class LoginLogService {
    private final LoginLogRepository loginLogRepository;

    public void create(Customer cust) {
        final LoginLog loginLog = new LoginLog();

        if (cust != null) {
            loginLog.setCustCode(cust.getCustCode());
        }

        loginLog.setLoginLogCode(UUID.randomUUID());
        loginLog.setLoginDate(OffsetDateTime.now());
        loginLog.setLoginRole("Customer"); // need to change
        loginLog.setIsLogout(false);
        loginLogRepository.save(loginLog);
    }

    public void logout(Customer cust) {
        try {
            final Optional<LoginLog> find = loginLogRepository.findTopByCustCode(cust.getCustCode());
            if (find.isEmpty()) {
                throw new EntityNotFoundException("User not found");
            }

            final LoginLog loginLog = find.get();
            loginLog.setIsLogout(true);
            loginLog.setLogoutDate(OffsetDateTime.now());
            loginLog.setUsrLogout(OffsetDateTime.now());
            loginLogRepository.save(loginLog);
        } catch (Exception e) {
            log.error("error logout: {}", e.getMessage());
            throw e;
        }
    }
}
