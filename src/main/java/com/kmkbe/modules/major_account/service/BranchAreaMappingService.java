package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.BranchAreaMappingDto;
import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.nikita.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchAreaMappingService {
    private final BranchAreaMappingRepository branchAreaMappingRepository;
    private final MstBranchRepository mstBranchRepository;

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

    @Transactional
    public PaginationResult<BranchAreaMappingDto> updateBranch(   HttpServletRequest httpServletRequest,
                                                                  MultipartFile file){

        branchAreaMappingRepository.deleteAll();

        List<BranchAreaMapping> dataEntities = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Lewati header


                Optional<MstBranch> mstBranch = mstBranchRepository.findByBranchCode(String.valueOf(((int)getAsNumber(row, 5))));

                if (mstBranch.isEmpty()) continue;
                BranchAreaMapping dataEntity =  BranchAreaMapping.builder()
                        .branchAreaMappingId((long)getAsNumber(row, 0))
                        .area(getAsString(row, 1))
                        .province(getAsString(row, 2))
                        .city(getAsString(row, 3))
                        .mstBranch(mstBranch.get())

                        .isActive(true)
                        .usrCrt("SYSTEM")
                        .dtmCrt(DateTimeUtils.nowLocal())
                        .build();





                dataEntities.add(dataEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            branchAreaMappingRepository.saveAll(dataEntities);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }




        return  null;
    }
    public PaginationResult<BranchAreaMappingDto> listBranch(
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

    /*public CommonResult<Object> updateBranch(
            HttpServletRequest request,
            MultipartFile file
    ) throws IOException {
        try {

            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowCount = sheet.getLastRowNum();
            Row headerRow = rows.next();
            Map<String, Integer> headerMap = new HashMap<>();


            branchAreaMappingRepository.deleteAll();//tuncate

            Iterator<Cell> headerCells = headerRow.cellIterator();
            int headerIndex = 0;
            while (rows.hasNext()) {
                Cell cell = headerCells.next();
                BranchAreaMapping branchAreaMapping = BranchAreaMapping.builder()
                                .build();

                //branchAreaMappingRepository.save()
            }
            return null;
        } catch (Exception e) {
            log.error("placementBranch: error {}", e.getMessage());
            throw e;
        }
    }*/

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
        if (row.getCell(column) == null ){
            return null;
        }else  if (row.getCell(column).getCellType() == CellType.NUMERIC){
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
}
