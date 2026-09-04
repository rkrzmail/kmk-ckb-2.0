package com.kmkbe.modules.common.service;

import com.kmkbe.core.annotation.LogMethod;
import com.kmkbe.config.MailConfig;
import com.kmkbe.core.domain.dto.MailRemoteDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.EmailTemplate;
import com.kmkbe.core.domain.model.*;
import com.kmkbe.core.domain.repository.EmailTemplateRepository;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailService {
  private static final int MAX_SENT_FAIL_ATTEMPTS = 2;
  private static final String EMAIL_FROM = "CSUL.Finance@csul.co.id";
  private static final int EMAIL_PRIORITY = 2;

  private static final String M_CUST_NEW_OTP = "M_CUST_NEW_OTP";
  private static final String M_CUST_VERIFY = "M_CUST_VERIFY";
  private static final String M_CUST_CHANGE_OTP = "M_CUST_CHANGE_OTP";
  private static final String M_CUST_ACTIVE = "M_CUST_ACTIVE";
  private static final String M_CUST_REJECTED = "M_CUST_REJECTED";
  private static final String M_CUST_VERIFY_MJR = "M_CUST_VERIFY_MJR";
  private static final String M_CUST_LOAN = "M_CUST_LOAN";//(3)
  private static final String M_CUST_LOAN_SUBMITED = "M_CUST_LOAN_SUBMITED";//(4)
  private static final String M_BRANCH_ASSIGN = "M_BRANCH_ASSIGN";//(2)
  private static final String M_BRANCH_ASSIGN_MJR = "M_BRANCH_ASSIGN_MJR";
  private static final String M_BOUWHEER_PAYMENT = "M_BOUWHEER_PAYMENT";
  private static final String M_BRANCH_CONTRACT_UPLOAD = "M_BRANCH_CONTRACT_UPLOAD";
  private static final String M_CUST_LOAD_CHANGE = "M_CUST_LOAD_CHANGE";
  private static final String M_CUST_PENCAIRAN = "M_CUST_PENCAIRAN";//(5)
  private static final String M_SIM_LOAN = "M_SIM_LOAN";//(1)
  private static final String M_INV_LINK = "M_INV_LINK";


  private final EmailTemplateRepository emailTemplateRepository;
  private final ConfigRemoteService configRemoteService;
  private final MailConfig mailConfig;
  private final ErrorLogRepository errorLogRepository;
  private final Environment environment;

  @Value("${spring.mail.host:}")
  private String mailHost;

  @Value("${spring.mail.port:0}")
  private Integer mailPort;

  @Value("${spring.mail.username:}")
  private String mailUsername;

  @Value("${spring.mail.password:}")
  private String mailPassword;

  @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
  private Boolean mailEnableSSL;

  @Value("${testing.mail.host}")
  private String testingMailHost;

  @Value("${testing.mail.port}")
  private Integer testingMailPort;

  @Value("${testing.mail.username}")
  private String testingMailUsername;

  @Value("${testing.mail.password}")
  private String testingMailPassword;


  @Async
  public void sendPerubahanSimulasi(
    final Customer customer,
    LoanDisburseEmailPayload payload
  ) {
    try {
      EmailTemplate template = emailTemplateRepository.findByEmailTemplateCodeAndIsActive(M_SIM_LOAN, true);

      Map<String, Object> args = new HashMap<>();
      Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);
      if (payloadArgs != null) {
        payloadArgs.remove("invoices");
        payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
      }

      args.put("email", customer.getCustEmail());
      args.put("name", customer.getCustName());
      args.put("id_no", customer.getCustIdNo());
      args.put("additionalArgs", payloadArgs);

      args.put("financingCode", payload.getFinancingCode());
      args.put("companyName", payload.getCompanyName());
      args.put("phoneNumber", payload.getPhoneNumber());
      args.put("applicationDate", payload.getApplicationDate());
      args.put("invoiceAmt", payload.getInvoiceAmt());
      args.put("retention", payload.getRetention());
      args.put("financingAmt", payload.getFinancingAmt());
      args.put("totalFeeAmt", payload.getTotalFeeAmt());
      args.put("disburseAmt", payload.getDisburseAmt());
      args.put("tenor", payload.getTenor());
      args.put("financingDueDate", payload.getFinancingDueDate());

      String bodyMail = mappingBody(template.getBodyMail(), args);

      template.setBodyMail(bodyMail);
      template.setMailTo(customer.getCustEmail());

      sendMailMessage(template, customer.getCustEmail());

    } catch (Exception e) {
      log.error("Error sendNotificationLoanSubmited {}", e.getMessage());
    }
  }

  public EmailService(
    EmailTemplateRepository emailTemplateRepository,
    ConfigRemoteService configRemoteService,
    MailConfig mailConfig,
    ErrorLogRepository errorLogRepository,
    Environment environment
  ) {
    this.emailTemplateRepository = emailTemplateRepository;
    this.configRemoteService = configRemoteService;
    this.mailConfig = mailConfig;
    this.errorLogRepository = errorLogRepository;
    this.environment = environment;
  }

  @Async
  public void sendOtp(Customer customer, String otpCode) {
    try {
      Map<String, Object> obj = new HashMap<>();
      obj.put("name", customer.getCustName());
      obj.put("otp_code", otpCode);
      obj.put("id_no", customer.getCustIdNo());
      obj.put("email", customer.getCustEmail());

      send(customer.getCustEmail(), obj, M_CUST_NEW_OTP);
    } catch (Exception e) {
      log.error("Error sendOtp {}", e.getMessage());
    }
  }

  //new for otp account info
  @Async
  public void sendOtp2(String email, String otpCode) {
    try {
      Map<String, Object> obj = new HashMap<>();
      obj.put("otp_code", otpCode);
      obj.put("email", email);

      send(email, obj, M_CUST_VERIFY);
    } catch (Exception e) {
      log.error("Error sending OTP to email {}: {}", email, e.getMessage());
    }
  }

  @Async
  public void sendOtpChangePin2(String email, String otpCode) {
    try {
      Map<String, Object> obj = new HashMap<>();
      obj.put("otp_code", otpCode);
      obj.put("email", email);

      send(email, obj, M_CUST_VERIFY);
    } catch (Exception e) {
      log.error("Error sendOtpChangePin {}", e.getMessage());
    }
  }

  // done

  @Async
  public void sendOtpChangePin(Customer customer, String otpCode) {
    try {
      Map<String, Object> obj = new HashMap<>();
      obj.put("name", customer.getCustName());
      obj.put("otp_code", otpCode);
      obj.put("id_no", customer.getCustIdNo());
      obj.put("email", customer.getCustEmail());

      send(customer.getCustEmail(), obj, M_CUST_CHANGE_OTP);
    } catch (Exception e) {
      log.error("Error sendOtpChangePin {}", e.getMessage());
    }
  }

  @Async
  public void sendNotificationActive(Customer customer,String note) {
    try {
      Map<String, Object> obj = new HashMap<>();
      obj.put("name", customer.getCustName());
      obj.put("id_no", customer.getCustIdNo());
      obj.put("email", customer.getCustEmail());
      obj.put("note",note);

      send(customer.getCustEmail(), obj, M_CUST_ACTIVE);
    } catch (Exception e) {
      log.error("Error sendNotificationActive {}", e.getMessage());
    }
  }

  @Async
  public void sendNotificationRejected(Customer customer, String approvalNote) {
    try {
      Map<String, Object> obj = new HashMap<>();
      obj.put("name", customer.getCustName());
      obj.put("id_no", customer.getCustIdNo());
      obj.put("email", customer.getCustEmail());
      obj.put("additionalArgs", Map.of(
        "approval_note",
        HtmlUtils.htmlEscape(approvalNote == null || approvalNote.isBlank() ? "-" : approvalNote)
      ));

      send(customer.getCustEmail(), obj, M_CUST_REJECTED);
    } catch (Exception e) {
      log.error("Error sendNotificationRejected for {}", customer.getCustEmail(), e);
    }
  }

  @Async
  public void sendNotificationCustomerVerification(String recipients, Customer customer) {
    try {
      Map<String, Object> args = new HashMap<>();
      args.put("name", customer.getCustName());
      args.put("email", customer.getCustEmail());
      args.put("id_no", customer.getCustIdNo());
      args.put("additionalArgs", Map.of(
        "vendor_code", customer.getCustExternalCode() == null ? "-" : customer.getCustExternalCode(),
        "customer_type", customer.getCustTypeCode() == null ? "-" : customer.getCustTypeCode()
      ));

      send(recipients, args, M_CUST_VERIFY_MJR);
    } catch (Exception e) {
      log.error(
        "sendNotificationCustomerVerification failed. customerCode={}, recipients={}",
        customer == null ? null : customer.getCustCode(),
        recipients,
        e
      );
    }
  }

  @Async
  public void sendNotificationLoanChangeLimit(
    final Customer customer,
    LoanDisburseEmailPayload payload
  ) {
    try {
      Map<String, Object> args = new HashMap<>();
      Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);
      if (payloadArgs != null) {
        payloadArgs.remove("invoices");
        payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
      }

      args.put("email", customer.getCustEmail());
      args.put("name", customer.getCustName());
      args.put("id_no", customer.getCustIdNo());
      args.put("additionalArgs", payloadArgs);

      args.put("nama_perusahaan", customer.getCustIdNo());
      args.put("tanggal_pengajuan", customer.getCustIdNo());
      args.put("no_hp", customer.getCustIdNo());


      args.put("nilai_transaksi", customer.getCustName());
      args.put("nilai_layanan", customer.getCustName());
      args.put("nilai_pembiayaan", customer.getCustName());
      args.put("retensi", customer.getCustIdNo());
      args.put("tenor", customer.getCustIdNo());
      args.put("tanggal_jatuh_tempo", customer.getCustIdNo());
      args.put("total_pencairan", customer.getCustIdNo());


      send(customer.getCustEmail(), args, M_CUST_LOAD_CHANGE);
    } catch (Exception e) {
      log.error("Error sendNotificationChangeLimit {}", e.getMessage());
    }
  }

  @Async
  public void sendNotificationLoanDisbursement(
    final Customer customer,
    LoanDisburseEmailPayload payload
  ) {
    try {
      Map<String, Object> args = new HashMap<>();
      Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);
      if (payloadArgs != null) {
        payloadArgs.remove("invoices");
        payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
      }

      args.put("email", customer.getCustEmail());
      args.put("name", customer.getCustName());
      args.put("id_no", customer.getCustIdNo());
      args.put("additionalArgs", payloadArgs);

      send(customer.getCustEmail(), args, M_CUST_LOAN);
    } catch (Exception e) {
      log.error("Error sendNotificationLoanDisbursement {}", e.getMessage());
    }
  }

