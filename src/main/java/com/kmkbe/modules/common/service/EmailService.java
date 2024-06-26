package com.kmkbe.modules.common.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.common.entity.EmailTemplate;
import com.kmkbe.modules.common.repository.EmailTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class EmailService {
    private static final String EMAIL_FROM = "CSUL.Finance@csul.co.id";
    private static final int EMAIL_PRIORITY = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private static final String M_CUST_NEW_OTP = "M_CUST_NEW_OTP";
    private static final String M_CUST_CHANGE_OTP = "M_CUST_CHANGE_OTP";
    private static final String M_CUST_ACTIVE = "M_CUST_ACTIVE";


    private final EmailTemplateRepository emailTemplateRepository;
    private final JavaMailSender mailSender;

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

    private void send(
            final Customer customer,
            final Map<String, Object> additionalArgs,
            final String templateCode
    ) throws MessagingException {
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

        template.setBodyMail(body);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        mimeMessageTemplate(mimeMessage, template);

        mailSender.send(mimeMessage);
    }

    private MimeMessageHelper mimeMessageTemplate(MimeMessage mimeMessage, EmailTemplate template) throws MessagingException {
        return mimeMessageTemplate(mimeMessage, template, false);
    }

    private MimeMessageHelper mimeMessageTemplate(MimeMessage mimeMessage, EmailTemplate template, boolean multipart) throws MessagingException {
        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multipart, "utf-8");
        boolean isHtml = template.getBodyMail().contains("html");
        helper.setFrom(EMAIL_FROM);
        helper.setTo(template.getMailTo());
        helper.setSubject(template.getSubjectMail());
        helper.setText(template.getBodyMail(), isHtml);

        if (template.getMailCc() != null && !template.getMailCc().isEmpty()) {
            helper.setCc(template.getMailCc().split(";"));
        }
        if (template.getMailBcc() != null && !template.getMailBcc().isEmpty()) {
            helper.setBcc(template.getMailCc().split(";"));
        }

        helper.setPriority(EMAIL_PRIORITY);

        return helper;
    }
}
