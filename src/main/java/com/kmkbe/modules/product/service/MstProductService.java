package com.kmkbe.modules.product.service;

import com.kmkbe.modules.product.model.dto.ProductDto;
import com.kmkbe.modules.product.model.entity.Product;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.modules.product.repository.ProductRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.utils.PageableUtil;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MstProductService {
  private final ProductRepository productRepository;
  private final BouwheerRepository bouwheerRepository;
  private final ProductExcelParser productExcelParser;
  private final CurrentUserService currentUserService;
  public BaseResponseBuilder<List<ProductDto>> all() {
    List<ProductDto> products = productRepository.findAll()
      .stream()
      .map(this::toDto)
      .toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, products);
  }

  public BaseResponseBuilder<PaginationResult<ProductDto>> pages(BasePaginationRequest request) {
    String sortBy = request.getSortBy() != null && !request.getSortBy().isEmpty() ? request.getSortBy() : "productId";
    Pageable pageable = PageableUtil.createPageRequest(
      request,
      request.getPageSize(),
      request.getPageNo(),
      sortBy,
      request.getSortType()
    );

    Page<Product> page = productRepository.findAll((root, query, builder) -> {
      if (request.getSearchValue() == null || request.getSearchValue().isBlank()) {
        return builder.conjunction();
      }

      String searchValue = "%" + request.getSearchValue().toLowerCase() + "%";
      String searchBy = request.getSearchBy() != null ? request.getSearchBy() : "productName";

      if ("bouwheerName".equalsIgnoreCase(searchBy)) {
        return builder.like(builder.lower(root.join("bouwheer", JoinType.LEFT).get("bouwheerName")), searchValue);
      }

      if ("bouwheerCode".equalsIgnoreCase(searchBy)) {
        return builder.equal(root.join("bouwheer", JoinType.LEFT).get("bouwheerCode"), UUID.fromString(request.getSearchValue()));
      }

      if ("productId".equalsIgnoreCase(searchBy)) {
        return builder.equal(root.get("productId"), Long.valueOf(request.getSearchValue()));
      }

      if ("branchCode".equalsIgnoreCase(searchBy)) {
        return builder.like(builder.lower(root.get("branchCode")), searchValue);
      }

      if ("productCode".equalsIgnoreCase(searchBy)) {
        return builder.like(builder.lower(root.get("productCode")), searchValue);
      }

      return builder.like(builder.lower(root.get("productName")), searchValue);
    }, pageable);

    return new BaseResponseBuilder<>(
      true,
      AppConstants.CODE_OK,
      AppConstants.PROCESS_SUCCESSFULLY,
      PaginationResult.<ProductDto>builder()
        .currentPage(page.getNumber() + 1)
        .totalData(page.getTotalElements())
        .totalPage(page.getTotalPages())
        .list(page.getContent().stream().map(this::toDto).toList())
        .build()
    );
  }

  @Transactional
  public BaseResponse create(ProductDto productDto) {
    validateProductCodeAvailable(productDto.getProductCode(), null);

    Product product = fromDto(productDto, new Product());
    product.setUsrCrt(currentUserService.usernameOrDefault(AppConstants.CREATOR));
    product.setDtmCrt(DateTimeUtils.nowLocal());
    productRepository.save(product);

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }

  @Transactional
  public BaseResponse update(Long productId, ProductDto productDto) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, AppConstants.CODE_NOT_FOUND, "Product not found"));

    validateProductCodeAvailable(productDto.getProductCode(), productId);
    fromDto(productDto, product);
    product.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR));
    product.setDtmUpd(DateTimeUtils.nowLocal());
    productRepository.save(product);

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }

  public BaseResponse findById(Long productId) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, AppConstants.CODE_NOT_FOUND, "Product not found"));

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, toDto(product));
  }

  @Transactional
  public void uploadProduct(HttpServletRequest httpServletRequest,
                            MultipartFile file) {
    productRepository.deleteAll();

    List<Product> dataEntities = new ArrayList<>();

    try {
      for (ProductExcelParser.ProductRow row : productExcelParser.parse(file)) {
        Product
          dataEntity = Product.builder()
          .productId(row.productId())
          .productCode(row.productCode())
          .branchCode(row.branchCode())
          .productName(row.productName())//1

          .effectiveDate(row.effectiveDate())

          .ntfFrom(row.ntfFrom())
          .ntfTo(row.ntfTo())
          .effectiveRate(row.effectiveRate())
          .provisionRate(row.provisionRate())
          .surveyFee(row.surveyFee())
          .legalFee(row.legalFee())
          .adminLimitFee(row.adminLimitFee())
          .adminRate(row.adminRate())
          .othersFee(row.othersFee())
          .isActive(row.active())
          .usrCrt("SYSTEM")
          .dtmCrt(DateTimeUtils.nowLocal())
          .build();
        dataEntities.add(dataEntity);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }


    try {
      productRepository.saveAll(dataEntities);
    } catch (Exception e) {
      log.error("Error {}",e.getMessage());
      throw new RuntimeException(e);
    }

  }

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
      Page<Product> pagination = productRepository.findAll(PageRequest.of(pageNo, pageSize));

      List<ProductDto> result = pagination.stream()
        .map(this::toDto)
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

  public PaginationResult<ProductDto> listProductItem(
    PaginationRequest request,
    Long id
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
      Page<Product> pagination = productRepository.findAllByProductId(
        PageRequest.of(pageNo, pageSize),
        id
      );


      List<ProductDto> result = pagination.stream()
        .map(this::toDto)
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

  //tambah produk
  @Transactional
  public ProductDto createProduct(ProductDto productDto) {
    Product product = fromDto(productDto, new Product());
    product.setUsrCrt(currentUserService.usernameOrDefault(AppConstants.CREATOR));
    product.setDtmCrt(DateTimeUtils.nowLocal());

    Product savedProduct = productRepository.save(product);

    return toDto(savedProduct);
  }


  // Mendapatkan productCode terakhir
  public Long getLastProductId() {
    // Ambil produk terakhir dengan pageable (limit 1)
    Pageable pageable = PageRequest.of(0, 1);  // Membatasi hanya 1 hasil
    List<Product> products = productRepository.findLatestProduct(pageable);

    // Ambil productCode dari produk terakhir
    Product lastProduct = productRepository.findTopByOrderByProductIdDesc();
    if (lastProduct != null) {
      return lastProduct.getProductId();
    }
    return null;
  }

  // GET EDIT
  public ResponseEntity<Product> getProductByCode(String productCode) {
    Optional<Product> product = productRepository.findByProductCode(productCode);
    return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
  }

  // UPDATE
  @Transactional
  public Product updateProduct(String productCode, Product productDetails) {
    Optional<Product> existingProductOpt = productRepository.findByProductCode(productCode);

    if (existingProductOpt.isPresent()) {
      Product existingProduct = existingProductOpt.get();

      existingProduct.setProductName(productDetails.getProductName());
      existingProduct.setProductCode(productDetails.getProductCode());
      existingProduct.setBranchCode(productDetails.getBranchCode());
      existingProduct.setBouwheer(productDetails.getBouwheer());
      existingProduct.setEffectiveDate(productDetails.getEffectiveDate());
      existingProduct.setNtfFrom(productDetails.getNtfFrom());
      existingProduct.setNtfTo(productDetails.getNtfTo());
      existingProduct.setEffectiveRate(productDetails.getEffectiveRate());
      existingProduct.setProvisionRate(productDetails.getProvisionRate());
      existingProduct.setSurveyFee(productDetails.getSurveyFee());
      existingProduct.setLegalFee(productDetails.getLegalFee());
      existingProduct.setAdminLimitFee(productDetails.getAdminLimitFee());
      existingProduct.setAdminRate(productDetails.getAdminRate());
      existingProduct.setInsuranceRate(productDetails.getInsuranceRate());
      existingProduct.setOthersFee(productDetails.getOthersFee());
      existingProduct.setIsActive(productDetails.getIsActive());
      existingProduct.setUsrUpd("SYSTEM");
      existingProduct.setDtmUpd(LocalDateTime.now());
      // Simpan perubahan ke database
      productRepository.save(existingProduct);

      return existingProduct;
    } else {
      throw new RuntimeException("Product not found with code: " + productCode);
    }
  }

  private Product fromDto(ProductDto productDto, Product product) {
    product.setProductCode(productDto.getProductCode());
    product.setBranchCode(productDto.getBranchCode());
    product.setProductName(productDto.getProductName());
    product.setBouwheer(resolveBouwheer(productDto.getBouwheerCode()));
    product.setEffectiveDate(productDto.getEffectiveDate());
    product.setNtfFrom(productDto.getNtfFrom());
    product.setNtfTo(productDto.getNtfTo());
    product.setEffectiveRate(productDto.getEffectiveRate());
    product.setProvisionRate(productDto.getProvisionRate());
    product.setSurveyFee(productDto.getSurveyFee());
    product.setLegalFee(productDto.getLegalFee());
    product.setAdminLimitFee(productDto.getAdminLimitFee());
    product.setAdminRate(productDto.getAdminRate());
    product.setInsuranceRate(productDto.getInsuranceRate());
    product.setOthersFee(productDto.getOthersFee());
    product.setIsActive(productDto.getIsActive());
    return product;
  }

  private ProductDto toDto(Product product) {
    Bouwheer bouwheer = product.getBouwheer();
    return ProductDto.builder()
      .productId(product.getProductId())
      .productCode(product.getProductCode())
      .branchCode(product.getBranchCode())
      .bouwheerCode(bouwheer != null ? bouwheer.getBouwheerCode() : null)
      .bouwheerName(bouwheer != null ? bouwheer.getBouwheerName() : null)
      .productName(product.getProductName())
      .effectiveDate(product.getEffectiveDate())
      .ntfFrom(product.getNtfFrom())
      .ntfTo(product.getNtfTo())
      .effectiveRate(product.getEffectiveRate())
      .provisionRate(product.getProvisionRate())
      .surveyFee(product.getSurveyFee())
      .legalFee(product.getLegalFee())
      .adminLimitFee(product.getAdminLimitFee())
      .adminRate(product.getAdminRate())
      .insuranceRate(product.getInsuranceRate())
      .othersFee(product.getOthersFee())
      .isActive(product.getIsActive())
      .status(Boolean.TRUE.equals(product.getIsActive()) ? "ACTIVE" : "INACTIVE")
      .usrCrt(product.getUsrCrt())
      .dtmCrt(product.getDtmCrt())
      .usrUpd(product.getUsrUpd())
      .dtmUpd(product.getDtmUpd())
      .build();
  }

  private Bouwheer resolveBouwheer(UUID bouwheerCode) {
    if (bouwheerCode == null) {
      return null;
    }

    return bouwheerRepository.findByBouwheerCode(bouwheerCode)
      .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, AppConstants.CODE_NOT_FOUND, "Bouwheer not found"));
  }

  private void validateProductCodeAvailable(String productCode, Long currentProductId) {
    if (productCode == null || productCode.isBlank()) {
      return;
    }

    productRepository.findByProductCodeIgnoreCase(productCode)
      .filter(product -> currentProductId == null || !product.getProductId().equals(currentProductId))
      .ifPresent(product -> {
        throw new BusinessException(HttpStatus.CONFLICT, AppConstants.CODE_CONFLICT, "Product code already exists");
      });
  }
}
