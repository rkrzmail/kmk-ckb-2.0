package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.CustomerPlafondDto;
import com.kmkbe.core.domain.dto.CustomerPerjanjianDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDashboardServiceIssueRegressionTest {

  @Mock private FinancingHdrService financingHdrService;
  @Mock private CwrRepository cwrRepository;
  @Mock private EntityManager entityManager;
  @Mock private AgreementRepository agreementRepository;
  @Mock private NotifDebtorRepository notifDebtorRepository;
  @Mock private AgreementFileSigningRepository agreementFileSigningRepository;
  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private CurrentUserService currentUserService;

  private CustomerDashboardService service;

  @BeforeEach
  void setUp() {
    service = new CustomerDashboardService(
      financingHdrService,
      cwrRepository,
      entityManager,
      agreementRepository,
      notifDebtorRepository,
      agreementFileSigningRepository,
      financingHdrRepository,
      currentUserService
    );
  }

  @Test
  void plafondReturnsZeroDataWhenCustomerHasNoFinancingHeader() throws Exception {
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custEmail("user@example.com")
      .custTypeCode("Company")
      .custIdTypeCode("NPWP")
      .custIdNo("123")
      .build();

    when(currentUserService.customer()).thenReturn(customer);
    when(financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer)).thenReturn(Optional.empty());

    BaseResponseBuilder<CustomerPlafondDto> response = service.plafond();

    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getData().getCustCode()).isEqualTo(customer.getCustCode());
    assertThat(response.getData().getPlafond().getPlafond()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.getData().getPlafond().getTotalPlafond()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.getData().getPlafond().getAvailablePlafond()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.getData().getPlafond().getJumlahInvoice()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void perjanjianCountsOnlyUploadedAgreementDocumentsByExpectedStatuses() {
    UUID financingHdrCode = UUID.fromString("11111111-1111-1111-1111-111111111111");
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custEmail("debtor@example.com")
      .custTypeCode("Company")
      .custIdTypeCode("NPWP")
      .custIdNo("123")
      .build();
    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(financingHdrCode);
    financingHdr.setCustomer(customer);
    financingHdr.setBouwheer(Bouwheer.builder()
      .bouwheerCode(UUID.randomUUID())
      .bouwheerName("Bouwheer")
      .build());
    Agreement agreement = Agreement.builder().agreementCode("AGR001").build();

    when(financingHdrService.findByCode(financingHdrCode.toString())).thenReturn(financingHdr);
    when(agreementRepository.findAgreement(financingHdrCode)).thenReturn(Optional.of(agreement));
    when(agreementFileSigningRepository.countUploadedAgreementsByCustomer(financingHdrCode)).thenReturn(5L);
    when(agreementFileSigningRepository.countRunningUploadedAgreementsByCustomer(financingHdrCode)).thenReturn(3L);
    when(agreementFileSigningRepository.countCompletedUploadedAgreementsByCustomer(financingHdrCode)).thenReturn(2L);

    CustomerPerjanjianDto result = service.perjanjian(financingHdrCode.toString());

    assertThat(result.getPerjanjian().getTotalPerjanjian()).isEqualTo(5);
    assertThat(result.getPerjanjian().getPerjanjianBerjalan()).isEqualTo(3);
    assertThat(result.getPerjanjian().getPerjanjianBerakhir()).isEqualTo(2);
    verify(financingHdrRepository, never()).countSigningAndSigned(financingHdrCode.toString());
    verify(financingHdrRepository, never()).countCompleted(financingHdrCode.toString());
    verify(agreementFileSigningRepository, never()).countBySigner(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void perjanjianReturnsZeroSummaryWhenNoAgreementDocumentHasBeenUploaded() {
    UUID financingHdrCode = UUID.fromString("22222222-2222-2222-2222-222222222222");
    Customer customer = Customer.builder()
      .custCode(UUID.randomUUID())
      .custName("Debitur")
      .custEmail("debtor@example.com")
      .custTypeCode("Personal")
      .custIdTypeCode("KTP")
      .custIdNo("456")
      .build();
    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(financingHdrCode);
    financingHdr.setCustomer(customer);
    financingHdr.setBouwheer(Bouwheer.builder()
      .bouwheerCode(UUID.randomUUID())
      .bouwheerName("Bouwheer")
      .build());

    when(financingHdrService.findByCode(financingHdrCode.toString())).thenReturn(financingHdr);
    when(agreementRepository.findAgreement(financingHdrCode))
      .thenReturn(Optional.of(Agreement.builder().agreementCode("AGR002").build()));

    CustomerPerjanjianDto result = service.perjanjian(financingHdrCode.toString());

    assertThat(result.getPerjanjian().getTotalPerjanjian()).isZero();
    assertThat(result.getPerjanjian().getPerjanjianBerjalan()).isZero();
    assertThat(result.getPerjanjian().getPerjanjianBerakhir()).isZero();
  }
}
