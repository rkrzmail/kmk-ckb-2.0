package com.kmkbe.modules.common.service;

import com.kmkbe.config.MailConfig;
import com.kmkbe.core.domain.dto.MailRemoteDto;
import com.kmkbe.core.domain.entity.EmailTemplate;
import com.kmkbe.core.domain.model.BouwheerPaymentEmailPayload;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.model.LoanDisburseEmailPayload;
import com.kmkbe.core.domain.model.PencarianPayload;
import com.kmkbe.core.domain.repository.EmailTemplateRepository;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private EmailTemplateRepository emailTemplateRepository;
  @Mock private ConfigRemoteService configRemoteService;
  @Mock private MailConfig mailConfig;
  @Mock private ErrorLogRepository errorLogRepository;
  @Mock private JavaMailSender testingMailSender;

  private EmailService service;

  @BeforeEach
  void setUp() {
    service = new EmailService(
      emailTemplateRepository,
      configRemoteService,
      mailConfig,
      errorLogRepository,
      new MockEnvironment().withProperty("env", "prod")
    );
    ReflectionTestUtils.setField(service, "testingMailHost", "smtp.test");
    ReflectionTestUtils.setField(service, "testingMailPort", 2525);
    ReflectionTestUtils.setField(service, "testingMailUsername", "test-user");
    ReflectionTestUtils.setField(service, "testingMailPassword", "test-pass");
  }

  @Test
  void sendOtpMapsTemplateAndSendsInternalMail() throws Exception {
    Customer customer = customer();
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_NEW_OTP", true))
        .thenReturn(template("M_CUST_NEW_OTP", "Hi {name} {email} {id_no} {otp_code}"));
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(true));
    doNothing().when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));

    service.sendOtp(customer, "1234");

    ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
    verify(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), captor.capture(), eq(true));
    EmailTemplate sent = captor.getValue();
    assertThat(sent.getMailTo()).isEqualTo("customer@example.com");
    assertThat(sent.getBodyMail()).isEqualTo("Hi Customer customer@example.com KTP001 1234");
  }

  @Test
  void sendOtpUsesYamlMailConfigForNonProductionEnvironment() throws Exception {
    EmailService localService = new EmailService(
      emailTemplateRepository,
      configRemoteService,
      mailConfig,
      errorLogRepository,
      new MockEnvironment().withProperty("env", "local")
    );
    ReflectionTestUtils.setField(localService, "mailHost", "smtp.local");
    ReflectionTestUtils.setField(localService, "mailPort", 587);
    ReflectionTestUtils.setField(localService, "mailUsername", "local-user");
    ReflectionTestUtils.setField(localService, "mailPassword", "local-pass");
    ReflectionTestUtils.setField(localService, "mailEnableSSL", true);

    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_VERIFY", true))
      .thenReturn(template("M_CUST_VERIFY", "{email}:{otp_code}"));
    doNothing().when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));

    localService.sendOtp2("user@example.com", "1111");

    ArgumentCaptor<MailRemoteDto> mailCaptor = ArgumentCaptor.forClass(MailRemoteDto.class);
    verify(mailConfig).sendHtmlEmail(mailCaptor.capture(), any(EmailTemplate.class), eq(true));
    verify(configRemoteService, never()).fetchEmailInfo();

    MailRemoteDto mail = mailCaptor.getValue();
    assertThat(mail.getServerUrl()).isEqualTo("smtp.local");
    assertThat(mail.getPort()).isEqualTo(587);
    assertThat(mail.getUsername()).isEqualTo("local-user");
    assertThat(mail.getPassword()).isEqualTo("local-pass");
    assertThat(mail.getEnableSSL()).isTrue();
  }

  @Test
  void sendOtp2AndChangePin2UseVerificationTemplate() throws Exception {
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_VERIFY", true))
        .thenReturn(template("M_CUST_VERIFY", "{email}:{otp_code}"))
        .thenReturn(template("M_CUST_VERIFY", "{email}:{otp_code}"));
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(false));
    doNothing().when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(false));

    service.sendOtp2("user@example.com", "1111");
    service.sendOtpChangePin2("user@example.com", "2222");

    ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
    verify(mailConfig, org.mockito.Mockito.times(2)).sendHtmlEmail(any(MailRemoteDto.class), captor.capture(), eq(false));
    assertThat(captor.getAllValues()).extracting(EmailTemplate::getBodyMail)
        .containsExactly("user@example.com:1111", "user@example.com:2222");
  }

  @Test
  void notificationMethodsMapTemplatesRecipientsSubjectAndAdditionalArgs() throws Exception {
    Customer customer = customer();
    LoanDisburseEmailPayload loanPayload = loanPayload();
    PencarianPayload pencarianPayload = pencarianPayload();
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(true));
    doNothing().when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_ACTIVE", true))
        .thenReturn(template("M_CUST_ACTIVE", "{name}|{email}|{id_no}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_CHANGE_OTP", true))
        .thenReturn(template("M_CUST_CHANGE_OTP", "{otp_code}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAN", true))
        .thenReturn(template("M_CUST_LOAN", "{companyName}|{invoices}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAN_SUBMITED", true))
        .thenReturn(template("M_CUST_LOAN_SUBMITED", "{companyName}|{invoices}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_PENCAIRAN", true))
        .thenReturn(template("M_CUST_PENCAIRAN", "{branchArea}|{companyName}|{invoices}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_BRANCH_ASSIGN", true))
        .thenReturn(templateWithSubject("M_BRANCH_ASSIGN", "Assign {bouwheerName}", "{branchArea}|{companyName}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_BRANCH_ASSIGN_MJR", true))
        .thenReturn(templateWithSubject("M_BRANCH_ASSIGN_MJR", "Major {bouwheerName}", "{branchArea}|{companyName}"));

    service.sendNotificationActive(customer);
    service.sendOtpChangePin(customer, "3333");
    service.sendNotificationLoanDisbursement(customer, loanPayload);
    service.sendNotificationLoanSubmited(customer, loanPayload);
    service.sendNotificationPencairan("pencairan@example.com", "Bouwheer", "Jakarta", pencarianPayload);
    service.sendNotificationBranchAssign("ignored@example.com", "Bouwheer", "Jakarta", loanPayload);
    service.sendNotificationMajorAccount("ignored@example.com", "Bouwheer", "Jakarta", loanPayload);

    ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
    verify(mailConfig, org.mockito.Mockito.times(7)).sendHtmlEmail(any(MailRemoteDto.class), captor.capture(), eq(true));
    List<EmailTemplate> sent = captor.getAllValues();
    assertThat(sent.get(0).getBodyMail()).isEqualTo("Customer|customer@example.com|KTP001");
    assertThat(sent.get(1).getBodyMail()).isEqualTo("3333");
    assertThat(sent.get(2).getBodyMail()).contains("Company").contains("INV001");
    assertThat(sent.get(4).getMailTo()).isEqualTo("pencairan@example.com");
    assertThat(sent.get(4).getMailCc()).isEqualTo("pencairan@example.com");
    assertThat(sent.get(5).getSubjectMail()).isEqualTo("Assign Bouwheer");
    assertThat(sent.get(5).getMailTo()).isEqualTo("to@example.com");
    assertThat(sent.get(5).getMailCc()).isEqualTo("cc@example.com");
    assertThat(sent.get(6).getSubjectMail()).isEqualTo("Major Bouwheer");
  }

  @Test
  void sendPerubahanSimulasiAndLoanChangeLimitUseExpectedTemplates() throws Exception {
    Customer customer = customer();
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(true));
    doNothing().when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_SIM_LOAN", true))
        .thenReturn(template("M_SIM_LOAN", "{companyName}|{invoiceAmt}|{invoices}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAD_CHANGE", true))
        .thenReturn(template("M_CUST_LOAD_CHANGE", "{name}|{additionalArgs}|{invoices}"));

    service.sendPerubahanSimulasi(customer, loanPayload());
    service.sendNotificationLoanChangeLimit(customer, loanPayload());

    ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
    verify(mailConfig, org.mockito.Mockito.times(2)).sendHtmlEmail(any(MailRemoteDto.class), captor.capture(), eq(true));
    assertThat(captor.getAllValues().get(0).getMailTo()).isEqualTo("customer@example.com");
    assertThat(captor.getAllValues().get(0).getBodyMail()).contains("Company").contains("1000").contains("INV001");
    assertThat(captor.getAllValues().get(1).getBodyMail()).contains("Customer");
  }

  @Test
  void sendNotificationBouwheerPaymentBuildsBodyAndStopsAfterSuccess() throws Exception {
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(true));
    doNothing().when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_BOUWHEER_PAYMENT", true))
        .thenReturn(templateWithSubject("M_BOUWHEER_PAYMENT", "Payment {vendorCode}", "{bouwheerName}|{vendorName}|{vendorCode}|{accountNo}|{bankAccount}|{bankName}|{bankKey}|{tglPengajuan}"));

    service.sendNotificationBouwheerPayment("vendor@example.com", bouwheerPaymentPayload());

    ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
    verify(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), captor.capture(), eq(true));
    EmailTemplate sent = captor.getValue();
    assertThat(sent.getSubjectMail()).isEqualTo("Payment V001");
    assertThat(sent.getMailTo()).isEqualTo("vendor@example.com");
    assertThat(sent.getBodyMail()).isEqualTo("Bouwheer|Vendor|V001|123|Account|Bank|BK|01/01/2026");
  }

  @Test
  void sendMailMessageFallsBackToTestingMailSenderWhenInternalMailFails() throws Exception {
    Customer customer = customer();
    EmailTemplate fallbackTemplate = template("M_CUST_NEW_OTP", "<html>{email}</html>");
    fallbackTemplate.setMailCc("cc1@example.com;cc2@example.com");
    fallbackTemplate.setMailBcc("bcc1@example.com;bcc2@example.com");
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_NEW_OTP", true))
        .thenReturn(fallbackTemplate);
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(true));
    doThrow(new RuntimeException("internal down"))
        .when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
    when(mailConfig.javaMailSender("smtp.test", 2525, "test-user", "test-pass", false)).thenReturn(testingMailSender);
    MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
    when(testingMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(testingMailSender).send(any(MimeMessage.class));

    service.sendOtp(customer, "1234");

    verify(mailConfig, org.mockito.Mockito.times(2)).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
    verify(testingMailSender).send(mimeMessage);
  }

  @Test
  void publicMethodsSwallowNullPayloadAndTemplateErrors() throws Exception {
    Customer customer = customer();
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(true));
    doNothing().when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_SIM_LOAN", true))
        .thenReturn(template("M_SIM_LOAN", "{companyName}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAD_CHANGE", true))
        .thenReturn(template("M_CUST_LOAD_CHANGE", "{name}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAN", true))
        .thenReturn(template("M_CUST_LOAN", "{email}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAN_SUBMITED", true))
        .thenReturn(template("M_CUST_LOAN_SUBMITED", "{email}"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_PENCAIRAN", true))
        .thenReturn(template("M_CUST_PENCAIRAN", "{email}"));

    service.sendPerubahanSimulasi(customer, null);
    service.sendNotificationLoanChangeLimit(customer, null);
    service.sendNotificationLoanDisbursement(customer, null);
    service.sendNotificationLoanSubmited(customer, null);
    service.sendNotificationPencairan("to@example.com", "Bouwheer", "Jakarta", null);
    service.sendNotificationBranchAssign("to@example.com", "Bouwheer", "Jakarta", null);
    service.sendNotificationMajorAccount("to@example.com", "Bouwheer", "Jakarta", null);
    service.sendNotificationBouwheerPayment("to@example.com", null);

    verify(mailConfig, org.mockito.Mockito.times(4)).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
  }

  @Test
  void loanChangeLimitSwallowsTemplateFailure() {
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAD_CHANGE", true))
        .thenThrow(new RuntimeException("change limit template down"));

    service.sendNotificationLoanChangeLimit(customer(), loanPayload());
  }

  @Test
  void simpleNotificationMethodsSwallowRepositoryFailures() {
    Customer customer = customer();
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_VERIFY", true))
        .thenThrow(new RuntimeException("verify template down"))
        .thenThrow(new RuntimeException("change template down"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_CHANGE_OTP", true))
        .thenThrow(new RuntimeException("otp change template down"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_ACTIVE", true))
        .thenThrow(new RuntimeException("active template down"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAN", true))
        .thenThrow(new RuntimeException("loan template down"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_LOAN_SUBMITED", true))
        .thenThrow(new RuntimeException("submitted template down"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_PENCAIRAN", true))
        .thenThrow(new RuntimeException("pencairan template down"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_BRANCH_ASSIGN", true))
        .thenThrow(new RuntimeException("branch template down"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_BRANCH_ASSIGN_MJR", true))
        .thenThrow(new RuntimeException("major template down"));
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_BOUWHEER_PAYMENT", true))
        .thenThrow(new RuntimeException("payment template down"));

    service.sendOtp2("to@example.com", "1111");
    service.sendOtpChangePin2("to@example.com", "2222");
    service.sendOtpChangePin(customer, "3333");
    service.sendNotificationActive(customer);
    service.sendNotificationLoanDisbursement(customer, loanPayload());
    service.sendNotificationLoanSubmited(customer, loanPayload());
    service.sendNotificationPencairan("to@example.com", "Bouwheer", "Jakarta", pencarianPayload());
    service.sendNotificationBranchAssign("to@example.com", "Bouwheer", "Jakarta", loanPayload());
    service.sendNotificationMajorAccount("to@example.com", "Bouwheer", "Jakarta", loanPayload());
    service.sendNotificationBouwheerPayment("to@example.com", bouwheerPaymentPayload());
  }

  @Test
  void bouwheerPaymentRetriesWhenTemplateBodyMappingFails() {
    EmailTemplate brokenTemplate = templateWithSubject("M_BOUWHEER_PAYMENT", "Payment {vendorCode}", null);
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_BOUWHEER_PAYMENT", true))
        .thenReturn(brokenTemplate);

    service.sendNotificationBouwheerPayment("vendor@example.com", bouwheerPaymentPayload());

    verify(configRemoteService, never()).fetchEmailInfo();
  }

  @Test
  void bouwheerPaymentRetriesWhenSendMappingFailsInsideLoop() {
    EmailTemplate template = org.mockito.Mockito.spy(templateWithSubject("M_BOUWHEER_PAYMENT", "Payment {vendorCode}", "{bouwheerName}"));
    org.mockito.Mockito.doReturn("{bouwheerName}").doReturn(null).when(template).getBodyMail();
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_BOUWHEER_PAYMENT", true))
        .thenReturn(template);

    service.sendNotificationBouwheerPayment("vendor@example.com", bouwheerPaymentPayload());

    verify(configRemoteService, never()).fetchEmailInfo();
  }

  @Test
  void fallbackMailSenderHandlesEmptyCcAndBcc() throws Exception {
    Customer customer = customer();
    EmailTemplate fallbackTemplate = template("M_CUST_NEW_OTP", "<html>{email}</html>");
    fallbackTemplate.setMailCc("");
    fallbackTemplate.setMailBcc("");
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_NEW_OTP", true))
        .thenReturn(fallbackTemplate);
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(true));
    doThrow(new RuntimeException("internal down"))
        .when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
    when(mailConfig.javaMailSender("smtp.test", 2525, "test-user", "test-pass", false)).thenReturn(testingMailSender);
    MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
    when(testingMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(testingMailSender).send(any(MimeMessage.class));

    service.sendOtp(customer, "1234");

    verify(testingMailSender).send(mimeMessage);
  }

  @Test
  void fallbackMailSenderHandlesNullCcAndBcc() throws Exception {
    Customer customer = customer();
    EmailTemplate fallbackTemplate = template("M_CUST_NEW_OTP", "<html>{email}</html>");
    fallbackTemplate.setMailCc(null);
    fallbackTemplate.setMailBcc(null);
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_NEW_OTP", true))
        .thenReturn(fallbackTemplate);
    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(true));
    doThrow(new RuntimeException("internal down"))
        .when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(true));
    when(mailConfig.javaMailSender("smtp.test", 2525, "test-user", "test-pass", false)).thenReturn(testingMailSender);
    MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
    when(testingMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(testingMailSender).send(any(MimeMessage.class));

    service.sendOtp(customer, "1234");

    verify(testingMailSender).send(mimeMessage);
  }

  @Test
  void sendMethodsSwallowTemplateAndMailErrors() throws Exception {
    Customer customer = customer();
    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_CUST_NEW_OTP", true)).thenThrow(new RuntimeException("template down"));
    service.sendOtp(customer, "1234");
    verify(configRemoteService, never()).fetchEmailInfo();

    when(emailTemplateRepository.findByEmailTemplateCodeAndIsActive("M_INV_LINK", true))
        .thenReturn(template("M_INV_LINK", "${name}:${invitationLink}"));
    when(configRemoteService.fetchEmailInfo()).thenThrow(new RuntimeException("mail config down"));
    service.sendInvitationLinkEmail("to@example.com", "https://invite", "Signer");
  }

  @Test
  void sendInvitationLinkEmailRejectsNullLinkAndCustomerVerificationSendsInlineTemplate() throws Exception {
    service.sendInvitationLinkEmail("to@example.com", null, "Signer");
    verify(emailTemplateRepository, never()).findByEmailTemplateCodeAndIsActive("M_INV_LINK", true);

    when(configRemoteService.fetchEmailInfo()).thenReturn(mailRemote(false));
    doNothing().when(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), any(EmailTemplate.class), eq(false));

    service.customerVerification("mail@example.com", "Company", "KTP001", "9999");

    ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
    verify(mailConfig).sendHtmlEmail(any(MailRemoteDto.class), captor.capture(), eq(false));
    assertThat(captor.getValue().getSubjectMail()).isEqualTo("Verifikasi Akun Dana Sakti");
    assertThat(captor.getValue().getBodyMail()).contains("Company").contains("KTP001").contains("9999");
  }

  private static EmailTemplate template(String code, String body) {
    return templateWithSubject(code, "Subject", body);
  }

  private static EmailTemplate templateWithSubject(String code, String subject, String body) {
    EmailTemplate template = new EmailTemplate();
    template.setEmailTemplateCode(code);
    template.setSubjectMail(subject);
    template.setBodyMail(body);
    template.setIsActive(true);
    return template;
  }

  private static MailRemoteDto mailRemote(boolean ssl) {
    return MailRemoteDto.builder()
        .serverUrl("smtp.internal")
        .port(25)
        .username("internal")
        .password("secret")
        .enableSSL(ssl)
        .build();
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustName("Customer");
    customer.setCustEmail("customer@example.com");
    customer.setCustIdNo("KTP001");
    return customer;
  }

  private static LoanDisburseEmailPayload loanPayload() {
    return LoanDisburseEmailPayload.builder()
        .financingCode("FIN001")
        .companyName("Company")
        .phoneNumber("08123")
        .email("payload@example.com")
        .toEmail("to@example.com")
        .ccEmail("cc@example.com")
        .applicationDate("01/01/2026")
        .invoiceAmt("1000")
        .retention("100")
        .financingAmt("900")
        .totalFeeAmt("10")
        .disburseAmt("890")
        .tenor(30L)
        .financingDueDate("31/01/2026")
        .invoices(List.of(invoice()))
        .build();
  }

  private static PencarianPayload pencarianPayload() {
    return PencarianPayload.builder()
        .financingCode("FIN001")
        .companyName("Company")
        .phoneNumber("08123")
        .applicationDate("01/01/2026")
        .invoiceAmt("1000")
        .retention("100")
        .financingAmt("900")
        .totalFeeAmt("10")
        .disburseAmt("890")
        .tenor(30L)
        .financingDueDate("31/01/2026")
        .invoices(List.of(invoice()))
        .build();
  }

  private static BouwheerPaymentEmailPayload bouwheerPaymentPayload() {
    return BouwheerPaymentEmailPayload.builder()
        .bouwheerName("Bouwheer")
        .vendorName("Vendor")
        .vendorCode("V001")
        .accountNo("123")
        .bankAccount("Account")
        .bankName("Bank")
        .bankKey("BK")
        .tglPengajuan("01/01/2026")
        .invoices(List.of(invoice()))
        .build();
  }

  private static InvoiceEmailPayload invoice() {
    return InvoiceEmailPayload.builder()
        .invoiceNo("INV001")
        .description("Desc")
        .bouwheerName("Bouwheer")
        .invoiceDate("01/01/2026")
        .invoiceDueDate("31/01/2026")
        .invoiceAmt("1000")
        .build();
  }
}
