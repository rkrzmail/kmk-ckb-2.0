package com.kmkbe.modules.major_account.service;

import com.kmkbe.helpers.utils.Utils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class BranchAreaMappingExcelParser {

    public List<BranchAreaMappingRow> parse(MultipartFile file) {
        List<BranchAreaMappingRow> rows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                rows.add(new BranchAreaMappingRow(
                        (long) getAsNumber(row, 0),
                        getAsString(row, 1),
                        getAsString(row, 2),
                        getAsString(row, 3),
                        String.valueOf(((int) getAsNumber(row, 5)))
                ));
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getAsString(Row row, int column) {
        return String.valueOf(get(row, column));
    }

    private double getAsNumber(Row row, int column) {
        return Utils.getDouble(get(row, column));
    }

    private Object get(Row row, int column) {
        if (row.getCell(column) == null) {
            return null;
        } else if (row.getCell(column).getCellType() == CellType.NUMERIC) {
            return row.getCell(column).getNumericCellValue();
        } else if (row.getCell(column).getCellType() == CellType.STRING) {
            return row.getCell(column).getStringCellValue();
        } else if (row.getCell(column).getCellType() == CellType.BOOLEAN) {
            return row.getCell(column).getBooleanCellValue();
        } else if (row.getCell(column).getCellType() == CellType.ERROR) {
            return row.getCell(column).getErrorCellValue();
        } else if (row.getCell(column).getCellType() == CellType.FORMULA) {
            return row.getCell(column).getCellFormula();
        } else if (row.getCell(column).getCellType() == CellType._NONE) {
            return "";
        } else {
            return null;
        }
    }

    public record BranchAreaMappingRow(
            Long branchAreaMappingId,
            String area,
            String province,
            String city,
            String branchCode
    ) {
    }
}
