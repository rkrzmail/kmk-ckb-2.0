package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.Visitor;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.VisitorRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private VisitorRepository visitorRepository;
    @Autowired
    private CustomerRepository customerRepository;


    public PaginationResult<VisitorDto>getVisitorReport(
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

            pageSize = 1000;
            Page<Visitor> pagination = visitorRepository.findAll(PageRequest.of(pageNo, pageSize));

            List<VisitorDto> result = pagination.stream()
                    .map((e) -> VisitorDto.builder()
                            .visitorId(e.getVisitorId())
                            .vendorCode(e.getVendorCode())
                            .debtorName(e.getDebtorName())
                            .debtorStatus(e.getDebtorStatus())
                            .bouwheerName(e.getBouwheerName())
                            .visitDate(e.getVisitDate())
                            .build())
                    .toList();

            return PaginationResult.<VisitorDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(pagination.getTotalElements())
                    .totalPage(pagination.getTotalPages())
                    .list(result)
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    public PaginationResult<ProyeksiReportDto> getProyeksiReport(PaginationRequest request) {
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

            // Use the Pageable object to manage pagination
            Page<ProyeksiReportDto> pagination = customerRepository.findActiveCustomersWithInvoiceDetails(PageRequest.of(pageNo, pageSize));

            // Collecting the results into a list
            List<ProyeksiReportDto> result = pagination.stream()
                    .map(e -> new ProyeksiReportDto(
                            e.getDebtorName(),
                            e.getDebtorStatus(),
                            e.getBouwheerName(),
                            e.getInvoiceNo(),
                            e.getAmountInvoice(),
                            e.getAmountFinancing(),
                            e.getInvoiceDueDate(),
                            e.getEffectiveDate()
                    ))
                    .collect(Collectors.toList());

            return PaginationResult.<ProyeksiReportDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(pagination.getTotalElements())
                    .totalPage(pagination.getTotalPages())
                    .list(result)
                    .build();

        } catch (Exception e) {
            throw e;
        }
    }

}
