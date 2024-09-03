package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.model.LoanDisburseEmailPayload;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionSubmissionService {
    private final FinancingHdrRepository financingHdrRepository;
    private final InvoiceRepository invoiceRepository;
    private final FinancingDtlRepository financingDtlRepository;
    private final EmailService emailService;
    private final MstBranchRepository mstBranchRepository;
    private final FinancingHdrService financingHdrService;
    private final CustomerRepository customerRepository;

    public PaginationResult<DistributionSubmissionDto> submissionDistribution(
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

            final Page<FinancingHdr> paginationFinancing = financingHdrRepository.findAllByRawOrder(
                    PageRequest.of(pageNo, pageSize)
                    //FinancingHdrSpec.bySearchBy(request.getSearchBy(), request.getSearchValue())
            );

            final List<DistributionSubmissionDto> list = paginationFinancing.getContent()
                    .stream()
                    /*.filter((e) -> e.getFinancingStatus().equalsIgnoreCase("inprocess")
                            || e.getFinancingStatus().equalsIgnoreCase("signing")
                            || e.getFinancingStatus().equalsIgnoreCase("signed")
                            || e.getFinancingStatus().equalsIgnoreCase("live")
                            || e.getFinancingStatus().equalsIgnoreCase("golive")
                            || e.getFinancingStatus().equalsIgnoreCase("new")
                    )*/
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
                                e.getFinancingStatus().equalsIgnoreCase("inprocess")
                                        || e.getFinancingStatus().equalsIgnoreCase("signing")
                                        || e.getFinancingStatus().equalsIgnoreCase("signed")
                                        || e.getFinancingStatus().equalsIgnoreCase("live")
                                        || e.getFinancingStatus().equalsIgnoreCase("golive")

                        ) {
                            color = "#ccffcc";
                        } else {
                            color = "#FF5C5C";
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
                                MappedFinancingStatus.Type.MajorAccount
                        );

                        return DistributionSubmissionDto.builder()
                                .financingHdrCode(e.getFinancingHdrCode().toString())
                                .custName(e.getCustomer().getCustName())
                                .bouwheerName(e.getBouwheer().getBouwheerName())
                                .city(city)
                                .dueDate(Date.from(e.getFinancingDueDate()))
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

            return PaginationResult.<DistributionSubmissionDto>builder()
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

    public void assignSubmission(
            Authentication authentication,
            AssignInvoiceToBranchRequest request
    ) throws SignatureException {
        try {
            final UUID financingHdrCode;
            try {
                financingHdrCode = UUID.fromString(request.getFinancingHdrCode());
            } catch (IllegalArgumentException ignored) {
                throw new IllegalStateException("Invalid given financingHdrCode");
            }

            MstUser authenticateUser = UserInternalUtils.authenticateUser(authentication);
            MstBranch mstBranch = mstBranchRepository.findByBranchCode(request.getBranchCode())
                    .orElseThrow(() -> new IllegalStateException("Branch Not Found with given argument"));
            FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(financingHdrCode)
                    .orElseThrow(() -> new IllegalStateException("Financing Not Found with given argument"));

            // Major account melakukan assignment leads ke cabang
            financingHdr.setFinancingStatus("INPROCESS");
            financingHdr.setFinancingStep("ASSIGNMENT");
            financingHdr.setMstBranch(mstBranch);
            financingHdr.setDtmUpd(Instant.now());
            financingHdr.setUsrUpd(authenticateUser.getUsername());
            financingHdrRepository.save(financingHdr);

            if (mstBranch.getEmployees() != null && !mstBranch.getEmployees().isEmpty()) {
                final List<InvoiceEmailPayload> invoices = financingHdr.getFinancingDtls()
                        .stream()
                        .map((item) ->
                                InvoiceEmailPayload.builder()
                                        //.seq(item.getInvoiceSeqno())
                                        .invoiceAmt(CommonFormattingUtils.formatAmount(item.getInvoice().getInvoiceAmt().doubleValue()))
                                        .invoiceDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDate()))
                                        .invoiceDueDate(DateTimeUtils.formatToDate(item.getInvoice().getInvoiceDueDate()))
                                        .description("Invoice By Trakindo")
                                        .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
                                        .build()
                        ).toList();

                final double totalFeeAmt =
                        financingHdr.getAdminFeeAmt()
                                + financingHdr.getLegalFeeAmtNett()
                                + financingHdr.getInsuranceFeeAmt()
                                + financingHdr.getOthersFeeAmt()
                                + financingHdr.getProvisionFeeAmt()
                                + financingHdr.getSurveyFeeAmtNett();

                emailService.sendNotificationBranchAssign(
                        //mstBranch.getEmployees().stream().toList().getFirst().getEmail(),
                        "vandikalvandi@gmail.com",
                        financingHdr.getBouwheer().getBouwheerName(),
                        LoanDisburseEmailPayload.builder()
                                .financingCode(financingHdr.getFinancingHdrCode().toString())
                                .applicationDate(DateTimeUtils.formatToDate(financingHdr.getFinancingDate()))
                                .companyName(financingHdr.getBouwheer().getBouwheerName())
                                .phoneNumber(financingHdr.getCustomer().getCustMobilePhone())
                                .tenor(financingHdr.getTenor())
                                .financingCode(financingHdr.getFinancingHdrCode().toString())
                                .financingDueDate(DateTimeUtils.formatToDate(financingHdr.getFinancingDueDate()))
                                .retention(CommonFormattingUtils.formatAmount(financingHdr.getRetention()))
                                .financingAmt(CommonFormattingUtils.formatAmount(financingHdr.getFinancingAmt()))
                                .totalFeeAmt(CommonFormattingUtils.formatAmount(totalFeeAmt))
                                .invoiceAmt(CommonFormattingUtils.formatAmount(financingHdr.getTotalInvoiceAmt()))
                                .disburseAmt(CommonFormattingUtils.formatAmount(financingHdr.getDisburseAmt()))
                                .invoices(invoices)
                                .build()
                );
            }

        } catch (Exception e) {
            log.error("assignSubmission: error {}", e.getMessage());
            throw e;
        }
    }
}
