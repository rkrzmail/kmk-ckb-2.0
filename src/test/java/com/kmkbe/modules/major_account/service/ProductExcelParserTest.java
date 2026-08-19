package com.kmkbe.modules.major_account.service;

import com.kmkbe.modules.product.service.ProductExcelParser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductExcelParserTest {

    private final ProductExcelParser parser = new ProductExcelParser();

    @Test
    void parseReadsProductRowsAndKeepsExistingColumnMapping() throws Exception {
        MockMultipartFile file = workbookFile(workbook -> {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("id");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(1);
            row.createCell(1).setCellValue("PRD001");
            row.createCell(2).setCellValue(101);
            row.createCell(3).setCellValue("Dana Sakti");
            row.createCell(4).setCellValue("2026-08-12 10:00:00");
            row.createCell(5).setCellValue(100);
            row.createCell(6).setCellValue(200);
            row.createCell(7).setCellValue(3.456);
            row.createCell(8).setCellValue(1.234);
            row.createCell(9).setCellValue(10);
            row.createCell(10).setCellValue(20);
            row.createCell(11).setCellValue(30);
            row.createCell(13).setCellValue(40);
            row.createCell(14).setCellValue(true);
        });

        List<ProductExcelParser.ProductRow> rows = parser.parse(file);

        assertThat(rows).hasSize(1);
        ProductExcelParser.ProductRow row = rows.getFirst();
        assertThat(row.productId()).isEqualTo(1L);
        assertThat(row.productCode()).isEqualTo("PRD001");
        assertThat(row.branchCode()).isEqualTo("101");
        assertThat(row.productName()).isEqualTo("Dana Sakti");
        assertThat(row.effectiveDate()).isEqualTo(LocalDateTime.of(2026, 8, 12, 10, 0));
        assertThat(row.effectiveRate()).isEqualTo(3.46);
        assertThat(row.provisionRate()).isEqualTo(1.23);
        assertThat(row.adminRate()).isEqualTo(1.234);
        assertThat(row.active()).isTrue();
    }

    private MockMultipartFile workbookFile(WorkbookCustomizer customizer) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            customizer.customize(workbook);
            workbook.write(outputStream);
            return new MockMultipartFile(
                    "file",
                    "product.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray()
            );
        }
    }

    private interface WorkbookCustomizer {
        void customize(Workbook workbook);
    }
}
