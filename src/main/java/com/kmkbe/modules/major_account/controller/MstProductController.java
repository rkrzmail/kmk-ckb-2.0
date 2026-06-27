package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.ProductDto;
import com.kmkbe.core.domain.entity.Product;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.major_account.service.BranchAreaMappingService;
import com.kmkbe.modules.major_account.service.MstProductService;

import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/v1/product")
@Tag(
  name = "Penempatan cabang API",
  description = "Berisi endpoints data penembatan cabang / branch area mapping"
)
@RequiredArgsConstructor
public class MstProductController {
  private final MstProductService productService;
  private final BranchAreaMappingService branchAreaMappingService;


  @GetMapping("/list")
  public CommonResult<PaginationResult<ProductDto>> getList(
    Authentication authentication, @ParameterObject PaginationRequest request
  ) throws SignatureException {
    UserInternalUtils.authenticated(authentication);
    return new CommonResult<PaginationResult<ProductDto>>().success(
      productService.listProduct(request)
    );
  }

  @GetMapping("/listitem/{id}")
  public CommonResult<PaginationResult<ProductDto>> getListItem(
    Authentication authentication, @ParameterObject PaginationRequest request,
    @PathVariable("id") Long id
  ) throws SignatureException {
    UserInternalUtils.authenticated(authentication);
    return new CommonResult<PaginationResult<ProductDto>>().success(
      productService.listProductItem(request, id)
    );
  }

  @PostMapping(
    value = "/update/upload",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public CommonResult<Object> postUploadPlacementBranch(
    HttpServletRequest httpServletRequest,
    Authentication authentication,
    @Valid @RequestPart MultipartFile file
  ) throws SignatureException {
    UserInternalUtils.authenticated(authentication);

    productService.uploadProduct(httpServletRequest, authentication, file);
    return new CommonResult<>().success(null);
  }

  // TAMBAH PRODUK
  @PostMapping("/create")
  public CommonResult<ProductDto> createProduct(
    Authentication authentication,
    @Valid @RequestBody ProductDto productDto
  ) throws SignatureException {
    UserInternalUtils.authenticated(authentication);

    ProductDto createdProduct = productService.createProduct(productDto);

    return new CommonResult<ProductDto>().success(createdProduct);
  }

  // AMBIL LAST PRODUK CODE
  @GetMapping("/last")
  public ResponseEntity<Map<String, Long>> getLastProductId() {
    Long lastProductId = productService.getLastProductId();
    Map<String, Long> response = new HashMap<>();
    response.put("productId", lastProductId);
    return ResponseEntity.ok(response);
  }

  // EDIT PRODUK
  @GetMapping("/item/{productCode}")
  public ResponseEntity<Product> getProductByCode(@PathVariable String productCode) {
     return productService.getProductByCode(productCode);
  }

  @PutMapping("/item/{productCode}")
  public ResponseEntity<?> updateProduct(@PathVariable String productCode, @RequestBody Product productDetails) {
    try {
      Product updatedProduct = productService.updateProduct(productCode, productDetails);
      CommonResult<Product> result = new CommonResult<Product>().success(updatedProduct);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      CommonResult<String> result = new CommonResult<String>().fail(500, "An error occurred: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
  }
}
