package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingService {

    public void paidByMST(FinancingInvoicePaidRequest request) {
        try {
        } catch (Exception e) {
            log.error("paidByMST, error {}", e.getMessage());
            throw e;
        }
    }
}
