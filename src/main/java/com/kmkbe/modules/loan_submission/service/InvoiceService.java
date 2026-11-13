package com.kmkbe.modules.loan_submission.service;


import com.kmkbe.core.domain.dto.CustomerCreditFacilityDto;
import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.InvoiceMapper;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.spec.FinancingDtlSpec;
import com.kmkbe.core.domain.spec.InvoiceSpec;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.helpers.utils.Utils;
import com.kmkbe.helpers.utils.PaginationSort;
import com.kmkbe.helpers.utils.PaginationRequests;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSubmissionRequest;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
  private final FinancingDtlRepository financingDtlRepository;
  private final InvoiceRepository invoiceRepository;

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
    CreateSubmissionRequest request
  ) {
    if (request.getInvoices() == null || request.getInvoices().isEmpty()) {
      return new ArrayList<>();
    }

    String defaultDescription = "Invoice By " + bouwheer.getBouwheerName();
    Map<String, PostedInvoicePayload> uniqueInvoices = new LinkedHashMap<>();
    for (PostedInvoicePayload posted : request.getInvoices()) {
      uniqueInvoices.putIfAbsent(invoiceKey(posted), posted);
    }

    List<Invoice> invoices = uniqueInvoices.values().stream().map(posted -> {
      // 1. Check if the invoice already exists in the database.
      //    Kunci unik: customer + bouwheer + bouwheerInvNo + custInvNo
      return invoiceRepository.findFirstByCustomerAndBouwheerAndBouwheerInvNoAndCustInvNo(
          customer,
          bouwheer,
          posted.getBouwheerInvoiceNo(),
          posted.getCustomerInvoiceNo()
        )
        .map(existingInvoice -> {
          // 2. If it EXISTS -> Update seluruh field yang dikirim
          applyPostedFields(existingInvoice, posted, bouwheer, defaultDescription);
          // Set update audit trails instead of create audit trails
          existingInvoice.setUsrUpd(customer.getCustName());
          existingInvoice.setDtmUpd(DateTimeUtils.now());
          return existingInvoice;
        })
        .orElseGet(() ->
          // 3. If it DOES NOT exist -> Create a new one using the Builder
          Invoice.builder()
            .customer(customer)
            .bouwheer(bouwheer)
            .bouwheerInvNo(posted.getBouwheerInvoiceNo())
            .custInvNo(posted.getCustomerInvoiceNo())
            .invoiceDescription(StringUtil.isNullOrEmpty(posted.getInvoiceDescription()) ? defaultDescription : posted.getInvoiceDescription())
            .invoiceDate(Utils.toInstant(posted.getInvoiceDate()))
            .invoiceDueDate(Utils.toInstant(posted.getInvoiceDueDate()))
            .invoiceAmt(posted.getInvoiceAmount() == null ? null : posted.getInvoiceAmount().doubleValue())
            .poNumber(posted.getPoNumber())
            .postingDate(posted.getPostingDate())
            .usrCrt(bouwheer.getBouwheerName())
            .build()
        );
    }).toList();

    return invoiceRepository.saveAll(invoices)
      .stream()
      .map(InvoiceMapper.INSTANCE::dtoFromEntity)
      .toList();
  }

  /**
   * Key unik invoice: customer + bouwheerInvNo + custInvNo.
   * Null-safe agar invoice tanpa nomor tetap dapat diproses.
   */
  private String invoiceKey(PostedInvoicePayload posted) {
    return String.valueOf(posted.getBouwheerInvoiceNo())
      + "|" + String.valueOf(posted.getCustomerInvoiceNo());
  }

  private void applyPostedFields(
    Invoice invoice,
    PostedInvoicePayload posted,
    Bouwheer bouwheer,
    String defaultDescription
  ) {
    invoice.setInvoiceDescription(
      StringUtil.isNullOrEmpty(posted.getInvoiceDescription()) ? defaultDescription : posted.getInvoiceDescription()
    );
    invoice.setInvoiceDate(Utils.toInstant(posted.getInvoiceDate()));
    invoice.setInvoiceDueDate(Utils.toInstant(posted.getInvoiceDueDate()));
    if (posted.getInvoiceAmount() != null) {
      invoice.setInvoiceAmt(posted.getInvoiceAmount().doubleValue());
    }
    invoice.setPoNumber(posted.getPoNumber());
    invoice.setPostingDate(posted.getPostingDate());
    invoice.setBouwheer(bouwheer);
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
    FinancingHdr financingHdr, BasePaginationRequest request) {
    return invoiceSubmissionByFinancingHdr(financingHdr, PaginationRequests.from(request));
  }

  public PaginationResult<PostedInvoiceDto> invoiceSubmissionByFinancingHdr(
    FinancingHdr financingHdr,
    PaginationRequest request
  ) {
    try {
      int pageNo = (request.getPageNo() != null && request.getPageNo() > 0) ? request.getPageNo() - 1 : 0;
      int pageSize = (request.getPageSize() != null) ? request.getPageSize() : 10;

      Pageable pageable = PageRequest.of(pageNo, pageSize, PaginationSort.invoices(request));
      Page<FinancingDtl> financingDtls;

      if (request.getSearchBy() != null && request.getSearchValue() != null && !request.getSearchValue().trim().isEmpty()) {
        financingDtls = financingDtlRepository.findAll(
          Specification.where(
            FinancingDtlSpec.custInvoiceFilterBy(
              financingHdr.getFinancingHdrCode(),
              request.getSearchBy(),
              request.getSearchValue()
            )
          ),
          pageable
        );
      } else {
        financingDtls = financingDtlRepository.findByFinancingHdr(
          financingHdr,
          pageable
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
  ) throws SignatureException {
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
              ? "Invoice By "+e.getFinancingHdr().getBouwheer().getBouwheerName()
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
            ? "Invoice By "+financingDtlPage.getContent().get(i).getFinancingHdr().getBouwheer().getBouwheerName()
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
            ? "Invoice By "+financingDtlPage.getContent().get(i).getFinancingHdr().getBouwheer().getBouwheerName()
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
