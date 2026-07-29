package com.kmkbe.modules.loan_submission.service;


import com.kmkbe.core.domain.dto.CustomerCreditFacilityDto;
import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.InvoiceMapper;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.spec.FinancingDtlSpec;
import com.kmkbe.core.domain.spec.InvoiceSpec;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.nikita.utils.Utils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    private final FinancingDtlRepository financingDtlRepository;
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
                    .map((posted) -> Invoice.builder()
                            .customer(customer)
                            .bouwheer(bouwheer)
                            .bouwheerInvNo(posted.getBouwheerInvoiceNo())
                            .custInvNo(posted.getCustomerInvoiceNo())
                            .invoiceDescription(
                                    StringUtil.isNullOrEmpty(posted.getInvoiceDescription())
                                            ? "Invoice By CKB"
                                            : posted.getInvoiceDescription()
                            )
                            .invoiceDate(Utils.toInstant( posted.getInvoiceDate()))
                            .invoiceDueDate(Utils.toInstant( posted.getInvoiceDueDate()))
                            .invoiceAmt(posted.getInvoiceAmount().doubleValue())
                            .poNumber(posted.getPoNumber())
                            .postingDate(posted.getPostingDate())
                            .usrCrt(customer.getCustName())
                            .build())
                    .toList();

            return invoiceRepository.saveAll(invoices)
                    .stream()
                    .map(InvoiceMapper.INSTANCE::dtoFromEntity)
                    .toList();
        } catch (Exception e) {
            log.error("createBulk, error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<InvoiceDto> getPaginate(
            Authentication authentication,
            PaginationRequest request
    ) throws Exception {
        try {
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final Page<Invoice> invoicesPagination = invoiceRepository.findByCustomer(
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

    public void delete(Invoice invoice) {
        try {
            invoiceRepository.delete(invoice);
        } catch (Exception e) {
            log.error("delete, error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<PostedInvoiceDto> invoiceSubmissionByFinancingHdr(
            FinancingHdr financingHdr,
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

             Page<FinancingDtl> financingDtls = null;
            if (request.getSearchBy() != null) {
                 final String searchBy = request.getSearchBy();
                 final String searchValue = request.getSearchValue();

                    financingDtls = financingDtlRepository.findAll(
                        Specification.where(
                                FinancingDtlSpec.custInvoiceFilterBy(financingHdr.getFinancingHdrCode() , request.getSearchBy(), request.getSearchValue())
                        ),
                        PageRequest.of(pageNo, pageSize)
                );

                /*    Specification<FinancingDtl> spec = Specification.where((root, query, criteriaBuilder) -> {
                        return criteriaBuilder.equal(root.get("financingHdr"),   financingHdr  );
                     });


                spec = spec.and((root, query, criteriaBuilder) -> {
                        // Lakukan join antara Product dan Category

                        Join<FinancingHdr, FinancingDtl> joinDtl = root.join("financingDtls", JoinType.INNER);
                        Join<FinancingHdr, Customer> joinCust = root.join("customer", JoinType.INNER);
                        Join<FinancingHdr, Bouwheer> joinBouwheer = root.join("bouwheer", JoinType.INNER);
                        switch (searchBy){
                            case "customer":
                                return criteriaBuilder.like(joinCust.get(searchBy), "%" + searchValue + "%");
                            default:

                        }
                        return criteriaBuilder.like(root.get(searchBy), "%" + searchValue + "%");
                    });

                   financingDtls = financingDtlRepository.findAll(spec,    PageRequest.of(pageNo, pageSize)
                );*/

            }

            if (financingDtls == null){

                financingDtls = financingDtlRepository.findByFinancingHdr(
                        financingHdr,
                        PageRequest.of(pageNo, pageSize)
                );
            }


            return paginateInvoice(
                    financingDtls,
                    pageNo
            );
        } catch (Exception e) {
            log.error("invoiceSubmission: error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<CustomerCreditFacilityDto> customerCreditFacilities(
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        try {
            int pageNo = 0, pageSize = 10;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            Customer customer = CustomerUtils.authenticateCustomer(authentication);
           /* final Page<FinancingDtl> financingDtlPage = financingDtlRepository.findAll(
                    Specification.where(
                            FinancingDtlSpec.custDashboardFilterBy(customer, request.getSearchBy(), request.getSearchValue())
                    ),
                    PageRequest.of(pageNo, pageSize)
            );
*/
            final Page<FinancingDtl> financingDtlPage = financingDtlRepository.findAll(
                    Specification.where(
                            FinancingDtlSpec.custDashboardFilterBy(customer, null, null)
                    ),
                    PageRequest.of(pageNo, pageSize)
            );

            return paginateCustDashboard(financingDtlPage, pageNo, pageSize);
        } catch (Exception e) {
            log.error("customerCreditFacilities: error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<CustomerCreditFacilityDto> customerDueDateInvoices(
            Authentication authentication,
            PaginationRequest request
    )throws SignatureException {
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


            Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final Page<FinancingDtl> financingDtlPage = financingDtlRepository.findAll(
                    Specification.where(
                            FinancingDtlSpec.custDashboardFilterBy(customer, request.getSearchBy(), request.getSearchValue())
                    ),
                    PageRequest.of(pageNo, pageSize)
            );


            //Customer customer = CustomerUtils.authenticateCustomer(authentication);
            //List<Invoice> invoices  = invoiceRepository.findAllByCustomer(customer);
            /*return PaginationResult.<PostedInvoiceDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(0L)
                    .totalPage(1)
                    .list(invoices)
                    .build();*/
            return paginateCustDashboard(financingDtlPage, pageNo, pageSize);
        } catch (Exception e) {
            log.error("customerDueDateInvoice: error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<PostedInvoiceDto> customerActiveInvoices(
            Authentication authentication,
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

            return PaginationResult.<PostedInvoiceDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(0L)
                    .totalPage(1)
                    .list(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            log.error("customerActiveInvoices: error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<PostedInvoiceDto> paginateInvoice(
            Page<FinancingDtl> financingDtls,
            int pageNo
    ) {
        if (pageNo > 0) {
            pageNo = pageNo - 1;
        }

        final List<PostedInvoiceDto> postedInvoice = financingDtls
                .stream()
                .filter(
                        (e) ->
                                e.getFinancingHdr() != null
                                        && e.getInvoice() != null
                )
                .map((e) ->
                        PostedInvoiceDto.builder()
                                .bouwheerCode(e.getFinancingHdr().getBouwheer().getBouwheerCode().toString())
                                .bouwheerName(e.getFinancingHdr().getBouwheer().getBouwheerName())
                                .customerInvoiceNo(e.getInvoice().getCustInvNo())
                                .bouwheerInvoiceNo(e.getInvoice().getBouwheerInvNo())
                                .poNumber(e.getInvoice().getPoNumber())
                                .invoiceDate(Utils.fromInstant(e.getInvoice().getInvoiceDate()))
                                .invoiceDueDate(Utils.fromInstant(e.getInvoice().getInvoiceDueDate()))
                                .invoiceAmount(BigDecimal.valueOf(e.getInvoice().getInvoiceAmt()))
                                .invoiceDescription(
                                        StringUtil.isNullOrEmpty(e.getInvoice().getInvoiceDescription())
                                                ? "Invoice By Trakindo"
                                                : e.getInvoice().getInvoiceDescription()
                                )
                                .currencyCode("IDR")
                                .build()
                )
                .toList();

        return PaginationResult.<PostedInvoiceDto>builder()
                .currentPage(pageNo + 1)
                .totalData(financingDtls.getTotalElements())
                .totalPage(financingDtls.getTotalPages())
                .list(postedInvoice)
                .build();
    }

    public PaginationResult<CustomerCreditFacilityDto> paginateCustDashboard(
            Page<FinancingDtl> financingDtlPage,
            int pageNo,
            int pageSize
    ) {
        if (pageNo > 0) {
            pageNo = pageNo - 1;
        }

        final int startRow = pageNo * pageSize + 1;
        final List<CustomerCreditFacilityDto> results = IntStream.range(0, financingDtlPage.getNumberOfElements())
                .mapToObj((i) -> {
                    String agreementCode = null;
                    boolean hasAction = false;

                    if (
                            financingDtlPage.getContent().get(i).getFinancingHdr().getAgreement() != null
                                    && !financingDtlPage.getContent().get(i).getFinancingHdr().getAgreement().isEmpty()
                    ) {
                        Agreement agreement = financingDtlPage.getContent().get(i).getFinancingHdr().getAgreement().stream().toList().getFirst();
                        agreementCode = agreement.getAgreementCode();
                        hasAction = agreement.getAgreementFile() != null;
                    }

                    MappedFinancingStatus status = new MappedFinancingStatus(
                            financingDtlPage.getContent().get(i).getFinancingHdr(),
                            MappedFinancingStatus.Type.Customer
                    );

                    return CustomerCreditFacilityDto.builder()
                            //.no(startRow + i)
                            .agreementCode(agreementCode)
                            .bouwheerCode(financingDtlPage.getContent().get(i).getFinancingHdr().getBouwheer().getBouwheerCode().toString())
                            .bouwheerName(financingDtlPage.getContent().get(i).getFinancingHdr().getBouwheer().getBouwheerName())
                            .invoiceAmount(new BigDecimal(financingDtlPage.getContent().get(i).getInvoice().getInvoiceAmt(), MathContext.DECIMAL64))
                            .invoiceDescription(StringUtil.isNullOrEmpty(financingDtlPage.getContent().get(i).getInvoice().getInvoiceDescription())
                                    ? "Invoice By Trakindo"
                                    : financingDtlPage.getContent().get(i).getInvoice().getInvoiceDescription())
                            .status(status.getStatus())
                            .statusLabel(status.getLabel())
                            .invoiceDate(Utils.fromInstant(financingDtlPage.getContent().get(i).getInvoice().getInvoiceDate()))
                            .facilityDueDate(null)
                            .verifDate(null)
                            .disburseDate(Utils.fromInstant(financingDtlPage.getContent().get(i).getBouwheerPaidDate()))
                            .hasAction(hasAction)
                            .build();
                })
                .toList();

        return PaginationResult.<CustomerCreditFacilityDto>builder()
                .currentPage(pageNo + 1)
                .totalData(financingDtlPage.getTotalElements())
                .totalPage(financingDtlPage.getTotalPages())
                .list(results)
                .build();
    }


    public PaginationResult<CustomerCreditFacilityDto> pageNikitaDebitur(
            Page<FinancingDtl> financingDtlPage,
            int pageNo,
            int pageSize
    ) {
        if (pageNo > 0) {
            pageNo = pageNo - 1;
        }

        final int startRow = pageNo * pageSize + 1;


        final List<CustomerCreditFacilityDto> results = IntStream.range(0, financingDtlPage.getNumberOfElements())
                .mapToObj((i) -> {
                    String agreementCode = null;
                    boolean hasAction = false;

                    if (
                            financingDtlPage.getContent().get(i).getFinancingHdr().getAgreement() != null
                                    && !financingDtlPage.getContent().get(i).getFinancingHdr().getAgreement().isEmpty()
                    ) {
                        Agreement agreement = financingDtlPage.getContent().get(i).getFinancingHdr().getAgreement().stream().toList().getFirst();
                        agreementCode = agreement.getAgreementCode();
                        hasAction = agreement.getAgreementFile() != null;
                    }

                    MappedFinancingStatus status = new MappedFinancingStatus(
                            financingDtlPage.getContent().get(i).getFinancingHdr(),
                            MappedFinancingStatus.Type.Customer
                    );

                    return CustomerCreditFacilityDto.builder()
                            .no(startRow + i)
                            .agreementCode(agreementCode)
                            .bouwheerCode(financingDtlPage.getContent().get(i).getFinancingHdr().getBouwheer().getBouwheerCode().toString())
                            .bouwheerName(financingDtlPage.getContent().get(i).getFinancingHdr().getBouwheer().getBouwheerName())
                            .invoiceAmount(new BigDecimal(financingDtlPage.getContent().get(i).getInvoice().getInvoiceAmt(), MathContext.DECIMAL64))
                            .invoiceDescription(StringUtil.isNullOrEmpty(financingDtlPage.getContent().get(i).getInvoice().getInvoiceDescription())
                                    ? "Invoice By Trakindo"
                                    : financingDtlPage.getContent().get(i).getInvoice().getInvoiceDescription())
                            .status(status.getStatus())
                            .statusLabel(status.getLabel())
                            .invoiceDate(Utils.fromInstant(financingDtlPage.getContent().get(i).getInvoice().getInvoiceDate()))
                            .facilityDueDate(null)
                            .verifDate(null)
                            .disburseDate(Utils.fromInstant(financingDtlPage.getContent().get(i).getBouwheerPaidDate()))
                            .hasAction(hasAction)
                            .build();
                })
                .toList();

        return PaginationResult.<CustomerCreditFacilityDto>builder()
                .currentPage(pageNo + 1)
                .totalData(financingDtlPage.getTotalElements())
                .totalPage(financingDtlPage.getTotalPages())
                .list(results)
                .build();
    }

}
