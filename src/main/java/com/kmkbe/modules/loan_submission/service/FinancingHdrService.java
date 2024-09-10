package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.constant.FinancingStatus;
import com.kmkbe.core.domain.dto.DisburseInvoiceDto;
import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.dto.PaidInvoiceDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.FinancingMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.model.SimulationDisburseResult;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
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
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingHdrService {
    private final FinancingHdrRepository financingHdrRepository;
    private final InvoiceRepository invoiceRepository;
    private final RedisRepository redisRepository;
    public FinancingHdr findLastBy(Customer customer) {
        try {
            return financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer).orElse(null);
        } catch (Exception e) {
            log.error("findBy, error {}", e.getMessage());
            throw e;
        }
    }

    public FinancingHdr create(
            Authentication authentication,
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
                double interestAmount = disburseAmount * bankLoanInterest;;

                // ((tanggal due date invoice) 10 - hari berjalan) + 7 (bouwheer grace period)
                Long top = (long) DateTimeUtils.dateDiffInDay(request.getInvoices().getFirst().getInvoiceDueDate(), new Date());

                header.setFinancingHdrCode(UUID.randomUUID());
                header.setCustomer(customer);
                header.setBouwheer(bouwheer);
                header.setTenor(top + bouwheer.getGracePeriod());
                header.setFinancingDate(Instant.now());
                header.setCurrencyCode(firstInvoice.getCurrencyCode());
                header.setInvoiceQty((long) request.getInvoices().size());
                header.setInterestType("COF"); // not clear
                header.setInterestAmt(interestAmount); // not clear
                header.setTermOfPayment(top); // not clear
                header.setGracePeriod(bouwheer.getGracePeriod()); // get from bouwheer grace_period
                header.setRetention(100 - request.getDisbursePercentage()); // not clear
                header.setTotalInvoiceAmt(totalInvoiceAmount);
                header.setFinancingDueDate(simulationResult.getMaxInvoiceDate().toInstant()); // not clear
                header.setProvisionFeeAmt(simulationResult.getProvisionFeeAmount().doubleValue()); // not clear
                header.setProvisionFeePercentage(simulationResult.getProvisionRate());
                header.setEffectiveRate(simulationResult.getEffectiveRate());
                header.setSurveyFeeAmt(simulationResult.getSurveyFeeAmount().doubleValue());
                header.setSurveyFeeAmtNett(simulationResult.getSurveyFeeAmount().doubleValue());
                header.setLegalFeeAmt(simulationResult.getLegalFeeAmount().doubleValue());
                header.setLegalFeeAmtNett(simulationResult.getLegalFeeAmount().doubleValue());
                header.setInsuranceFeePercentage(product.getInsuranceRate());
                header.setOthersFeeAmt(simulationResult.getOthersFeeAmount().doubleValue());
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

                //save _redis by customer customer.getCustCode() Authentication authentication,
                Redis  redis =   Redis.builder()
                                 .redis(String.valueOf(authentication.getCredentials()))
                                 .session("")
                                 .json(  Map.of("FinancingHdr", header) ).build();

                redisRepository.delete(redis);
                redisRepository.save(redis);
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

    public FinancingHdr paidFinancing(
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

            financingHdr.setFinancingStatus("LIVE");
            financingHdr.setFinancingStep("PAID");
            financingHdr.setUsrUpd(user);
            financingHdr.setDtmUpd(Instant.now());
            return financingHdrRepository.save(financingHdr);
        } catch (Exception e) {
            log.error("paidFinancing, error {}", e.getMessage());
            throw e;
        }
    }

    public FinancingHdr findByCode(String financingHdrCode) {
        return financingHdrRepository
                .findByFinancingHdrCode(UUID.fromString(financingHdrCode))
                .orElseThrow(() -> new IllegalStateException("Invalid given financingHdrCode"));
    }
}
