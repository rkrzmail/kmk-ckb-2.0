package com.kmkbe.modules.common.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.common.entity.EmailTemplate;
import com.kmkbe.modules.common.repository.EmailTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Service
@AllArgsConstructor
public class EmailService {
    private static final String CSUL_LOGO_IMAGE = "static/images/csul_logo.png";
    private final EmailTemplateRepository emailTemplateRepository;
    private static final int EMAIL_PRIORITY = 2;
    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void send(EmailTemplate template) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            mimeMessageTemplate(mimeMessage, template);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            LOGGER.error("failed to send email", e);
            throw new IllegalStateException("failed to send email");
        }
    }

    @Async
    public void sendOtp(Customer customer, String otpCode) {
        try {
            final String templateCode = "MOTP1";
            final EmailTemplate template = emailTemplateRepository
                    .findByEmailTemplateCodeAndIsActive(templateCode, true);
            template.setMailTo(customer.getCustEmail());

            final Context context = new Context();
            context.setVariable("name", customer.getCustName());
            context.setVariable("email", customer.getCustEmail());
            context.setVariable("id_no", customer.getCustIdNo());
            context.setVariable("otp_code", otpCode);
            context.setVariable("csul_logo", CSUL_LOGO_IMAGE);

            final String body = templateEngine.process(template.getBodyMail(), context);
            template.setBodyMail(body);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            mimeMessageTemplate(mimeMessage, template);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new IllegalStateException("failed to send email");
        }
    }

    private MimeMessageHelper mimeMessageTemplate(MimeMessage mimeMessage, EmailTemplate template) throws MessagingException {
        return mimeMessageTemplate(mimeMessage, template, false);
    }

    private MimeMessageHelper mimeMessageTemplate(MimeMessage mimeMessage, EmailTemplate template, boolean multipart) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multipart, "utf-8");
        helper.setTo(template.getMailTo());
        helper.setSubject(template.getSubjectMail());
        helper.setText(template.getBodyMail(), true);

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
