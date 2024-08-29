package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.CustomerPlafondDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerPlafondService {
    private final FinancingHdrService financingHdrService;

    public CustomerPlafondDto plafond(
            String financingHdrCode
    ) {
        try {
            FinancingHdr financingHdr = financingHdrService.findByCode(financingHdrCode);

            final String address;
            if (financingHdr.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
                address = financingHdr.getCustomer().getCompany().getCompanyAddress();
            } else {
                address = financingHdr.getCustomer().getPersonal().getLegalAddress();
            }

            return CustomerPlafondDto.builder()
                    .financingHdrCode(financingHdr.getFinancingHdrCode())
                    .bouwheerCode(financingHdr.getBouwheer().getBouwheerCode())
                    .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
                    .custCode(financingHdr.getCustomer().getCustCode())
                    .custName(financingHdr.getCustomer().getCustName())
                    .custIdTypeCode(financingHdr.getCustomer().getCustIdTypeCode())
                    .custIdNo(financingHdr.getCustomer().getCustIdNo())
                    .email(financingHdr.getCustomer().getCustEmail())
                    .custTypeCode(financingHdr.getCustomer().getCustTypeCode())
                    .address(address)
                    .plafond(CustomerPlafondDto.PlafondDto.builder()
                            .plafond(BigDecimal.valueOf(0))
                            .totalPlafond(BigDecimal.valueOf(0))
                            .availablePlafond(BigDecimal.valueOf(0))
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("detailSubmissionDistribution: error {}", e.getMessage());
            throw e;
        }
    }
}
