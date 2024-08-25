package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.DetailDistributionSubmissionDto;
import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.InvoiceListRequest;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.major_account.request.DistributionListRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionSubmissionService {
    private final FinancingHdrRepository financingHdrRepository;
    private final InvoiceRepository invoiceRepository;
    private final FinancingDtlRepository financingDtlRepository;
    private final EmailService emailService;

    public PaginationResult<DistributionSubmissionDto> submissionDistribution(
            DistributionListRequest request
    ) {
        try {
            final Page<FinancingHdr> paginationFinancing = financingHdrRepository.findAll(
                    PageRequest.of(request.getPageNo(), request.getPageSize())
            );

            final List<DistributionSubmissionDto> list = paginationFinancing.getContent()
                    .stream()
                    .map((e) -> {
                        String city;
                        if (e.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
                            city = e.getCustomer().getCompany().getCity();
                        } else {
                            city = e.getCustomer().getPersonal().getCity();
                        }

                        boolean isNewCust = financingHdrRepository.countByCustomerAndFinancingStatus(e.getCustomer(), "PAID") == 0;

                        String color, label;
                        if (e.getFinancingStatus().equalsIgnoreCase("new")) {
                            color = "#808080";
                            label = "Baru";
                        } else if (
                                e.getFinancingStatus().equalsIgnoreCase("inprocess")
                                        || e.getFinancingStatus().equalsIgnoreCase("signing")
                                        || e.getFinancingStatus().equalsIgnoreCase("signed")
                                        || e.getFinancingStatus().equalsIgnoreCase("live")
                                        || e.getFinancingStatus().equalsIgnoreCase("golive")

                        ) {
                            color = "#ccffcc";
                            label = "Aktif";
                        } else {
                            color = "#FF5C5C";
                            label = "Selesai";
                        }

                        return DistributionSubmissionDto.builder()
                                .financingHdrCode(e.getFinancingHdrCode().toString())
                                .custName(e.getCustomer().getCustName())
                                .bouwheerName(e.getBouwheer().getBouwheerName())
                                .city(city)
                                .dueDate(Date.from(e.getFinancingDueDate()))
                                .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
                                .branchRecommended("")
                                .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                                .status(StatusLabelDto.builder()
                                        .status(e.getFinancingStatus())
                                        .statusLabel(label)
                                        .color(color)
                                        .build())
                                .build();
                    })
                    .toList();

            return PaginationResult.<DistributionSubmissionDto>builder()
                    .currentPage(request.getPageNo())
                    .totalData(paginationFinancing.getTotalElements())
                    .totalPage(paginationFinancing.getTotalPages())
                    .list(list)
                    .build();
        } catch (Exception e) {
            log.error("submissionDistribution: error {}", e.getMessage());
            throw e;
        }
    }

    public DetailDistributionSubmissionDto detailSubmissionDistribution(String financingHdrCode) {
        try {
            FinancingHdr financingHdr = financingHdrByCode(financingHdrCode);

            final String address;
            if (financingHdr.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
                address = financingHdr.getCustomer().getCompany().getCompanyAddress();
            } else {
                address = financingHdr.getCustomer().getPersonal().getLegalAddress();
            }

            return DetailDistributionSubmissionDto.builder()
                    .financingHdrCode(financingHdr.getFinancingHdrCode())
                    .custName(financingHdr.getCustomer().getCustName())
                    .custIdTypeCode(financingHdr.getCustomer().getCustIdTypeCode())
                    .custIdNo(financingHdr.getCustomer().getCustIdNo())
                    .email(financingHdr.getCustomer().getCustEmail())
                    .custTypeCode(financingHdr.getCustomer().getCustTypeCode())
                    .address(address)
                    .plafond(DetailDistributionSubmissionDto.PlafondDto.builder()
                            .plafond(BigDecimal.valueOf(0))
                            .totalPlafond(BigDecimal.valueOf(0))
                            .availablePlafond(BigDecimal.valueOf(0))
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("detailSubmissionDistribution: error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<PostedInvoiceDto> invoiceSubmission(
            String financingHdrCode,
            InvoiceListRequest request
    ) {
        try {
            final FinancingHdr financingHdr = financingHdrByCode(financingHdrCode);
            final Page<FinancingDtl> financingDtls = financingDtlRepository.findByFinancingHdr(
                    financingHdr,
                    PageRequest.of(request.getPageNo(), request.getPageSize())
            );

            final List<PostedInvoiceDto> postedInvoice = financingDtls
                    .stream()
                    .map((e) ->
                            PostedInvoiceDto.builder()
                                    .bouwheerCode(e.getFinancingHdr().getBouwheer().getBouwheerCode().toString())
                                    .bouwheerName(e.getFinancingHdr().getBouwheer().getBouwheerName())
                                    .customerInvoiceNo(e.getInvoice().getCustInvNo())
                                    .bouwheerInvoiceNo(e.getInvoice().getBouwheerInvNo())
                                    .invoiceDate(Date.from(e.getInvoice().getInvoiceDate()))
                                    .invoiceDueDate(Date.from(e.getInvoice().getInvoiceDueDate()))
                                    .invoiceAmount(BigDecimal.valueOf(e.getInvoice().getInvoiceAmt()))
                                    .invoiceDescription(e.getInvoice().getInvoiceDescription())
                                    .currencyCode("IDR").build()
                    )
                    .toList();

            return PaginationResult.<PostedInvoiceDto>builder()
                    .currentPage(request.getPageNo())
                    .totalData(financingDtls.getTotalElements())
                    .totalPage(financingDtls.getTotalPages())
                    .list(postedInvoice)
                    .build();
        } catch (Exception e) {
            log.error("invoiceSubmission: error {}", e.getMessage());
            throw e;
        }
    }

    public void assignSubmission() {
        try {

        } catch (Exception e) {
            log.error("assignSubmission: error {}", e.getMessage());
            throw e;
        }
    }

    private FinancingHdr financingHdrByCode(String financingHdrCode) {
        return financingHdrRepository
                .findByFinancingHdrCode(UUID.fromString(financingHdrCode))
                .orElseThrow(() -> new IllegalStateException("Invalid given financingHdrCode"));
    }
}
