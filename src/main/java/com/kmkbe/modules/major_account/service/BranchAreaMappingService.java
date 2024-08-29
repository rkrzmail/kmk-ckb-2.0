package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.BranchAreaMappingDto;
import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchAreaMappingService {
    private final BranchAreaMappingRepository branchAreaMappingRepository;

    public PaginationResult<BranchAreaMappingDto> placementBranch(
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

            Page<BranchAreaMapping> pagination = branchAreaMappingRepository.findAll(
                    PageRequest.of(pageNo, pageSize, Sort.by("province"))
            );

            List<BranchAreaMappingDto> result = pagination.stream()
                    .map((e) -> BranchAreaMappingDto.builder()
                            .branchAreaMappingId(e.getBranchAreaMappingId())
                            .area(e.getArea())
                            .province(e.getProvince())
                            .city(e.getCity())
                            .branch(e.getMstBranch().getBranchName())
                            .build())
                    .toList();

            return PaginationResult.<BranchAreaMappingDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(pagination.getTotalElements())
                    .totalPage(pagination.getTotalPages())
                    .list(result)
                    .build();
        } catch (Exception e) {
            log.error("placementBranch: error {}", e.getMessage());
            throw e;
        }
    }
}
