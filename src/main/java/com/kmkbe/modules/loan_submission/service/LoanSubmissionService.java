package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.external.dto.PostedInvoiceDto;
import com.kmkbe.modules.external.service.MSTLoanService;
import com.kmkbe.modules.loan_submission.dto.DisbursePercentageDto;
import com.kmkbe.modules.loan_submission.dto.EstimatedDisburseDto;
import com.kmkbe.modules.loan_submission.entity.Bouwheer;
import com.kmkbe.modules.loan_submission.entity.Product;
import com.kmkbe.modules.loan_submission.repository.BouwheerRepository;
import com.kmkbe.modules.loan_submission.repository.ProductRepository;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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

    private final MSTLoanService mstLoanService;
    private final InvoiceService invoiceService;
    private final FinancingService financingService;
    private final AuthService authService;

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
            final BigDecimal serviceFee = BigDecimal.valueOf(product.getSurveyFee()
                    + product.getLegalFee()
                    + product.getAdminLimitFee()
                    + product.getOthersFee());
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
    public void createSimulation(
            Authentication authentication,
            CreateSimulationRequest request
    ) throws Exception {
        try {
            final String bouwheerCode = request.getInvoices().getFirst().bouwheerCode();
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final Bouwheer bouwheer = bouwheerRepository.findByBouwheerCode(UUID.fromString(bouwheerCode)).get();
            final Product product = productRepository.findById(request.getProductId()).get();

            final double totalInvoiceAmount = request.getInvoices()
                    .stream()
                    .mapToDouble(CreateSimulationRequest.CreatePostedInvoice::invoiceAmount)
                    .sum();

            final Date maxInvoiceDueDate = request.getInvoices()
                    .stream()
                    .map(CreateSimulationRequest.CreatePostedInvoice::invoiceDueDate)
                    .max(Date::compareTo)
                    .get();

            final CalculateSimulationRequest simulation = new CalculateSimulationRequest();
            {
                simulation.setDisbursePercentage(request.getDisbursePercentage());
                simulation.setTotalInvoiceAmount(BigDecimal.valueOf(totalInvoiceAmount).setScale(2, RoundingMode.CEILING));
            }

            final EstimatedDisburseDto calculateDisburse = calculateDisburse(simulation);

            request.setDisburse(
                    new CreateSimulationRequest.SimulationDisburse(
                            calculateDisburse.getFinancingAmount(),
                            calculateDisburse.getEstimatedDisburseAmount(),
                            maxInvoiceDueDate,
                            totalInvoiceAmount
                    )
            );

            financingService.create(customer, bouwheer, product, request);
            invoiceService.create(customer, bouwheer, request);
        } catch (Exception e) {
            log.error("createSimulation, error {}", e.getMessage());
            throw e;
        }
    }
}
