package com.kmkbe.modules.common.service;

import com.kmkbe.core.config.MailConfig;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.common.entity.EmailTemplate;
import com.kmkbe.modules.common.model.LoanDisburseEmailPayload;
import com.kmkbe.modules.common.repository.EmailTemplateRepository;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.remote.service.CsulConfigService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    private final EmailTemplateRepository emailTemplateRepository;
    private final CsulConfigService csulConfigService;
    private final MailConfig mailConfig;

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
            CsulConfigService csulConfigService,
            MailConfig mailConfig
    ) {
        this.emailTemplateRepository = emailTemplateRepository;
        this.csulConfigService = csulConfigService;
        this.mailConfig = mailConfig;
    }

    @Async
    public void sendOtp(Customer customer, String otpCode) throws MessagingException {
        send(customer, Map.of("otp_code", otpCode), M_CUST_NEW_OTP);
    }

    @Async
    public void sendOtpChangePin(Customer customer, String otpCode) throws MessagingException {
        send(customer, Map.of("otp_code", otpCode), M_CUST_CHANGE_OTP);
    }

    @Async
    public void sendNotificationActive(Customer customer) throws MessagingException {
        send(customer, null, M_CUST_ACTIVE);
    }

    @Async
    public void sendNotificationLoanDisbursement(
            final Customer customer,
            LoanDisburseEmailPayload payload
    ) throws MessagingException {
        final Map<String, Object> payloadArgs = ObjectUtils.objectToJson(payload);
        if (payloadArgs != null) {
            payloadArgs.remove("invoices");
            payloadArgs.put("invoices", LoanDisburseEmailPayload.InvoicePayload.toHtmlListBody(payload.getInvoices()));
        }

        send(customer, payloadArgs, M_CUST_LOAN);
    }

    private void send(
            final Customer customer,
            final Map<String, Object> additionalArgs,
            final String templateCode
    ) throws MessagingException {
        /*final CsulMailDto internalMail = csulConfigService.fetchEmailInfo();
        JavaMailSender mailSender = mailConfig.javaMailSender(
                internalMail.getServerUrl(),
                internalMail.getPort(),
                internalMail.getUsername(),
                internalMail.getPassword()
        );*/

        final EmailTemplate template = emailTemplateRepository
                .findByEmailTemplateCodeAndIsActive(templateCode, true);
        template.setMailTo(customer.getCustEmail());

        String body = template.getBodyMail();
        body = body.replace("{name}", customer.getCustName());
        body = body.replace("{email}", customer.getCustEmail());
        body = body.replace("{id_no}", customer.getCustIdNo());

        if (additionalArgs != null && additionalArgs.get("otp_code") != null) {
            body = body.replace("{otp_code}", additionalArgs.get("otp_code").toString());
        } else {
            body = body.replace("{otp_code}", "");
        }

        if (additionalArgs != null) {
            for (Map.Entry<String, Object> entry : additionalArgs.entrySet()) {
                body = body.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
        }

        template.setBodyMail(body);

        JavaMailSender mailSender = testingMailSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        mimeMessageTemplate(mimeMessage, template);

       /* int attempts = 0;
        boolean success = false;
        for (int i = 0; i < MAX_SENT_FAIL_ATTEMPTS; i++) {
            try {
                mailSender.send(mimeMessage);
                success = true;
            } catch (Exception e) {
                attempts++;
                log.error("EmailService Failed to send email to {} due to {}", customer.getCustEmail(), e.getMessage());
                log.error("EmailService try attempts: {}", attempts);
            }
        }*/

        // for testing purpose only
        /*if (!success) {
            log.info("EmailService send email with testing mail sender");
            mailSender = testingMailSender();
            mailSender.send(mimeMessage);
        }*/
        log.info("EmailService send email with testing mail sender");
        mailSender = testingMailSender();
        mailSender.send(mimeMessage);
    }

    // only for testing purpose and if failed to send using intenal mail
    private JavaMailSender testingMailSender() {
        return mailConfig.javaMailSender(
                testingMailHost,
                testingMailPort,
                testingMailUsername,
                testingMailPassword
        );
    }

    private MimeMessageHelper mimeMessageTemplate(MimeMessage mimeMessage, EmailTemplate template) throws MessagingException {
        return mimeMessageTemplate(mimeMessage, template, false);
    }

    private MimeMessageHelper mimeMessageTemplate(MimeMessage mimeMessage, EmailTemplate template, boolean multipart) throws MessagingException {
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
}
