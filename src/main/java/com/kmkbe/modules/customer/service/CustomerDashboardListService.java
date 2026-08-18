package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.CustomerCreditFacilityDueDateDto;
import com.kmkbe.core.domain.dto.CustomerCreditFacilityNewDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.helpers.utils.Utils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerDashboardListService {
    private final FinancingHdrRepository financingHdrRepository;
    private final InvoiceRepository invoiceRepository;
    private final FinancingDtlRepository financingDtlRepository;
    private final MstBranchRepository mstBranchRepository;

    private final AgreementRepository agreementRepository;

    public PaginationResult<CustomerCreditFacilityNewDto> listcreditfacilities(
            Customer customer,
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

            final Page<FinancingHdr> paginationFinancing = financingHdrRepository.findAllByRawOrder(
                    customer.getCustCode().toString(), PageRequest.of(pageNo, pageSize)

            );

            //DateTimeUtils.now()

            final List<CustomerCreditFacilityNewDto> list = paginationFinancing.getContent()
                    .stream()

                    .map((e) -> {
                        String city = "", kelurahan = "", kecamatan = "";
                        if (e.getCustomer() != null) {
                            if (e.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
                                if (e.getCustomer().getCompany() != null) {
                                    city = e.getCustomer().getCompany().getCity();
                                    kelurahan = e.getCustomer().getCompany().getKelurahan();
                                    kecamatan = e.getCustomer().getCompany().getKecamatan();
                                }
                            } else {
                                if (e.getCustomer().getPersonal() != null) {
                                    city = e.getCustomer().getPersonal().getCity();
                                    kelurahan = e.getCustomer().getPersonal().getKelurahan();
                                    kecamatan = e.getCustomer().getPersonal().getKecamatan();
                                }
                            }
                        }

                        boolean isNewCust = financingHdrRepository.countByCustomerAndFinancingStatus(e.getCustomer(), "PAID") == 0;

                        String color,
                                currentBranch = null,
                                currentBranchCode = null,
                                branchRecommended = null,
                                branchRecommendedCode = null;
                        if (e.getFinancingStatus().equalsIgnoreCase("new")) {
                            color = "#808080";
                        } else if (
                              e.getFinancingStatus().equalsIgnoreCase("signing")
                                        || e.getFinancingStatus().equalsIgnoreCase("signed")

                                        || e.getFinancingStatus().equalsIgnoreCase("golive")

                        ) {
                            color = "#ccffcc";
                        } else if (
                                e.getFinancingStatus().equalsIgnoreCase("inprocess")


                        ) {
                            color = "#FF5C5C";
                        } else if (
                                e.getFinancingStatus().equalsIgnoreCase("live")

                        ) {
                            color = "#808080";
                        } else {
                            color = "#ccffcc";
                        }

                        if (e.getMstBranch() != null) {
                            branchRecommendedCode = e.getMstBranch().getBranchCode();
                            branchRecommended = e.getMstBranch().getBranchName();
                            currentBranchCode = e.getMstBranch().getBranchCode();
                            currentBranch = e.getMstBranch().getBranchName();
                        } else {
                            if (
                                    !StringUtil.isNullOrEmpty(city)
                                            && !StringUtil.isNullOrEmpty(kelurahan)
                                            && !StringUtil.isNullOrEmpty(kecamatan)
                            ) {
                                Optional<MstBranch> findBranch = mstBranchRepository.findTopLikeBranchNameRawQuery(
                                        city,
                                        kelurahan,
                                        kecamatan
                                );

                                if (findBranch.isPresent()) {
                                    branchRecommendedCode = findBranch.get().getBranchCode();
                                    branchRecommended = findBranch.get().getBranchName();
                                }
                            }
                        }

                        MappedFinancingStatus mappedFinancingStatus = new MappedFinancingStatus(
                                e,
                                MappedFinancingStatus.Type.Customer
                        );
                        //Optional<Agreement> agreement = agreementRepository.findTopByFinancingHdr(e.getAgreement().stream().toList()) ;


                        return CustomerCreditFacilityNewDto.builder()
                                //.agreementCode(agreement.isPresent()?agreement.get().getAgreementCode():"")
                                .agreementCode(e.getAgreement().isEmpty()?"":e.getAgreement().stream().toList().getLast().getAgreementCode())
                                .financingHdrCode(e.getFinancingHdrCode().toString())
                                .custName(e.getCustomer().getCustName())
                                .bouwheerName(e.getBouwheer().getBouwheerName())
                                .city(city)
                                .dueDate(Utils.fromInstant(e.getFinancingDueDate()))
                                .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
                                .branchRecommendedCode(branchRecommendedCode)
                                .branchRecommended(branchRecommended)
                                .currentBranchCode(currentBranchCode)
                                .currentBranch(currentBranch)
                                .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                                .status(StatusLabelDto.builder()
                                        .status(mappedFinancingStatus.getStatus())
                                        .statusLabel(mappedFinancingStatus.getLabel())
                                        .color(color)
                                        .build())
                                .dtmCrt(e.getDtmCrt())
                                .build();
                    })
                    .toList();

            return PaginationResult.<CustomerCreditFacilityNewDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(paginationFinancing.getTotalElements())
                    .totalPage(paginationFinancing.getTotalPages())
                    .list(list)
                    .build();
        } catch (Exception e) {
            log.error("submissionDistribution: error {}", e.getMessage());
            throw e;
        }
    }



    public PaginationResult<CustomerCreditFacilityDueDateDto> listinvoicesduedate(
            Customer customer,
            PaginationRequest request
    ) throws SignatureException {
        /*
        status samapi paid
        nama debitur diganti PO
        tgl ver digato postdate
         */

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

            final Page<FinancingDtl> financingDtls =   financingDtlRepository.findByCustomer(customer.getCustCode().toString(),PageRequest.of(pageNo, pageSize) );



            final List<CustomerCreditFacilityDueDateDto> list = financingDtls.getContent()
                    .stream()

                    .map((e) -> {
                        String city = "", kelurahan = "", kecamatan = "";
                        final FinancingHdr hdr = e.getFinancingHdr();
                        if (hdr.getCustomer() != null) {
                            if (hdr.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
                                if (hdr.getCustomer().getCompany() != null) {
                                    city = hdr.getCustomer().getCompany().getCity();
                                    kelurahan = hdr.getCustomer().getCompany().getKelurahan();
                                    kecamatan = hdr.getCustomer().getCompany().getKecamatan();
                                }
                            } else {
                                if (hdr.getCustomer().getPersonal() != null) {
                                    city = hdr.getCustomer().getPersonal().getCity();
                                    kelurahan = hdr.getCustomer().getPersonal().getKelurahan();
                                    kecamatan = hdr.getCustomer().getPersonal().getKecamatan();
                                }
                            }
                        }


                        String color,
                                currentBranch = null,
                                currentBranchCode = null,
                                branchRecommended = null,
                                branchRecommendedCode = null;
                        if (hdr.getFinancingStatus().equalsIgnoreCase("new")) {
                            color = "#808080";
                        } else if (
                                hdr.getFinancingStatus().equalsIgnoreCase("inprocess")
                                        || hdr.getFinancingStatus().equalsIgnoreCase("signing")
                                        || hdr.getFinancingStatus().equalsIgnoreCase("signed")
                                        || hdr.getFinancingStatus().equalsIgnoreCase("live")
                                        || hdr.getFinancingStatus().equalsIgnoreCase("golive")

                        ) {
                            color = "#ccffcc";
                        } else {
                            color = "#FF5C5C";
                        }

                        if (hdr.getMstBranch() != null) {
                            branchRecommendedCode = hdr.getMstBranch().getBranchCode();
                            branchRecommended = hdr.getMstBranch().getBranchName();
                            currentBranchCode = hdr.getMstBranch().getBranchCode();
                            currentBranch = hdr.getMstBranch().getBranchName();
                        } else {
                            if (
                                    !StringUtil.isNullOrEmpty(city)
                                            && !StringUtil.isNullOrEmpty(kelurahan)
                                            && !StringUtil.isNullOrEmpty(kecamatan)
                            ) {
                                Optional<MstBranch> findBranch = mstBranchRepository.findTopLikeBranchNameRawQuery(
                                        city,
                                        kelurahan,
                                        kecamatan
                                );

                                if (findBranch.isPresent()) {
                                    branchRecommendedCode = findBranch.get().getBranchCode();
                                    branchRecommended = findBranch.get().getBranchName();
                                }
                            }
                        }
                        boolean isNewCust = financingHdrRepository.countByCustomerAndFinancingStatus(hdr.getCustomer(), "PAID") == 0;


                        MappedFinancingStatus mappedFinancingStatus = new MappedFinancingStatus(
                                hdr,
                                MappedFinancingStatus.Type.MajorAccount
                        );

                        return CustomerCreditFacilityDueDateDto.builder()
                                .financingHdrCode(hdr.getFinancingHdrCode().toString())

                                .agreementCode(hdr.getAgreement().isEmpty()?"":hdr.getAgreement().stream().toList().getLast().getAgreementCode())
                                .poNumber(e.getInvoice().getPoNumber())
                                .postingDate(e.getInvoice().getPostingDate())

                                .custName(hdr.getCustomer().getCustName())
                                .bouwheerName(hdr.getBouwheer().getBouwheerName())
                                .city(city)
                                .dueDate(Utils.fromInstant(hdr.getFinancingDueDate()))
                                .financingAmount(BigDecimal.valueOf(hdr.getFinancingAmt()))
                                .branchRecommendedCode(branchRecommendedCode)
                                .branchRecommended(branchRecommended)
                                .currentBranchCode(currentBranchCode)
                                .currentBranch(currentBranch)
                                .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                                .status(StatusLabelDto.builder()
                                        .status(mappedFinancingStatus.getStatus())
                                        .statusLabel(mappedFinancingStatus.getLabel())
                                        .color(color)
                                        .build())
                                .dtmCrt(e.getDtmCrt())
                                .build();
                    })
                    .toList();

            return PaginationResult.<CustomerCreditFacilityDueDateDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(financingDtls.getTotalElements())
                    .totalPage(financingDtls.getTotalPages())
                    .list(list)
                    .build();
        } catch (Exception e) {
            log.error("submissionDistribution: error {}", e.getMessage());
            throw e;
        }
    }

}
