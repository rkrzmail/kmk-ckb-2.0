package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.ProductDto;
import com.kmkbe.core.domain.entity.Product;
import com.kmkbe.core.domain.mapper.ProductMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.ProductRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.nikita.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MstProductService {
  private final ProductRepository productRepository;
  private static final ProductMapper productMapper = ProductMapper.INSTANCE;

  @Transactional
  public void uploadProduct(HttpServletRequest httpServletRequest,
                            Authentication authentication,
                            MultipartFile file) {
    productRepository.deleteAll();

    List<Product> dataEntities = new ArrayList<>();

    try (InputStream inputStream = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(inputStream)) {

      Sheet sheet = workbook.getSheetAt(0);
      for (Row row : sheet) {
        if (row.getRowNum() == 0) continue; // Lewati header

        Product
          dataEntity = Product.builder()
          .productId((long) getAsNumber(row, 0))
          .productCode(getAsString(row, 1))
          .branchCode(String.valueOf(((int) getAsNumber(row, 2))))
          .productName(getAsString(row, 3))//1

          .effectiveDate(DateTimeUtils.formatDateTimeWithNull(getAsString(row, 4)))

          .ntfFrom(getAsNumber(row, 5))
          .ntfTo(getAsNumber(row, 6))
          .effectiveRate(Utils.scale(getAsNumber(row, 7), 2))
          .provisionRate(Utils.scale(getAsNumber(row, 8), 2))
          .surveyFee(getAsNumber(row, 9))
          .legalFee(getAsNumber(row, 10))
          .adminLimitFee(getAsNumber(row, 11))
          .adminRate(Utils.scale(getAsNumber(row, 8), 12))
          .othersFee(getAsNumber(row, 13))
          .isActive(getAsBoolean(row, 14))
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

  public boolean getAsBoolean(Row row, int column) {
    return Boolean.parseBoolean(String.valueOf(get(row, column)));
  }

  public String getAsString(Row row, int column) {
    return String.valueOf(get(row, column));
  }

  public double getAsNumber(Row row, int column) {
    return Utils.getDouble(get(row, column));
  }

  public Object get(Row row, int column) {
    Object object;
    if (row.getCell(column).getCellType() == CellType.NUMERIC) {
      object = row.getCell(column).getNumericCellValue();
    } else if (row.getCell(column).getCellType() == CellType.STRING) {
      object = row.getCell(column).getStringCellValue();
    } else if (row.getCell(column).getCellType() == CellType.BOOLEAN) {
      object = row.getCell(column).getBooleanCellValue();
    } else if (row.getCell(column).getCellType() == CellType.ERROR) {
      object = row.getCell(column).getErrorCellValue();
    } else if (row.getCell(column).getCellType() == CellType.FORMULA) {
      object = row.getCell(column).getCellFormula();
    } else if (row.getCell(column).getCellType() == CellType._NONE) {
      object = "";
    } else {
      object = null;
    }
    if (String.valueOf(object).startsWith("'")) {
      return String.valueOf(object).substring(1);
    }
    return object;

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