//    @Async
//    public void sendNotificationLoanSubmited(
//            final Customer customer,
//            LoanDisburseEmailPayload payload
//    ) {
//        try {
//            Map<String, Object> args = new HashMap<>();
//            Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);
//            if (payloadArgs != null) {
//                payloadArgs.remove("invoices");
//                payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
//            }
//
//            args.put("email", customer.getCustEmail());
//            args.put("name", customer.getCustName());
//            args.put("id_no", customer.getCustIdNo());
//            args.put("additionalArgs", payloadArgs);
//
//            send(customer.getCustEmail(), args, M_CUST_LOAN_SUBMITED);
//        } catch (Exception e) {
//            log.error("Error sendNotificationLoanDisbursement {}", e.getMessage());
//        }
//    }

  @Async
  public void sendNotificationLoanSubmited(
    final Customer customer,
    LoanDisburseEmailPayload payload
  ) {
    try {
      Map<String, Object> args = new HashMap<>();
      Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);
      if (payloadArgs != null) {
        payloadArgs.remove("invoices");
        payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
      }

      args.put("email", customer.getCustEmail());
      args.put("name", customer.getCustName());
      args.put("id_no", customer.getCustIdNo());
      args.put("additionalArgs", payloadArgs);

      EmailTemplate template = emailTemplateRepository
        .findByEmailTemplateCodeAndIsActive(M_CUST_LOAN_SUBMITED, true);
      template.setMailTo(customer.getCustEmail());

//            sendMailMessage(template, customer.getCustEmail());
      send(args, template);
    } catch (Exception e) {
      log.error("Error sendNotificationLoanSubmited {}", e.getMessage());
    }
  }

  @Async
  public void sendNotificationPencairan(
    String email,
    String bouwheerName,
    String branchArea,
    PencarianPayload payload
  ) {
    try {
      Map<String, Object> args = new HashMap<>();
      Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);

      if (payloadArgs != null) {
        payloadArgs.remove("invoices");
        payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
      }

      args.put("additionalArgs", payloadArgs);
      args.put("bouwheerName", bouwheerName);
      args.put("branchArea", branchArea);
      args.put("email", email);

      final EmailTemplate template = emailTemplateRepository
        .findByEmailTemplateCodeAndIsActive(M_CUST_PENCAIRAN, true);
      template.setMailTo(email);
      template.setMailCc(email);

      send(args, template);
    } catch (Exception e) {
      log.error("Error sendNotificationPencairan {}", e.getMessage());
    }
  }

