package com.kmkbe.core.config;

import com.kmkbe.core.domain.dto.MailRemoteDto;
import com.kmkbe.core.domain.entity.EmailTemplate;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Date;
import java.util.Properties;

@Configuration
public class MailConfig {

    public JavaMailSender javaMailSender(
            String host,
            Integer port,
            String user,
            String password,
            boolean isSsl
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(user);
        mailSender.setPassword(password);
        mailSender.setProtocol("smtp");

        Properties props = getProperties(host, port, isSsl);
        mailSender.setJavaMailProperties(props);

        /*Session session = Session.getDefaultInstance(props);
        Message msg = new MimeMessage(session);

        Transport t = session.getTransport("smtp");
        t.connect(user, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();*/

        return mailSender;
    }

    public Properties getProperties(String host, Integer port, boolean isSsl) {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", host);
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.starttls.enable", "true");

        if (isSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "true");
        }

        return props;
    }

    public void sendHtmlEmail(
            MailRemoteDto mail,
            EmailTemplate template
    ) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", mail.getServerUrl());
        properties.put("mail.smtp.port", mail.getPort());
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.user", mail.getUsername());
        properties.put("mail.smtp.ssl.trust", mail.getServerUrl());

        Session session = Session.getDefaultInstance(properties);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(mail.getUsername()));

        InternetAddress[] toAddresses = {new InternetAddress(template.getMailTo())};
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(template.getSubjectMail());
        msg.setSentDate(new Date());

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(template.getBodyMail(), "text/html");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        msg.setContent(multipart);

        Transport.send(
                msg,
                msg.getAllRecipients(),
                mail.getUsername(),
                mail.getPassword()
        );
    }
}
