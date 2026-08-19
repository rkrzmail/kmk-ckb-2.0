package com.kmkbe.modules.product.service;

import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.helpers.utils.Utils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductExcelParser {

    public List<ProductRow> parse(MultipartFile file) {
        List<ProductRow> rows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                rows.add(new ProductRow(
                        (long) getAsNumber(row, 0),
                        getAsString(row, 1),
                        String.valueOf(((int) getAsNumber(row, 2))),
                        getAsString(row, 3),
                        DateTimeUtils.formatDateTimeWithNull(getAsString(row, 4)),
                        getAsNumber(row, 5),
                        getAsNumber(row, 6),
                        Utils.scale(getAsNumber(row, 7), 2),
                        Utils.scale(getAsNumber(row, 8), 2),
                        getAsNumber(row, 9),
                        getAsNumber(row, 10),
                        getAsNumber(row, 11),
                        Utils.scale(getAsNumber(row, 8), 12),
                        getAsNumber(row, 13),
                        getAsBoolean(row, 14)
                ));
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getAsBoolean(Row row, int column) {
        return Boolean.parseBoolean(String.valueOf(get(row, column)));
    }

    private String getAsString(Row row, int column) {
        return String.valueOf(get(row, column));
    }

    private double getAsNumber(Row row, int column) {
        return Utils.getDouble(get(row, column));
    }

    private Object get(Row row, int column) {
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

    public record ProductRow(
            Long productId,
            String productCode,
            String branchCode,
            String productName,
            LocalDateTime effectiveDate,
            Double ntfFrom,
            Double ntfTo,
            Double effectiveRate,
            Double provisionRate,
            Double surveyFee,
            Double legalFee,
            Double adminLimitFee,
            Double adminRate,
            Double othersFee,
            Boolean active
    ) {
    }
}
