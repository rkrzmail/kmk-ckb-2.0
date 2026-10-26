package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.LegalFileDto;
import com.kmkbe.core.domain.dto.MstFileTypeDto;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.DokumentHistoryUpload;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

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
  @Mock private DokumentHistoryUploadRepository dokumentHistoryUploadRepository;
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
      dokumentHistoryUploadRepository,
      auditTrailService
    );
  }

  @Test
  void uploadLoanDocumentUsesOriginalFilenameForDisplayAndStorageRequest() throws Exception {
    String uuid = String.valueOf(UUID.randomUUID());
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .bouwheer(uuid)
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

    when(mstFileTypeRepository.findByFileTypeCodeAndBouwheerCode("DOC001", UUID.fromString(uuid))).thenReturn(Optional.of(fileType));
    when(legalFileService.fetchByMstFileTypeAndCust(customer, fileType)).thenReturn(null);
    when(fileStorageService.save(eq(file), eq(customer.getCustCode() + "/loan_submission"), eq("invoice asli.pdf"), isNull()))
      .thenReturn(customer.getCustCode() + "/loan_submission");
    when(legalFileService.create(any(), eq(customer), eq(fileType), eq(file), anyString(), eq("invoice asli.pdf")))
      .thenReturn(dto);

    service.uploadLoanDocument(new MockHttpServletRequest(), customer, file, "DOC001");

    verify(fileStorageService).save(eq(file), eq(customer.getCustCode() + "/loan_submission"), eq("invoice asli.pdf"), isNull());
    verify(legalFileService).create(any(), eq(customer), eq(fileType), eq(file), anyString(), eq("invoice asli.pdf"));
  }

  @Test
  void documentRequirementFiltersByDocumentTypeDescription() {
    UUID bouwheerCode = UUID.randomUUID();
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .bouwheer(bouwheerCode.toString())
      .build();
    MstFileType ktp = MstFileType.builder()
      .fileTypeCode("KTP01")
      .fileTypeName("KTP.pdf")
      .fileTypeDesc("KTP / Identitas Pengurus")
      .build();
    MstFileType npwp = MstFileType.builder()
      .fileTypeCode("NPWP01")
      .fileTypeName("NPWP.pdf")
      .fileTypeDesc("NPWP Perusahaan")
      .build();
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    request.setSearchBy("fileTypeDesc");
    request.setSearchValue("KTP");
    MockHttpServletRequest httpRequest = new MockHttpServletRequest();
    httpRequest.setParameter("owner", "debitur");

    when(mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(
      List.of("Legal", "Financing"),
      bouwheerCode
    )).thenReturn(List.of(ktp, npwp));
    when(legalFileService.fetchByMstFileTypeAndCust(eq(customer), any(MstFileType.class))).thenReturn(null);

    PaginationResult<MstFileTypeDto> result = service.fetchAllLoanDocumentRequirement(
      httpRequest,
      customer,
      request,
      false
    );

    assertThat(result.getTotalData()).isEqualTo(1);
    assertThat(result.getList())
      .singleElement()
      .extracting(MstFileTypeDto::getFileTypeCode)
      .isEqualTo("KTP01");
  }

  @Test
  void sitDocumentUsesActiveLegalFileForLeadNotCustomerFile() {
    UUID code = UUID.randomUUID();
    UUID bouwheerCode = UUID.randomUUID();
    Customer customer = Customer.builder().custCode(UUID.randomUUID()).build();
    var bouwheer = com.kmkbe.modules.bouwheer.model.entity.Bouwheer.builder()
      .bouwheerCode(bouwheerCode).build();
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(code);
    hdr.setCustomer(customer);
    hdr.setBouwheer(bouwheer);
    MstFileType sit = MstFileType.builder().fileTypeCode("DOC006").build();
    LegalFile active = new LegalFile();
    active.setFileId(12L);
    active.setFileName("signed.pdf");
    active.setDtmUpd(java.time.LocalDateTime.of(2026, 10, 25, 12, 0));
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    when(mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(
      any(), eq(bouwheerCode), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sit)));
    when(legalFileRepository.findByFinancingHdr_FinancingHdrCodeAndFileTypeCode_FileTypeCode(code, "DOC006"))
      .thenReturn(Optional.of(active));

    var result = service.fetchAllLoanDocumentDebitur(new MockHttpServletRequest(), new PaginationRequest(), code.toString());

    assertThat(result.getList().get(0).getLegalFile().getFileUrl())
      .isEqualTo("/api/v1/document-uploads/" + code + "/DOC006/latest/download");
    verify(dokumentHistoryUploadRepository, never())
      .findTopByFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCodeOrderByUploadedAtDescIdDesc(code, "DOC006");
    verify(legalFileService, never()).fetchByMstFileTypeAndCust(customer, sit);
  }
}
