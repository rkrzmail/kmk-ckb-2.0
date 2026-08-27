package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.CustomerPlafondDto;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.customer.model.entity.Customer;
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
}
