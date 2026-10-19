package com.kmkbe.core.utils;

import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.service.FileStorageService_;
import com.kmkbe.modules.major_account.service.BranchAreaMappingExcelParser;
import com.kmkbe.modules.product.service.ProductExcelParser;
import jakarta.servlet.ServletContext;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FileUploadAndPathTraversalSecurityTest {

    @Mock private ServletContext servletContext;

    // ---------- 1. FileUtils Extension Extraction Safety ----------

    @Test
    @DisplayName("H6: FileUtils extracts only the final segment extension (preventing double extension bypass)")
    void fileUtilsExtractsLastSegmentExtension() throws Exception {
        // Normal single extension
        MockMultipartFile normalFile = new MockMultipartFile("f", "document.pdf", "application/pdf", "content".getBytes());
        assertThat(FileUtils.getUploadFileExtension(normalFile)).isEqualTo("pdf");

        // Double extension (evil file: invoice.pdf.jsp -> must extract jsp)
        MockMultipartFile doubleExtFile = new MockMultipartFile("f", "invoice.pdf.jsp", "application/octet-stream", "evil".getBytes());
        assertThat(FileUtils.getUploadFileExtension(doubleExtFile)).isEqualTo("jsp");

        // Triple extension
        MockMultipartFile tripleExt = new MockMultipartFile("f", "archive.tar.gz.sh", "application/x-sh", "bash".getBytes());
        assertThat(FileUtils.getUploadFileExtension(tripleExt)).isEqualTo("sh");

        // String filename overload
        assertThat(FileUtils.getFileNameExtension("document.pdf.exe")).isEqualTo("exe");
        assertThat(FileUtils.getFileNameExtension("my.photo.jpeg")).isEqualTo("jpeg");
        assertThat(FileUtils.getFileNameExtension("no-extension")).isNull();
        assertThat(FileUtils.getFileNameExtension(null)).isNull();
    }

    // ---------- 2. FileStorageService & FileStorageService_ Traversal Prevention ----------

    @Test
    @DisplayName("H6: FileStorageService_ rejects path traversal in save, load, delete, and download")
    void fileStorageService_PreventsPathTraversal(@TempDir Path tempDir) {
        FileStorageService_ service = new FileStorageService_(servletContext, tempDir.toString());

        // 1. Path traversal in load
        assertThatThrownBy(() -> service.load("../../etc/passwd"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal attempt detected");

        // 2. Path traversal in delete
        assertThatThrownBy(() -> service.delete("../../../config.yaml", "code"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal attempt detected");

        // 3. Path traversal in downloadUploadFile
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThatThrownBy(() -> service.downloadUploadFile(request, "../../etc", "passwd"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Path traversal attempt detected");
    }

    @Test
    @DisplayName("H6: FileStorageService downloadUploadFile sanitizes Content-Disposition filename against header injection")
    void fileStorageServiceSanitizesContentDispositionHeader(@TempDir Path tempDir) throws Exception {
        FileStorageService service = new FileStorageService(servletContext, tempDir.toString());

        // Create a dummy file inside tempDir
        Path subDir = tempDir.resolve("loan_docs");
        java.nio.file.Files.createDirectories(subDir);
        Path sampleFile = subDir.resolve("sample.pdf");
        java.nio.file.Files.writeString(sampleFile, "dummy pdf content");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("cd", "attachment");

        // Filename containing malicious quotes / CRLF attempt
        String maliciousName = "report\r\nSet-Cookie: evil=1\".pdf";
        var response = service.downloadUploadFile(request, "loan_docs/sample.pdf", maliciousName, "attachment");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        String header = response.getHeaders().getFirst("Content-Disposition");
        assertThat(header).isNotNull();
        // Filename must have CRLF and double-quotes replaced to prevent HTTP Response Splitting / Header Injection
        assertThat(header).doesNotContain("\r");
        assertThat(header).doesNotContain("\n");
        assertThat(header).contains("report__Set-Cookie: evil=1_.pdf");
    }

    // ---------- 3. Excel Bomb / OOM Denial-of-Service Prevention ----------

    @Test
    @DisplayName("H6: BranchAreaMappingExcelParser enforces MAX_EXCEL_ROWS to protect against Excel bombs")
    void branchAreaMappingParserRejectsExcessiveRows() throws IOException {
        BranchAreaMappingExcelParser parser = new BranchAreaMappingExcelParser();

        // Generate synthetic Excel workbook with 5002 rows (> 5000 limit)
        byte[] excelBytes = createExcelWithRows(BranchAreaMappingExcelParser.MAX_EXCEL_ROWS + 2);
        MockMultipartFile excelFile = new MockMultipartFile("file", "large.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        assertThatThrownBy(() -> parser.parse(excelFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum allowed rows limit");
    }

    @Test
    @DisplayName("H6: ProductExcelParser enforces MAX_EXCEL_ROWS to protect against Excel bombs")
    void productExcelParserRejectsExcessiveRows() throws IOException {
        ProductExcelParser parser = new ProductExcelParser();

        byte[] excelBytes = createExcelWithRows(ProductExcelParser.MAX_EXCEL_ROWS + 2);
        MockMultipartFile excelFile = new MockMultipartFile("file", "large_products.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        assertThatThrownBy(() -> parser.parse(excelFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum allowed rows limit");
    }

    private byte[] createExcelWithRows(int rowCount) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Col1");

            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue("data-" + i);
            }
            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}
