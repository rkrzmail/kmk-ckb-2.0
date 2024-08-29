package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.model.PaginationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentSubmissionService {

    public PaginationResult<PostedInvoiceDto> assignmentList(
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
