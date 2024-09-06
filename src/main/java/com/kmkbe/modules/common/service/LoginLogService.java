package com.kmkbe.modules.common.service;

import com.kmkbe.core.domain.constant.LoginRole;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.LoginLog;
import com.kmkbe.core.domain.repository.LoginLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class LoginLogService {
    private final LoginLogRepository loginLogRepository;

    public void create(Customer cust, LoginRole role) {
        final LoginLog loginLog = new LoginLog();

        if (cust != null) {
            loginLog.setCustCode(cust);
        }

        loginLog.setLoginLogCode(UUID.randomUUID());
        loginLog.setLoginDate(Instant.now());
        loginLog.setLoginRole(role.name()); // need to change
        loginLog.setIsLogout(false);
        loginLogRepository.save(loginLog);
    }

    public void logout(Customer cust) {
        try {
            final Optional<LoginLog> find = loginLogRepository.findTopByCustCode(cust);
            if (find.isEmpty()) {
                throw new EntityNotFoundException("User not found");
            }

            final LoginLog loginLog = find.get();
            loginLog.setIsLogout(true);
            loginLog.setLogoutDate(Instant.now());
            loginLog.setUsrLogout(Instant.now());
            loginLogRepository.save(loginLog);
        } catch (Exception e) {
            log.error("error logout: {}", e.getMessage());
            throw e;
        }
    }
}
