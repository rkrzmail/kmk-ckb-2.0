package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.CustomerDashboardDto;
import com.kmkbe.core.domain.dto.CustomerPlafondDto;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.utils.FormatingUtils;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerDashboardService {
    private final FinancingHdrService financingHdrService;
    private final CwrRepository cwrRepository;
    private final EntityManager entityManager;

    public CustomerPlafondDto plafond(
            String financingHdrCode
    ) {
        try {
            FinancingHdr financingHdr = financingHdrService.findByCode(financingHdrCode);

            final String address, phoneNo;
            if (financingHdr.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
                address = financingHdr.getCustomer().getCompany() == null ? "" : String.valueOf(financingHdr.getCustomer().getCompany().getCompanyAddress());
                phoneNo = financingHdr.getCustomer().getCompany() == null ? "" :financingHdr.getCustomer().getCompany().getPhone();
            } else {
                address = financingHdr.getCustomer().getPersonal() == null ? "" : String.valueOf(financingHdr.getCustomer().getPersonal().getLegalAddress());
                phoneNo = financingHdr.getCustomer().getPersonal() == null ? "" :financingHdr.getCustomer().getPersonal().getPhone();
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
                    .phoneNo(phoneNo)
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

    public CustomerDashboardDto mainDashboard(Authentication authentication) throws SignatureException {
        try {
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final Cwr lastCwr = cwrRepository.findTopByCustomerOrderByCwrEndDateDesc(customer)
                    .orElse(null);

            if (lastCwr == null) {
                return new CustomerDashboardDto();
            }

            final FormatingUtils.CurrencyFormatter plafond = new FormatingUtils.CurrencyFormatter(lastCwr.getPlafondAmt());
            final FormatingUtils.CurrencyFormatter used = new FormatingUtils.CurrencyFormatter(lastCwr.getRealisationAmt());
            final FormatingUtils.CurrencyFormatter available = new FormatingUtils.CurrencyFormatter((lastCwr.getPlafondAmt() - lastCwr.getRealisationAmt()));

            final Long invoiceFunded = totalInvoiceFunded(customer);
            return CustomerDashboardDto.builder()
                    .totalPlafond(plafond.getValue())
                    .totalPlafondUnit(plafond.getUnit())
                    .totalPlafondUsed(used.getValue())
                    .totalPlafondUsedUnit(used.getUnit())
                    .totalAvailablePlafond(available.getValue())
                    .totalAvailablePlafondUnit(available.getUnit())
                    .validityLimitDate(Date.from(lastCwr.getCwrEndDate()))
                    .totalInvoiceFounded(invoiceFunded)
                    .build();
        } catch (Exception e) {
            log.error("dashboard: error {}", e.getMessage());
            throw e;
        }
    }

    private Long totalInvoiceFunded(Customer customer) {
        String rawSql = """
                select
                    count(*) as total_invoice
                from
                    agreement amt
                        join cwr on amt.cwr_code = cwr.cwr_code
                        join financing_hdr fhdr on amt.financing_hdr_code = fhdr.financing_hdr_code
                        join financing_dtl fdtl on fhdr.financing_hdr_code = fdtl.financing_hdr_code
                        join invoice ice on fdtl.invoice_code = ice.invoice_code
                where
                    cwr.cust_code = :custCode
                """;

        Query query = entityManager.createNativeQuery(rawSql);
        query.setParameter("custCode", customer.getCustCode());

        var a = query.getResultList();
        return (Long) query.getSingleResult();
    }

    public CustomerDashboardDto.Agreement agreementDashboard(Authentication authentication) throws SignatureException {
        try {

            return new CustomerDashboardDto.Agreement();
        } catch (Exception e) {
            log.error("agreementDashboard: error {}", e.getMessage());
            throw e;
        }
    }
}