//    @Async
//    public void sendNotificationPencairan(
//            String email,
//            String bouwheerName,
//            String branchArea,
//            PencarianPayload payload
//    ) {
//        try {
//            Map<String, Object> args = new HashMap<>();
//            Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);
//
//            if (payloadArgs != null) {
//                payloadArgs.remove("invoices");
//                payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
//                payloadArgs.remove("invoice_rows");
//                payloadArgs.put("invoice_rows", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
//            }
//
//            args.put("additionalArgs", payloadArgs);
//            args.put("bouwheerName", bouwheerName);
//            args.put("branchArea", branchArea);
//            args.put("email", email);
//
//            final EmailTemplate template = emailTemplateRepository
//                    .findByEmailTemplateCodeAndIsActive(M_CUST_PENCAIRAN, true);
//
//            String subjectMail = template.getSubjectMail().replace("{bouwheerName}", bouwheerName);
//            template.setSubjectMail(subjectMail);
//
//            String bodyEmail = template.getBodyMail();
//            bodyEmail = bodyEmail.replace("{bouwheerName}", bouwheerName)
//                    .replace("{companyName}", payloadArgs.get("companyName").toString())
//                    .replace("{email}", email)
//                    .replace("{invoices}", payloadArgs.get("invoices").toString())
//                    .replace("{invoiceAmt}", payloadArgs.get("invoiceAmt").toString())
//                    .replace("{retention}", payloadArgs.get("retention").toString())
//                    .replace("{financingAmt}", payloadArgs.get("financingAmt").toString())
//                    .replace("{totalFeeAmt}", payloadArgs.get("totalFeeAmt").toString())
//                    .replace("{tenor}", payloadArgs.get("tenor").toString())
//                    .replace("{financingDueDate}", payloadArgs.get("financingDueDate").toString())
//                    .replace("{disburseAmt}", payloadArgs.get("disburseAmt").toString());
//
//            template.setBodyMail(bodyEmail);
//
//            template.setMailTo("tedyaditia047@gmail.com");
//
//            send(args, template);
//        } catch (Exception e) {
//            log.error("Error sendNotificationPencairan {}", e.getMessage());
//        }
//    }

  @Async
  public void sendNotificationBranchAssign(
    String email,
    String bouwheerName,
    String branchArea,
    LoanDisburseEmailPayload payload
  ) {
    try {
      Map<String, Object> args = new HashMap<>();
      Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);

      if (payloadArgs != null) {
        payloadArgs.remove("invoices");
        payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
      }

      args.put("additionalArgs", payloadArgs);
      args.put("bouwheerName", bouwheerName);
      args.put("branchArea", branchArea);
      args.put("email", payload.getEmail());

      final EmailTemplate template = emailTemplateRepository
        .findByEmailTemplateCodeAndIsActive(M_BRANCH_ASSIGN, true);
      template.setSubjectMail(template.getSubjectMail().replace("{bouwheerName}", bouwheerName));
      template.setMailTo(payload.getToEmail());
      template.setMailCc(payload.getCcEmail());

      send(args, template);
    } catch (Exception e) {
      log.error("Error sendNotificationBranchAssign {}", e.getMessage());
    }
  }

  @Async
  public void sendNotificationMajorAccount(
    String email,
    String bouwheerName,
    String branchArea,
    LoanDisburseEmailPayload payload
  ) {
    try {
      Map<String, Object> args = new HashMap<>();
      Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);

      if (payloadArgs != null) {
        payloadArgs.remove("invoices");
        payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
      }

      args.put("additionalArgs", payloadArgs);
      args.put("bouwheerName", bouwheerName);
      args.put("branchArea", branchArea);
      args.put("email", payload.getEmail());

      final EmailTemplate template = emailTemplateRepository
        .findByEmailTemplateCodeAndIsActive(M_BRANCH_ASSIGN_MJR, true);
      template.setSubjectMail(template.getSubjectMail().replace("{bouwheerName}", bouwheerName));
      template.setMailTo(payload.getToEmail());
      template.setMailCc(payload.getCcEmail());

      send(args, template);
    } catch (Exception e) {
      log.error("Error sendNotificationBranchAssign {}", e.getMessage());
    }
  }

  @Async
  public void sendNotificationBouwheerPayment(
    String email,
    BouwheerPaymentEmailPayload payload
  ) {
    try {
      Map<String, Object> args = new HashMap<>();
      Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);

      if (payloadArgs != null) {
        payloadArgs.remove("invoices");
        payloadArgs.put("invoices", InvoiceEmailPayload.toHtmlListBody(payload.getInvoices()));
      }

      args.put("additionalArgs", payloadArgs);

      final EmailTemplate template = emailTemplateRepository
        .findByEmailTemplateCodeAndIsActive(M_BOUWHEER_PAYMENT, true);
      template.setSubjectMail(template.getSubjectMail().replace("{vendorCode}", payload.getVendorCode()));
      template.setMailTo(email);
      template.setBodyMail(payload.bodyMail(template));

      boolean success = false;
      for (int i = 0; i < 3; i++) {
        try {
          send(args, template);
          success = true;
        } catch (Exception e) {
          log.error("failed send sendNotificationBouwheerPayment to {} due to {}", email, e.getMessage());
        }

        if (success) {
          break;
        }
      }
    } catch (Exception e) {
      log.error("Error sendNotificationBouwheerPayment {}", e.getMessage());
    }
  }

  @Async
  public void sendNotificationContractUploadRequired(
    String branchAdminEmails,
    AgreementContractEmailPayload payload
  ) {
    try {
      Map<String, Object> args = new HashMap<>();
      args.put("additionalArgs", ObjectUtils.objectToJson(payload));

      EmailTemplate template = emailTemplateRepository
        .findByEmailTemplateCodeAndIsActive(M_BRANCH_CONTRACT_UPLOAD, true);
      template.setMailTo(branchAdminEmails);
      template.setSubjectMail(template.getSubjectMail().replace("{agreementCode}", payload.getAgreementCode()));

      send(args, template);
    } catch (Exception e) {
      log.error(
        "sendNotificationContractUploadRequired failed. agreementCode={}, recipients={}",
        payload == null ? null : payload.getAgreementCode(),
        branchAdminEmails,
        e
      );
    }
  }

  private void send(
    final String email,
    final Map<String, Object> args,
    final String templateCode
  ) {
    final EmailTemplate template = emailTemplateRepository
      .findByEmailTemplateCodeAndIsActive(templateCode, true);
    {
      template.setMailTo(email);
      template.setBodyMail(mappingBody(template.getBodyMail(), args));
    }

    sendMailMessage(
      template,
      email
    );
  }

  private void send(
    final Map<String, Object> args,
    final EmailTemplate template
  ) {
    template.setBodyMail(mappingBody(template.getBodyMail(), args));
    sendMailMessage(
      template,
      template.getMailTo()
    );
  }

  private String mappingBody(
    String bodyMail,
    final Map<String, Object> args
  ) {
    String body = bodyMail;
    if (args.get("email") != null) {
      body = body.replace("{email}", args.get("email").toString());
    } else {
      body = body.replace("{email}", "");
    }

    if (args.get("name") != null) {
      body = body.replace("{name}", args.get("name").toString());
    } else {
      body = body.replace("{name}", "");
    }

    if (args.get("id_no") != null) {
      body = body.replace("{id_no}", args.get("id_no").toString());
    } else {
      body = body.replace("{id_no}", "");
    }

    if (args.get("otp_code") != null) {
      body = body.replace("{otp_code}", args.get("otp_code").toString());
    } else {
      body = body.replace("{otp_code}", "");
    }

    if (args.get("branchArea") != null) {
      body = body.replace("{branchArea}", args.get("branchArea").toString());
    } else {
      body = body.replace("{branchArea}", "");
    }


    if (args.get("additionalArgs") != null) {
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) args.get("additionalArgs")).entrySet()) {
        body = body.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
      }
    }

    return body;
  }

  // only for testing purpose and if failed to send using intenal mail
  private JavaMailSender testingMailSender() throws MessagingException {
    return mailConfig.javaMailSender(
      testingMailHost,
      testingMailPort,
      testingMailUsername,
      testingMailPassword,
      false
    );
  }

  private MimeMessageHelper mimeMessageTemplate(
    MimeMessage mimeMessage,
    EmailTemplate template
  ) throws MessagingException {
    return mimeMessageTemplate(mimeMessage, template, false);
  }

  private MimeMessageHelper mimeMessageTemplate(
    MimeMessage mimeMessage,
    EmailTemplate template,
    boolean multipart
  ) throws MessagingException {
    final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multipart, "utf-8");
    final boolean isHtml = template.getBodyMail().contains("html");

    helper.setFrom(EMAIL_FROM);
    helper.setTo(template.getMailTo().split(";"));
    helper.setSubject(template.getSubjectMail());
    helper.setText(template.getBodyMail(), isHtml);

    if (template.getMailCc() != null && !template.getMailCc().isEmpty()) {
      helper.setCc(template.getMailCc().split(";"));
    }
    if (template.getMailBcc() != null && !template.getMailBcc().isEmpty()) {
      helper.setBcc(template.getMailBcc().split(";"));
    }

    helper.setPriority(EMAIL_PRIORITY);

    return helper;
  }

  private MailRemoteDto resolveMailConfig() {
    if (isProductionEnvironment() || isDevelopmentEnvironment()) {
      MailRemoteDto remoteMail = configRemoteService.fetchEmailInfo();
      log.info(
        "EmailService using remote mail config host={} port={} username={}",
        remoteMail.getServerUrl(),
        remoteMail.getPort(),
        remoteMail.getUsername()
      );
      return remoteMail;
    }

    log.info("EmailService using yaml mail config host={} port={} username={}", mailHost, mailPort, mailUsername);
    return MailRemoteDto.builder()
      .serverUrl(mailHost)
      .port(mailPort)
      .username(mailUsername)
      .password(mailPassword)
      .enableSSL(Boolean.TRUE.equals(mailEnableSSL))
      .build();
  }

  private boolean isProductionEnvironment() {
    String env = environment.getProperty("env", "");
    return isProductionValue(env) || Arrays.stream(environment.getActiveProfiles()).anyMatch(this::isProductionValue);
  }

  private boolean isDevelopmentEnvironment() {
    String env = environment.getProperty("env", "");
    return isDevelopmentValue(env) || Arrays.stream(environment.getActiveProfiles()).anyMatch(this::isDevelopmentValue);
  }

  private boolean isProductionValue(String value) {
    return "prod".equalsIgnoreCase(value) || "production".equalsIgnoreCase(value);
  }

  private boolean isDevelopmentValue(String value) {
    return "dev".equalsIgnoreCase(value) || "development".equalsIgnoreCase(value);
  }

  @LogMethod
  private boolean sendMailMessage(
    EmailTemplate template,
    String email
  ) {
    try {
      //CsulMailSender csulMailSender = new CsulMailSender(mailConfig, configRemoteService);
      int attempts = 0;
      boolean success = false;
      MailRemoteDto internalMail = resolveMailConfig();
      for (int i = 0; i < MAX_SENT_FAIL_ATTEMPTS; i++) {
        try {
          mailConfig.sendHtmlEmail(
            internalMail,
            template,
            internalMail.getEnableSSL()
          );
          success = true;
        } catch (Exception e) {
          attempts++;
          log.error("EmailService failed to send email to {} on attempt {}", email, attempts, e);
          log.error("EmailService try attempts: {}", attempts);

        }

        if (success) {
          break;
        }
      }

      if (!success) {
        log.info("EmailService send email with testing mail sender");
        JavaMailSender mailSender = testingMailSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        mimeMessageTemplate(mimeMessage, template);
        mailSender.send(mimeMessage);
      }

      return true;
    } catch (Exception e) {
      log.error("sendMailMessage failed for {}", email, e);
      return false;
    }
  }

  @Async
  public void sendInvitationLinkEmail(String toEmail, String invitationLink, String karyawanName) {
    try {
      if (invitationLink == null) {
        throw new IllegalArgumentException("Invitation link cannot be null");
      }

      EmailTemplate template = emailTemplateRepository.findByEmailTemplateCodeAndIsActive(M_INV_LINK, true);

      Map<String, Object> args = new HashMap<>();
      args.put("invitationLink", invitationLink);
      args.put("name", "Customer");

      String bodyMail = template.getBodyMail()
        .replace("${invitationLink}", invitationLink)
        .replace("${name}", karyawanName);

      template.setBodyMail(bodyMail);
      template.setMailTo(toEmail);

      sendMailMessage(template, toEmail);
    } catch (Exception e) {
      log.error("Error sendInvitationLinkEmail: {}", e.getMessage(), e);
    }
  }


  @Async
  public void customerVerification(String mailTo, String company, String ktpNpwp, String opt) {
    String htmlContent = "<div style='font-family: Arial, sans-serif; font-size: 14px; line-height: 1.6; color: #000000;'>"
      + "<p>Hi " + company + ",<br>"
      + "Selamat akun anda telah terdaftar dengan detail informasi:</p>"
      + "<p style='margin-left: 20px;'>"
      + "Email : " + mailTo + "<br>"
      + "No. KTP/NPWP : " + ktpNpwp + "</p>"
      + "<p>Jika informasi di atas telah sesuai, harap memasukkan 4 digit kode OTP di bawah ini di website Dana Sakti untuk memverifikasi akun anda</p>"
      + "<h2 style='font-size: 24px; font-weight: bold; margin: 20px 0;'>" + opt + "</h2>"
      + "<p>Pada saat akun anda telah aktif, silahkan melanjutkan proses transaksi anda.</p>"
      + "<p>Jika anda membutuhkan bantuan, harap hubungi customer service kami melalui email <a href='mailto:help.danasakti@csul.com' style='color: #0000EE;'>help.danasakti@csul.com</a></p>"
      + "<br><br>"
      + "<p>Hormat Kami,</p>"
      + "<p style='color: #2E7D32; font-weight: 500;'>PT. Chandra Sakti Utama Leasing</p>"
      + "<br>"
      + "<!-- Logo CSUL Finance -->"
      + "<div style='margin-top: 10px;'>"
      + "  <span style='font-size: 28px; font-weight: bold; color: #0D47A1;'>CSUL</span>"
      + "  <span style='font-size: 20px; color: #0D47A1; font-style: italic;'>finance</span>"
      + "  <div style='font-size: 10px; color: #4CAF50; letter-spacing: 1px;'>one stop solution financing</div>"
      + "</div>"
      + "</div>";

    EmailTemplate template = new EmailTemplate();
    template.setSubjectMail("Verifikasi Akun Dana Sakti");
    template.setBodyMail(htmlContent);
    sendMailMessage(template, mailTo);
  }
}
