package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.DisburseInvoiceDto;
import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.dto.PaidInvoiceDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.FinancingMapper;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.model.SimulationDisburseResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.spec.FinancingHdrSpec;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingHdrService {
    private final FinancingHdrRepository financingHdrRepository;
    private final InvoiceRepository invoiceRepository;
    private final RedisRepository redisRepository;
    private final AgreementRepository agreementRepository;
    private final DisbursementLogRepository disbursementLogRepository;

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

                // ((tanggal due date invoice) 10 - hari berjalan) + 7 (bouwheer grace period)
                Long top = (long) DateTimeUtils.dateDiffInDay(new Date(), request.getInvoices().getFirst().getInvoiceDueDate());

                header.setFinancingHdrCode(UUID.randomUUID());
                header.setCustomer(customer);
                header.setBouwheer(bouwheer);
                header.setTenor(top + bouwheer.getGracePeriod());
                header.setFinancingDate(DateTimeUtils.now());
                header.setCurrencyCode(firstInvoice.getCurrencyCode());
                header.setInvoiceQty((long) request.getInvoices().size());
                header.setInterestType("COF"); // not clear
                header.setTermOfPayment(top); // not clear
                header.setGracePeriod(bouwheer.getGracePeriod()); // get from bouwheer grace_period
                header.setRetention(100 - request.getDisbursePercentage()); // not clear
                header.setTotalInvoiceAmt(totalInvoiceAmount);
                header.setFinancingAmt(financingAmount);
                header.setDisburseAmt(disburseAmount);
                header.setInterestAmt(simulationResult.getInterestFeeAmount().doubleValue()); // not clear
                header.setFinancingDueDate(simulationResult.getMaxInvoiceDate().toInstant()); // not clear
                header.setProvisionFeeAmt(simulationResult.getProvisionFeeAmount().doubleValue()); // not clear
                header.setProvisionFeePercentage(simulationResult.getProvisionRate());
                header.setAdminFeePercentage(simulationResult.getAdminRate());
                header.setEffectiveRate(simulationResult.getEffectiveRate());
                header.setSurveyFeeAmt(simulationResult.getSurveyFeeAmount().doubleValue());
                header.setLegalFeeAmt(simulationResult.getLegalFeeAmount().doubleValue());
                header.setOthersFeeAmt(simulationResult.getOthersFeeAmount().doubleValue());
                header.setAdminFeeAmt(simulationResult.getAdminFeeAmount().doubleValue());

                header.setSurveyFeeAmtNett(simulationResult.getSurveyFeeAmount().doubleValue());
                header.setLegalFeeAmtNett(simulationResult.getLegalFeeAmount().doubleValue());
                header.setInsuranceFeeAmt(0.0);
                header.setInsuranceFeePercentage(0.0);
                header.setAdminLimitAmt(0.0);

                header.setDisburseDate(DateTimeUtils.now());
                header.setFinancingStatus(""); // fresh input will store as NEW
                header.setFinancingStep("");
                header.setUsrCrt(customer.getCustName());
                header.setDtmCrt(DateTimeUtils.now());
 

                header = financingHdrRepository.save(header);


            }

            return header;
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }

    public FinancingHdr getByCode(UUID code) throws Exception {
        try {
            return financingHdrRepository.findByFinancingHdrCode(code).orElseThrow(
                    () -> CommonInvalidException.builder()
                            .title("Tidak ada data financing")
                            .message("Tidak ada data financing")
                            .build()
            );
        } catch (Exception e) {
            log.error("getByCode, error {}", e.getMessage());
            throw e;
        }
    }

    public FinancingHdrDto dtoFromEntity(FinancingHdr entity) throws Exception {
        try {
            FinancingHdrDto dto = FinancingMapper.INSTANCE.hdrDtoFromEntity(entity);
            dto.setCustomer(entity.getCustomer());
            dto.setBouwheer(entity.getBouwheer());
            return dto;
        } catch (Exception e) {
            log.error("dtoFromEntity, error {}", e.getMessage());
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

    public PaginationResult<PaidInvoiceDto> paidInvoice(
            PaginationRequest request
    ) {
        try {
            int pageNo = 0, pageSize = 10;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }
            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            final Page<FinancingHdr> financingHdrs = financingHdrRepository.findAll(
                    FinancingHdrSpec.byStepStatus(request.getSearchBy(),request.getSearchValue()),
                    PageRequest.of(pageNo, pageSize)
            );

            return paginatePaidInvoice (
                    financingHdrs,
                    pageNo
            );
        } catch (Exception e) {
            log.error("paidInvoice, error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<PaidInvoiceDto> paginatePaidInvoice(
            Page<FinancingHdr> financingHdrs,
            int pageNo
    ) {
        if (pageNo > 0) {
            pageNo = pageNo - 1;
        }

        final List<PaidInvoiceDto> paidInvoiceDto = financingHdrs
                .stream()
                .map((e) -> {
                    FinancingDtl financingDtl = e.getFinancingDtls()
                            .stream()
                            .findFirst()
                            .orElse(null);

                    String invoiceNo = (financingDtl != null && financingDtl.getInvoice() != null)
                            ? financingDtl.getInvoice().getCustInvNo()
                            : null;
                    Date paidDate = (financingDtl != null && financingDtl.getInvoice() != null)
                            ? Date.from(financingDtl.getInvoice().getInvoiceDate())
                            : null;
                    Date dueDate = (financingDtl != null && financingDtl.getInvoice() != null)
                            ? Date.from(financingDtl.getInvoice().getInvoiceDueDate())
                            : null;
                    BigDecimal paidAmount = (financingDtl != null && financingDtl.getInvoice() != null)
                            ? BigDecimal.valueOf(financingDtl.getInvoice().getInvoiceAmt())
                            : null;
                    MappedFinancingStatus mappedFinancingStatus = new MappedFinancingStatus(
                            e,
                            MappedFinancingStatus.Type.Repayment
                    );

                    String color = getColor(e);
                    Customer customer = e.getCustomer();
                    String customerName = customer.getCustName();

                    return PaidInvoiceDto.builder()
                            .invoiceNo(invoiceNo)
                            .custName(customerName)
                            .bouwheerName(e.getBouwheer().getBouwheerName())
                            .paidDate(paidDate)
                            .dueDate(dueDate)
                            .paidAmount(paidAmount)
                            .status(StatusLabelDto.builder()
                                    .status(mappedFinancingStatus.getStatus())
                                    .statusLabel(mappedFinancingStatus.getLabel())
                                    .color(color)
                                    .build())
                            .build();
                })
                .toList();
        return PaginationResult.<PaidInvoiceDto>builder()
                .currentPage(pageNo + 1)
                .totalData(financingHdrs.getTotalElements())
                .totalPage(financingHdrs.getTotalPages())
                .list(paidInvoiceDto)
                .build();
    }

    public PaginationResult<DisburseInvoiceDto> paginatePaidDisbursement(
            Page<FinancingHdr> financingHdrs,
            int pageNo
    ) {
        if (pageNo > 0) {
            pageNo = pageNo - 1;
        }

        final List<DisburseInvoiceDto> disburseInvoiceDto = financingHdrs
                .stream()
                .map((e) -> {
                    Agreement agreement = agreementRepository.findByFinancingHdr_FinancingHdrCode(e.getFinancingHdrCode());


                    if (agreement == null) {
                        return null;
                    }

                    DisbursementLog disbursementLog = disbursementLogRepository.findByAgreement_AgreementCode(agreement.getAgreementCode());

                    FinancingDtl financingDtl = e.getFinancingDtls()
                            .stream()
                            .findFirst()
                            .orElse(null);

                    Date paidDate = (financingDtl != null && financingDtl.getInvoice() != null)
                            ? Date.from(financingDtl.getInvoice().getInvoiceDate())
                            : null;
                    String color = getColor(e);
                    Customer customer = e.getCustomer();
                    String customerName = customer.getCustName();
                    MappedFinancingStatus mappedFinancingStatus = new MappedFinancingStatus(
                            e,
                            MappedFinancingStatus.Type.Disbursement
                    );

                    return DisburseInvoiceDto.builder()
                            .agreementNo(agreement.getAgreementCode())
                            .custName(customerName)
                            .bouwheerName(e.getBouwheer().getBouwheerName())
                            .disburseDate(Date.from(e.getDisburseDate()))
                            .paidDate(paidDate)
                            .retentionRefund(BigDecimal.valueOf(disbursementLog.getApAmt()))
                            .paidAmount(BigDecimal.valueOf(disbursementLog.getApPaidAmt()))
                            .retentionRefundDate(Date.from(disbursementLog.getApDueDate())).
                            status(StatusLabelDto.builder()
                                    .status(mappedFinancingStatus.getStatus())
                                    .statusLabel(mappedFinancingStatus.getLabel())
                                    .color(color)
                                    .build())
                            .build();
                })
                .toList();

        return PaginationResult.<DisburseInvoiceDto>builder()
                .currentPage(pageNo + 1)
                .totalData(financingHdrs.getTotalElements())
                .totalPage(financingHdrs.getTotalPages())
                .list(disburseInvoiceDto)
                .build();
    }

    private static String getColor(FinancingHdr e) {
        String color;
        if (e.getFinancingStatus().equalsIgnoreCase("new")) {
            color = "#808080";
        } else if (
                e.getFinancingStatus().equalsIgnoreCase("inprocess")
                        || e.getFinancingStatus().equalsIgnoreCase("signing")
                        || e.getFinancingStatus().equalsIgnoreCase("signed")
                        || e.getFinancingStatus().equalsIgnoreCase("live")
                        || e.getFinancingStatus().equalsIgnoreCase("golive")

        ) {
            color = "#ccffcc";
        } else {
            color = "#FF5C5C";
        }
        return color;
    }

    public PaginationResult<DisburseInvoiceDto> disburseInvoice(
            PaginationRequest request,
            FinancingHdr financingHdr
    ) {
        try {
            int pageNo = 0, pageSize = 10;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }
            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            final Page<FinancingHdr> financingHdrs = financingHdrRepository.findAll(
              FinancingHdrSpec.byDisbursement(request.getSearchBy(), request.getSearchValue()),
              PageRequest.of(pageNo, pageSize)
            );

            return paginatePaidDisbursement(
                    financingHdrs,
                    pageNo
            );

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
            financingHdr.setDtmUpd(DateTimeUtils.now());
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
