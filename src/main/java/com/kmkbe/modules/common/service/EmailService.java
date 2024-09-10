package com.kmkbe.modules.common.service;

import com.kmkbe.core.annotation.LogMethod;
import com.kmkbe.core.config.MailConfig;
import com.kmkbe.core.domain.dto.MailRemoteDto;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.EmailTemplate;
import com.kmkbe.core.domain.entity.ErrorLog;
import com.kmkbe.core.domain.model.BouwheerPaymentEmailPayload;
import com.kmkbe.core.domain.model.InvoiceEmailPayload;
import com.kmkbe.core.domain.model.LoanDisburseEmailPayload;
import com.kmkbe.core.domain.repository.EmailTemplateRepository;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailService {
    private static final int MAX_SENT_FAIL_ATTEMPTS = 2;
    private static final String EMAIL_FROM = "CSUL.Finance@csul.co.id";
    private static final int EMAIL_PRIORITY = 2;

    private static final String M_CUST_NEW_OTP = "M_CUST_NEW_OTP";
    private static final String M_CUST_CHANGE_OTP = "M_CUST_CHANGE_OTP";
    private static final String M_CUST_ACTIVE = "M_CUST_ACTIVE";
    private static final String M_CUST_LOAN = "M_CUST_LOAN";
    private static final String M_BRANCH_ASSIGN = "M_BRANCH_ASSIGN";
    private static final String M_BOUWHEER_PAYMENT = "M_BOUWHEER_PAYMENT";

    private final EmailTemplateRepository emailTemplateRepository;
    private final ConfigRemoteService configRemoteService;
    private final MailConfig mailConfig;
    private final ErrorLogRepository errorLogRepository;

    @Value("${testing.mail.host}")
    private String testingMailHost;

    @Value("${testing.mail.port}")
    private Integer testingMailPort;

    @Value("${testing.mail.username}")
    private String testingMailUsername;

    @Value("${testing.mail.password}")
    private String testingMailPassword;


    public EmailService(
            EmailTemplateRepository emailTemplateRepository,
            ConfigRemoteService configRemoteService,
            MailConfig mailConfig,
            ErrorLogRepository errorLogRepository
    ) {
        this.emailTemplateRepository = emailTemplateRepository;
        this.configRemoteService = configRemoteService;
        this.mailConfig = mailConfig;
        this.errorLogRepository = errorLogRepository;
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
    public void sendNotificationActive(Customer customer) {
        try {
            Map<String, Object> obj = new HashMap<>();
            obj.put("name", customer.getCustName());
            obj.put("id_no", customer.getCustIdNo());
            obj.put("email", customer.getCustEmail());

            send(customer.getCustEmail(), obj, M_CUST_ACTIVE);
        } catch (Exception e) {
            log.error("Error sendNotificationActive {}", e.getMessage());
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
            args.put("email", email);

            final EmailTemplate template = emailTemplateRepository
                    .findByEmailTemplateCodeAndIsActive(M_BRANCH_ASSIGN, true);
            template.setSubjectMail(template.getSubjectMail().replace("{bouwheerName}", bouwheerName));
            template.setMailTo(email);

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
                body = body.replace("{" + entry.getKey() + "}", entry.getValue().toString());
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
        helper.setTo(template.getMailTo());
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

    @LogMethod
    private boolean sendMailMessage(
            EmailTemplate template,
            String email
    ) {
        try {
            CsulMailSender csulMailSender = new CsulMailSender(mailConfig, configRemoteService);
            int attempts = 0;
            boolean success = false;
            for (int i = 0; i < MAX_SENT_FAIL_ATTEMPTS; i++) {
                try {
                    mailConfig.sendHtmlEmail(
                            csulMailSender.getInternalMail(),
                            template
                    );
                    success = true;
                } catch (Exception e) {
                    attempts++;
                    log.error("EmailService Failed to send email to {} due to {}", email, e.getMessage());
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
            log.error("sendMailMessage, error {}", e.getMessage());
            return false;
        }
    }


    @Getter
    private static class CsulMailSender {
        private final MailRemoteDto internalMail;
        private final JavaMailSender mailSender;

        private CsulMailSender(
                MailConfig mailConfig,
                ConfigRemoteService configRemoteService
        ) throws MessagingException {
            internalMail = configRemoteService.fetchEmailInfo();
            mailSender = mailConfig.javaMailSender(
                    internalMail.getServerUrl(),
                    internalMail.getPort(),
                    internalMail.getUsername(),
                    internalMail.getPassword(),
                    internalMail.getEnableSSL() != null && internalMail.getEnableSSL()
            );
        }
    }
}
