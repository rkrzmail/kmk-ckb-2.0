package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.BranchAreaMappingDto;
import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchAreaMappingService {
    private final BranchAreaMappingRepository branchAreaMappingRepository;

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
    public CommonResult<Object> updateBranch(
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
    }


}
