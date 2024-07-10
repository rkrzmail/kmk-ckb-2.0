package com.kmkbe.modules.kredit.service;

import com.kmkbe.modules.kredit.dto.ProductDto;
import com.kmkbe.modules.kredit.entity.Product;
import com.kmkbe.modules.kredit.mapper.ProductMapper;
import com.kmkbe.modules.kredit.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            log.error("showAllActive: error {}", e.getMessage());
            throw e;
        }
    }
}
