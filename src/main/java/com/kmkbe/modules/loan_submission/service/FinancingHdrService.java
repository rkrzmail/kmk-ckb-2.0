package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.constant.FinancingStatus;
import com.kmkbe.core.domain.dto.DisburseInvoiceDto;
import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.dto.PaidInvoiceDto;
import com.kmkbe.core.domain.entity.Bouwheer;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Product;
import com.kmkbe.core.domain.mapper.FinancingMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.model.SimulationDisburseResult;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingHdrService {
    private final FinancingHdrRepository financingHdrRepository;
    private final InvoiceRepository invoiceRepository;

    public FinancingHdr findLastBy(Customer customer) {
        try {
            return financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer).orElse(null);
        } catch (Exception e) {
            log.error("findBy, error {}", e.getMessage());
            throw e;
        }
    }

    public FinancingHdr create(
            Customer customer,
            Bouwheer bouwheer,
            Product product,
            CreateSimulationRequest request,
            SimulationDisburseResult simulationResult
    ) {
        try {
            final PostedInvoicePayload firstInvoice = request.getInvoices().getFirst();
            FinancingHdr header = new FinancingHdr();
            {
                final double disburseAmount = simulationResult.getEstimatedDisburseAmount().doubleValue();
                final double totalInvoiceAmount = simulationResult.getTotalInvoiceAmount();
                final double financingAmount = simulationResult.getFinancingAmount().doubleValue();

                double retentionRate = 0.0 / 100;
                double retentionAmount = disburseAmount * retentionRate;

                double bankLoanInterest = 0.11;
                double interestAmount = disburseAmount * bankLoanInterest;

                header.setFinancingHdrCode(UUID.randomUUID());
                header.setCustomer(customer);
                header.setBouwheer(bouwheer);
                header.setTenor(90L); // 90 as default
                header.setFinancingDate(Instant.now());
                header.setCurrencyCode(firstInvoice.getCurrencyCode());
                header.setInvoiceQty((long) request.getInvoices().size());
                header.setInterestType("COF"); // not clear
                header.setInterestAmt(interestAmount); // not clear
                header.setTermOfPayment(0L); // not clear
                header.setGracePeriod(0L); // not clear
                header.setRetention(retentionAmount); // not clear
                header.setTotalInvoiceAmt(totalInvoiceAmount);
                header.setFinancingDueDate(simulationResult.getMaxInvoiceDate().toInstant()); // not clear
                header.setProvisionFeeAmt(product.getProvisionRate()); // not clear
                header.setEffectiveRate(product.getEffectiveRate());
                header.setProvisionFeePercentage(product.getProvisionRate());
                header.setSurveyFeeAmt(product.getSurveyFee());
                header.setSurveyFeeAmtNett(product.getSurveyFee());
                header.setLegalFeeAmt(product.getLegalFee());
                header.setLegalFeeAmtNett(product.getLegalFee());
                header.setInsuranceFeePercentage(product.getInsuranceRate());
                header.setOthersFeeAmt(product.getOthersFee());
                header.setFinancingAmt(financingAmount);
                header.setDisburseAmt(disburseAmount);

                Long countCustomerTransaction = invoiceRepository.countByCustomer(customer);
                if (countCustomerTransaction == 0) {
                    // admin fee only for new customer
                    header.setAdminLimitAmt(product.getAdminLimitFee());
                    header.setAdminFeeAmt(product.getAdminLimitFee() * (product.getAdminRate() / 100));
                } else {
                    header.setAdminLimitAmt(0.0);
                    header.setAdminFeeAmt(0.0);
                }

                header.setDisburseDate(Instant.now());
                header.setFinancingStatus(FinancingStatus.NEW.name()); // fresh input will store as NEW
                header.setFinancingStep(FinancingStatus.NEW.name());
                header.setUsrCrt(customer.getCustName());
                header.setDtmCrt(Instant.now());

                header = financingHdrRepository.save(header);
            }

            return header;
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }

    public FinancingHdrDto getByCode(UUID code) throws Exception {
        try {
            FinancingHdr entity = financingHdrRepository.findByFinancingHdrCode(code).orElseThrow(
                    () -> CommonInvalidException.builder()
                            .title("Tidak ada data financing")
                            .message("Tidak ada data financing")
                            .build()
            );
            FinancingHdrDto dto = FinancingMapper.INSTANCE.hdrDtoFromEntity(entity);
            dto.setCustomer(entity.getCustomer());
            dto.setBouwheer(entity.getBouwheer());
            return dto;
        } catch (Exception e) {
            log.error("getByCode, error {}", e.getMessage());
            throw e;
        }
    }

    public void delete(FinancingHdr financingHdr) {
        try {
            financingHdrRepository.delete(financingHdr);
        } catch (Exception e) {
            log.error("delete, error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<PaidInvoiceDto> paidInvoice() {
        try {
            return PaginationResult.<PaidInvoiceDto>builder()
                    .currentPage(1)
                    .totalData(0L)
                    .totalPage(1)
                    .list(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            log.error("paidInvoice, error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<DisburseInvoiceDto> disburseInvoice() {
        try {
            return PaginationResult.<DisburseInvoiceDto>builder()
                    .currentPage(1)
                    .totalData(0L)
                    .totalPage(1)
                    .list(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            log.error("disburseInvoice, error {}", e.getMessage());
            throw e;
        }
    }

    public void paidFinancing(
            Authentication authentication,
            FinancingInvoicePaidRequest request,
            String apiKey
    ) throws SignatureException {
        try {
            final UUID financingHdrCode;
            try {
                financingHdrCode = UUID.fromString(request.getFinancingCode());
            } catch (IllegalArgumentException ignored) {
                throw new IllegalStateException("Invalid given financingCode");
            }

            final String user;
            if (authentication != null) {
                MstUser authenticateUser = UserInternalUtils.authenticateUser(authentication);
                user = authenticateUser.getUsername();
            } else if (!StringUtil.isNullOrEmpty(apiKey)) {
                user = "POST";
            } else {
                throw new IllegalStateException("can perform action, invalid credentials given");
            }

            FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(financingHdrCode)
                    .orElseThrow(() -> new IllegalStateException("Financing Not Found with given financingCode"));

            financingHdr.setFinancingStatus(FinancingStatus.PAID.name());
            financingHdr.setFinancingStep(FinancingStatus.PAID.name());
            financingHdr.setUsrUpd(user);
            financingHdr.setDtmUpd(Instant.now());
            financingHdrRepository.save(financingHdr);

            updateFinancing(
                    financingHdrCode,
                    user,
                    FinancingStatus.PAID,
                    FinancingStatus.PAID
            );
        } catch (Exception e) {
            log.error("paidFinancing, error {}", e.getMessage());
            throw e;
        }
    }

    public void updateFinancing(
            UUID financingHdrCode,
            String user,
            FinancingStatus financingStatus,
            FinancingStatus financingStep
    ) {
        FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(financingHdrCode)
                .orElseThrow(() -> new IllegalStateException("Financing Not Found with given financingCode"));

        financingHdr.setFinancingStatus(financingStatus.name());
        financingHdr.setFinancingStep(financingStep.name());
        financingHdr.setUsrUpd(user);
        financingHdr.setDtmUpd(Instant.now());
        financingHdrRepository.save(financingHdr);
    }

    public FinancingHdr findByCode(String financingHdrCode) {
        return financingHdrRepository
                .findByFinancingHdrCode(UUID.fromString(financingHdrCode))
                .orElseThrow(() -> new IllegalStateException("Invalid given financingHdrCode"));
    }
}
