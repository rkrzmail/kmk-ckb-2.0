package com.kmkbe.modules.loan_submission.controller;


import com.kmkbe.core.model.CommonResult;
import com.kmkbe.core.model.PaginationResult;
import com.kmkbe.modules.loan_submission.dto.ProductDto;
import com.kmkbe.modules.loan_submission.request.ProductListRequest;
import com.kmkbe.modules.loan_submission.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/products")
@Tag(
        name = "Product",
        description = "Berisi endpoints Master untuk melakukan kalkulasi pengajuan Kredit"
)
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public CommonResult<PaginationResult<ProductDto>> getAll(
            ProductListRequest request
    ) {
        return new CommonResult<PaginationResult<ProductDto>>()
                .success(productService.fetchAll(request));
    }

    @GetMapping("/actives")
    public CommonResult<List<ProductDto>> getAllActive() {
        return new CommonResult<List<ProductDto>>()
                .success(productService.fetchAllActive());
    }
}
