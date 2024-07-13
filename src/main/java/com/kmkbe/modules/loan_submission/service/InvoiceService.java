package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.model.PaginationResult;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.mapper.CustomerMapper;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.external.service.MSTLoanService;
import com.kmkbe.modules.loan_submission.dto.EstimatedDisburseDto;
import com.kmkbe.modules.loan_submission.dto.InvoiceDto;
import com.kmkbe.modules.loan_submission.entity.Invoice;
import com.kmkbe.modules.loan_submission.entity.Product;
import com.kmkbe.modules.loan_submission.mapper.InvoiceMapper;
import com.kmkbe.modules.loan_submission.repository.InvoiceRepository;
import com.kmkbe.modules.loan_submission.repository.ProductRepository;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.InvoiceListRequest;
import com.kmkbe.modules.loan_submission.spec.InvoiceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final MSTLoanService mstLoanService;
    private final AuthService authService;

    public PaginationResult<InvoiceDto> fetchActiveInvoice(
            Authentication authentication,
            InvoiceListRequest request
    ) throws Exception {
        try {
            final Customer cust = CustomerMapper.INSTANCE.custEntityFromDto(authService.authenticatedCustomer(authentication));
            final Page<Invoice> invoicesPagination = invoiceRepository.findByCustCode(
                    cust,
                    InvoiceSpec.list(request),
                    PageRequest.of(request.getPageNo(), request.getPageSize())
            );

            List<InvoiceDto> invoices = invoicesPagination.getContent()
                    .stream()
                    .map(InvoiceMapper.INSTANCE::dtoFromEntity)
                    .toList();

            PaginationResult<InvoiceDto> dto = new PaginationResult<>();
            dto.setCurrentPage(invoicesPagination.getNumber());
            dto.setTotalData(invoicesPagination.getTotalElements());
            dto.setTotalPage(invoicesPagination.getTotalPages());
            dto.setList(invoices);

            return dto;
        } catch (Exception e) {
            log.error("fetchActiveInvoice, error {}", e.getMessage());
            throw e;
        }
    }

    public EstimatedDisburseDto calculateDisburse(CalculateSimulationRequest request) {
        try {
            final Double ntfResult = BigDecimal.valueOf((request.getDisbursePercentage() / 100) * request.getTotalInvoiceAmount())
                    .setScale(2, RoundingMode.DOWN)
                    .doubleValue();
            final Optional<Product> findProduct = productRepository.findNtfRange(ntfResult);

            if (findProduct.isEmpty()) {
                return null;
            }

            return getEstimatedDisburseDto(findProduct, ntfResult);
        } catch (Exception e) {
            log.error("calculateDisburse, error {}", e.getMessage());
            throw e;
        }
    }

    private static EstimatedDisburseDto getEstimatedDisburseDto(Optional<Product> findProduct, Double ntfResult) {
        final Product product = findProduct.get();
        final Double serviceFee = product.getSurveyFee() + product.getLegalFee() + product.getAdminLimitFee() + product.getOthersFee();
        final EstimatedDisburseDto dto = new EstimatedDisburseDto();

        final double estimateDisburse = BigDecimal.valueOf(ntfResult - serviceFee)
                .setScale(2, RoundingMode.CEILING)
                .doubleValue();

        dto.setProductId(product.getProductId());
        dto.setFinancingAmount(BigDecimal.valueOf(ntfResult));
        dto.setServiceFeeAmount(BigDecimal.valueOf(serviceFee));
        dto.setEstimatedDisburseAmount(BigDecimal.valueOf(estimateDisburse));

        return dto;
    }
}
