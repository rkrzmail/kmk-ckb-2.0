package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.ProductDto;
import com.kmkbe.core.domain.entity.Product;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.ProductRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.nikita.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MstProductService {
    private final ProductRepository productRepository;

    @Transactional
    public PaginationResult<ProductDto> uploadProduct(    HttpServletRequest httpServletRequest,
                                                          Authentication authentication,
                                                           MultipartFile file)  {
        productRepository.deleteAll();

        List<Product> dataEntities = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Lewati header

                Product
                dataEntity =  Product.builder()
                .productId((long)getAsNumber(row, 0))
                .branchCode(String.valueOf(((int)getAsNumber(row, 2))))
                .productName(getAsString(row, 3))//1
                .effectiveDate(DateTimeUtils.formatDateTime(getAsString(row, 3)))

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
             e.printStackTrace();
             throw new RuntimeException(e);
         }

        return null;

    }
    public boolean getAsBoolean(Row row, int column) {
        return Boolean.valueOf(String.valueOf(get(row, column))) ;
    }
    public String getAsString(Row row, int column) {
        return String.valueOf(get(row, column));
    }
    public double getAsNumber(Row row, int column) {
        return Utils.getDouble(get(row, column));
    }
    public Object get(Row row, int column) {
        if (row.getCell(column).getCellType() == CellType.NUMERIC){
            return row.getCell(column).getNumericCellValue();
        }else if (row.getCell(column).getCellType() == CellType.STRING){
            return row.getCell(column).getStringCellValue();
        }else if (row.getCell(column).getCellType() == CellType.BOOLEAN){
            return row.getCell(column).getBooleanCellValue();
        }else if (row.getCell(column).getCellType() == CellType.ERROR){
            return row.getCell(column).getErrorCellValue();
        }else if (row.getCell(column).getCellType() == CellType.FORMULA){
            return row.getCell(column).getCellFormula();
        }else if (row.getCell(column).getCellType() == CellType._NONE){
            return "";
        }else{
            return null;
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
            Page<Product> pagination = productRepository.findAllByIsActive(
                    PageRequest.of(pageNo, pageSize ), true
            );




           List<ProductDto> result = pagination.stream()
                    .map((e) -> ProductDto.builder()
                            .productId(e.getProductId())
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
