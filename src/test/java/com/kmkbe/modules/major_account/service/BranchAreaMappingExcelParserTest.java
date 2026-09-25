package com.kmkbe.modules.major_account.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BranchAreaMappingExcelParserTest {

    private final BranchAreaMappingExcelParser parser = new BranchAreaMappingExcelParser();

    @Test
    void parseReadsBranchAreaMappingRowsAndSkipsHeader() throws Exception {
        MockMultipartFile file = workbookFile(workbook -> {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("id");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(10);
            row.createCell(1).setCellValue("AREA 1");
            row.createCell(2).setCellValue("DKI Jakarta");
            row.createCell(3).setCellValue("Jakarta Selatan");
            row.createCell(5).setCellValue(101);
        });

        List<BranchAreaMappingExcelParser.BranchAreaMappingRow> rows = parser.parse(file);

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().branchAreaMappingId()).isEqualTo(10L);
        assertThat(rows.getFirst().area()).isEqualTo("AREA 1");
        assertThat(rows.getFirst().province()).isEqualTo("DKI Jakarta");
        assertThat(rows.getFirst().city()).isEqualTo("Jakarta Selatan");
        assertThat(rows.getFirst().branchCode()).isEqualTo("101");
    }

    private MockMultipartFile workbookFile(WorkbookCustomizer customizer) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            customizer.customize(workbook);
            workbook.write(outputStream);
            return new MockMultipartFile(
                    "file",
                    "branch.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray()
            );
        }
    }

    private interface WorkbookCustomizer {
        void customize(Workbook workbook);
    }
}
