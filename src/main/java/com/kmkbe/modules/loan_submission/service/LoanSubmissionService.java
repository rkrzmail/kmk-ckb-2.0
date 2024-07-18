package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.exception.LoanDocMandatoryException;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.model.LoanDisburseEmailPayload;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.external.dto.PostedInvoiceDto;
import com.kmkbe.modules.external.service.MSTLoanService;
import com.kmkbe.modules.loan_submission.dto.*;
import com.kmkbe.modules.loan_submission.entity.Bouwheer;
import com.kmkbe.modules.loan_submission.entity.Product;
import com.kmkbe.modules.loan_submission.model.PostedInvoicePayload;
import com.kmkbe.modules.loan_submission.model.SimulationDisburseResult;
import com.kmkbe.modules.loan_submission.repository.BouwheerRepository;
import com.kmkbe.modules.loan_submission.repository.ProductRepository;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateLoanApplicationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanSubmissionService {
    private final ProductRepository productRepository;
    private final BouwheerRepository bouwheerRepository;
    private final BCryptPasswordEncoder bcryptEncoder;

    private final MSTLoanService mstLoanService;
    private final InvoiceService invoiceService;
    private final FinancingService financingService;
    private final MstFileTypeService mstFileTypeService;
    private final EmailService emailService;

    public List<PostedInvoiceDto> fetchActiveInvoice(Authentication authentication) {
        try {

            return mstLoanService.fetchListOfPostedInvoice(authentication);
        } catch (Exception e) {
            log.error("fetchActiveInvoice, error {}", e.getMessage());
            throw e;
        }
    }

    public List<DisbursePercentageDto> fetchDisbursePercentage() {
        try {
            return Arrays.asList(
                    DisbursePercentageDto.builder()
                            .disbursePercentage(50.0)
                            .build(),
                    DisbursePercentageDto.builder()
                            .disbursePercentage(55.0)
                            .build(),
                    DisbursePercentageDto.builder()
                            .disbursePercentage(60.0)
                            .build(),
                    DisbursePercentageDto.builder()
                            .disbursePercentage(65.0)
                            .build(),
                    DisbursePercentageDto.builder()
                            .disbursePercentage(70.0)
                            .build(),
                    DisbursePercentageDto.builder()
                            .disbursePercentage(75.0)
                            .build(),
                    DisbursePercentageDto.builder()
                            .disbursePercentage(80.0)
                            .build(),
                    DisbursePercentageDto.builder()
                            .disbursePercentage(85.0)
                            .build(),
                    DisbursePercentageDto.builder()
                            .disbursePercentage(90.0)
                            .build()
            );
        } catch (Exception e) {
            log.error("fetchDisbursePercentage, error {}", e.getMessage());
            throw e;
        }
    }

    public EstimatedDisburseDto calculateDisburse(CalculateSimulationRequest request) {
        try {
            final BigDecimal ntfResult = request.getTotalInvoiceAmount()
                    .multiply(BigDecimal.valueOf(request.getDisbursePercentage() / 100.0));

            final Optional<Product> findProduct = productRepository.findNtfRange(ntfResult.doubleValue());

            if (findProduct.isEmpty()) {
                return null;
            }

            final Product product = findProduct.get();
            final BigDecimal serviceFee = BigDecimal.valueOf(
                    product.getSurveyFee()
                            + product.getLegalFee()
                            + product.getAdminLimitFee()
                            + product.getOthersFee()
            );
            final BigDecimal estimateDisburse = ntfResult.subtract(serviceFee);

            return EstimatedDisburseDto.builder()
                    .productId(product.getProductId())
                    .financingAmount(ntfResult)
                    .serviceFeeAmount(serviceFee)
                    .estimatedDisburseAmount(estimateDisburse)
                    .build();
        } catch (Exception e) {
            log.error("calculateDisburse, error {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public CreatedSimulationDto createSimulation(
            Authentication authentication,
            CreateSimulationRequest request
    ) throws Exception {
        try {
            final String bouwheerCode = request.getInvoices().getFirst().getBouwheerCode();
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final Bouwheer bouwheer = bouwheerRepository.findByBouwheerCode(UUID.fromString(bouwheerCode)).get();
            final Product product = productRepository.findById(request.getProductId()).orElseThrow();

            final double totalInvoiceAmount = request.getInvoices()
                    .stream()
                    .mapToDouble((item) -> item.getInvoiceAmount().doubleValue())
                    .sum();

            final Date maxInvoiceDueDate = request.getInvoices()
                    .stream()
                    .map(PostedInvoicePayload::getInvoiceDueDate)
                    .max(Date::compareTo)
                    .get();

            final CalculateSimulationRequest simulation = new CalculateSimulationRequest();
            {
                simulation.setDisbursePercentage(request.getDisbursePercentage());
                simulation.setTotalInvoiceAmount(BigDecimal.valueOf(totalInvoiceAmount).setScale(2, RoundingMode.CEILING));
            }

            final EstimatedDisburseDto calculateDisburse = calculateDisburse(simulation);
            final List<InvoiceDto> invoices = invoiceService.createBulk(customer, bouwheer, request);

            final SimulationDisburseResult simulationDisburseResult = SimulationDisburseResult.builder()
                    .financingAmount(calculateDisburse.getFinancingAmount())
                    .estimatedDisburseAmount(calculateDisburse.getEstimatedDisburseAmount())
                    .maxInvoiceDate(maxInvoiceDueDate)
                    .totalInvoiceAmount(totalInvoiceAmount)
                    .createdInvoices(invoices)
                    .build();

            final UUID financingHdrCode = financingService.create(
                    customer,
                    bouwheer,
                    product,
                    request,
                    simulationDisburseResult
            );

            return CreatedSimulationDto.builder()
                    .productId(request.getProductId())
                    .financingHdrCode(financingHdrCode)
                    .invoices(invoices)
                    .build();
        } catch (Exception e) {
            log.error("createSimulation, error {}", e.getMessage());
            throw e;
        }
    }

    public void createLoanSubmission(
            Authentication authentication,
            CreateLoanApplicationRequest request
    ) throws Exception {
        try {
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            if (!bcryptEncoder.matches(request.getPin(), customer.getCustPin())) {
                throw new BadCredentialsException("Pin is invalid, try to entry right pin");
            }

            mstFileTypeService.getAllMandatory()
                    .forEach(mstFileType -> {
                        if (
                                request.getDocuments()
                                        .stream()
                                        .noneMatch(document -> document.getFileTypeCode().equals(mstFileType.getFileTypeCode()))
                        ) {
                            throw new LoanDocMandatoryException("Mandatory file: " + mstFileType.getFileTypeDesc() + " is not present, try to attach the file");
                        }
                    });

            final FinancingHdrDto createdFinancing = financingService.getByCode(request.getFinancingHdrCode());
            final List<LoanDisburseEmailPayload.InvoicePayload> invoices = createdFinancing.getDetails()
                    .stream()
                    .map((item) ->
                            LoanDisburseEmailPayload.InvoicePayload.builder()
                                    .seq(item.getInvoiceSeqno())
                                    .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt().doubleValue()))
                                    .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
                                    .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
                                    .description(item.getInvoice().getInvoiceDescription())
                                    .bouwheerName(createdFinancing.getBouwheer().getBouwheerName())
                                    .build()
                    ).toList();

            final double totalFeeAmt =
                    createdFinancing.getAdminFeeAmt()
                            + createdFinancing.getLegalFeeAmtNett()
                            + createdFinancing.getInsuranceFeeAmt()
                            + createdFinancing.getOthersFeeAmt()
                            + createdFinancing.getProvisionFeeAmt()
                            + createdFinancing.getSurveyFeeAmtNett();

            emailService.sendNotificationLoanDisbursement(
                    customer,
                    LoanDisburseEmailPayload.builder()
                            .financingCode(createdFinancing.getFinancingHdrCode().toString())
                            .applicationDate(DateTimeUtils.formatToDate(createdFinancing.getDisburseDate()))
                            .companyName(createdFinancing.getBouwheer().getBouwheerName())
                            .phoneNumber(createdFinancing.getCustomer().getCustMobilePhone())
                            .tenor(createdFinancing.getTenor())
                            .financingCode(createdFinancing.getFinancingHdrCode().toString())
                            .financingDueDate(DateTimeUtils.formatToDate(createdFinancing.getFinancingDueDate()))
                            .retention(CommonFormattingUtils.formatAmount(createdFinancing.getRetention()))
                            .financingAmt(CommonFormattingUtils.formatAmount(createdFinancing.getFinancingAmt()))
                            .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
                            .invoiceAmt(CommonFormattingUtils.formatAmount(createdFinancing.getTotalInvoiceAmt()))
                            .disburseAmt(CommonFormattingUtils.formatAmount(createdFinancing.getDisburseAmt()))
                            .invoices(invoices)
                            .build()
            );

        } catch (Exception e) {
            log.error("createLoanSubmission, error {}", e.getMessage());
            throw e;
        }
    }
}
