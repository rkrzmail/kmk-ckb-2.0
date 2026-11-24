package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.ExternalIntegrationLoanSimulationDto;
import com.kmkbe.core.domain.dto.LoanSubmissionSessionDto;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.entity.SimulationHist;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.domain.repository.SimulationHistRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.product.model.entity.Product;
import com.kmkbe.modules.product.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanSubmissionSmallServicesCoverageTest {
  @Mock ProductRepository productRepository;
  @Mock MstFileTypeRepository mstFileTypeRepository;
  @Mock SimulationHistRepository simulationHistRepository;
  @Mock JdbcTemplate jdbcTemplate;
  @Mock LegalFileRepository legalFileRepository;
  @Mock FileStorageService fileStorageService;

  @Test
  void productServiceBranches() {
    ProductService service = new ProductService(productRepository);
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(0);
    request.setPageSize(10);
    Product product = new Product();
    product.setProductId(1L);
    product.setProductName("Product");
    when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));
    assertThat(service.fetchAll(request).getList()).hasSize(1);

    when(productRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.fetchAll(request)).hasMessage("db down");

    when(productRepository.getAllActive()).thenReturn(Optional.of(List.of(product)));
    assertThat(service.fetchAllActive()).hasSize(1);
    when(productRepository.getAllActive()).thenReturn(Optional.empty());
    assertThat(service.fetchAllActive()).isEmpty();
    when(productRepository.getAllActive()).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.fetchAllActive()).hasMessage("db down");
  }

  @Test
  void mstFileTypeServiceDelegates() {
    MstFileTypeService service = new MstFileTypeService(mstFileTypeRepository);
    when(mstFileTypeRepository.findAll()).thenReturn(List.of(new MstFileType()));
    when(mstFileTypeRepository.findAllMandatory()).thenReturn(List.of(new MstFileType()));
    assertThat(service.getAll()).hasSize(1);
    assertThat(service.getAllMandatory()).hasSize(1);
    assertThatCode(() -> {
      service.getByCode();
      service.create();
      service.update();
      service.delete();
    }).doesNotThrowAnyException();
  }

  @Test
  void simulationHistoryServiceBranches() {
    SimulationHistoryService service = new SimulationHistoryService(simulationHistRepository);
    SimulationHist hist = SimulationHist.builder().simulationHistId(1L).build();
    when(simulationHistRepository.findFirstByFinancingHdrOrderBySimulationHistIdDesc(any()))
      .thenReturn(Optional.empty(), Optional.of(hist));
    assertThat(service.findLastBy(null)).isNull();
    assertThat(service.findLastBy(null)).isNotNull();
    when(simulationHistRepository.findFirstByFinancingHdrOrderBySimulationHistIdDesc(any()))
      .thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.findLastBy(null)).hasMessage("db down");

    when(simulationHistRepository.findAllByFinancingHdr(any()))
      .thenReturn(Optional.empty(), Optional.of(List.of(hist)));
    assertThat(service.findAllBy(null)).isEmpty();
    assertThat(service.findAllBy(null)).hasSize(1);
    when(simulationHistRepository.findAllByFinancingHdr(any())).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.findAllBy(null)).hasMessage("db down");

    Customer customer = new Customer();
    customer.setCustName("Customer");
    when(simulationHistRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    assertThat(service.create(customer, null, 1, 2, 3, 4).getFinancingAmt()).isEqualTo(4);
    when(simulationHistRepository.save(any())).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.create(customer, null, 1, 2, 3, 4)).hasMessage("db down");
  }

  @Test
  void sessionServiceInsertUpdateAndQuery() throws Exception {
    SessionLoanSubmissionService service = new SessionLoanSubmissionService(jdbcTemplate);
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    LoanSubmissionSessionDto session = LoanSubmissionSessionDto.builder().lastStep(1).build();

    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(customer.getCustCode())))
      .thenThrow(new EmptyResultDataAccessException(1))
      .thenReturn(session, session);
    when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);
    when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);
    assertThat(service.create(customer, 1, new Object()).getLastStep()).isEqualTo(1);
    assertThat(service.create(customer, 2, new Object()).getLastStep()).isEqualTo(1);

    stubSessionQuery(customer.getCustCode(), session);
    assertThat(service.findLastByCust(customer)).isPresent();
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(customer.getCustCode())))
      .thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.findLastByCust(customer)).hasMessage("db down");
  }

  @Test
  void importantNotesServiceInsertUpdateAndQuery() throws Exception {
    ImportantNotesService service = new ImportantNotesService(jdbcTemplate);
    assertThat(service.importantNotesDto().getLegals()).isNotEmpty();

    ExternalIntegrationLoanSimulationDto dto = ExternalIntegrationLoanSimulationDto.builder()
      .bouwheerCode("B1").build();
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("B1"), eq("V1")))
      .thenThrow(new EmptyResultDataAccessException(1))
      .thenReturn(Optional.of(dto).get(), dto);
    when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);
    var remote = com.kmkbe.core.domain.dto.InquiryVendorRemoteDto.builder()
      .vendorName("Vendor").email("e@x.com").phone("0812").npwp("123").build();
    assertThat(service.create(remote, "B1", "V1").getVendor()).isNotBlank();
    assertThat(service.create(remote, "B1", "V1").getVendor()).isNotBlank();

    assertThat(service.generateVendor(remote, "V1", "Perusahaan").getName()).isEqualTo("Vendor");
    stubNotesQuery();
    assertThat(service.findExternalIntegrationByBouwheerCode("B1", "V1")).isPresent();
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("B1"), eq("V1")))
      .thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.findExternalIntegrationByBouwheerCode("B1", "V1"))
      .hasMessage("db down");
  }

  @Test
  void legalFileServiceCrudAndUrlBranches() throws Exception {
    LegalFileService service = new LegalFileService(legalFileRepository, fileStorageService);
    Customer customer = new Customer();
    customer.setCustName("Customer");
    MstFileType fileType = new MstFileType();
    fileType.setFileTypeCode("DOC001");
    MultipartFile file = mock(MultipartFile.class);
    when(file.getContentType()).thenReturn("application/pdf");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
    request.setRequestURI("/api/v1/loan-submissions/documents/requirement/upload");

    LegalFile existing = new LegalFile();
    existing.setFileId(1L);
    when(legalFileRepository.findByCustCodeAndFileTypeCodeAndFinancingHdrIsNull(customer, fileType))
      .thenReturn(Optional.of(existing), Optional.empty());
    assertThat(service.create(request, customer, fileType, file, "/tmp", "file.pdf").getFileUrl())
      .contains("https://localhost:8080");

    MstFileType noUrlType = new MstFileType();
    noUrlType.setFileTypeCode("DOC005");
    when(legalFileRepository.findByCustCodeAndFileTypeCodeAndFinancingHdrIsNull(customer, noUrlType))
      .thenReturn(Optional.empty());
    assertThat(service.create(request, customer, noUrlType, file, "/tmp", "file.pdf").getFileUrl()).isNull();

    assertThatThrownBy(() -> service.create(request, customer, fileType, file, "", "file.pdf"))
      .hasMessageContaining("File path cannot be null");

    when(legalFileRepository.findByFileIdFirst(1L)).thenReturn(Optional.of(existing));
    assertThat(service.findByFileId(1L)).isSameAs(existing);
    when(legalFileRepository.findByFileIdFirst(2L)).thenReturn(Optional.empty());
    assertThat(service.findByFileId(2L)).isNull();
    assertThat(service.fetchByMstFileTypeAndCust(customer, fileType)).isNull();

    when(legalFileRepository.findById(1L)).thenReturn(Optional.of(existing));
    service.delete(1L);
    verify(fileStorageService).delete(existing.getFilePath(), existing.getFileName());
    when(legalFileRepository.findById(2L)).thenReturn(Optional.empty());
    service.delete(2L);
  }

  private void stubSessionQuery(UUID custCode, LoanSubmissionSessionDto session) {
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(custCode)))
      .thenAnswer(call -> {
        RowMapper<LoanSubmissionSessionDto> mapper = call.getArgument(1);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("last_step")).thenReturn(1);
        when(rs.getTimestamp("dtm_crt")).thenReturn(Timestamp.from(Instant.parse("2026-11-01T00:00:00Z")));
        when(rs.getTimestamp("dtm_upd")).thenReturn(null);
        when(rs.getString("session")).thenReturn("{\"step\":1}");
        return mapper.mapRow(rs, 0);
      });
  }

  private void stubNotesQuery() {
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("B1"), eq("V1")))
      .thenAnswer(call -> {
        RowMapper<ExternalIntegrationLoanSimulationDto> mapper = call.getArgument(1);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("bouwheer_code")).thenReturn("B1");
        when(rs.getBoolean("already_accept_important_notes")).thenReturn(true);
        when(rs.getTimestamp("dtm_crt")).thenReturn(Timestamp.from(Instant.parse("2026-11-01T00:00:00Z")));
        return mapper.mapRow(rs, 0);
      });
  }
}
