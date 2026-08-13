package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.BranchDto;
import com.kmkbe.core.domain.dto.AppFactoringResponse;
import com.kmkbe.core.domain.dto.AppResponse;
import com.kmkbe.core.domain.dto.CwrBwhrResponse;
import com.kmkbe.core.domain.dto.CwrListBwhrResponse;
import com.kmkbe.core.domain.dto.ExternalSigningRequest;
import com.kmkbe.core.domain.dto.ExternalSigningResponse;
import com.kmkbe.core.domain.dto.FinancialDataResponse;
import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import com.kmkbe.core.domain.dto.ProyeksiReportDto;
import com.kmkbe.core.domain.dto.RekDebiturResponse;
import com.kmkbe.core.domain.dto.ReportDueDateDto;
import com.kmkbe.core.domain.dto.SigningResponse;
import com.kmkbe.core.domain.dto.SitDto;
import com.kmkbe.core.domain.dto.SummaryByAODto;
import com.kmkbe.core.domain.dto.SummaryByBranchDto;
import com.kmkbe.core.domain.dto.SummaryDetailDto;
import com.kmkbe.core.domain.dto.VisitorDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.CsulSignerRepository;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.VisitorRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.ExternalApiService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.major_account.service.MstBranchService;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  private static final UUID FINANCING_HDR_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private CsulSignerRepository csulSignerRepository;
  @Mock private AgreementCodeService agreementCodeService;
  @Mock private AgreementFileSigningRepository agreementFileSigningRepository;
  @Mock private VisitorRepository visitorRepository;
  @Mock private CwrRepository cwrRepository;
  @Mock private MstBranchService mstBranchService;
  @Mock private AuthRemoteService authRemoteService;
  @Mock private EmailAo emailAo;
  @Mock private InvoiceService invoiceService;
  @Mock private AgreementRepository agreementRepo;
  @Mock private ExternalApiService externalApiService;
  @Mock private FinancingHdrService financingHdrService;
  @Mock private DebtorRepository debtorRepository;
  @Mock private JasperReportRenderer jasperReportRenderer;
  @Mock private SigningClient signingClient;
  @Mock private AgreementFileSigningService agreementFileSigningService;

  private ReportService service;

  @BeforeEach
  void setUp() {
    service = new ReportService();
    injectDependencies(service);
  }

  @Test
  void getVisitorReportMapsRepositoryPage() {
    PaginationRequest request = reportRequest();
    VisitorDto row = VisitorDto.builder()
        .debtorName("Debtor")
        .debtorStatus("NEW")
        .bouwheerName("Bouwheer")
        .periodStart(LocalDateTime.of(2026, 1, 1, 0, 0))
        .periodEnd(LocalDateTime.of(2026, 1, 31, 0, 0))
        .countVisit(2L)
        .build();
    when(visitorRepository.getDebtorVisitStats(any(), eq("2026-01-01"), eq("2026-01-31")))
        .thenReturn(new PageImpl<>(List.of(row)));

    PaginationResult<VisitorDto> result = service.getVisitorReport(request);

    assertThat(result.getCurrentPage()).isEqualTo(1);
    assertThat(result.getList()).extracting(VisitorDto::getDebtorName).containsExactly("Debtor");
  }

  @Test
  void getProyeksiReportMapsRepositoryPage() {
    ProyeksiReportDto row = ProyeksiReportDto.builder()
        .debtorName("Debtor")
        .debtorStatus("ACTIVE")
        .bouwheerName("Bouwheer")
        .invoiceNo("INV001")
        .amountInvoice(100D)
        .amountFinancing(80D)
        .invoiceDueDate(LocalDateTime.of(2026, 2, 1, 0, 0))
        .effectiveDate(LocalDateTime.of(2026, 1, 1, 0, 0))
        .build();
    when(financingHdrRepository.findActiveCustomersWithInvoiceDetails(any(), eq("2026-01-01"), eq("2026-01-31")))
        .thenReturn(new PageImpl<>(List.of(row)));

    PaginationResult<ProyeksiReportDto> result = service.getProyeksiReport(reportRequest());

    assertThat(result.getList()).extracting(ProyeksiReportDto::getInvoiceNo).containsExactly("INV001");
  }

  @Test
  void getSummaryByBranchResolvesBranchNameAndFallsBackToUnknown() {
    when(mstBranchService.branchList(null)).thenReturn(List.of(BranchDto.builder().branchCode("JKT").branchName("Jakarta").build()));
    SummaryByBranchDto row = new SummaryByBranchDto("Debtor", "JKT", "NPWP", "Bouwheer", 1D, 2D, 3D, 4D);
    SummaryByBranchDto unknown = new SummaryByBranchDto("Other", "BDG", "NPWP2", "Bouwheer2", 5D, 6D, 7D, 8D);
    when(financingHdrRepository.findSummaryBranch(any(), eq("2026-01-01"), eq("2026-01-31")))
        .thenReturn(new PageImpl<>(List.of(row, unknown)));

    PaginationResult<SummaryByBranchDto> result = service.getSummaryByBranch(reportRequest());

    assertThat(result.getList()).extracting(SummaryByBranchDto::getBranchCode)
        .containsExactly("Jakarta", "Unknown Branch");
  }

  @Test
  void getAllReportBranchByAOUsesJwtBranchNameAndAoFallback() {
    stubJwt();
    when(mstBranchService.branchList(null)).thenReturn(List.of(BranchDto.builder().branchCode("JKT").branchName("Jakarta").build()));
    when(emailAo.getEmailByPosition("JKT", "AO/AM", "jwt")).thenReturn(List.of(Map.of("employeeName", "AO Name")));
    when(emailAo.getEmailByPosition("BDG", "AO/AM", "jwt")).thenReturn(List.of());
    Object[] known = {1D, 2D, "Customer", "Bouwheer", 3D, 4D, "JKT"};
    Object[] unknown = {5D, 6D, "Customer2", "Bouwheer2", 7D, 8D, "BDG"};
    when(financingHdrRepository.findFinancingDataByFinancingHdrCode(any(), eq("2026-01-01"), eq("2026-01-31")))
        .thenReturn(new PageImpl<>(List.of(known, unknown)));

    PaginationResult<SummaryByAODto> result = service.getAllReportBranchByAO(reportRequest());

    assertThat(result.getList()).extracting(SummaryByAODto::getAoName).containsExactly("AO Name", "N/A");
    assertThat(result.getList()).extracting(SummaryByAODto::getBranch).containsExactly("Jakarta", "Unknown Branch");
  }

  @Test
  void getSummaryDetailAndDueDateDetailMapRows() {
    stubJwt();
    when(mstBranchService.branchList(null)).thenReturn(List.of(BranchDto.builder().branchCode("JKT").branchName("Jakarta").build()));
    when(emailAo.getEmailByPosition("JKT", "AO/AM", "jwt")).thenReturn(List.of(Map.of("employeeName", "AO Name")));
    LocalDateTime now = LocalDateTime.of(2026, 1, 2, 3, 4);
    Object[] summary = {"Debtor", "NPWP", "NEW", "Bouwheer", "JKT", "CWR", "AGR", 1L, 2D, 3D, 4D, 5D, 6D, 7D, now, "LIVE", now, now, now, now};
    Object[] due = {"Debtor", "NPWP", "Bouwheer", "JKT", "AGR", now, 1L, 2D, 3D, 4D, 5D, 6D, now, now, "DUE"};
    when(financingHdrRepository.findSummaryByCustCode(any(), eq("2026-01-01"), eq("2026-01-31")))
        .thenReturn(new PageImpl<>(java.util.Collections.singletonList(summary)));
    when(financingHdrRepository.findDueDateReport(any(), eq("2026-01-01"), eq("2026-01-31")))
        .thenReturn(new PageImpl<>(java.util.Collections.singletonList(due)));

    PaginationResult<SummaryDetailDto> summaryResult = service.getSummaryDetail(reportRequest());
    PaginationResult<ReportDueDateDto> dueResult = service.getDueDateDetail(reportRequest());

    assertThat(summaryResult.getList().get(0).getAgreementCode()).isEqualTo("AGR");
    assertThat(summaryResult.getList().get(0).getAoName()).isEqualTo("AO Name");
    assertThat(dueResult.getList().get(0).getAgreementNo()).isEqualTo("AGR");
    assertThat(dueResult.getList().get(0).getEmployeeName()).isEqualTo("AO Name");
  }

  @Test
  void getSummaryDetailAndDueDateDetailUseAoFallbackWhenEmployeeListEmpty() {
    stubJwt();
    when(mstBranchService.branchList(null)).thenReturn(List.of(BranchDto.builder().branchCode("JKT").branchName("Jakarta").build()));
    when(emailAo.getEmailByPosition("JKT", "AO/AM", "jwt")).thenReturn(List.of());
    LocalDateTime now = LocalDateTime.of(2026, 1, 2, 3, 4);
    Object[] summary = {"Debtor", "NPWP", "NEW", "Bouwheer", "JKT", "CWR", "AGR", 1L, 2D, 3D, 4D, 5D, 6D, 7D, now, "LIVE", now, now, now, now};
    Object[] due = {"Debtor", "NPWP", "Bouwheer", "JKT", "AGR", now, 1L, 2D, 3D, 4D, 5D, 6D, now, now, "DUE"};
    when(financingHdrRepository.findSummaryByCustCode(any(), eq("2026-01-01"), eq("2026-01-31")))
        .thenReturn(new PageImpl<>(java.util.Collections.singletonList(summary)));
    when(financingHdrRepository.findDueDateReport(any(), eq("2026-01-01"), eq("2026-01-31")))
        .thenReturn(new PageImpl<>(java.util.Collections.singletonList(due)));

    assertThat(service.getSummaryDetail(reportRequest()).getList().get(0).getAoName()).isEqualTo("N/A");
    assertThat(service.getDueDateDetail(reportRequest()).getList().get(0).getEmployeeName()).isEqualTo("N/A");
  }

  @Test
  void reportQueriesRethrowOrWrapRepositoryFailures() {
    when(visitorRepository.getDebtorVisitStats(any(), any(), any())).thenThrow(new IllegalStateException("visitor"));
    assertThatThrownBy(() -> service.getVisitorReport(reportRequest()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("visitor");

    when(financingHdrRepository.findActiveCustomersWithInvoiceDetails(any(), any(), any())).thenThrow(new IllegalStateException("proyeksi"));
    assertThatThrownBy(() -> service.getProyeksiReport(reportRequest()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("proyeksi");

    when(financingHdrRepository.findSummaryBranch(any(), any(), any())).thenThrow(new IllegalStateException("branch"));
    assertThatThrownBy(() -> service.getSummaryByBranch(reportRequest()))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Error fetching summary by branch");

    stubJwt();
    when(financingHdrRepository.findFinancingDataByFinancingHdrCode(any(), any(), any())).thenThrow(new IllegalStateException("ao"));
    assertThatThrownBy(() -> service.getAllReportBranchByAO(reportRequest()))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Error fetching report for all branches by AO");

    when(financingHdrRepository.findSummaryByCustCode(any(), any(), any())).thenThrow(new IllegalStateException("detail"));
    assertThatThrownBy(() -> service.getSummaryDetail(reportRequest()))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Error fetching summary details");

    when(financingHdrRepository.findDueDateReport(any(), any(), any())).thenThrow(new IllegalStateException("due"));
    assertThatThrownBy(() -> service.getDueDateDetail(reportRequest()))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Error fetching report due date");
  }

  @Test
  void reportQueriesUseDefaultPaginationWhenPageNoAndPageSizeAreNull() {
    PaginationRequest request = new PaginationRequest();
    request.setStartDate(java.sql.Date.valueOf("2026-01-01"));
    request.setEndDate(java.sql.Date.valueOf("2026-01-31"));
    when(visitorRepository.getDebtorVisitStats(any(), eq("2026-01-01"), eq("2026-01-31"))).thenReturn(new PageImpl<>(List.of()));
    when(financingHdrRepository.findActiveCustomersWithInvoiceDetails(any(), eq("2026-01-01"), eq("2026-01-31"))).thenReturn(new PageImpl<>(List.of()));
    when(financingHdrRepository.findSummaryBranch(any(), eq("2026-01-01"), eq("2026-01-31"))).thenReturn(new PageImpl<>(List.of()));
    stubJwt();
    when(financingHdrRepository.findFinancingDataByFinancingHdrCode(any(), eq("2026-01-01"), eq("2026-01-31"))).thenReturn(new PageImpl<>(List.of()));
    when(financingHdrRepository.findSummaryByCustCode(any(), eq("2026-01-01"), eq("2026-01-31"))).thenReturn(new PageImpl<>(List.of()));
    when(financingHdrRepository.findDueDateReport(any(), eq("2026-01-01"), eq("2026-01-31"))).thenReturn(new PageImpl<>(List.of()));

    assertThat(service.getVisitorReport(request).getList()).isEmpty();
    assertThat(service.getProyeksiReport(request).getList()).isEmpty();
    assertThat(service.getSummaryByBranch(request).getList()).isEmpty();
    assertThat(service.getAllReportBranchByAO(request).getList()).isEmpty();
    assertThat(service.getSummaryDetail(request).getList()).isEmpty();
    assertThat(service.getDueDateDetail(request).getList()).isEmpty();
  }

  @Test
  void sendDocumentForSigningSkipsAlreadySignedDocument() {
    when(agreementFileSigningRepository.findByAgreementCode("AGR001"))
        .thenReturn(List.of(AgreementFileSigning.builder().stamp("signed").build()));

    SigningResponse result = service.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker");

    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getMessage()).contains("sudah ditandatangani");
  }

  @Test
  void generateReportRendersPdfWithRemoteAndRepositoryData() throws Exception {
    Agreement agreement = agreement();
    agreement.setApplicationCode("APP001");
    agreement.setFinancingAmt(100D);
    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.of(csulSignerWithJabatan("ASM", "Area Sales Manager")));
    when(externalApiService.getAppByAppNo("APP001")).thenReturn(appResponse());
    when(externalApiService.getRekDebitur("APP001")).thenReturn(rekDebiturResponse());
    when(externalApiService.getAppFactoringData(123)).thenReturn(appFactoringResponse());
    when(agreementRepo.findAgreementCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("AGR-CODE"));
    when(externalApiService.getFinancialData("AGR-CODE")).thenReturn(financialDataResponse());
    when(agreementRepo.findCwrCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("CWR001"));
    when(externalApiService.getCwrBwhr("CWR001")).thenReturn(cwrBwhrResponse());
    when(agreementRepo.findCwrCodeAndDate(FINANCING_HDR_CODE, "AGR001"))
        .thenReturn(Optional.of(Map.of("cwr_code", "CWR001", "cwr_start_date", new Date(0))));
    when(externalApiService.getListCwrBwhr("CWR001", "BWHR001")).thenReturn(cwrListBwhrResponse());
    when(agreementRepo.findCustNameByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("Debtor"));
    when(debtorRepository.findKaryawanByFinancingHdrCode(FINANCING_HDR_CODE.toString())).thenReturn(List.of(reportDebtor()));
    when(agreementRepo.findFaciltyByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn("Factoring");
    when(agreementRepo.finddetailDebtor(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(Map.of(
        "cust_company_type", "PT",
        "cust_id_no", "NPWP001",
        "company_address", "Jl. Test",
        "cust_email", "debtor@example.com",
        "phone", "021"
    )));
    when(agreementCodeService.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE))
        .thenReturn(new CommonResult<SitDto>().success(sitDto()));
    FinancingHdr financingHdr = financingHdr();
    when(financingHdrService.findByCode(FINANCING_HDR_CODE.toString())).thenReturn(financingHdr);
    when(invoiceService.invoiceSubmissionByFinancingHdr(eq(financingHdr), any(PaginationRequest.class)))
        .thenReturn(PaginationResult.<PostedInvoiceDto>builder().list(List.of(postedInvoice())).build());
    when(jasperReportRenderer.renderToPdf(eq("/Reports/main_report.jasper"), any(Map.class))).thenReturn("pdf".getBytes());

    byte[] result = service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM");

    assertThat(result).isEqualTo("pdf".getBytes());
    ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
    verify(jasperReportRenderer).renderToPdf(eq("/Reports/main_report.jasper"), paramsCaptor.capture());
    assertThat(paramsCaptor.getValue()).containsEntry("NamaBranchManager", "BM");
    assertThat(paramsCaptor.getValue()).containsEntry("AgrmntNo", "AGR-CODE");
    assertThat(paramsCaptor.getValue()).containsKey("tableDataSource");
  }

  @Test
  void generateReportValidatesInputAndRequiredReferences() {
    assertThatThrownBy(() -> service.generateReport("bad-uuid", "AGR001", "BM", "ASM"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid financingHdrCode format");

    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM"))
        .isInstanceOf(java.util.NoSuchElementException.class)
        .hasMessageContaining("Data Agreement tidak ditemukan");

    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement()));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Branch Manager BM tidak ditemukan");

    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Area Sales Manager ASM tidak ditemukan");
  }

  @Test
  void generateReportUsesFallbackRowsWhenRemoteAndInvoiceDataEmpty() throws Exception {
    Agreement agreement = agreement();
    agreement.setApplicationCode("APP001");
    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.of(csulSignerWithJabatan("ASM", "Area Sales Manager")));
    when(externalApiService.getAppByAppNo("APP001")).thenThrow(new RuntimeException("app down"));
    when(externalApiService.getRekDebitur("APP001")).thenReturn(new RekDebiturResponse());
    when(externalApiService.getAppFactoringData(0)).thenThrow(new RuntimeException("factoring down"));
    when(agreementRepo.findAgreementCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("AGR-CODE"));
    when(externalApiService.getFinancialData("AGR-CODE")).thenReturn(financialDataWithoutPayload());
    when(agreementRepo.findCwrCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("CWR001"));
    when(externalApiService.getCwrBwhr("CWR001")).thenReturn(null);
    when(agreementRepo.findCwrCodeAndDate(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(Map.of("cwr_start_date", "not-date")));
    when(externalApiService.getListCwrBwhr("CWR001", "-")).thenReturn(new CwrListBwhrResponse(List.of()));
    when(agreementRepo.findCustNameByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("Debtor"));
    when(debtorRepository.findKaryawanByFinancingHdrCode(FINANCING_HDR_CODE.toString())).thenReturn(List.of());
    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(List.of());
    when(agreementRepo.findFaciltyByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn("Factoring");
    when(agreementRepo.finddetailDebtor(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(agreementCodeService.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(new CommonResult<SitDto>().success(sitDto()));
    FinancingHdr financingHdr = financingHdr();
    when(financingHdrService.findByCode(FINANCING_HDR_CODE.toString())).thenReturn(financingHdr);
    when(invoiceService.invoiceSubmissionByFinancingHdr(eq(financingHdr), any(PaginationRequest.class)))
        .thenReturn(PaginationResult.<PostedInvoiceDto>builder().list(List.of()).build());
    when(jasperReportRenderer.renderToPdf(eq("/Reports/main_report.jasper"), any(Map.class))).thenReturn("pdf".getBytes());

    byte[] result = service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM");

    assertThat(result).isEqualTo("pdf".getBytes());
  }

  @Test
  void generateReportThrowsWhenAgreementDataCommonResultFails() throws Exception {
    Agreement agreement = agreement();
    agreement.setApplicationCode("APP001");
    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.of(csulSignerWithJabatan("ASM", "Area Sales Manager")));
    when(externalApiService.getAppByAppNo("APP001")).thenReturn(appResponse());
    when(externalApiService.getRekDebitur("APP001")).thenReturn(rekDebiturResponse());
    when(externalApiService.getAppFactoringData(123)).thenReturn(appFactoringResponse());
    when(agreementRepo.findAgreementCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("AGR-CODE"));
    when(externalApiService.getFinancialData("AGR-CODE")).thenReturn(financialDataResponse());
    when(agreementRepo.findCwrCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("CWR001"));
    when(externalApiService.getCwrBwhr("CWR001")).thenReturn(cwrBwhrResponse());
    when(agreementRepo.findCwrCodeAndDate(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(externalApiService.getListCwrBwhr("CWR001", "BWHR001")).thenReturn(cwrListBwhrResponse());
    when(agreementRepo.findCustNameByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("Debtor"));
    when(debtorRepository.findKaryawanByFinancingHdrCode(FINANCING_HDR_CODE.toString())).thenReturn(List.of(reportDebtor()));
    when(agreementRepo.findFaciltyByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn("Factoring");
    when(agreementRepo.finddetailDebtor(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(agreementCodeService.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE))
        .thenReturn(new CommonResult<SitDto>().fail(500, "sit failed"));

    assertThatThrownBy(() -> service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to get agreement data: sit failed");
  }

  @Test
  void generateReportThrowsWhenAgreementDataFailsWithoutMessage() throws Exception {
    Agreement agreement = agreement();
    agreement.setApplicationCode("APP001");
    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.of(csulSignerWithJabatan("ASM", "Area Sales Manager")));
    when(externalApiService.getAppByAppNo("APP001")).thenReturn(appResponse());
    when(externalApiService.getRekDebitur("APP001")).thenReturn(rekDebiturResponse());
    when(externalApiService.getAppFactoringData(123)).thenReturn(appFactoringResponse());
    when(agreementRepo.findAgreementCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("AGR-CODE"));
    when(externalApiService.getFinancialData("AGR-CODE")).thenReturn(financialDataResponse());
    when(agreementRepo.findCwrCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("CWR001"));
    when(externalApiService.getCwrBwhr("CWR001")).thenReturn(cwrBwhrResponse());
    when(agreementRepo.findCwrCodeAndDate(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(externalApiService.getListCwrBwhr("CWR001", "BWHR001")).thenReturn(cwrListBwhrResponse());
    when(agreementRepo.findCustNameByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("Debtor"));
    when(debtorRepository.findKaryawanByFinancingHdrCode(FINANCING_HDR_CODE.toString())).thenReturn(List.of(reportDebtor()));
    when(agreementRepo.findFaciltyByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn("Factoring");
    when(agreementRepo.finddetailDebtor(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(agreementCodeService.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE))
        .thenReturn(new CommonResult<SitDto>().fail(500, null));

    assertThatThrownBy(() -> service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to get agreement data: ");
  }

  @Test
  void generateReportCoversNullInvoiceFieldsSignerFallbackAndDueDateFallback() throws Exception {
    Agreement agreement = agreement();
    agreement.setApplicationCode("APP001");
    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.of(csulSignerWithJabatan("ASM", "Area Sales Manager")));
    when(externalApiService.getAppByAppNo("APP001")).thenReturn(appResponse());
    when(externalApiService.getRekDebitur("APP001")).thenReturn(rekDebiturResponse());
    when(externalApiService.getAppFactoringData(123)).thenReturn(appFactoringResponse());
    when(agreementRepo.findAgreementCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("AGR-CODE"));
    when(externalApiService.getFinancialData("AGR-CODE")).thenReturn(financialDataCreditInsuranceOnly());
    when(agreementRepo.findCwrCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("CWR001"));
    when(externalApiService.getCwrBwhr("CWR001")).thenReturn(null);
    when(agreementRepo.findCwrCodeAndDate(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(externalApiService.getListCwrBwhr("CWR001", "-")).thenReturn(new CwrListBwhrResponse(List.of()));
    when(agreementRepo.findCustNameByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("Debtor"));
    when(debtorRepository.findKaryawanByFinancingHdrCode(FINANCING_HDR_CODE.toString())).thenReturn(List.of());
    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(List.of(reportDebtor()));
    when(agreementRepo.findFaciltyByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn("Factoring");
    when(agreementRepo.finddetailDebtor(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(agreementCodeService.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE))
        .thenReturn(new CommonResult<SitDto>().success(sitDtoWithoutDueDate()));
    FinancingHdr financingHdr = financingHdr();
    when(financingHdrService.findByCode(FINANCING_HDR_CODE.toString())).thenReturn(financingHdr);
    when(invoiceService.invoiceSubmissionByFinancingHdr(eq(financingHdr), any(PaginationRequest.class)))
        .thenReturn(PaginationResult.<PostedInvoiceDto>builder().list(List.of(postedInvoiceWithNullFields())).build());
    when(jasperReportRenderer.renderToPdf(eq("/Reports/main_report.jasper"), any(Map.class))).thenReturn("pdf".getBytes());

    assertThat(service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM")).isEqualTo("pdf".getBytes());
  }

  @Test
  void generateReportCoversAdditionalFallbackBranches() throws Exception {
    Agreement agreement = agreement();
    agreement.setApplicationCode("APP001");
    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.of(csulSignerWithJabatan("ASM", "Area Sales Manager")));
    when(externalApiService.getAppByAppNo("APP001")).thenReturn(appResponse());
    when(externalApiService.getRekDebitur("APP001")).thenReturn(null);
    when(externalApiService.getAppFactoringData(123)).thenReturn(appFactoringResponse());
    when(agreementRepo.findAgreementCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("AGR-CODE"));
    when(externalApiService.getFinancialData("AGR-CODE")).thenReturn(null);
    when(agreementRepo.findCwrCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(externalApiService.getCwrBwhr("-")).thenReturn(cwrBwhrResponseWithNullList());
    when(agreementRepo.findCwrCodeAndDate(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(externalApiService.getListCwrBwhr("-", "-")).thenReturn(new CwrListBwhrResponse(List.of()));
    when(agreementRepo.findCustNameByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(debtorRepository.findKaryawanByFinancingHdrCode(FINANCING_HDR_CODE.toString())).thenReturn(List.of());
    when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of(reportDebtor()));
    when(agreementRepo.findFaciltyByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn("Factoring");
    when(agreementRepo.finddetailDebtor(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(agreementCodeService.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE))
        .thenReturn(new CommonResult<SitDto>().success(sitDto()));
    FinancingHdr financingHdr = financingHdr();
    when(financingHdrService.findByCode(FINANCING_HDR_CODE.toString())).thenReturn(financingHdr);
    when(invoiceService.invoiceSubmissionByFinancingHdr(eq(financingHdr), any(PaginationRequest.class)))
        .thenReturn(PaginationResult.<PostedInvoiceDto>builder().list(null).build());
    when(jasperReportRenderer.renderToPdf(eq("/Reports/main_report.jasper"), any(Map.class))).thenReturn("pdf".getBytes());

    assertThat(service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM")).isEqualTo("pdf".getBytes());
  }

  @Test
  void generateReportCoversEmptyCollectionFallbackBranches() throws Exception {
    Agreement agreement = agreement();
    agreement.setApplicationCode("APP001");
    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.of(csulSignerWithJabatan("ASM", "Area Sales Manager")));
    when(externalApiService.getAppByAppNo("APP001")).thenReturn(appResponse());
    when(externalApiService.getRekDebitur("APP001")).thenReturn(rekDebiturResponseWithEmptyAccounts());
    when(externalApiService.getAppFactoringData(123)).thenReturn(appFactoringResponse());
    when(agreementRepo.findAgreementCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("AGR-CODE"));
    when(externalApiService.getFinancialData("AGR-CODE")).thenReturn(financialDataWithNullFeeList());
    when(agreementRepo.findCwrCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("CWR001"));
    when(externalApiService.getCwrBwhr("CWR001")).thenReturn(cwrBwhrResponseWithEmptyList());
    when(agreementRepo.findCwrCodeAndDate(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(externalApiService.getListCwrBwhr("CWR001", "-")).thenReturn(new CwrListBwhrResponse(List.of()));
    when(agreementRepo.findCustNameByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("Debtor"));
    when(debtorRepository.findKaryawanByFinancingHdrCode(FINANCING_HDR_CODE.toString())).thenReturn(List.of());
    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(List.of(reportDebtor()));
    when(agreementRepo.findFaciltyByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn("Factoring");
    when(agreementRepo.finddetailDebtor(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(agreementCodeService.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE))
        .thenReturn(new CommonResult<SitDto>().success(sitDto()));
    FinancingHdr financingHdr = financingHdr();
    when(financingHdrService.findByCode(FINANCING_HDR_CODE.toString())).thenReturn(financingHdr);
    when(invoiceService.invoiceSubmissionByFinancingHdr(eq(financingHdr), any(PaginationRequest.class))).thenReturn(null);
    when(jasperReportRenderer.renderToPdf(eq("/Reports/main_report.jasper"), any(Map.class))).thenReturn("pdf".getBytes());

    assertThat(service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM")).isEqualTo("pdf".getBytes());
  }

  @Test
  void generateReportThrowsWhenAgreementDataSuccessHasNullPayload() throws Exception {
    Agreement agreement = agreement();
    agreement.setApplicationCode("APP001");
    when(agreementRepo.findByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("BM", "Branch Manager")).thenReturn(Optional.of(csulSignerWithJabatan("BM", "Branch Manager")));
    when(csulSignerRepository.findByKaryawanNameAndJabatan("ASM", "Area Sales Manager")).thenReturn(Optional.of(csulSignerWithJabatan("ASM", "Area Sales Manager")));
    when(externalApiService.getAppByAppNo("APP001")).thenReturn(appResponse());
    when(externalApiService.getRekDebitur("APP001")).thenReturn(rekDebiturResponse());
    when(externalApiService.getAppFactoringData(123)).thenReturn(appFactoringResponse());
    when(agreementRepo.findAgreementCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("AGR-CODE"));
    when(externalApiService.getFinancialData("AGR-CODE")).thenReturn(financialDataResponse());
    when(agreementRepo.findCwrCodeByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("CWR001"));
    when(externalApiService.getCwrBwhr("CWR001")).thenReturn(cwrBwhrResponse());
    when(agreementRepo.findCwrCodeAndDate(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    when(externalApiService.getListCwrBwhr("CWR001", "BWHR001")).thenReturn(cwrListBwhrResponse());
    when(agreementRepo.findCustNameByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of("Debtor"));
    when(debtorRepository.findKaryawanByFinancingHdrCode(FINANCING_HDR_CODE.toString())).thenReturn(List.of(reportDebtor()));
    when(agreementRepo.findFaciltyByFinancingHdrCode(FINANCING_HDR_CODE, "AGR001")).thenReturn("Factoring");
    when(agreementRepo.finddetailDebtor(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.empty());
    CommonResult<SitDto> result = new CommonResult<>();
    result.setCode(200);
    result.setData(null);
    when(agreementCodeService.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(result);

    assertThatThrownBy(() -> service.generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to get agreement data: ");
  }

  @Test
  void sendDocumentForSigningSavesSuccessfulSigningResult() throws Exception {
    ReportService spy = org.mockito.Mockito.spy(new ReportService());
    injectDependencies(spy);
    when(agreementFileSigningRepository.findByAgreementCode("AGR001")).thenReturn(List.of());
    doReturn("pdf".getBytes()).when(spy).generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM");
    when(agreementRepo.findByAgreementCode("AGR001")).thenReturn(Optional.of(agreement()));
    when(cwrRepository.findByCwrCode("CWR001")).thenReturn(Optional.of(cwr("JKT")));
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(List.of(debtor()));
    when(csulSignerRepository.findByKaryawanName("BM")).thenReturn(Optional.of(csulSigner("BM")));
    when(csulSignerRepository.findByKaryawanName("ASM")).thenReturn(Optional.of(csulSigner("ASM")));
    when(signingClient.sendDocumentSigning(any(ExternalSigningRequest.class))).thenReturn(successSigningResponse());

    SigningResponse result = spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getDocumentId()).isEqualTo("DOC001");
    verify(agreementFileSigningService).saveSigningResult("AGR001", "DOC001", "maker", FINANCING_HDR_CODE.toString());
    ArgumentCaptor<ExternalSigningRequest> captor = ArgumentCaptor.forClass(ExternalSigningRequest.class);
    verify(signingClient).sendDocumentSigning(captor.capture());
    assertThat(captor.getValue().getAudit().getCallerId()).isEqualTo("maker");
    assertThat(captor.getValue().getRequests().get(0).getSigners()).hasSize(3);
  }

  @Test
  void sendDocumentForSigningReturnsFailureForSigningApiErrorAndMissingSigner() throws Exception {
    ReportService spy = org.mockito.Mockito.spy(new ReportService());
    injectDependencies(spy);
    when(agreementFileSigningRepository.findByAgreementCode("AGR001")).thenReturn(List.of());
    doReturn("pdf".getBytes()).when(spy).generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM");
    when(agreementRepo.findByAgreementCode("AGR001")).thenReturn(Optional.of(agreement()));
    when(cwrRepository.findByCwrCode("CWR001")).thenReturn(Optional.of(cwr("JKT")));
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(List.of(debtor()));
    when(csulSignerRepository.findByKaryawanName("BM")).thenReturn(Optional.of(csulSigner("BM")));
    when(csulSignerRepository.findByKaryawanName("ASM")).thenReturn(Optional.of(csulSigner("ASM")));
    when(signingClient.sendDocumentSigning(any(ExternalSigningRequest.class)))
        .thenReturn(ExternalSigningResponse.builder()
            .status(ExternalSigningResponse.Status.builder().code(9).message("failed").build())
            .documents(List.of())
            .build());

    SigningResponse result = spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker");
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getMessage()).isEqualTo("E-sign API error: failed");

    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(List.of());
    SigningResponse missingSigner = spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker");
    assertThat(missingSigner.isSuccess()).isFalse();
    assertThat(missingSigner.getMessage()).contains("Tidak ada data signer active");
  }

  @Test
  void sendDocumentForSigningReturnsFailureForMissingAgreementBranchAndCsulSigners() throws Exception {
    ReportService spy = org.mockito.Mockito.spy(new ReportService());
    injectDependencies(spy);
    when(agreementFileSigningRepository.findByAgreementCode("AGR001")).thenReturn(List.of());
    doReturn("pdf".getBytes()).when(spy).generateReport(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM");

    when(agreementRepo.findByAgreementCode("AGR001")).thenReturn(Optional.empty());
    assertThat(spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker").getMessage())
        .isEqualTo("Agreement dengan code AGR001 tidak ditemukan");

    Agreement noFinancing = agreement();
    noFinancing.setFinancingHdr(null);
    when(agreementRepo.findByAgreementCode("AGR001")).thenReturn(Optional.of(noFinancing));
    assertThat(spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker").getMessage())
        .isEqualTo("FinancingHdr tidak ditemukan untuk agreement AGR001");

    when(agreementRepo.findByAgreementCode("AGR001")).thenReturn(Optional.of(agreement()));
    when(cwrRepository.findByCwrCode("CWR001")).thenReturn(Optional.of(cwr("")));
    assertThat(spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker").getMessage())
        .isEqualTo("Branch code kosong untuk cwr CWR001");

    when(cwrRepository.findByCwrCode("CWR001")).thenReturn(Optional.of(cwr(null)));
    assertThat(spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker").getMessage())
        .isEqualTo("Branch code kosong untuk cwr CWR001");

    when(cwrRepository.findByCwrCode("CWR001")).thenReturn(Optional.empty());
    assertThat(spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker").getMessage())
        .isEqualTo("Cwr dengan code CWR001 tidak ditemukan");

    when(cwrRepository.findByCwrCode("CWR001")).thenReturn(Optional.of(cwr("JKT")));
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(List.of(debtor()));
    when(csulSignerRepository.findByKaryawanName("BM")).thenReturn(Optional.empty());
    assertThat(spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker").getMessage())
        .isEqualTo("Branch Manager BM tidak ditemukan di csul_signer");

    when(csulSignerRepository.findByKaryawanName("BM")).thenReturn(Optional.of(csulSigner("BM")));
    when(csulSignerRepository.findByKaryawanName("ASM")).thenReturn(Optional.empty());
    assertThat(spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker").getMessage())
        .isEqualTo("Area Sales Manager ASM tidak ditemukan di csul_signer");

    when(agreementRepo.findByAgreementCode("AGR001")).thenReturn(Optional.of(agreement()), Optional.empty());
    when(cwrRepository.findByCwrCode("CWR001")).thenReturn(Optional.of(cwr("JKT")));
    assertThat(spy.sendDocumentForSigning(FINANCING_HDR_CODE.toString(), "AGR001", "BM", "ASM", "maker").getMessage())
        .isEqualTo("Agreement not found");
  }

  @Test
  void privateFormattersCoverNullValidAndInvalidInputs() {
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", (Object) null)).isEqualTo("-");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", new Date(0))).contains("1970");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", LocalDate.of(2026, 1, 2))).isEqualTo("02/01/2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", LocalDateTime.of(2026, 1, 2, 3, 4))).isEqualTo("02/01/2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", "2026-01-02T03:04:00")).isEqualTo("02/01/2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", "2026-01-02")).isEqualTo("02/01/2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", "2026-01-02 03:04:00")).isEqualTo("02/01/2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", "")).isEqualTo("-");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", "bad-date")).isEqualTo("bad-date");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj", throwingToString())).isEqualTo("-");

    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", (Object) null)).isEqualTo("-");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", new Date(0))).contains("1970");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", LocalDate.of(2026, 1, 2))).contains("2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", LocalDateTime.of(2026, 1, 2, 3, 4))).contains("2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", "2026-01-02T03:04:00")).contains("2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", "2026-01-02")).contains("2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", "2026-01-02 03:04:00")).contains("2026");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", "")).isEqualTo("-");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", "bad-date")).isEqualTo("bad-date");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtDateObj2", throwingToString())).isEqualTo("-");

    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtAmount", (Object) null)).isEqualTo("IDR 0.00");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtAmount", BigDecimal.ONE)).isEqualTo("IDR 1.00");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtAmount", "12,345")).isEqualTo("IDR 12345.00");
    assertThat((String) ReflectionTestUtils.invokeMethod(ReportService.class, "fmtAmount", "x")).isEqualTo("IDR 0.00");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "fmtRupiah", (Object) null)).isEqualTo("-");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "fmtRupiah", "1234")).isEqualTo("IDR 1,234.00");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "fmtRupiah", "x")).isEqualTo("-");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "fmtRupiah2", (Object) null)).isEqualTo("-");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "fmtRupiah2", "1234")).isEqualTo("1,234.00");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "fmtRupiah2", "x")).isEqualTo("-");
  }

  private void injectDependencies(ReportService target) {
    ReflectionTestUtils.setField(target, "financingHdrRepository", financingHdrRepository);
    ReflectionTestUtils.setField(target, "csulSignerRepository", csulSignerRepository);
    ReflectionTestUtils.setField(target, "agreementCodeService", agreementCodeService);
    ReflectionTestUtils.setField(target, "agreementFileSigningRepository", agreementFileSigningRepository);
    ReflectionTestUtils.setField(target, "visitorRepository", visitorRepository);
    ReflectionTestUtils.setField(target, "cwrRepository", cwrRepository);
    ReflectionTestUtils.setField(target, "mstBranchService", mstBranchService);
    ReflectionTestUtils.setField(target, "authRemoteService", authRemoteService);
    ReflectionTestUtils.setField(target, "emailAo", emailAo);
    ReflectionTestUtils.setField(target, "invoiceService", invoiceService);
    ReflectionTestUtils.setField(target, "agreementRepo", agreementRepo);
    ReflectionTestUtils.setField(target, "externalApiService", externalApiService);
    ReflectionTestUtils.setField(target, "financingHdrService", financingHdrService);
    ReflectionTestUtils.setField(target, "debtorRepository", debtorRepository);
    ReflectionTestUtils.setField(target, "executor", (Executor) Runnable::run);
    ReflectionTestUtils.setField(target, "jasperReportRenderer", jasperReportRenderer);
    ReflectionTestUtils.setField(target, "signingClient", signingClient);
    ReflectionTestUtils.setField(target, "agreementFileSigningService", agreementFileSigningService);
  }

  private static PaginationRequest reportRequest() {
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    request.setStartDate(java.sql.Date.valueOf("2026-01-01"));
    request.setEndDate(java.sql.Date.valueOf("2026-01-31"));
    return request;
  }

  private void stubJwt() {
    BaseLdapRemoteResponseDto<String> response = new BaseLdapRemoteResponseDto<>();
    response.setData("jwt");
    when(authRemoteService.fetchAuthJwt()).thenReturn(response);
  }

  private static Agreement agreement() {
    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(FINANCING_HDR_CODE);
    Cwr cwr = cwr("JKT");
    cwr.setCwrCode("CWR001");
    return Agreement.builder()
        .agreementCode("AGR001")
        .financingHdr(financingHdr)
        .cwr(cwr)
        .build();
  }

  private static FinancingHdr financingHdr() {
    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(FINANCING_HDR_CODE);
    return financingHdr;
  }

  private static Cwr cwr(String branchCode) {
    return Cwr.builder()
        .cwrCode("CWR001")
        .branchCode(branchCode)
        .build();
  }

  private static Debtor debtor() {
    Debtor debtor = new Debtor();
    debtor.setIdentityNo("KTP001");
    debtor.setNoTelp("08123");
    debtor.setEmail("debtor@example.com");
    return debtor;
  }

  private static CsulSigner csulSigner(String name) {
    return CsulSigner.builder()
        .karyawanName(name)
        .identityNo(name + "-KTP")
        .noTelp("08123")
        .email(name.toLowerCase() + "@example.com")
        .build();
  }

  private static CsulSigner csulSignerWithJabatan(String name, String jabatan) {
    return CsulSigner.builder()
        .karyawanName(name)
        .jabatan(jabatan)
        .identityNo(name + "-KTP")
        .noTelp("08123")
        .email(name.toLowerCase() + "@example.com")
        .build();
  }

  private static AppResponse appResponse() {
    AppResponse response = new AppResponse();
    response.setAppId(123);
    response.setAppNo("APP001");
    response.setTenor("12");
    response.setLobCode("CBU");
    response.setProdOfferingName("Product");
    return response;
  }

  private static RekDebiturResponse rekDebiturResponse() {
    RekDebiturResponse response = new RekDebiturResponse();
    response.setBankAccounts(List.of(new RekDebiturResponse.BankAccount("Bank", "123", "Debtor")));
    return response;
  }

  private static RekDebiturResponse rekDebiturResponseWithEmptyAccounts() {
    RekDebiturResponse response = new RekDebiturResponse();
    response.setBankAccounts(List.of());
    return response;
  }

  private static AppFactoringResponse appFactoringResponse() {
    AppFactoringResponse response = new AppFactoringResponse();
    response.setDiskontoAmount("10");
    response.setTotalRetentionAmount("20");
    response.setTotalInvoiceAmount("1000");
    return response;
  }

  private static FinancialDataResponse financialDataResponse() {
    FinancialDataResponse response = new FinancialDataResponse();
    FinancialDataResponse.FinancialData data = new FinancialDataResponse.FinancialData();
    data.setNtfAmount("900");
    data.setEffectiveRate("12.345");
    data.setInstallmentAmount("100");
    data.setMaxRefundAmount("5");
    data.setTotalFeeAmount("30");
    data.setGracePeriod("7");
    response.setFinancialData(data);
    FinancialDataResponse.AgreementFee factoring = new FinancialDataResponse.AgreementFee();
    factoring.setFeeTypeName("BIAYA FACTORING");
    factoring.setFeeAmount("10");
    FinancialDataResponse.AgreementFee admin = new FinancialDataResponse.AgreementFee();
    admin.setFeeTypeName("BIAYA ADMINISTRASI PENCAIRAN");
    admin.setFeeAmount("20");
    FinancialDataResponse.AgreementFee insurance = new FinancialDataResponse.AgreementFee();
    insurance.setFeeTypeName("Total CWR Insurance Fee");
    insurance.setFeeAmount("30");
    FinancialDataResponse.AgreementFee creditInsurance = new FinancialDataResponse.AgreementFee();
    creditInsurance.setFeeTypeName("Total CWR Credit Insurance Fee");
    creditInsurance.setFeeAmount("40");
    response.setFeeList(List.of(factoring, admin, insurance, creditInsurance));
    return response;
  }

  private static FinancialDataResponse financialDataCreditInsuranceOnly() {
    FinancialDataResponse response = new FinancialDataResponse();
    FinancialDataResponse.FinancialData data = new FinancialDataResponse.FinancialData();
    data.setNtfAmount("900");
    data.setEffectiveRate("12");
    data.setInstallmentAmount("100");
    data.setMaxRefundAmount("5");
    data.setTotalFeeAmount("30");
    data.setGracePeriod("7");
    response.setFinancialData(data);
    FinancialDataResponse.AgreementFee creditInsurance = new FinancialDataResponse.AgreementFee();
    creditInsurance.setFeeTypeName("Total CWR Credit Insurance Fee");
    creditInsurance.setFeeAmount("40");
    FinancialDataResponse.AgreementFee nullType = new FinancialDataResponse.AgreementFee();
    nullType.setFeeTypeName(null);
    nullType.setFeeAmount("50");
    FinancialDataResponse.AgreementFee other = new FinancialDataResponse.AgreementFee();
    other.setFeeTypeName("OTHER");
    other.setFeeAmount("60");
    response.setFeeList(List.of(creditInsurance, nullType, other));
    return response;
  }

  private static FinancialDataResponse financialDataWithoutPayload() {
    FinancialDataResponse response = new FinancialDataResponse();
    response.setFeeList(List.of());
    return response;
  }

  private static FinancialDataResponse financialDataWithNullFeeList() {
    FinancialDataResponse response = new FinancialDataResponse();
    FinancialDataResponse.FinancialData data = new FinancialDataResponse.FinancialData();
    data.setNtfAmount("900");
    data.setEffectiveRate("12");
    data.setInstallmentAmount("100");
    data.setMaxRefundAmount("5");
    data.setTotalFeeAmount("30");
    data.setGracePeriod("7");
    response.setFinancialData(data);
    response.setFeeList(null);
    return response;
  }

  private static CwrBwhrResponse cwrBwhrResponse() {
    CwrBwhrResponse response = new CwrBwhrResponse();
    CwrBwhrResponse.ListCwrBwhr row = new CwrBwhrResponse.ListCwrBwhr();
    row.setCwrBouwheerCustNo("BWHR001");
    response.setCwrBouwheerCustNos(List.of(row));
    return response;
  }

  private static CwrBwhrResponse cwrBwhrResponseWithNullList() {
    CwrBwhrResponse response = new CwrBwhrResponse();
    response.setCwrBouwheerCustNos(null);
    return response;
  }

  private static CwrBwhrResponse cwrBwhrResponseWithEmptyList() {
    CwrBwhrResponse response = new CwrBwhrResponse();
    response.setCwrBouwheerCustNos(List.of());
    return response;
  }

  private static CwrListBwhrResponse cwrListBwhrResponse() {
    CwrListBwhrResponse.ListData row = new CwrListBwhrResponse.ListData();
    row.setCooperationAgreementNo("COOP001");
    row.setStartPeriod("2026-01-01T00:00:00");
    return new CwrListBwhrResponse(List.of(row));
  }

  private static Debtor reportDebtor() {
    Debtor debtor = debtor();
    debtor.setKaryawanName("Signer One");
    debtor.setJabatan("Director");
    debtor.setIdentityNo("KTP001");
    debtor.setAlamat("Jl. Signer");
    return debtor;
  }

  private static SitDto sitDto() {
    return SitDto.builder()
        .agreementCode("AGR001")
        .financingDueDate(LocalDateTime.of(2026, 12, 31, 0, 0))
        .totalInvoiceAmt(1000D)
        .DirectorName("Director")
        .build();
  }

  private static SitDto sitDtoWithoutDueDate() {
    return SitDto.builder()
        .agreementCode("AGR001")
        .totalInvoiceAmt(1000D)
        .DirectorName("Director")
        .build();
  }

  private static PostedInvoiceDto postedInvoice() {
    return PostedInvoiceDto.builder()
        .customerInvoiceNo("INV001")
        .invoiceDate(new Date(0))
        .invoiceDueDate(new Date(86_400_000L))
        .invoiceAmount(BigDecimal.valueOf(1000))
        .invoiceDescription("Invoice")
        .bouwheerName("Bouwheer")
        .build();
  }

  private static PostedInvoiceDto postedInvoiceWithNullFields() {
    return PostedInvoiceDto.builder().build();
  }

  private static Object throwingToString() {
    return new Object() {
      @Override
      public String toString() {
        throw new IllegalStateException("boom");
      }
    };
  }

  private static ExternalSigningResponse successSigningResponse() {
    return ExternalSigningResponse.builder()
        .status(ExternalSigningResponse.Status.builder().code(0).message("ok").build())
        .documents(List.of(ExternalSigningResponse.Document.builder().documentId("DOC001").build()))
        .build();
  }
}
