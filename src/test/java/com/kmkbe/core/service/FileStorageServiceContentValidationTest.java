package com.kmkbe.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regresi test untuk security issue #15 (SECURITY.md):
 * validasi upload tidak boleh hanya mengandalkan extension,
 * isi file (magic bytes) juga harus divalidasi.
 */
@ExtendWith(MockitoExtension.class)
class FileStorageServiceContentValidationTest {

  private FileStorageService service;

  @BeforeEach
  void setUp() {
    service = new FileStorageService(null, "target/test-uploads");
  }

  private static final byte[] PDF_BYTES = "%PDF-1.4 dummy content".getBytes();
  private static final byte[] JPEG_BYTES = new byte[]{
    (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10
  };
  private static final byte[] PNG_BYTES = new byte[]{
    (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A
  };
  private static final byte[] DOC_BYTES = new byte[]{
    (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
  };
  // HTML dengan script - polyglot klasik yang dikirim dengan extension palsu
  private static final byte[] HTML_BYTES = "<html><script>alert(1)</script></html>".getBytes();
  private static final byte[] EXE_BYTES = new byte[]{
    'M', 'Z', (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00
  };

  private MockMultipartFile file(String name, String originalFilename, byte[] content) {
    return new MockMultipartFile(name, originalFilename, "application/octet-stream", content);
  }

  @Test
  void validPdfContentWithPdfExtensionIsAccepted() throws Exception {
    String path = service.save(file("file", "surat.pdf", PDF_BYTES), "docs", "surat.pdf");
    assertThat(path).startsWith("docs/").endsWith(".pdf");
  }

  @Test
  void validJpegContentWithJpgExtensionIsAccepted() throws Exception {
    String path = service.save(file("file", "foto.jpg", JPEG_BYTES), "docs", "foto.jpg");
    assertThat(path).startsWith("docs/").endsWith(".jpg");
  }

  @Test
  void validJpegContentWithJpegExtensionIsAccepted() throws Exception {
    String path = service.save(file("file", "foto.jpeg", JPEG_BYTES), "docs", "foto.jpeg");
    assertThat(path).startsWith("docs/").endsWith(".jpeg");
  }

  @Test
  void validPngContentWithPngExtensionIsAccepted() throws Exception {
    String path = service.save(file("file", "logo.png", PNG_BYTES), "docs", "logo.png");
    assertThat(path).startsWith("docs/").endsWith(".png");
  }

  @Test
  void validDocContentWithDocExtensionIsAccepted() throws Exception {
    String path = service.save(file("file", "akta.doc", DOC_BYTES), "docs", "akta.doc");
    assertThat(path).startsWith("docs/").endsWith(".doc");
  }

  @Test
  void htmlContentWithPdfExtensionIsRejected() {
    assertThatThrownBy(() -> service.save(file("file", "xss.pdf", HTML_BYTES), "docs", "xss.pdf"))
      .isInstanceOf(Exception.class)
      .hasMessageContaining("does not match the declared extension: pdf");
  }

  @Test
  void htmlContentWithJpgExtensionIsRejected() {
    assertThatThrownBy(() -> service.save(file("file", "xss.jpg", HTML_BYTES), "docs", "xss.jpg"))
      .isInstanceOf(Exception.class)
      .hasMessageContaining("does not match the declared extension: jpg");
  }

  @Test
  void htmlContentWithPngExtensionIsRejected() {
    assertThatThrownBy(() -> service.save(file("file", "xss.png", HTML_BYTES), "docs", "xss.png"))
      .isInstanceOf(Exception.class)
      .hasMessageContaining("does not match the declared extension: png");
  }

  @Test
  void exeContentWithDocExtensionIsRejected() {
    assertThatThrownBy(() -> service.save(file("file", "malware.doc", EXE_BYTES), "docs", "malware.doc"))
      .isInstanceOf(Exception.class)
      .hasMessageContaining("does not match the declared extension: doc");
  }

  @Test
  void textContentWithPdfExtensionIsRejected() {
    byte[] text = "bukan pdf, hanya teks biasa".getBytes();
    assertThatThrownBy(() -> service.save(file("file", "fake.pdf", text), "docs", "fake.pdf"))
      .isInstanceOf(Exception.class)
      .hasMessageContaining("does not match the declared extension: pdf");
  }

  @Test
  void pdfBytesWithJpgExtensionIsRejected() {
    assertThatThrownBy(() -> service.save(file("file", "mismatch.jpg", PDF_BYTES), "docs", "mismatch.jpg"))
      .isInstanceOf(Exception.class)
      .hasMessageContaining("does not match the declared extension: jpg");
  }

  @Test
  void tooSmallFileIsRejected() {
    byte[] tiny = new byte[]{0x25, 0x50}; // "%P" saja, kurang dari 4 byte
    assertThatThrownBy(() -> service.save(file("file", "tiny.pdf", tiny), "docs", "tiny.pdf"))
      .isInstanceOf(Exception.class)
      .hasMessageContaining("too small or empty");
  }

  @Test
  void disallowedExtensionStillRejectedBeforeContentCheck() {
    // zip punya magic bytes PK, tapi tidak ada di whitelist extension
    byte[] zip = new byte[]{'P', 'K', 0x03, 0x04, 0x00, 0x00};
    assertThatThrownBy(() -> service.save(file("file", "arsip.zip", zip), "docs", "arsip.zip"))
      .isInstanceOf(Exception.class)
      .hasMessageContaining("Allowed extensions");
  }
}
