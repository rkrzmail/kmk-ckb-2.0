package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.entity.ChangePasswordLog;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.repository.ChangePasswordLogRepository;
import com.kmkbe.core.domain.repository.CustomerCompanyRepository;
import com.kmkbe.core.domain.repository.CustomerPersonalRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerSupportingServicesCoverageTest {
  @Mock CustomerCompanyRepository companyRepository;
  @Mock CustomerPersonalRepository personalRepository;
  @Mock CurrentUserService currentUserService;
  @Mock ChangePasswordLogRepository changePasswordLogRepository;
  @Mock MstAppRoleFormUserRepository roleRepository;
  @Mock EmailService emailService;
  @Mock BouwheerRepository bouwheerRepository;

  Customer customer;

  @BeforeEach
  void setUp() {
    customer = new Customer();
    customer.setCustName("Customer");
    customer.setCustCode(UUID.randomUUID());
  }

  @Test
  void companyCreateAndUpdate() {
    CustomerCompanyService service = new CustomerCompanyService(companyRepository, currentUserService);
    when(companyRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    SignUpRequest.Company companyReq = SignUpRequest.Company.builder()
      .companyType("PT").companyModel("MODEL").identityType("ID").identityNo("123")
      .identityIssuedDate(LocalDateTime.now().minusDays(5)).identityExpiredDate(LocalDateTime.now().plusDays(5))
      .companyAddress("Address").staySince(LocalDateTime.now().minusDays(30)).build();

    when(companyRepository.findByCustomer(customer)).thenReturn(Optional.empty());
    service.create(customer, companyReq);
    verify(companyRepository).save(any(CustomerCompany.class));

    CustomerCompany existing = new CustomerCompany();
    when(companyRepository.findByCustomer(customer)).thenReturn(Optional.of(existing));
    service.create(customer, companyReq);
    assertThat(existing.getCustomer()).isSameAs(customer);

    UpdateCustomerRequest.UpdateCompanyRequest update = new UpdateCustomerRequest.UpdateCompanyRequest();
    update.setCustCompanyType("PT");
    update.setIdentityNo("ID");
    update.setCompanyAddress("Address");
    update.setStaySince(new Date());
    update.setIdentityIssuedDate(new Date());
    update.setIdentityExpiredDate(new Date());
    when(currentUserService.usernameOrDefault(any())).thenReturn("maker");
    when(companyRepository.findByCustomer(customer)).thenReturn(Optional.empty());
    assertThat(service.update(customer, update, addressRequest()).getCustomer()).isSameAs(customer);

    when(companyRepository.findByCustomer(customer)).thenReturn(Optional.of(existing));
    assertThat(service.update(customer, update, addressRequest())).isSameAs(existing);
  }

  @Test
  void personalCreateAndUpdate() {
    CustomerPersonalService service = new CustomerPersonalService(personalRepository);
    when(personalRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    SignUpRequest.Personal personalReq = SignUpRequest.Personal.builder()
      .identityType("KTP").identityNo("123").phone("08123")
      .customerModel(com.kmkbe.core.domain.constant.CustomerModel.values()[0]).build();

    when(personalRepository.findByCustomer(customer)).thenReturn(Optional.empty());
    service.create(customer, personalReq);
    verify(personalRepository).save(any(CustomerPersonal.class));

    CustomerPersonal existing = new CustomerPersonal();
    when(personalRepository.findByCustomer(customer)).thenReturn(Optional.of(existing));
    service.create(customer, personalReq);
    assertThat(existing.getCustomer()).isSameAs(customer);

    UpdateCustomerRequest.UpdatePersonalRequest update = new UpdateCustomerRequest.UpdatePersonalRequest();
    update.setStaySince(new Date());
    update.setBirthDate(new Date());
    when(personalRepository.findByCustomer(customer)).thenReturn(Optional.empty());
    assertThat(service.update(customer, update, addressRequest()).getCustomer()).isSameAs(customer);

    update.setExpiredDate(new Date());
    when(personalRepository.findByCustomer(customer)).thenReturn(Optional.of(existing));
    assertThat(service.update(customer, update, addressRequest())).isSameAs(existing);

    when(personalRepository.findByCustomer(customer)).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.update(customer, update, addressRequest())).hasMessage("db down");
  }

  @Test
  void changePasswordLogCreatesAndRethrows() {
    ChangePasswordLogService service = new ChangePasswordLogService(changePasswordLogRepository);

    service.create(customer, "1111", "2222");
    verify(changePasswordLogRepository).save(any(ChangePasswordLog.class));

    doThrow(new RuntimeException("db down")).when(changePasswordLogRepository).save(any());
    assertThatThrownBy(() -> service.create(customer, "1111", "2222")).hasMessage("db down");
  }

  @Test
  void majorAccountNotificationBranches() {
    MajorAccountRegistrationNotificationService service =
      new MajorAccountRegistrationNotificationService(roleRepository, emailService, bouwheerRepository);
    customer.setBouwheer(UUID.randomUUID().toString());
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerName("Bouwheer");
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer));

    when(roleRepository.findActiveMajorAccountEmails()).thenReturn(Arrays.asList(" a@x.com ", null, "  ", "a@x.com"));
    service.notifyRegistrationCompleted(customer);
    verify(emailService).sendNotificationCustomerVerification(eq("a@x.com"), eq(customer), eq("Bouwheer"));

    when(roleRepository.findActiveMajorAccountEmails()).thenReturn(List.of());
    service.notifyRegistrationCompleted(null);
    verify(emailService, org.mockito.Mockito.times(1))
      .sendNotificationCustomerVerification(any(), any(), any());

    when(roleRepository.findActiveMajorAccountEmails()).thenThrow(new RuntimeException("remote down"));
    assertThatCode(() -> service.notifyRegistrationCompleted(customer)).doesNotThrowAnyException();
  }

  private static UpdateCustomerRequest.UpdateAddressRequest addressRequest() {
    UpdateCustomerRequest.UpdateAddressRequest address = new UpdateCustomerRequest.UpdateAddressRequest();
    address.setRt("01");
    address.setRw("02");
    address.setZipCode("12345");
    return address;
  }
}
