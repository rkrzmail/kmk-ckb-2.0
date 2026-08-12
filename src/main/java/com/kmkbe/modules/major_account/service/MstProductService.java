package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.ProductDto;
import com.kmkbe.core.domain.entity.Product;
import com.kmkbe.core.domain.mapper.ProductMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.ProductRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MstProductService {
  private final ProductRepository productRepository;
  private final ProductExcelParser productExcelParser;
  private static final ProductMapper productMapper = ProductMapper.INSTANCE;

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
        .map((e) -> ProductDto.builder()
          .productId(e.getProductId())
          .productCode(e.getProductCode())
          .productName(e.getProductName())
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
          .isActive(e.getIsActive())
          .status(e.getIsActive() ? "ACTIVE" : "INACTIVE")
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
        .map((e) -> ProductDto.builder()
          .productId(e.getProductId())
          .productCode(e.getProductCode())
          .productName(e.getProductName())
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
          .isActive(e.getIsActive())
          .status(e.getIsActive() ? "ACTIVE" : "INACTIVE")
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

  //tambah produk
  @Transactional
  public ProductDto createProduct(ProductDto productDto) {
    // Mengonversi ProductDto menjadi Product (Entity)
    Product product = Product.builder()
      .productCode(productDto.getProductCode())
      .branchCode(productDto.getBranchCode())
      .productName(productDto.getProductName())
      .effectiveDate(productDto.getEffectiveDate())
      .ntfFrom(productDto.getNtfFrom())
      .ntfTo(productDto.getNtfTo())
      .effectiveRate(productDto.getEffectiveRate())
      .provisionRate(productDto.getProvisionRate())
      .surveyFee(productDto.getSurveyFee())
      .legalFee(productDto.getLegalFee())
      .adminLimitFee(productDto.getAdminLimitFee())
      .adminRate(productDto.getAdminRate())
      .insuranceRate(productDto.getInsuranceRate())
      .othersFee(productDto.getOthersFee())
      .isActive(productDto.getIsActive())
      .usrCrt("SYSTEM")  // atau bisa diganti sesuai dengan user yang menginput
      .dtmCrt(DateTimeUtils.nowLocal())  // timestamp saat produk dibuat
      .build();

    // Menyimpan Product ke database
    Product savedProduct = productRepository.save(product);

    // Mengonversi kembali Product ke ProductDto untuk dikirim ke controller
    return productMapper.entityToDto(savedProduct);
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
}
