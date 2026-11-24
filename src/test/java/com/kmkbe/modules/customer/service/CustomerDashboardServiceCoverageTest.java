package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.NotifDebtor;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.NotifDebtorRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.user.entity.MstBranch;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDashboardServiceCoverageTest {
  @Mock FinancingHdrService financingHdrService;
  @Mock CwrRepository cwrRepository;
  @Mock EntityManager entityManager;
  @Mock AgreementRepository agreementRepository;
  @Mock NotifDebtorRepository notifDebtorRepository;
  @Mock AgreementFileSigningRepository agreementFileSigningRepository;
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock CurrentUserService currentUserService;
  CustomerDashboardService service;

  @BeforeEach
  void setUp() {
    service = new CustomerDashboardService(financingHdrService, cwrRepository, entityManager,
      agreementRepository, notifDebtorRepository, agreementFileSigningRepository,
      financingHdrRepository, currentUserService);
  }

  @Test
  void plafondWithoutFinancingAndWithFinancing() throws Exception {
    Customer customer = customer("Personal");
    when(currentUserService.customer()).thenReturn(customer);
    when(financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer))
      .thenReturn(Optional.empty());
    assertThat(((BaseResponseBuilder) service.plafond()).getData()).isNotNull();

    FinancingHdr hdr = hdr("Personal", null, null, null);
    when(financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer))
      .thenReturn(Optional.of(hdr));
    when(financingHdrRepository.findByFinancingHdrCode(hdr.getFinancingHdrCode()))
      .thenReturn(Optional.of(hdr));
    lenient().when(cwrRepository.findFirstByCustomerOrderByCwrEndDateDescCwrCodeDesc(any()))
      .thenReturn(Optional.empty());
    assertThat(((BaseResponseBuilder) service.plafond()).getData()).isNotNull();

    when(financingHdrRepository.findByFinancingHdrCode(UUID.randomUUID())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.plafondByFinancingHdrCode(UUID.randomUUID().toString()))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void plafondByCodeCompanyAndPersonalBranches() {
    CustomerCompany company = mock(CustomerCompany.class);
    when(company.getCompanyAddress()).thenReturn("Address");
    when(company.getPhone()).thenReturn("021");
    FinancingHdr companyHdr = hdr("Company", company, null, "AO");
    when(financingHdrRepository.findByFinancingHdrCode(companyHdr.getFinancingHdrCode()))
      .thenReturn(Optional.of(companyHdr));
    Cwr cwr = mock(Cwr.class);
    when(cwr.getPlafondAmt()).thenReturn(1000.0);
    when(cwr.getRealisationAmt()).thenReturn(400.0);
    when(cwr.getCwrEndDate()).thenReturn(LocalDateTime.of(2027, 1, 1, 0, 0));
    Agreement agreement = mock(Agreement.class);
    when(agreement.getFinancingAmt()).thenReturn(100.0);
    when(cwrRepository.findFirstByCustomerOrderByCwrEndDateDescCwrCodeDesc(any()))
      .thenReturn(Optional.of(cwr));
    when(agreementRepository.findAllByCwr(cwr)).thenReturn(List.of(agreement));

    assertThat(((BaseResponseBuilder) service.plafondByFinancingHdrCode(
      companyHdr.getFinancingHdrCode().toString())).getData()).isNotNull();

    CustomerPersonal personal = mock(CustomerPersonal.class);
    when(personal.getLegalAddress()).thenReturn("Legal");
    when(personal.getPhone()).thenReturn("0812");
    FinancingHdr personalHdr = hdr("Personal", null, personal, null);
    when(financingHdrRepository.findByFinancingHdrCode(personalHdr.getFinancingHdrCode()))
      .thenReturn(Optional.of(personalHdr));
    when(cwr.getCwrEndDate()).thenReturn(null);
    when(agreementRepository.findAllByCwr(cwr)).thenReturn(List.of());
    assertThat(((BaseResponseBuilder) service.plafondByFinancingHdrCode(
      personalHdr.getFinancingHdrCode().toString())).getData()).isNotNull();

    FinancingHdr bare = hdr("Company", null, null, "AO");
    when(financingHdrRepository.findByFinancingHdrCode(bare.getFinancingHdrCode()))
      .thenReturn(Optional.of(bare));
    assertThat(((BaseResponseBuilder) service.plafondByFinancingHdrCode(
      bare.getFinancingHdrCode().toString())).getData()).isNotNull();
  }

  @Test
  void mainDashboardAndAgreementDashboard() throws Exception {
    Customer customer = customer("Personal");
    when(currentUserService.customer()).thenReturn(customer);
    when(cwrRepository.findFirstByCustomerOrderByCwrEndDateDescCwrCodeDesc(customer))
      .thenReturn(Optional.empty());
    assertThat(((BaseResponseBuilder) service.mainDashboard()).getData()).isNotNull();

    Cwr cwr = mock(Cwr.class);
    when(cwr.getPlafondAmt()).thenReturn(1000.0);
    when(cwr.getRealisationAmt()).thenReturn(400.0);
    when(cwr.getCwrEndDate()).thenReturn(LocalDateTime.of(2027, 1, 1, 0, 0));
    when(cwrRepository.findFirstByCustomerOrderByCwrEndDateDescCwrCodeDesc(customer))
      .thenReturn(Optional.of(cwr));
    when(agreementRepository.countInvoiceFundedByCustCode(customer.getCustCode())).thenReturn(2L);
    assertThat(((BaseResponseBuilder) service.mainDashboard()).getData()).isNotNull();
    assertThat(service.agreementDashboard()).isNotNull();
  }

  @Test
  void perjanjianAndNotif() {
    CustomerCompany company = mock(CustomerCompany.class);
    when(company.getCompanyAddress()).thenReturn("Address");
    FinancingHdr hdr = hdr("Company", company, null, null);
    when(financingHdrService.findByCode(any())).thenReturn(hdr);
    Agreement agreement = mock(Agreement.class);
    when(agreement.getAgreementCode()).thenReturn("AGR-1");
    when(agreementRepository.findAgreement(any())).thenReturn(Optional.of(agreement));
    when(agreementFileSigningRepository.countUploadedAgreementsByCustomer(any())).thenReturn(3L);
    when(agreementFileSigningRepository.countCompletedUploadedAgreementsByCustomer(any())).thenReturn(1L);

    String code = hdr.getFinancingHdrCode().toString();
    assertThat(service.perjanjian(code).getAddress()).isEqualTo("Address");
    when(agreementRepository.findAgreement(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.perjanjian(code)).isInstanceOf(RuntimeException.class);

    FinancingHdr personalHdr = hdr("Personal", null, null, null);
    when(financingHdrService.findByCode(any())).thenReturn(personalHdr);
    when(agreementRepository.findAgreement(any())).thenReturn(Optional.of(agreement));
    assertThat(service.perjanjian(code).getAddress()).isEmpty();

    NotifDebtor notif = mock(NotifDebtor.class);
    when(notifDebtorRepository.findByCustCode("C1")).thenReturn(List.of(notif));
    assertThat(service.getNotifDebtors("C1")).containsExactly(notif);
    service.deleteAllNotifDebtors("C1");
    verify(notifDebtorRepository).deleteByCustCode("C1");
  }

  private static Customer customer(String type) {
    Customer customer = mock(Customer.class);
    lenient().when(customer.getCustCode()).thenReturn(UUID.randomUUID());
    lenient().when(customer.getCustName()).thenReturn("Customer");
    lenient().when(customer.getCustTypeCode()).thenReturn(type);
    lenient().when(customer.getCustIdTypeCode()).thenReturn("KTP");
    lenient().when(customer.getCustIdNo()).thenReturn("ID");
    lenient().when(customer.getCustEmail()).thenReturn("customer@example.com");
    return customer;
  }

  private static FinancingHdr hdr(String type, CustomerCompany company, CustomerPersonal personal, String ao) {
    FinancingHdr hdr = mock(FinancingHdr.class);
    Customer customer = customer(type);
    lenient().when(customer.getCompany()).thenReturn(company);
    lenient().when(customer.getPersonal()).thenReturn(personal);
    lenient().when(hdr.getCustomer()).thenReturn(customer);
    lenient().when(hdr.getFinancingHdrCode()).thenReturn(UUID.randomUUID());
    lenient().when(hdr.getBouwheer()).thenReturn(mock(com.kmkbe.modules.bouwheer.model.entity.Bouwheer.class));
    lenient().when(hdr.getMstBranch()).thenReturn(mock(MstBranch.class));
    lenient().when(hdr.getAssignedAoEmployeeCode()).thenReturn(ao);
    return hdr;
  }
}
