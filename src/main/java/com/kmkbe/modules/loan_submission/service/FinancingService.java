package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.loan_submission.constant.FinancingStatus;
import com.kmkbe.modules.loan_submission.dto.FinancingHdrDto;
import com.kmkbe.modules.loan_submission.entity.Bouwheer;
import com.kmkbe.modules.loan_submission.entity.FinancingHdr;
import com.kmkbe.modules.loan_submission.entity.Product;
import com.kmkbe.modules.loan_submission.mapper.FinancingMapper;
import com.kmkbe.modules.loan_submission.model.PostedInvoicePayload;
import com.kmkbe.modules.loan_submission.model.SimulationDisburseResult;
import com.kmkbe.modules.loan_submission.repository.FinancingDtlRepository;
import com.kmkbe.modules.loan_submission.repository.FinancingHdrRepository;
import com.kmkbe.modules.loan_submission.repository.InvoiceRepository;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingService {
    private final FinancingHdrRepository financingHdrRepository;
    private final FinancingDtlRepository financingDtlRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuthService authService;

    public UUID create(
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
                header.setCustCode(customer);
                header.setBouwheerCode(bouwheer);
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

                Long countCustomerTransaction = invoiceRepository.countByCustCode(customer);
                if (countCustomerTransaction == 0) {
                    // admin fee only for new customer
                    header.setAdminLimitAmt(product.getAdminLimitFee());
                    header.setAdminFeeAmt(product.getAdminLimitFee() * (product.getAdminRate() / 100));
                } else {
                    header.setAdminLimitAmt(0.0);
                    header.setAdminFeeAmt(0.0);
                }

                header.setDisburseDate(Instant.now());
                header.setFinancingStatus(FinancingStatus.DRAFT.name()); // fresh input will store as DRAFT
                header.setUsrCrt(customer.getCustName());
                header.setDtmCrt(Instant.now());

                header = financingHdrRepository.save(header);
            }

          /*  final FinancingHdr copyHeader = header;
            final Set<FinancingDtl> details = IntStream.range(0, request.getInvoices().size())
                    .mapToObj((index) -> {
                        final FinancingDtl detail = new FinancingDtl();
                        {
                            Invoice invoice = InvoiceMapper.INSTANCE.entityFromDto(simulationResult.getCreatedInvoices()
                                    .stream()
                                    .filter(item -> item.getBouwheerInvNo().equals(request.getInvoices().get(index).getBouwheerInvoiceNo()))
                                    .findFirst()
                                    .orElseThrow());
                            // remap
                            invoice.setUsrCrt(customer.getCustName());
                            invoice.setDtmCrt(Instant.now());
                            invoice.setCustCode(customer);
                            invoice.setBouwheerCode(bouwheer);

                            detail.setInvoiceCode(invoice);
                            detail.setFinancingDtlCode(UUID.randomUUID());
                            detail.setBouwheerInvNo(request.getInvoices().get(index).getBouwheerInvoiceNo());
                            detail.setInvoiceSeqno((long) index + 1);
                            detail.setFinancingHdrCode(copyHeader);
                            detail.setUsrCrt(customer.getCustName());
                            detail.setDtmCrt(Instant.now());
                        }

                        financingDtlRepository.save(detail);
                        return detail;
                    })
                    .collect(Collectors.toSet());

            header.setFinancingDtls(details);*/
            return header.getFinancingHdrCode();
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }

    public FinancingHdrDto getByCode(UUID code) throws Exception {
        try {
            FinancingHdr entity = financingHdrRepository.findByFinancingHdrCode(code).orElseThrow();
            FinancingHdrDto dto = FinancingMapper.INSTANCE.hdrDtoFromEntity(entity);
            dto.setCustomer(entity.getCustCode());
            dto.setBouwheer(entity.getBouwheerCode());
            return dto;
        } catch (Exception e) {
            log.error("getByCode, error {}", e.getMessage());
            throw e;
        }
    }
}
