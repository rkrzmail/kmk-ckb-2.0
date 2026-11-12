package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.email.MailDataDto;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.dto.AgreementAttributeContentDto;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InquiryAgreementByNoCwrRemoteDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.user.entity.MstBranch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SitTemplateServiceTest {
  private static final UUID LEAD = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock AgreementRepository agreementRepository;
  @Mock ConfigRemoteService configRemoteService;
  @Mock CwrRemoteService cwrRemoteService;
  @Mock JasperReportRenderer jasperReportRenderer;
  SitTemplateService service;

  @BeforeEach
  void setUp() {
    service = new SitTemplateService(financingHdrRepository, agreementRepository,
      configRemoteService, cwrRemoteService, jasperReportRenderer);
  }

  @Test
  void listsAgreementCodesForLead() {
    when(financingHdrRepository.findByFinancingHdrCode(LEAD)).thenReturn(Optional.of(financing()));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(LEAD))
      .thenReturn(List.of(Agreement.builder().agreementCode("B").build(),
        Agreement.builder().agreementCode("A").build(),
        Agreement.builder().agreementCode("A").build()));

    assertThat(service.agreementCodes(LEAD)).containsExactly("A", "B");
  }

  @Test
  void rendersSelectedAgreementWithLeadAndSiscaValues() throws Exception {
    FinancingHdr financing = financing();
    Agreement agreement = Agreement.builder()
      .agreementCode("AGR-1")
      .dtmCrt(LocalDateTime.of(2026, 10, 26, 9, 0))
      .cwr(Cwr.builder()
        .cwrCode("AGR-1")
        .cwrStartDate(LocalDateTime.of(2026, 10, 26, 9, 0))
        .dtmCrt(LocalDateTime.of(2026, 10, 20, 8, 0)).build())
      .build();
    when(financingHdrRepository.findByFinancingHdrCode(LEAD)).thenReturn(Optional.of(financing));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(LEAD)).thenReturn(List.of(agreement));
    when(cwrRemoteService.inquiryAgreementByNoCwr("AGR-1")).thenReturn(response(List.of(
      InquiryAgreementByNoCwrRemoteDto.builder().agrmntNo("AGR-OTHER").appId(9).build(),
      InquiryAgreementByNoCwrRemoteDto.builder().agrmntNo("AGR-1").appId(42).build())));
    when(cwrRemoteService.agreementAttributeContent(42)).thenReturn(response(List.of(
      new AgreementAttributeContentDto(42, "LIMIT_AGR_NO", "142"),
      new AgreementAttributeContentDto(42, "LIMIT_AGR_DT", "2025-10-20"))));
    when(configRemoteService.getEmailByPosition("", "407", "RM"))
      .thenReturn(MailPositionDto.builder().data(List.of(MailDataDto.builder()
        .branchCode("407").employeeName("Manajer Regional").build())).build());
    byte[] pdf = "%PDF-1.4".getBytes();
    when(jasperReportRenderer.renderToPdf(eq("/Reports/sit.jasper"), anyMap())).thenReturn(pdf);

    assertThat(service.download(LEAD, "AGR-1")).isEqualTo(pdf);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
    verify(jasperReportRenderer).renderToPdf(eq("/Reports/sit.jasper"), captor.capture());
    assertThat(captor.getValue()).containsEntry("nomorPerjanjian", "AGR-1")
      .containsEntry("tanggalPerjanjian", "20 Oktober 2025")
      .containsEntry("tanggalCwr", "JAKARTA, 20 Oktober 2025")
      .containsEntry("namaDebitur", "Debitur Satu")
      .containsEntry("companyDebitur", "Debitur Satu")
      .containsEntry("namaBouwheer", "Bouwheer Satu")
      .containsEntry("namaRegionalManager", "Manajer Regional")
      .containsEntry("legalAddress", "Jakarta")
      .containsEntry("periodeBerlaku", "31 Desember 2026");
  }

  @Test
  void rejectsWhenSelectedAgreementHasNoAppId() throws Exception {
    when(financingHdrRepository.findByFinancingHdrCode(LEAD)).thenReturn(Optional.of(financing()));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(LEAD)).thenReturn(List.of(agreement()));
    when(cwrRemoteService.inquiryAgreementByNoCwr("CWR-1")).thenReturn(response(List.of(
      InquiryAgreementByNoCwrRemoteDto.builder().agrmntNo("AGR-OTHER").appId(42).build())));

    assertThatThrownBy(() -> service.download(LEAD, "AGR-1"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("AppId Confins");
  }


  private Agreement agreement() {
    return Agreement.builder().agreementCode("AGR-1")
      .cwr(Cwr.builder().cwrCode("CWR-1").build()).build();
  }

  private <T> BaseMstRemoteResponseDto<List<T>> response(List<T> data) {
    var response = new BaseMstRemoteResponseDto<List<T>>();
    response.setData(data);
    return response;
  }

  @Test
  void rejectsAmbiguousAgreement() {
    when(financingHdrRepository.findByFinancingHdrCode(LEAD)).thenReturn(Optional.of(financing()));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(LEAD))
      .thenReturn(List.of(Agreement.builder().agreementCode("A").build(),
        Agreement.builder().agreementCode("B").build()));

    assertThatThrownBy(() -> service.download(LEAD, null))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("lebih dari satu perjanjian");
  }

  @Test
  void rejectsLeadWithoutAgreement() {
    when(financingHdrRepository.findByFinancingHdrCode(LEAD)).thenReturn(Optional.of(financing()));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(LEAD)).thenReturn(List.of());

    assertThatThrownBy(() -> service.download(LEAD, null))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("Perjanjian untuk Leads ID ini tidak ditemukan");
  }

  private FinancingHdr financing() {
    FinancingHdr financing = new FinancingHdr();
    financing.setBouwheer(Bouwheer.builder().bouwheerName("Bouwheer Satu")
      .legalAddress("Jakarta").picName("PIC Satu").build());
    financing.setCustomer(Customer.builder().custName("Debitur Satu")
      .build());
    financing.setMstBranch(MstBranch.builder().branchName("JAKARTA ").branchCode("407").build());
    financing.setFinancingDueDate(LocalDateTime.of(2026, 12, 31, 0, 0));
    return financing;
  }
}
