package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.modules.product.model.dto.ProductDto;
import com.kmkbe.modules.product.model.entity.Product;
import com.kmkbe.core.domain.mapper.ProductMapper;
import com.kmkbe.core.domain.repository.ProductRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    public PaginationResult<ProductDto> fetchAll(PaginationRequest request) {
        try {
            Page<Product> productPagination = productRepository.findAll(
                    PageRequest.of(request.getPageNo(), request.getPageSize())
            );

            List<ProductDto> products = productPagination.getContent()
                    .stream()
                    .map(ProductMapper.INSTANCE::entityToDto)
                    .toList();

            PaginationResult<ProductDto> dto = new PaginationResult<>();
            dto.setCurrentPage(productPagination.getNumber());
            dto.setTotalData(productPagination.getTotalElements());
            dto.setTotalPage(productPagination.getTotalPages());
            dto.setList(products);

            return dto;
        } catch (Exception e) {
            log.error("fetchAll: error {}", e.getMessage());
            throw e;
        }
    }

    public List<ProductDto> fetchAllActive() {
        try {
            Optional<List<Product>> find = productRepository.getAllActive();
            return find.map(products -> products
                            .stream()
                            .map(ProductMapper.INSTANCE::entityToDto)
                            .collect(Collectors.toList())
                    )
                    .orElseGet(ArrayList::new);
        } catch (Exception e) {
            log.error("fetchAllActive: error {}", e.getMessage());
            throw e;
        }
    }
}
