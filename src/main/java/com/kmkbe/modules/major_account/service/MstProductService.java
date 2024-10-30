package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.ProductDto;
import com.kmkbe.core.domain.entity.Product;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.ProductRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MstProductService {
    private final ProductRepository findAll;

    public PaginationResult<ProductDto> listProduct(
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

            pageSize = 1000;
            Page<Product> pagination = findAll.findAllByIsActive(
                    PageRequest.of(pageNo, pageSize ), true
            );




           List<ProductDto> result = pagination.stream()
                    .map((e) -> ProductDto.builder()
                            .productId(e.getProductId())
                            .ntfTo(e.getNtfTo())
                            .ntfFrom(e.getNtfFrom())
                            .branchCode(e.getBranchCode())
                            .effectiveDate(e.getEffectiveDate())
                            .adminLimitFee(e.getAdminLimitFee())
                            .adminRate(e.getAdminRate())
                            .legalFee(e.getLegalFee())
                            .othersFee(e.getOthersFee())
                            .insuranceRate(e.getInsuranceRate())
                            .provisionRate(e.getProvisionRate())
                            .effectiveRate(e.getEffectiveRate())
                            .surveyFee(e.getSurveyFee())
                            .build())
                    .toList();

            return PaginationResult.<ProductDto>builder()
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
