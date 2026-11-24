package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.DocumentTemplateFinancingDto;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.domain.dto.LegalFileDto;
import com.kmkbe.core.domain.dto.MstFileTypeDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.DokumentHistoryUploadRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceCoverageTest {
  @Mock AgreementFileRepository agreementFileRepository;
  @Mock MstFileTypeRepository mstFileTypeRepository;
  @Mock LegalFileRepository legalFileRepository;
  @Mock FileStorageService fileStorageService;
  @Mock LegalFileService legalFileService;
  @Mock CustomerRemoteService customerRemoteService;
  @Mock CustomerRepository customerRepository;
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock DokumentHistoryUploadRepository dokumentHistoryUploadRepository;
  @Mock AuditTrailService auditTrailService;
  @Mock CurrentUserService currentUserService;
  DocumentService service;

  @BeforeEach
  void setUp() {
    service = new DocumentService(agreementFileRepository, mstFileTypeRepository, legalFileRepository,
      fileStorageService, legalFileService, customerRemoteService, customerRepository,
      financingHdrRepository, dokumentHistoryUploadRepository, auditTrailService, currentUserService);
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void templatesAndRequirementDocuments() throws Exception {
    Customer customer = customer();
    assertThat(service.fetchDocumentTemplateFinancing(customer)).hasSize(2);

    MockHttpServletRequest request = request();
    request.setParameter("owner", "debitur");
    MstFileType fileType = fileType("DOC001");
    when(mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(anyList(), any()))
      .thenReturn(List.of(fileType));
    when(legalFileService.fetchByMstFileTypeAndCust(customer, fileType)).thenReturn(legalFile());
    assertThat(service.fetchAllLoanDocumentRequirement(request, customer, new PaginationRequest(), true).getList())
      .hasSize(1);

    request.setParameter("owner", "internal");
    MstFileType noUrlType = fileType("DOC005");
    when(mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(anyList(), any()))
      .thenReturn(List.of(noUrlType));
    when(legalFileService.fetchByMstFileTypeAndCust(customer, noUrlType)).thenReturn(legalFile());
    assertThat(service.fetchAllLoanDocumentRequirement(request, customer, new PaginationRequest(), false).getList())
      .hasSize(1);

    when(legalFileService.fetchByMstFileTypeAndCust(customer, noUrlType)).thenThrow(new RuntimeException("db down"));
    assertThat(service.fetchAllLoanDocumentRequirement(request, customer, new PaginationRequest(), false).getList())
      .hasSize(1);

    when(mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(anyList(), any()))
      .thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.fetchAllLoanDocumentRequirement(request, customer,
      new PaginationRequest(), false)).hasMessage("db down");
  }

  @Test
  void privatePaginationHelpers() {
    List<MstFileTypeDto> docs = List.of(fileTypeDto("DOC001", "Name", "Desc", "file.pdf"),
      fileTypeDto("DOC002", "Other", "Other", "other.pdf"));
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    assertThat((boolean) ReflectionTestUtils.invokeMethod(service, "hasDocumentQuery", request)).isFalse();

    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setSearchValue("name");
    request.setSearchBy("fileTypeName");
    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setSearchBy("legalFile.fileName");
    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setSearchValue(null);
    request.setSortBy("fileTypeCode");
    request.setSortType("desc");
    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setSortBy("fileTypeName");
    request.setSortType("asc");
    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setSortBy("fileTypeDesc");
    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setSortBy("legalFile.fileName");
    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setSortBy("legalFile.uploadedDate");
    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setSortBy("legalFile");
    assertThat((PaginationResult) ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isNotNull();

    request.setPageNo(0);
    assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "paginateDocuments", docs, request))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void uploadDeleteAndDownloads() throws Exception {
    Customer customer = customer();
    MstFileType fileType = fileType("DOC001");
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("file.pdf");
    when(file.getContentType()).thenReturn("application/pdf");
    when(mstFileTypeRepository.findByFileTypeCodeAndBouwheerCode(anyString(), any()))
      .thenReturn(Optional.of(fileType));
    when(fileStorageService.save(any(), anyString(), anyString(), any())).thenReturn("/tmp/file.pdf");
    LegalFileDto uploaded = new LegalFileDto();
    uploaded.setFileId(1L);
    uploaded.setFileName("file.pdf");
    when(legalFileService.create(any(), any(), any(), any(), anyString(), anyString())).thenReturn(uploaded);

    assertThat(service.uploadLoanDocument(request(), customer, file, "DOC001").getFileId()).isEqualTo(1L);
    when(mstFileTypeRepository.findByFileTypeCodeAndBouwheerCode(anyString(), any()))
      .thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.uploadLoanDocument(request(), customer, file, "DOC001"))
      .isInstanceOf(IllegalStateException.class);

    LegalFile legal = legalFile();
    legal.setCustCode(customer);
    when(legalFileService.findByFileId(1L)).thenReturn(legal);
    service.delete(1L);
    verify(legalFileService).delete(1L);

    setAuthentication(new UsernamePasswordAuthenticationToken(customer, null, List.of()));
    MockHttpServletRequest downloadRequest = request();
    downloadRequest.setParameter("cd", "attachment");
    when(fileStorageService.downloadUploadFile(any(), anyString(), anyString(), anyString()))
      .thenReturn(ResponseEntity.ok(new ByteArrayResource(new byte[]{1})));
    assertThat(service.documentByLegalFileId(downloadRequest, 1L).getStatusCode().value()).isEqualTo(200);

    legal.setFilePath("https://example.com/file.pdf");
    assertThat(service.documentByLegalFileId(downloadRequest, 1L).getBody()).isNull();
    when(legalFileService.findByFileId(2L)).thenReturn(null);
    assertThat(service.documentByLegalFileId(downloadRequest, 2L).getBody()).isNull();

    setAuthentication(new UsernamePasswordAuthenticationToken(new MstUser(), null, List.of()));
    assertThat(service.documentByLegalFileId(downloadRequest, 1L).getBody()).isNull();
    setAuthentication(null);
    assertThatThrownBy(() -> service.documentByLegalFileId(downloadRequest, 1L))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void agreementDownloads() {
    AgreementFile agreementFile = new AgreementFile();
    agreementFile.setAgreementFileId(1L);
    agreementFile.setFilePath("/tmp/file.pdf");
    agreementFile.setFileName("file.pdf");
    Agreement agreement = new Agreement();
    agreement.setAgreementCode("AGR-1");
    Cwr cwr = new Cwr();
    Customer owner = customer();
    cwr.setCustomer(owner);
    agreement.setCwr(cwr);
    agreementFile.setAgreement(agreement);
    when(agreementFileRepository.findById(1L)).thenReturn(Optional.of(agreementFile));
    when(agreementFileRepository.findById(2L)).thenReturn(Optional.empty());
    when(fileStorageService.downloadUploadFile(any(), anyString(), anyString(), anyString()))
      .thenReturn(ResponseEntity.ok(new ByteArrayResource(new byte[]{1})));

    setAuthentication(new UsernamePasswordAuthenticationToken(owner, null, List.of()));
    assertThat(service.agreementDocByAgreementId(request(), 1L).getStatusCode().value()).isEqualTo(200);
    assertThat(service.agreementDocByAgreementId(request(), 2L).getBody()).isNull();

    agreementFile.setFilePath("https://example.com/file.pdf");
    assertThat(service.agreementDocByAgreementId(request(), 1L).getBody()).isNull();

    setAuthentication(new UsernamePasswordAuthenticationToken(new MstUser(), null, List.of()));
    assertThat(service.agreementDocByAgreementId(request(), 1L).getBody()).isNull();
  }

  @Test
  void mappingFromInquiryVendorCreatesAndReusesFileTypes() {
    Customer customer = customer();
    InquiryVendorRemoteDto vendor = vendor();
    when(legalFileRepository.findAllRawByCustAndFileTypeCodeStr(anyString(), anyString()))
      .thenReturn(List.of(legalFile()));
    when(mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc(anyString(), any()))
      .thenReturn(Optional.empty());
    when(mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc(eq("IPS"), any()))
      .thenReturn(Optional.of(fileType("IPS")));

    service.mappingFromInquiryVendor(customer, vendor);
    verify(mstFileTypeRepository, org.mockito.Mockito.atLeastOnce()).save(any(MstFileType.class));
    verify(legalFileRepository, org.mockito.Mockito.atLeastOnce()).deleteAll(anyList());

    service.mappingFromInquiryVendor(customer, null);

    when(mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc(anyString(), any()))
      .thenReturn(Optional.of(fileType("DOC001")));
    service.mappingFromInquiryVendor(customer, vendor);
  }

  @Test
  void uploadedCustomerDocAndDebiturDocs() {
    Customer customer = customer();
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    when(legalFileRepository.findAllByCustCodeAndFinancingHdrIsNullOrderByFileName(any(), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(legalFile())));
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    assertThat(service.uploadedCustomerDoc(customer.getCustCode().toString(), request(), request).getList())
      .hasSize(1);

    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setBouwheer(new com.kmkbe.modules.bouwheer.model.entity.Bouwheer());
    hdr.getBouwheer().setBouwheerCode(UUID.randomUUID());
    hdr.setCustomer(customer);
    when(financingHdrRepository.findByFinancingHdrCode(hdr.getFinancingHdrCode())).thenReturn(Optional.of(hdr));
    when(mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(
      anyList(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(fileType("DOC001"))));
    assertThat(service.fetchAllLoanDocumentDebitur(request(), new PaginationRequest(),
      hdr.getFinancingHdrCode().toString()).getList()).hasSize(1);

    PaginationRequest custom = new PaginationRequest();
    custom.setSearchValue("Name");
    custom.setSearchBy("fileTypeName");
    when(mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(anyList(), any()))
      .thenReturn(List.of(fileType("DOC001")));
    assertThat(service.fetchAllLoanDocumentDebitur(request(), custom,
      hdr.getFinancingHdrCode().toString()).getList()).hasSize(1);

    custom.setPageNo(0);
    assertThatThrownBy(() -> service.fetchAllLoanDocumentDebitur(request(), custom,
      hdr.getFinancingHdrCode().toString())).isInstanceOf(RuntimeException.class);
  }

  private void setAuthentication(UsernamePasswordAuthenticationToken authentication) {
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private static MockHttpServletRequest request() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
    request.setRequestURI("/api/v1/documents/download/loan/1");
    return request;
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    customer.setCustName("Customer");
    customer.setBouwheer(UUID.randomUUID().toString());
    return customer;
  }

  private static MstFileType fileType(String code) {
    return MstFileType.builder().fileTypeCode(code).fileTypeName("Name").fileTypeDesc("Desc").build();
  }

  private static LegalFile legalFile() {
    LegalFile legal = new LegalFile();
    legal.setFileId(1L);
    legal.setFileName("file.pdf");
    legal.setFilePath("/tmp/file.pdf");
    legal.setContentType("application/pdf");
    legal.setCustCode(customer());
    legal.setFileTypeCode(fileType("DOC001"));
    legal.setDtmUpd(LocalDateTime.now());
    return legal;
  }

  private static MstFileTypeDto fileTypeDto(String code, String name, String desc, String fileName) {
    MstFileTypeDto dto = new MstFileTypeDto();
    dto.setFileTypeCode(code);
    dto.setFileTypeName(name);
    dto.setFileTypeDesc(desc);
    LegalFileDto legal = new LegalFileDto();
    legal.setFileName(fileName);
    legal.setUploadedDate(LocalDateTime.now());
    dto.setLegalFile(legal);
    return dto;
  }

  private static InquiryVendorRemoteDto vendor() {
    return InquiryVendorRemoteDto.builder()
      .aktaPendirianLink("a").aktaPerubahanLink("b").pengesahanKemenkumhamLink("c")
      .npwp("npwp").npwpLink("d").nipSiup("nib").nipSiupLink("e")
      .pkpNumber("pkp").pkpLink("f").ktpNpwpVendorStockId("id").ktpNpwpVendorStockLink("g")
      .laporanKeuanganLink("h").ktpDirectur("ktp").ktpDirekturLink("i")
      .bankDetail(List.of(InquiryVendorRemoteDto.BankDetail.builder().docLink("bank").build()))
      .otherDocument(List.of(InquiryVendorRemoteDto.OtherDocument.builder()
        .documentName("Other").documentNo("1").documentUrl("url").build()))
      .build();
  }
}
