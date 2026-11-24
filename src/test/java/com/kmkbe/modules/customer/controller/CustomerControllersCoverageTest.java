package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.CustomerDashboardDto;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.entity.EmailDeliveryLog;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.EmailDeliveryService;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.ApprovalRequest;
import com.kmkbe.modules.customer.model.request.ForgotPinRequest;
import com.kmkbe.modules.customer.model.request.LoginRequest;
import com.kmkbe.modules.customer.model.request.RequestOtpRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.model.request.UpdateFapRequest;
import com.kmkbe.modules.customer.model.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.service.*;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllersCoverageTest {
  @Mock CustomerService customerService;
  @Mock CustomerCompanyService customerCompanyService;
  @Mock CustomerPersonalService customerPersonalService;
  @Mock CustomerDashboardService customerDashboardService;
  @Mock InvoiceService invoiceService;
  @Mock CustomerDashboardListService customerDashboardListService;
  @Mock DocumentService documentService;
  @Mock CustomerRepository customerRepository;
  @Mock CurrentUserService currentUserService;
  @Mock BouwheerRepository bouwheerRepository;
  @Mock AuthService authService;
  @Mock SecurityContextLogoutHandler logoutHandler;
  @Mock RedisAttackRepository redisAttackRepository;
  @Mock com.kmkbe.core.domain.repository.RedisRepository redisRepository;
  @Mock OtpService otpService;
  @Mock EmailDeliveryService emailDeliveryService;
  @Mock MstAppRoleFormUserRepository roleRepository;

  CustomerController customerController;
  AuthController authController;
  OtpController otpController;
  CustomerAgreementController agreementController;
  ApprovalEmailDeliveryController approvalController;

  final CommonResult commonResult = new CommonResult().success(null);
  final BaseResponseBuilder baseResponse = new BaseResponseBuilder(true, 200, "Success", null);

  @BeforeEach
  void setUp() {
    customerController = new CustomerController(customerService, customerCompanyService,
      customerPersonalService, customerDashboardService, invoiceService, customerDashboardListService,
      documentService, customerRepository, currentUserService, bouwheerRepository);
    authController = new AuthController(authService, logoutHandler, redisAttackRepository, currentUserService);
    otpController = new OtpController(otpService, redisRepository, redisAttackRepository);
    agreementController = new CustomerAgreementController(customerDashboardService, currentUserService);
    approvalController = new ApprovalEmailDeliveryController(emailDeliveryService, currentUserService, roleRepository);
  }

  @Test
  void customerControllerDelegatesReadEndpoints() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    Customer customer = customer("OTHER");
    when(customerService.profile(request)).thenReturn(commonResult);
    when(currentUserService.customer()).thenReturn(customer);
    when(customerService.prolifeFAP(request)).thenReturn(null);
    when(customerService.prolifeSIT(request)).thenReturn(null);
    when(invoiceService.customerActiveInvoices(any())).thenReturn(PaginationResult.empty(1));
    when(customerDashboardListService.listinvoicesduedate(eq(customer), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
    when(customerDashboardListService.listcreditfacilities(eq(customer), any())).thenReturn(PaginationResult.empty(1));
    when(customerDashboardService.plafondByFinancingHdrCode("F")).thenReturn(baseResponse);
    when(customerDashboardService.plafond()).thenReturn(baseResponse);
    when(documentService.uploadedCustomerDoc(eq("C1"), eq(request), any())).thenReturn(PaginationResult.empty(1));
    when(customerDashboardService.mainDashboard()).thenReturn(baseResponse);
    when(customerDashboardService.perjanjian("F")).thenReturn(null);
    when(customerDashboardService.getNotifDebtors("C1")).thenReturn(List.of());
    when(customerService.pages(any())).thenReturn(baseResponse);
    when(customerService.findByCustomerCode("C1")).thenReturn(baseResponse);
    when(customerService.approval(any(), eq("maker"))).thenReturn(baseResponse);
    when(currentUserService.internalUsername()).thenReturn("maker");

    assertThat(customerController.profile(request)).isSameAs(commonResult);
    assertThat(customerController.getProfileFap(request).isSuccess()).isTrue();
    assertThat(customerController.getProfileSit(request).isSuccess()).isTrue();
    assertThat(customerController.getPostedInvoices(new com.kmkbe.core.domain.request.PaginationRequest()).isSuccess()).isTrue();
    assertThat(customerController.getPostedInvoicesDue(new BasePaginationRequest()).isSuccess()).isTrue();
    assertThat(customerController.getActiveCreditFacilities(new BasePaginationRequest()).isSuccess()).isTrue();
    assertThat(customerController.getPlafond("F")).isSameAs(baseResponse);
    assertThat(customerController.getPlafondCustomer()).isSameAs(baseResponse);
    assertThat(customerController.getDocuments("C1", new com.kmkbe.core.domain.request.PaginationRequest(), request).isSuccess()).isTrue();
    assertThat(customerController.getDashboard()).isSameAs(baseResponse);
    assertThat(customerController.getPerjanjian("F").isSuccess()).isTrue();
    assertThat(customerController.getAllNotifDebtors("C1").isSuccess()).isTrue();
    assertThat(customerController.deleteAllNotifDebtors("C1").isSuccess()).isTrue();
    assertThat(customerController.getAllCustomers(new BasePaginationRequest())).isSameAs(baseResponse);
    assertThat(customerController.getCustomerCode("C1")).isSameAs(baseResponse);
    assertThat(customerController.approvalCustomer(new ApprovalRequest())).isSameAs(baseResponse);
    verify(currentUserService).authenticatedInternalUser();
  }

  @Test
  void customerControllerUpdateBranches() throws Exception {
    Customer customer = customer("OTHER");
    when(currentUserService.customer()).thenReturn(customer);
    when(customerService.update(eq(customer), any())).thenReturn(customer);

    assertThat(customerController.updateCustomer(new UpdateCustomerRequest()).isSuccess()).isTrue();

    customer.setCustTypeCode(CustomerType.Company.name());
    UpdateCustomerRequest companyRequest = new UpdateCustomerRequest();
    companyRequest.setAddress(new UpdateCustomerRequest.UpdateAddressRequest());
    companyRequest.getAddress().setZipCode("12345");
    UpdateCustomerRequest.UpdateCompanyRequest company = new UpdateCustomerRequest.UpdateCompanyRequest();
    company.setIdentityNo("ID");
    company.setCompanyAddress("Address");
    company.setIdentityIssuedDate(new Date());
    company.setOwnershipStatus("OWNED");
    company.setCustCompanyType("PT");
    company.setStaySince(Date.from(Instant.now().minusSeconds(86_400)));
    companyRequest.setCompany(company);
    when(customerCompanyService.update(eq(customer), any(), any())).thenReturn(new CustomerCompany());
    assertThat(customerController.updateCustomer(companyRequest).isSuccess()).isTrue();

    customer.setCustTypeCode(CustomerType.Personal.name());
    UpdateCustomerRequest personalRequest = new UpdateCustomerRequest();
    personalRequest.setPersonal(new UpdateCustomerRequest.UpdatePersonalRequest());
    when(customerPersonalService.update(eq(customer), any(), any())).thenReturn(new CustomerPersonal());
    assertThat(customerController.updateCustomer(personalRequest).isSuccess()).isTrue();

    UpdateCustomerRequest missingCompany = new UpdateCustomerRequest();
    missingCompany.setAddress(new UpdateCustomerRequest.UpdateAddressRequest());
    customer.setCustTypeCode(CustomerType.Company.name());
    assertThatThrownBy(() -> customerController.updateCustomer(missingCompany))
      .isInstanceOf(IllegalArgumentException.class).hasMessage("Company cannot be null");
    customer.setCustTypeCode(CustomerType.Personal.name());
    assertThatThrownBy(() -> customerController.updateCustomer(new UpdateCustomerRequest()))
      .isInstanceOf(IllegalArgumentException.class).hasMessage("Personal cannot be null");
  }

  @Test
  void customerControllerUpdateFapData() throws Exception {
    when(currentUserService.customer()).thenReturn(customer("OTHER"));
    doNothing().when(customerService).updateFapData(any(), any());
    assertThat(customerController.updateFapData(new UpdateFapRequest()).getStatusCode().value()).isEqualTo(200);

    doThrow(new RuntimeException("bad")).when(customerService).updateFapData(any(), any());
    assertThat(customerController.updateFapData(new UpdateFapRequest()).getStatusCode().value()).isEqualTo(400);
  }

  @Test
  void authControllerDelegatesAndHandlesForgotPin() throws Exception {
    SignUpRequest signUp = new SignUpRequest();
    LoginRequest login = new LoginRequest("e@x.com", "1234");
    ForgotPinRequest forgot = new ForgotPinRequest("e@x.com", "1234", "token");
    RefreshTokenRequest refresh = new RefreshTokenRequest("token");

    when(authService.signUp(signUp)).thenReturn(commonResult);
    when(authService.signIn(login)).thenReturn(baseResponse);
    when(authService.forgotPin(forgot)).thenReturn("sent");
    when(authService.logout(any())).thenReturn("logged out");
    when(authService.refreshToken(refresh)).thenReturn(null);
    when(currentUserService.customer()).thenReturn(customer("OTHER"));

    assertThat(authController.signUp(signUp)).isSameAs(commonResult);
    assertThat(authController.signIn(login)).isSameAs(baseResponse);
    assertThat(authController.forgotPin(forgot).isSuccess()).isTrue();
    assertThat(authController.signOut(mock(HttpServletRequest.class), mock(HttpServletResponse.class)).isSuccess()).isTrue();
    assertThat(authController.refreshToken(refresh).isSuccess()).isTrue();
    verify(logoutHandler).logout(any(), any(), isNull());

    RedisAttack attack = RedisAttack.builder().countAttack(5).modifiedDate(Date.from(Instant.now())).build();
    when(redisAttackRepository.findTopByRedis("forgot:e@x.com")).thenReturn(Optional.of(attack));
    assertThatThrownBy(() -> authController.forgotPin(forgot)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void otpControllerDelegatesAndHandlesAttackBranches() throws Exception {
    VerifyOtpRequest verify = new VerifyOtpRequest("req", "e@x.com", "1234", null);
    RequestOtpRequest requestOtp = new RequestOtpRequest("e@x.com");
    when(otpService.verifySignUp(verify)).thenReturn("verified");
    when(otpService.verifyForgotPin(verify)).thenReturn("verified");
    when(otpService.sendForgotPin("e@x.com")).thenReturn(null);
    when(otpService.resend(eq(requestOtp), any())).thenReturn(null);
    when(otpService.resendOtpForEmail("e@x.com", OtpService.OtpType.SIGNUP)).thenReturn(null);
    when(otpService.verifyOtp("e@x.com", "1234")).thenReturn(null);

    assertThat(otpController.verifySignUp(verify).getMessage()).isEqualTo("verified");
    assertThat(otpController.verifyForgotPin(verify).isSuccess()).isTrue();
    assertThat(otpController.sendForgotPin(requestOtp).isSuccess()).isTrue();
    assertThat(otpController.resendSignUpOtp(requestOtp).isSuccess()).isTrue();
    assertThat(otpController.resendForgotPinOtp(requestOtp).isSuccess()).isTrue();
    assertThat(otpController.generateOtp(requestOtp).isSuccess()).isTrue();
    assertThat(otpController.resendOtp(requestOtp).isSuccess()).isTrue();
    assertThat(otpController.verifyOtp(verify).isSuccess()).isTrue();

    when(redisAttackRepository.findTopByRedis(anyString())).thenReturn(Optional.empty());
    assertThat(otpController.verifySignUp(verify).isSuccess()).isTrue();
    assertThat(otpController.verifyForgotPin(verify).isSuccess()).isTrue();

    RedisAttack attack = RedisAttack.builder().countAttack(5).modifiedDate(Date.from(Instant.now())).build();
    when(redisAttackRepository.findTopByRedis(anyString())).thenReturn(Optional.of(attack));
    assertThatThrownBy(() -> otpController.verifySignUp(verify)).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> otpController.verifyForgotPin(verify)).isInstanceOf(RuntimeException.class);

    RedisAttack oldAttack = RedisAttack.builder().countAttack(5)
      .modifiedDate(Date.from(Instant.now().minusSeconds(1200))).build();
    when(redisAttackRepository.findTopByRedis(anyString())).thenReturn(Optional.of(oldAttack));
    assertThat(otpController.verifySignUp(verify).isSuccess()).isTrue();
    assertThat(otpController.verifyForgotPin(verify).isSuccess()).isTrue();

    doThrow(new RuntimeException("boom")).when(otpService).resendOtpForEmail(anyString(), any());
    assertThat(otpController.generateOtp(requestOtp).isSuccess()).isFalse();
    assertThat(otpController.resendOtp(requestOtp).isSuccess()).isFalse();
    doThrow(new RuntimeException("boom")).when(otpService).verifyOtp(anyString(), anyString());
    assertThat(otpController.verifyOtp(verify).isSuccess()).isFalse();
  }

  @Test
  void agreementAndApprovalControllers() throws Exception {
    when(currentUserService.customer()).thenReturn(customer("OTHER"));
    when(customerDashboardService.agreementDashboard()).thenReturn(mock(CustomerDashboardDto.Agreement.class));
    assertThat(agreementController.getAgreementDashboard().isSuccess()).isTrue();
    assertThat(agreementController.getSignerPersons(new PaginationRequest()).isSuccess()).isTrue();
    assertThat(agreementController.getFinancing(new PaginationRequest()).isSuccess()).isTrue();
    assertThat(agreementController.postUploadSigning("1").isSuccess()).isTrue();

    MstUser user = new MstUser();
    user.setUserCode(UUID.randomUUID());
    when(currentUserService.internalUser()).thenReturn(user);
    when(roleRepository.hasActiveMajorAccountRole(user.getUserCode())).thenReturn(true);
    when(emailDeliveryService.history(any())).thenReturn(List.of());
    assertThat(approvalController.history(UUID.randomUUID())).isEmpty();

    EmailDeliveryLog delivery = new EmailDeliveryLog();
    delivery.setStatus(EmailDeliveryLog.Status.SENT);
    delivery.setTemplateCode("M_CUST_ACTIVE");
    when(emailDeliveryService.retry(1L)).thenReturn(delivery);
    assertThat(((BaseResponseBuilder) approvalController.retry(1L)).getMessage())
      .isEqualTo("Email berhasil diterima server SMTP.");

    delivery.setStatus(EmailDeliveryLog.Status.FAILED);
    delivery.setTemplateCode("OTHER");
    assertThat(((BaseResponseBuilder) approvalController.retry(1L)).getMessage())
      .contains("belum berhasil");

    when(roleRepository.hasActiveMajorAccountRole(user.getUserCode())).thenReturn(false);
    assertThatThrownBy(() -> approvalController.history(UUID.randomUUID())).isInstanceOf(RuntimeException.class);
  }

  private static Customer customer(String typeCode) {
    Customer customer = new Customer();
    customer.setCustTypeCode(typeCode);
    customer.setCustCode(UUID.randomUUID());
    customer.setCustName("Customer");
    return customer;
  }
}
