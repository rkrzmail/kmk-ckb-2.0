package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.entity.ChangePasswordLog;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.repository.ChangePasswordLogRepository;
import com.kmkbe.core.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChangePasswordLogService {
    private final ChangePasswordLogRepository changePasswordLogRepository;

    public void create(Customer cust, String oldPin, String newPin) {
        try {
            ChangePasswordLog changePasswordLog = new ChangePasswordLog();
            changePasswordLog.setCustCode(cust);
            changePasswordLog.setOldPin(oldPin);
            changePasswordLog.setNewPin(newPin);
            changePasswordLog.setUsrCrt(cust.getCustName());
            changePasswordLog.setDtmCrt(DateTimeUtils.now());
            changePasswordLogRepository.save(changePasswordLog);
        } catch (Exception e) {
            log.error("ChangePasswordLogService create: {}", e.getMessage());
            throw e;
        }
    }
}
