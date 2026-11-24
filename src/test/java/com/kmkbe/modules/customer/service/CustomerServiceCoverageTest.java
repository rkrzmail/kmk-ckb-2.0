package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.entity.EmailDeliveryLog;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailDeliveryService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.ApprovalRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.model.request.UpdateFapRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceCoverageTest {
  @Mock CustomerRepository customerRepository;
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock EmailService emailService;
  @Mock EmailDeliveryService emailDeliveryService;
  @Mock AuditTrailService auditTrailService;
  @Mock BouwheerRepository bouwheerRepository;
  @Mock CurrentUserService currentUserService;
  @Mock BCryptPasswordEncoder bcryptEncoder;
  CustomerService service;

  @BeforeEach
  void setUp() {
    service = new CustomerService(customerRepository, financingHdrRepository, emailService,
      emailDeliveryService, auditTrailService, bouwheerRepository, currentUserService, bcryptEncoder);
  }

  @Test
  void profileBranches() throws Exception {
    Customer customer = customer();
    customer.setCompany(new CustomerCompany());
    customer.getCompany().setDirectorName("Director");
    customer.getCompany().setArea("Area");
    when(currentUserService.customer()).thenReturn(customer);
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer()));

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("custCode")).thenReturn(null);
    assertThat(service.profile(request).getData().getBouwheerName()).isEqualTo("Bouwheer");

    when(request.getParameter("custCode")).thenReturn(customer.getCustCode().toString());
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    assertThat(service.profile(request).isSuccess()).isTrue();

    when(request.getParameter("custCode")).thenReturn(UUID.randomUUID().toString());
    when(customerRepository.findByCustCode(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.profile(request)).isInstanceOf(Exception.class);

    customer.setCompany(null);
    customer.setPersonal(new CustomerPersonal());
    when(currentUserService.customer()).thenReturn(customer);
    when(request.getParameter("custCode")).thenReturn(null);
    assertThat(service.profile(request).isSuccess()).isTrue();
  }

  @Test
  void createBranches() {
    SignUpRequest request = signUpRequest();
    request.setAgreeTc(false);
    assertThatThrownBy(() -> service.create(request, com.kmkbe.core.domain.constant.CustomerType.Personal))
      .isInstanceOf(RuntimeException.class);

    request.setAgreeTc(true);
    Customer other = customer();
    other.setCustEmail("customer@example.com");
    other.setCustExternalCode("OTHER");
    other.setActive(true);
    when(customerRepository.findByCustEmail("customer@example.com")).thenReturn(Optional.of(other));
    when(customerRepository.findFirstByCustExternalCode("V001")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.create(request, com.kmkbe.core.domain.constant.CustomerType.Personal))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("vendor lain");

    when(customerRepository.findByCustEmail("customer@example.com")).thenReturn(Optional.empty());
    request.setCustomerIdNo("123");
    assertThatThrownBy(() -> service.create(request, com.kmkbe.core.domain.constant.CustomerType.Personal))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("KTP");

    request.setCustomerIdNo("1234567890123456");
    when(bcryptEncoder.encode("1234")).thenReturn("encoded");
    when(customerRepository.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));
    assertThat(service.create(request, com.kmkbe.core.domain.constant.CustomerType.Personal).getCustPin())
      .isEqualTo("encoded");

    Customer existing = customer();
    existing.setCustEmail("customer@example.com");
    when(customerRepository.findFirstByCustExternalCode("V001")).thenReturn(Optional.of(existing));
    when(customerRepository.findByCustEmail("customer@example.com")).thenReturn(Optional.empty());
    assertThat(service.create(request, com.kmkbe.core.domain.constant.CustomerType.Company)).isSameAs(existing);
  }

  @Test
  void verifyEmailUpdateAndFap() {
    Customer customer = customer();
    customer.setApprovalStatus("REJECTED");
    customer.setIsEmailValid(false);
    when(customerRepository.save(customer)).thenReturn(customer);
    service.verifyEmail(customer);
    assertThat(customer.getApprovalStatus()).isEqualTo("OPEN");

    UpdateCustomerRequest update = new UpdateCustomerRequest();
    update.setCustEmail("new@example.com");
    update.setCustName("New Name");
    update.setCustIdNo("ID");
    update.setNpwp("NPWP");
    when(customerRepository.existsByCustEmailIgnoreCaseAndCustIdNoNot("new@example.com", customer.getCustIdNo()))
      .thenReturn(true);
    assertThatThrownBy(() -> service.update(customer, update)).isInstanceOf(IllegalArgumentException.class);

    when(customerRepository.existsByCustEmailIgnoreCaseAndCustIdNoNot("new@example.com", customer.getCustIdNo()))
      .thenReturn(false);
    when(customerRepository.save(customer)).thenReturn(customer);
    when(currentUserService.usernameOrDefault(anyString())).thenReturn("maker");
    assertThat(service.update(customer, update).getForceLogout()).isTrue();

    assertThatThrownBy(() -> service.updateFapData(null, new UpdateFapRequest())).isInstanceOf(IllegalArgumentException.class);
    UpdateFapRequest blank = new UpdateFapRequest();
    assertThatThrownBy(() -> service.updateFapData(customer, blank)).isInstanceOf(IllegalArgumentException.class);

    UpdateFapRequest request = new UpdateFapRequest();
    request.setFapStatus("APPROVED");
    FinancingHdr hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(UUID.randomUUID());
    hdr.setCustomer(customer);
    request.setFinancingHdrCode(hdr.getFinancingHdrCode());
    when(financingHdrRepository.findByFinancingHdrCode(any())).thenReturn(Optional.of(hdr));
    when(financingHdrRepository.save(hdr)).thenReturn(hdr);
    service.updateFapData(customer, request);

    request.setFinancingHdrCode(UUID.randomUUID());
    when(financingHdrRepository.findByFinancingHdrCode(request.getFinancingHdrCode())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.updateFapData(customer, request)).isInstanceOf(IllegalArgumentException.class);

    request.setFinancingHdrCode(null);
    when(financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.updateFapData(customer, request)).isInstanceOf(IllegalArgumentException.class);

    when(financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer)).thenReturn(Optional.of(hdr));
    Customer other = customer();
    hdr.setCustomer(other);
    assertThatThrownBy(() -> service.updateFapData(customer, request)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void pagesAndFindByCode() {
    Customer customer = customer();
    when(customerRepository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(customer)));
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer()));

    BasePaginationRequest request = new BasePaginationRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    assertThat(service.pages(request).getData().getContent()).hasSize(1);

    request.setSearchBy("bouwheerName");
    request.setSearchValue("Bouwheer");
    when(bouwheerRepository.findByBouwheerNameContainingIgnoreCase("Bouwheer"))
      .thenReturn(List.of(bouwheer()));
    assertThat(service.pages(request).getData().getContent()).hasSize(1);

    when(bouwheerRepository.findByBouwheerNameContainingIgnoreCase("Bouwheer")).thenReturn(List.of());
    assertThatThrownBy(() -> service.pages(request)).isInstanceOf(RuntimeException.class);

    request.setSearchBy("custName");
    request.setSearchValue("Customer");
    request.setSortBy("bouwheerName");
    assertThat(service.pages(request).getData().getContent()).hasSize(1);

    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    assertThat(service.findByCustomerCode(customer.getCustCode().toString()).getData().getCustName())
      .isEqualTo("Customer");
    when(customerRepository.findByCustCode(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.findByCustomerCode(UUID.randomUUID().toString()))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void pageSpecificationBranches() {
    Customer customer = customer();
    when(customerRepository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(customer)));
    ArgumentCaptor<Specification<Customer>> captor = ArgumentCaptor.forClass(Specification.class);

    BasePaginationRequest bouwheerSearch = new BasePaginationRequest();
    bouwheerSearch.setPageNo(1);
    bouwheerSearch.setPageSize(10);
    bouwheerSearch.setSearchBy("bouwheerName");
    bouwheerSearch.setSearchValue("Bouwheer");
    when(bouwheerRepository.findByBouwheerNameContainingIgnoreCase("Bouwheer"))
      .thenReturn(List.of(bouwheer()));
    service.pages(bouwheerSearch);
    verify(customerRepository, org.mockito.Mockito.atLeastOnce()).findAll(captor.capture(), any(Pageable.class));
    evaluateSpec(captor.getValue());

    BasePaginationRequest textSearch = new BasePaginationRequest();
    textSearch.setPageNo(1);
    textSearch.setPageSize(10);
    textSearch.setSearchBy("custName");
    textSearch.setSearchValue("Customer");
    service.pages(textSearch);
    evaluateSpec(captor.getValue());
  }

  @Test
  void approvalBranches() {
    Customer customer = customer();
    ApprovalRequest request = ApprovalRequest.builder().custCode(customer.getCustCode())
      .approvalStatus("APPROVED").approvalNote("ok").build();
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.approval(request, "maker")).isInstanceOf(RuntimeException.class);

    customer.setApprovalStatus("OPEN");
    customer.setIsEmailValid(false);
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    assertThatThrownBy(() -> service.approval(request, "maker")).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("verifikasi email");

    customer.setApprovalStatus("PROCESSED");
    assertThatThrownBy(() -> service.approval(request, "maker")).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("sudah pernh diproses");

    customer.setApprovalStatus("OPEN");
    customer.setIsEmailValid(true);
    when(customerRepository.save(customer)).thenReturn(customer);
    EmailDeliveryLog sent = new EmailDeliveryLog();
    sent.setStatus(EmailDeliveryLog.Status.SENT);
    sent.setEmailDeliveryId(1L);
    when(emailDeliveryService.sendApproval(customer, "APPROVED", "ok")).thenReturn(sent);
    assertThat(((CustomerService.ApprovalEmailResult) ((com.kmkbe.helpers.base.BaseResponseBuilder) service.approval(request, "maker")).getData()).emailAccepted())
      .isTrue();

    customer.setApprovalStatus("OPEN");
    request.setApprovalStatus("REJECTED");
    EmailDeliveryLog failed = new EmailDeliveryLog();
    failed.setStatus(EmailDeliveryLog.Status.FAILED);
    when(emailDeliveryService.sendApproval(customer, "REJECTED", "ok")).thenReturn(failed);
    assertThat(((com.kmkbe.helpers.base.BaseResponseBuilder) service.approval(request, "maker")).getMessage()).contains("email notifikasi gagal");
  }

  private void evaluateSpec(Specification<Customer> spec) {
    Root<Customer> root = mock(Root.class);
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);
    when(root.get(anyString())).thenReturn(path);
    when(builder.isTrue(any())).thenReturn(predicate);
    org.mockito.Mockito.lenient().when(builder.lower(any())).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
    org.mockito.Mockito.lenient().when(builder.like(any(), anyString())).thenReturn(predicate);
    when(builder.and(any(Predicate[].class))).thenReturn(predicate);
    org.mockito.Mockito.lenient().when(path.in(anyCollection())).thenReturn(predicate);
    assertThat(spec.toPredicate(root, mock(jakarta.persistence.criteria.CriteriaQuery.class), builder))
      .isSameAs(predicate);
  }

  private static Bouwheer bouwheer() {
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerCode(UUID.randomUUID());
    bouwheer.setBouwheerName("Bouwheer");
    return bouwheer;
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    customer.setCustNo("CUST-1");
    customer.setCustName("Customer");
    customer.setCustEmail("customer@example.com");
    customer.setCustIdNo("ID");
    customer.setCustTypeCode("Personal");
    customer.setBouwheer(UUID.randomUUID().toString());
    customer.setApprovalStatus("OPEN");
    customer.setIsEmailValid(true);
    customer.setActive(true);
    customer.setDtmCrt(LocalDateTime.now());
    return customer;
  }

  private static SignUpRequest signUpRequest() {
    SignUpRequest request = new SignUpRequest();
    request.setAgreeTc(true);
    request.setEmail("customer@example.com");
    request.setVendorCode("V001");
    request.setName("Customer");
    request.setCustomerType("perorangan");
    request.setCustomerIdNo("1234567890123456");
    request.setMobilePhone("081234567890");
    request.setBouwheerCode(UUID.randomUUID().toString());
    request.setPin("1234");
    return request;
  }
}
