package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.modules.branch_admin.response.DocumentUploadResponse;
import com.kmkbe.modules.branch_admin.service.DocumentUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentUploadControllerTest {
  @Mock DocumentUploadService service;
  MockMvc mvc;
  UUID code = UUID.randomUUID();
  DocumentUploadResponse resp = new DocumentUploadResponse(1L, code, "DOC006", "f.pdf", LocalDate.of(2027,1,1), "n", "u", LocalDateTime.now());

  @BeforeEach void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(new DocumentUploadController(service))
      .setMessageConverters(new MappingJackson2HttpMessageConverter(), new org.springframework.http.converter.ResourceHttpMessageConverter()).build();
  }

  @Test
  void endpoints() throws Exception {
    when(service.upload(eq(code), eq("DOC006"), any(), any(), any())).thenReturn(resp);
    when(service.latest(code, "DOC006")).thenReturn(resp);
    when(service.history(code, "DOC006")).thenReturn(List.of(resp));
    when(service.downloadLatest(eq(code), eq("DOC006"), any())).thenReturn(ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM).body(new ByteArrayResource("x".getBytes())));
    when(service.download(eq(code), eq("DOC006"), eq(1L), any())).thenReturn(ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM).body(new ByteArrayResource("x".getBytes())));

    mvc.perform(multipart("/api/v1/document-uploads/{c}/{t}", code, "DOC006")
      .file(new MockMultipartFile("file","f.pdf","application/pdf","%PDF".getBytes()))
      .param("expiredDate","2027-01-01").param("notes","n")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/document-uploads/{c}/{t}/latest", code, "DOC006")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/document-uploads/{c}/{t}/history", code, "DOC006")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/document-uploads/{c}/{t}/latest/download", code, "DOC006")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/document-uploads/{c}/{t}/1/download", code, "DOC006")).andExpect(status().isOk());
  }
}
