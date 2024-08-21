package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.model.PaginationResult;
import com.kmkbe.modules.customer.entity.Customer;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public Invoice byBouwheerInvoiceNo(String bouwheerInvoiceNo) throws Exception {
        try {
            Optional<Invoice> find = invoiceRepository.findByBouwheerInvNo(bouwheerInvoiceNo);
            return find.orElse(null);
        } catch (Exception e) {
            log.error("byBouwheerInvoiceNo, error {}", e.getMessage());
            throw e;
        }
    }

    public void create(Invoice invoice) throws Exception {
        try {
            invoiceRepository.save(invoice);
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }

    public List<InvoiceDto> createBulk(
            Customer customer,
            Bouwheer bouwheer,
            CreateSimulationRequest request
    ) throws Exception {
        try {
            List<Invoice> invoices = request.getInvoices()
                    .stream()
                    .map((posted) -> {
                        invoiceRepository.findByCustCodeAndBouwheerInvNoAndCustInvNo(
                                customer,
                                posted.getBouwheerInvoiceNo(),
                                posted.getCustomerInvoiceNo()
                        ).ifPresent(invoiceRepository::delete);

                        final Invoice invoice = new Invoice();
                        {
                            invoice.setInvoiceCode(UUID.randomUUID());
                            invoice.setCustCode(customer);
                            invoice.setBouwheerCode(bouwheer);
                            invoice.setBouwheerInvNo(posted.getBouwheerInvoiceNo());
                            invoice.setCustInvNo(posted.getCustomerInvoiceNo());
                            invoice.setInvoiceDescription(posted.getInvoiceDescription());
                            invoice.setInvoiceDate(posted.getInvoiceDate().toInstant());
                            invoice.setInvoiceDueDate(posted.getInvoiceDueDate().toInstant());
                            invoice.setInvoiceAmt(posted.getInvoiceAmount().doubleValue());
                            invoice.setUsrCrt(customer.getCustName());
                            invoice.setDtmCrt(Instant.now());
                            invoiceRepository.save(invoice);
                        }

                        return invoice;
                    }).collect(Collectors.toCollection(ArrayList::new));

            //invoices = invoiceRepository.saveAll(invoices);

            return invoices.stream().map(InvoiceMapper.INSTANCE::dtoFromEntity).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("createBulk, error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<InvoiceDto> getPaginate(
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
