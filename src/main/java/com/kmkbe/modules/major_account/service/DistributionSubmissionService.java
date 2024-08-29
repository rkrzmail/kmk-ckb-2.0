package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.DistributionSubmissionDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.major_account.request.AssignInvoiceToBranchRequest;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
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
                        String city = "";

                        if (e.getCustomer() != null) {
                            if (e.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
                                city = e.getCustomer().getCompany().getCity();
                            } else {
                                if (e.getCustomer().getPersonal() != null) {
                                    city = e.getCustomer().getPersonal().getCity();
                                }
                            }
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
                            label = e.getFinancingStatus();
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

            financingHdr.setFinancingStatus("INPROCESS");
            financingHdr.setFinancingStep("ASSIGNMENT");
            financingHdr.setMstBranch(mstBranch);
            financingHdr.setDtmUpd(Instant.now());
            financingHdr.setUsrUpd(authenticateUser.getUsername());
            financingHdrRepository.save(financingHdr);
        } catch (Exception e) {
            log.error("assignSubmission: error {}", e.getMessage());
            throw e;
        }
    }
}
