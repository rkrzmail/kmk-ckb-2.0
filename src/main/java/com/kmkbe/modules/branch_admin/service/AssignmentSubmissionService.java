package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public PaginationResult<AssignmentDto> assignmentList(
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

            MstUser user = UserInternalUtils.authenticateUser(authentication);
            Page<FinancingHdr> financingHdrPage = financingHdrRepository.findByFinancingStatusOrderByFinancingHdrIdDesc(
                    "NEW",
                    Pageable.ofSize(pageSize).withPage(pageNo)
            );

            List<AssignmentDto> result = financingHdrPage.stream()
                    .filter(e -> e.getCustomer() != null && e.getBouwheer() != null)
                    .map(e -> {
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

                        return AssignmentDto.builder()
                                .financingHdrCode(e.getFinancingHdrCode().toString())
                                .custName(e.getCustomer().getCustName())
                                .bouwheerName(e.getBouwheer().getBouwheerName())
                                .verifDate(new Date())
                                .dueDate(new Date(e.getFinancingDueDate().toEpochMilli()))
                                .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
                                .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                                .status(e.getFinancingStatus())
                                .statusLabel(label)
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
