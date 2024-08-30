package com.kmkbe.modules.branch_admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.CwrDto;
import com.kmkbe.core.domain.dto.InquiryAgreementCwrDto;
import com.kmkbe.core.domain.dto.InquiryCwrRemoteDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.CwrMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.branch_admin.request.CreateInquiryAgreementRequest;
import com.kmkbe.modules.branch_admin.request.CreateInquiryCwrRequest;
import com.kmkbe.modules.remote.request.FinancingSubmissionRequest;
import com.kmkbe.modules.remote.request.InquiryAgreementRemoteRequest;
import com.kmkbe.modules.remote.request.InquiryCwrRemoteRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CwrService {
    private final CustomerRepository customerRepository;
    private final BouwheerRepository bouwheerRepository;
    private final CwrRemoteService cwrRemoteService;
    private final CwrRepository cwrRepository;
    private final AgreementRepository agreementRepository;
    private final FinancingHdrRepository financingHdrRepository;
    private final FinancingRemoteService financingRemoteService;
    private final FinancingDtlRepository financingDtlRepository;

    public void create() {
        try {

        } catch (Exception e) {
            log.error("create: error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<CwrDto> list(
            String custCode,
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

            Customer customer = customerRepository.findByCustCode(UUID.fromString(custCode))
                    .orElseThrow(() -> new IllegalStateException("Customer not found or not valid"));

            Page<Cwr> page = cwrRepository.findAllByCustomer(
                    customer,
                    PageRequest.of(pageNo, pageSize)
            );

            List<CwrDto> result = page.stream()
                    .map(CwrMapper.INSTANCE::toDto)
                    .toList();

            return PaginationResult.<CwrDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(page.getTotalElements())
                    .totalPage(page.getTotalPages())
                    .list(result)
                    .build();
        } catch (Exception e) {
            log.error("list: error {}", e.getMessage());
            throw e;
        }
    }

    public void createInquiryCwr(
            Authentication authentication,
            CreateInquiryCwrRequest request
    ) throws JsonProcessingException, SignatureException, ParseException {
        try {
            final List<InquiryCwrRemoteDto> data;
            try {
                BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> response = cwrRemoteService.inquiryCwr(
                        InquiryCwrRemoteRequest.builder()
                                .cwrNo(request.getCwrNo())
                                .build()
                );

                data = response.getData();
            } catch (Exception e) {
                throw CommonInvalidException.builder()
                        .title("Peringatan")
                        .message("Harap input CWR aktif di Confins terlebih dahulu")
                        .build();
            }


            final MstUser user = UserInternalUtils.authenticateUser(authentication);
            final FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(request.getFinancingHdrCode()))
                    .orElseThrow(() -> new IllegalStateException("Financing not found or not valid"));

            final Bouwheer bouwheer = financingHdr.getBouwheer();
            if (bouwheer == null) {
                throw new IllegalStateException("Bouwheer not found or not valid");
            }

            final Customer customer = financingHdr.getCustomer();
            if (customer == null) {
                throw new IllegalStateException("Customer not found or not valid");
            }


            List<Cwr> cwrList = new ArrayList<>();
            if (!data.isEmpty()) {
                for (InquiryCwrRemoteDto inquiryCwr : data) {
                    cwrList.add(Cwr.builder()
                            .bouwheer(bouwheer)
                            .customer(customer)
                            .branchCode(inquiryCwr.getOfficeCode())
                            .cwrType(inquiryCwr.getCwrType())
                            .cwrTypeDesc(inquiryCwr.getCwrTypeDesc())
                            .facility(inquiryCwr.getFacility())
                            .isRevolving(inquiryCwr.getIsRevolving())
                            .currency(inquiryCwr.getCurrency())
                            .cwrStartDate(DateTimeUtils.SDF_STANDARD_DATE_TIME.parse(inquiryCwr.getStartDt()).toInstant())
                            .cwrEndDate(DateTimeUtils.SDF_STANDARD_DATE_TIME.parse(inquiryCwr.getEndDt()).toInstant())
                            .plafondAmt(inquiryCwr.getPlafondAmt())
                            .realisationAmt(inquiryCwr.getRealisationAmt())
                            .status(inquiryCwr.getCwrStatDescr())
                            .usrCrt(user.getUsername())
                            .dtmCrt(Instant.now())
                            .build());
                }
            }

            cwrRepository.saveAll(cwrList);
        } catch (Exception e) {
            log.error("agreementCredit: error {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void createInquiryAgreement(
            Authentication authentication,
            String financingHdrCode,
            String cwrCode,
            CreateInquiryAgreementRequest request
    ) throws SignatureException, JsonProcessingException {
        try {
            final List<InquiryAgreementCwrDto> data;
            try {
                BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = cwrRemoteService.inquiryAgreement(
                        InquiryAgreementRemoteRequest.builder()
                                .agreementNo(request.getAgreementNo())
                                .build()
                );

                data = response.getData();
            } catch (Exception e) {
                throw CommonInvalidException.builder()
                        .title("Peringatan")
                        .message("Tidak bisa mencari Agreement, harap input CWR aktif di Confins terlebih dahulu")
                        .build();
            }

            final MstUser user = UserInternalUtils.authenticateUser(authentication);
            final FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode))
                    .orElseThrow(() -> new IllegalStateException("Financing not found or not valid"));
            final Customer customer = financingHdr.getCustomer();
            if (customer == null) {
                throw new IllegalStateException("Customer not found or not valid");
            }

            List<Agreement> agreements = new ArrayList<>();
            if (!data.isEmpty()) {
                for (InquiryAgreementCwrDto inquiryCwr : data) {
                    Cwr cwr = cwrRepository.findTopByCwrCode(inquiryCwr.getCWRNo())
                            .orElseThrow(() -> new IllegalStateException("CWR not found or not valid"));


                    agreements.add(
                            Agreement.builder()
                                    .agreementCode(inquiryCwr.getAgrmntNo())
                                    .cwr(cwr)
                                    .applicationCode(inquiryCwr.getAppNo())
                                    .financingHdr(financingHdr)
                                    .facility(inquiryCwr.getFacility())
                                    .currency(inquiryCwr.getCurrency())
                                    .financingAmt(inquiryCwr.getNtfAmt())
                                    .status(inquiryCwr.getStatus())
                                    .productOffering(inquiryCwr.getProductOffering())
                                    .usrCrt(user.getUsrCrt())
                                    .dtmCrt(Instant.now())
                                    .build()
                    );
                }
            }

            if (!agreements.isEmpty()) {
                agreementRepository.saveAll(agreements);
            }

           List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr)
                   .orElseThrow(() -> new IllegalStateException("Financing Invoice not found or not valid"));

            List<FinancingSubmissionRequest.FinancingInvoice> financingInvoices = financingDtls.stream()
                    .filter((e) -> e.getInvoice() != null)
                    .map((e) -> FinancingSubmissionRequest.FinancingInvoice.builder()
                            .invoiceAmount(e.getInvoice().getInvoiceAmt())
                            .poNumber(e.getInvoice().getPoNumber())
                            .reference(e.getInvoice().getCustInvNo())
                            .accountingDocument(e.getInvoice().getBouwheerInvNo())
                            .build())
                    .toList();


            financingRemoteService.postedSubmission(
                    FinancingSubmissionRequest.builder()
                            .vendorCode(customer.getCustExternalCode())
                            .accountNo("")
                            .accountName("")
                            .bankName("")
                            .bankKey("")
                            .financingCode(financingHdrCode)
                            .financingAmount(0.0)
                            .financingInvoices(financingInvoices)
                            .build()
            );
        } catch (Exception e) {
            log.error("createAgreement: error {}", e.getMessage());
            throw e;
        }
    }
}
