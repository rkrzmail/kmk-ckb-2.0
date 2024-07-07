package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.customer.entity.ChangePasswordLog;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.repository.ChangePasswordLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChangePasswordLogService {
    private final ChangePasswordLogRepository changePasswordLogRepository;

    public void create(Customer cust, String oldPin, String newPin) {
        try {
            ChangePasswordLog changePasswordLog = new ChangePasswordLog();
            changePasswordLog.setCustCode(cust.getCustCode());
            changePasswordLog.setOldPin(oldPin);
            changePasswordLog.setNewPin(newPin);
            changePasswordLog.setUsrCrt(cust.getCustName());
            changePasswordLog.setDtmCrt(OffsetDateTime.now());
            changePasswordLogRepository.save(changePasswordLog);
        } catch (Exception e) {
            log.error("ChangePasswordLogService create: {}", e.getMessage());
            throw e;
        }
    }
}
