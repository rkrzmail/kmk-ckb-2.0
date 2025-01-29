package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.dto.DisburseInvoiceDto;
import com.kmkbe.core.domain.dto.StatusLabelDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.spec.FinancingHdrSpec;
import com.kmkbe.core.utils.HttpUtils;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import com.kmkbe.nikita.utils.Utils;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentSubmissionService {
    private final FinancingHdrRepository financingHdrRepository;
    private final MstUserRepository mstUserRepository;
    private final AgreementRepository agreementRepository;
    private final AgreementFileRepository agreementFileRepository;

    public PaginationResult<AssignmentDto> assignmentList(
            HttpServletRequest httpServletRequest,
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

            MstUser authenticateUser = UserInternalUtils.authenticateUser(authentication);
            MstUser user = mstUserRepository.findById(authenticateUser.getUserCode()).orElseThrow();
            /*Page<FinancingHdr> financingHdrPage = financingHdrRepository.findByMstBranchOrderByFinancingHdrIdDesc(
                    user.getEmployee().getBranch(),
                    PageRequest.of(pageNo, pageSize)
            );*/

            String financingStatusFilter = null,
                    custNameFilter = null,
                    bouwheerNameFilter = null;

            if (
                    !StringUtil.isNullOrEmpty(request.getSearchBy())
                            && !StringUtil.isNullOrEmpty(request.getSearchValue())
            ) {
                switch (request.getSearchBy().toLowerCase()) {
                    case "status":
                        financingStatusFilter = request.getSearchValue();
                        break;
                    case "namadebitur":
                        custNameFilter = request.getSearchValue();
                        break;
                    case "pemberikerja":
                        bouwheerNameFilter = request.getSearchValue();
                        break;
                    case "cabang":
                        break;
                }
            }

            Page<FinancingHdr> financingHdrPage = financingHdrRepository.findAllAssignmentFinancingRaw(
                    user.getEmployee().getBranch().getBranchCode(),
                    financingStatusFilter,
                    custNameFilter,
                    bouwheerNameFilter,
                    PageRequest.of(pageNo, pageSize)
            );

            List<AssignmentDto> result = financingHdrPage.stream()
                    .filter(e -> e.getCustomer() != null && e.getBouwheer() != null)
                    .map(e -> {
                        boolean isNewCust = financingHdrRepository
                                .countByCustomerAndFinancingStatus(
                                        e.getCustomer(),
                                        "PAID"
                                ) == 0;


                        MappedFinancingStatus financingStatus = new MappedFinancingStatus(
                                e,
                                MappedFinancingStatus.Type.BranchAdmin
                        );

                        Agreement agreement = agreementRepository.findTopByFinancingHdr(e).orElse(null);
                        AgreementFile agreementFile = null;

                        String agreementDoc = null, agreementCode = null;
                        if (agreement != null) {
                            agreementCode = agreement.getAgreementCode();
                            agreementFile = agreementFileRepository.findTopByAgreementOrderByAgreementFileId(
                                    agreement
                            ).orElse(null);
                        }

                        if (agreementFile != null) {
                            agreementDoc = UriUtils.fileUlr(
                                    httpServletRequest,
                                    Math.toIntExact(agreementFile.getAgreementFileId()),
                                    UriUtils.DocType.agreement
                            );
                        }

                        return AssignmentDto.builder()
                                .financingHdrCode(e.getFinancingHdrCode())
                                .agreementCode(agreementCode)
                                .custCode(e.getCustomer().getCustCode())
                                .custName(e.getCustomer().getCustName())
                                .bouwheerName(e.getBouwheer().getBouwheerName())
                                .verifDate(null)
                                .dueDate(Utils.fromInstant(e.getFinancingDueDate()))
                                .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
                                .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                                .status(financingStatus.getStatus())
                                .statusLabel(financingStatus.getLabel())
                                .agreementDoc(agreementDoc)
                                .build();
                    })
                    .toList();

            return PaginationResult.<AssignmentDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(financingHdrPage.getTotalElements())
                    .totalPage(financingHdrPage.getTotalPages())
                    .list(result)
                    .build();
        } catch (Exception e) {
            log.error("assignmentList: error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<Object> tocList(
            String financingHdrCode,
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

            final Page<FinancingHdr> financingHdrs = financingHdrRepository.findAll(
                    FinancingHdrSpec.bySearchTOCBy(request.getSearchBy(), request.getSearchValue()),
                    PageRequest.of(pageNo, pageSize)
            );


            final List<Object> disburseInvoiceDto = financingHdrs
                    .stream()
                    .map((e) -> {


                        return null;

                    })
                    .toList();

            List<DisburseInvoiceDto> disburseInvoiceDto2 = new ArrayList<>();;
           /* for (DisburseInvoiceDto invoiceDto : disburseInvoiceDto) {
                if (invoiceDto != null) {
                    disburseInvoiceDto2.add(invoiceDto);
                }
            }*/

            return PaginationResult.<Object>builder()
                    .currentPage(pageNo + 1)
                    .totalData(0L)
                    .totalPage(1)
                    .list(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            log.error("tocList: error {}", e.getMessage());
            throw e;
        }
    }
}
