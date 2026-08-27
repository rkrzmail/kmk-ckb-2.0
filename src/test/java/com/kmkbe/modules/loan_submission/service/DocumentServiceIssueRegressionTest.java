package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.LegalFileDto;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceIssueRegressionTest {

  @Mock private AgreementFileRepository agreementFileRepository;
  @Mock private MstFileTypeRepository mstFileTypeRepository;
  @Mock private LegalFileRepository legalFileRepository;
  @Mock private FileStorageService fileStorageService;
  @Mock private LegalFileService legalFileService;
  @Mock private CustomerRemoteService customerRemoteService;
  @Mock private CustomerRepository customerRepository;
  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private AuditTrailService auditTrailService;

  private DocumentService service;

  @BeforeEach
  void setUp() {
    service = new DocumentService(
      agreementFileRepository,
      mstFileTypeRepository,
      legalFileRepository,
      fileStorageService,
      legalFileService,
      customerRemoteService,
      customerRepository,
      financingHdrRepository,
      auditTrailService
    );
  }

  @Test
  void uploadLoanDocumentUsesOriginalFilenameForDisplayAndStorageRequest() throws Exception {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .build();
    MstFileType fileType = MstFileType.builder()
      .fileTypeCode("DOC001")
      .fileTypeName("NPWP")
      .build();
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "invoice asli.pdf",
      "application/pdf",
      "content".getBytes()
    );
    LegalFileDto dto = new LegalFileDto();
    dto.setFileId(99L);
    dto.setFileName("invoice asli.pdf");

    when(mstFileTypeRepository.findByFileTypeCode("DOC001")).thenReturn(Optional.of(fileType));
    when(legalFileService.fetchByMstFileTypeAndCust(customer, fileType)).thenReturn(null);
    when(fileStorageService.save(eq(file), eq(customer.getCustCode() + "/loan_submission"), eq("invoice asli.pdf"), isNull()))
      .thenReturn(customer.getCustCode() + "/loan_submission");
    when(legalFileService.create(any(), eq(customer), eq(fileType), eq(file), anyString(), eq("invoice asli.pdf")))
      .thenReturn(dto);

    service.uploadLoanDocument(new MockHttpServletRequest(), customer, file, "DOC001");

    verify(fileStorageService).save(eq(file), eq(customer.getCustCode() + "/loan_submission"), eq("invoice asli.pdf"), isNull());
    verify(legalFileService).create(any(), eq(customer), eq(fileType), eq(file), anyString(), eq("invoice asli.pdf"));
  }
}
