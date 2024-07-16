package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.loan_submission.constant.FinancingStatus;
import com.kmkbe.modules.loan_submission.entity.Bouwheer;
import com.kmkbe.modules.loan_submission.entity.FinancingDtl;
import com.kmkbe.modules.loan_submission.entity.FinancingHdr;
import com.kmkbe.modules.loan_submission.entity.Product;
import com.kmkbe.modules.loan_submission.repository.FinancingDtlRepository;
import com.kmkbe.modules.loan_submission.repository.FinancingHdrRepository;
import com.kmkbe.modules.loan_submission.repository.InvoiceRepository;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingService {
    private final FinancingHdrRepository financingHdrRepository;
    private final FinancingDtlRepository financingDtlRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuthService authService;

    public String create(
            Customer customer,
            Bouwheer bouwheer,
            Product product,
            CreateSimulationRequest request
    ) {
        try {
            final CreateSimulationRequest.CreatePostedInvoice fistInvoice = request.getInvoices().getFirst();
            final FinancingHdr header = new FinancingHdr();
            {
                final double disburseAmount = request.getDisburse().estimatedDisburseAmount().doubleValue();
                final double totalInvoiceAmount = request.getDisburse().totalInvoiceAmount();
                final double financingAmount = request.getDisburse().financingAmount().doubleValue();

                double retentionRate = 0.0 / 100;
                double retentionAmount = disburseAmount * retentionRate;

                double bankLoanInterest = 0.11;
                double interestAmount = disburseAmount * bankLoanInterest;

                header.setFinancingHdrCode(UUID.randomUUID());
                header.setCustCode(customer);
                header.setBouwheerCode(bouwheer);
                header.setTenor(90L); // 90 as default
                header.setFinancingDate(Instant.now());
                header.setCurrencyCode(fistInvoice.currencyCode());
                header.setInvoiceQty((long) request.getInvoices().size());
                header.setInterestType("COF"); // not clear
                header.setInterestAmt(interestAmount); // not clear
                header.setTermOfPayment(0L); // not clear
                header.setGracePeriod(0L); // not clear
                header.setRetention(retentionAmount); // not clear
                header.setTotalInvoiceAmt(totalInvoiceAmount);
                header.setProvisionFeeAmt(product.getProvisionRate()); // not clear
                header.setFinancingDueDate(request.getDisburse().maxInvoiceDate().toInstant()); // not clear

                {
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
                }

                Long countCustomerTransaction = invoiceRepository.countByCustCode(customer);
                if (countCustomerTransaction == 0) {
                    // admin fee only for new customer
                    header.setAdminLimitAmt(product.getAdminLimitFee());
                    header.setAdminFeeAmt(product.getAdminLimitFee() * (product.getAdminRate() / 100));
                }

                header.setDisburseDate(Instant.now());
                header.setFinancingStatus(FinancingStatus.DRAFT.name()); // fresh input will store as DRAFT
                header.setUsrCrt(customer.getCustName());
                header.setDtmCrt(Instant.now());

                financingHdrRepository.save(header);
            }

            final List<FinancingDtl> details = IntStream.range(0, request.getInvoices().size())
                    .mapToObj((index) -> {
                        final FinancingDtl detail = new FinancingDtl();
                        {
                            detail.setFinancingDtlCode(UUID.randomUUID());
                            detail.setBouwheerInvNo(request.getInvoices().get(index).bouwheerInvoiceNo());
                            detail.setInvoiceSeqno((long) index);
                            detail.setFinancingHdrCode(header);
                            detail.setUsrCrt(customer.getCustName());
                            detail.setDtmCrt(Instant.now());
                        }

                        financingDtlRepository.save(detail);
                        return detail;
                    })
                    .collect(Collectors.toList());


            financingDtlRepository.saveAll(details);
            return "Financing Successfully Create";
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }
}
