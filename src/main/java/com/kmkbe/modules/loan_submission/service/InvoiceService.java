package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.model.PaginationResult;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.dto.InvoiceDto;
import com.kmkbe.modules.loan_submission.entity.Bouwheer;
import com.kmkbe.modules.loan_submission.entity.Invoice;
import com.kmkbe.modules.loan_submission.mapper.InvoiceMapper;
import com.kmkbe.modules.loan_submission.repository.InvoiceRepository;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.InvoiceListRequest;
import com.kmkbe.modules.loan_submission.spec.InvoiceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final AuthService authService;

    public void create(
            Customer customer,
            Bouwheer bouwheer,
            CreateSimulationRequest request
    ) throws Exception {
        try {
            final List<Invoice> invoices = request.getInvoices()
                    .stream()
                    .map((posted) -> {
                        invoiceRepository.findByCustCodeAndBouwheerInvNoAndCustInvNo(
                                customer,
                                posted.bouwheerInvoiceNo(),
                                posted.customerInvoiceNo()
                        ).ifPresent(invoiceRepository::delete);

                        final Invoice invoice = new Invoice();
                        {
                            invoice.setInvoiceCode(UUID.randomUUID());
                            invoice.setCustCode(customer);
                            invoice.setBouwheerCode(bouwheer);
                            invoice.setBouwheerInvNo(posted.bouwheerInvoiceNo());
                            invoice.setCustInvNo(posted.customerInvoiceNo());
                            invoice.setInvoiceDescription(posted.invoiceDescription());
                            invoice.setInvoiceDate(posted.invoiceDate().toInstant());
                            invoice.setInvoiceDueDate(posted.invoiceDueDate().toInstant());
                            invoice.setInvoiceAmt(posted.invoiceAmount());
                            invoice.setUsrCrt(customer.getCustName());
                            invoice.setDtmCrt(Instant.now());
                            invoiceRepository.save(invoice);
                        }

                        return invoice;
                    }).collect(Collectors.toCollection(ArrayList::new));

            invoiceRepository.saveAll(invoices);
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<InvoiceDto> fetchInvoice(
            Authentication authentication,
            InvoiceListRequest request
    ) throws Exception {
        try {
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final Page<Invoice> invoicesPagination = invoiceRepository.findByCustCode(
                    customer,
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
            log.error("fetchInvoice, error {}", e.getMessage());
            throw e;
        }
    }
}
