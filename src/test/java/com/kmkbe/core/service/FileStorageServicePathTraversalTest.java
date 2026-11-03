package com.kmkbe.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regresi test untuk security issue #11 (SECURITY.md):
 * path traversal pada FileStorageService. Path yang lolos keluar storage
 * root (absolute path, "../", dst) harus ditolak; path relative valid
 * tetap berfungsi seperti biasa agar alur web tidak terdampak.
 */
class FileStorageServicePathTraversalTest {

  @TempDir
  Path tempRoot;

  private FileStorageService service;

  @BeforeEach
  void setUp() {
    service = new FileStorageService(null, tempRoot.toString());
  }

  private byte[] pdfBytes() {
    return "%PDF-1.4 test traversal".getBytes(StandardCharsets.UTF_8);
  }

  @Test
  void loadRejectsPathEscapingRoot() {
    assertThatThrownBy(() -> service.load("../outside.txt"))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Invalid file path");
  }

  @Test
  void loadRejectsDeepTraversal() {
    assertThatThrownBy(() -> service.load("../../../etc/passwd"))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Invalid file path");
  }

  @Test
  void loadRejectsAbsoluteFilesystemPath() {
    assertThatThrownBy(() -> service.load("/etc/passwd"))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Invalid file path");
  }

  @Test
  void loadRejectsBlankPath() {
    assertThatThrownBy(() -> service.load("  "))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Invalid file path");
  }

  @Test
  void loadAcceptsValidRelativePath() throws Exception {
    Files.createDirectories(tempRoot.resolve("docs/agreement"));
    Files.write(tempRoot.resolve("docs/agreement/sample.pdf"), pdfBytes());

    assertThat(service.load("docs/agreement/sample.pdf").isReadable()).isTrue();
  }

  @Test
  void saveRejectsMaliciousUploadDirOutsideRoot() {
    assertThatThrownBy(() -> service.save(
      new org.springframework.mock.web.MockMultipartFile(
        "file", "x.pdf", "application/pdf", pdfBytes()),
      "../../evil", "x.pdf"))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void saveRejectsAbsoluteUploadDir() {
    assertThatThrownBy(() -> service.save(
      new org.springframework.mock.web.MockMultipartFile(
        "file", "x.pdf", "application/pdf", pdfBytes()),
      tempRoot.getParent().resolve("elsewhere").toString(), "x.pdf"))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void saveWithMaliciousUploadDirDoesNotWriteOutsideRoot() {
    Path outside = tempRoot.getParent().resolve("evil-outside");
    try {
      service.save(
        new org.springframework.mock.web.MockMultipartFile(
          "file", "x.pdf", "application/pdf", pdfBytes()),
        "../evil-outside", "x.pdf");
    } catch (Exception ignored) {
      // diharapkan ditolak
    }
    assertThat(Files.exists(outside)).isFalse();
  }

  @Test
  void deleteRejectsTraversalAndDoesNotDeleteTargetOutsideRoot() throws Exception {
    Path outsideFile = tempRoot.getParent().resolve("victim.txt");
    Files.writeString(outsideFile, "rahasia");

    assertThatThrownBy(() -> service.delete("../../victim.txt", ""))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Invalid file path");

    assertThat(Files.exists(outsideFile)).isTrue();
  }

  @Test
  void downloadUploadFileRejectsTraversal() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    assertThatThrownBy(() -> service.downloadUploadFile(request, "../../etc/passwd", "passwd", "inline"))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void downloadUploadFileAcceptsValidRelativePath() throws Exception {
    Files.createDirectories(tempRoot.resolve("docs"));
    Files.write(tempRoot.resolve("docs/report.pdf"), pdfBytes());

    MockHttpServletRequest request = new MockHttpServletRequest();
    var response = service.downloadUploadFile(request, "docs/report.pdf", "report.pdf", "attachment");

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
  }
}
