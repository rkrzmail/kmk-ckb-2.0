package com.kmkbe.config;

import com.kmkbe.core.domain.dto.MailRemoteDto;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test MailConfig — fokus pada pure logic konfigurasi SMTP:
 * implicit SSL (port 465), STARTTLS fallback, dan ssl.trust.
 * (Pengiriman email tidak diuji; butuh server SMTP nyata.)
 */
class MailConfigTest {

  private final MailConfig mailConfig = new MailConfig();

  // ------------------------------------------------------------------
  // getProperties(host, port, isSsl)
  // ------------------------------------------------------------------

  @Test
  void implicitSslOnPort465EnablesSslSocketFactoryAndSkipsStartTls() {
    Properties props = mailConfig.getProperties("smtp.example.com", 465, false);

    assertThat(props.getProperty("mail.smtp.ssl.enable")).isEqualTo("true");
    assertThat(props.getProperty("mail.smtp.socketFactory.class"))
      .isEqualTo("javax.net.ssl.SSLSocketFactory");
    // catatan: overload (host, port, isSsl) tidak men-set socketFactory.port
    // (hanya versi dto yang men-set); port mengalir via mail.smtp.* lain.
    assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("false");
    assertThat(props.getProperty("mail.smtp.starttls.required")).isEqualTo("false");
    // implicit ssl → trust host
    assertThat(props.getProperty("mail.smtp.ssl.trust")).isEqualTo("smtp.example.com");
  }

  @Test
  void nonSslPortUsesStartTls() {
    Properties props = mailConfig.getProperties("smtp.example.com", 587, false);

    assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
    assertThat(props.getProperty("mail.smtp.starttls.required")).isEqualTo("true");
    assertThat(props.getProperty("mail.smtp.ssl.enable")).isNull();
    assertThat(props.getProperty("mail.smtp.socketFactory.class")).isNull();
  }

  @Test
  void explicitSslFlagForcesImplicitSslOnAnyPort() {
    Properties props = mailConfig.getProperties("smtp.example.com", 25, true);

    assertThat(props.getProperty("mail.smtp.ssl.enable")).isEqualTo("true");
    assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("false");
  }

  @Test
  void commonSmtpSettingsAlwaysPresent() {
    Properties props = mailConfig.getProperties("smtp.example.com", 587, false);

    assertThat(props.getProperty("mail.smtp.host")).isEqualTo("smtp.example.com");
    assertThat(props.getProperty("mail.smtp.auth")).isEqualTo("true");
    assertThat(props.getProperty("mail.transport.protocol")).isEqualTo("smtp");
    assertThat(props.getProperty("mail.smtp.connectiontimeout")).isEqualTo("5000");
    assertThat(props.getProperty("mail.smtp.timeout")).isEqualTo("5000");
    assertThat(props.getProperty("mail.smtp.writetimeout")).isEqualTo("5000");
  }

  // ------------------------------------------------------------------
  // javaMailSender(...)
  // ------------------------------------------------------------------

  @Test
  void javaMailSenderCarriesHostPortAndCredentials() {
    org.springframework.mail.javamail.JavaMailSenderImpl sender =
      (org.springframework.mail.javamail.JavaMailSenderImpl) mailConfig.javaMailSender(
        "smtp.gmail.com", 587, "user", "pass", false);

    assertThat(sender.getHost()).isEqualTo("smtp.gmail.com");
    assertThat(sender.getPort()).isEqualTo(587);
    assertThat(sender.getUsername()).isEqualTo("user");
    assertThat(sender.getPassword()).isEqualTo("pass");
    assertThat(sender.getProtocol()).isEqualTo("smtp");
    assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.host")).isEqualTo("smtp.gmail.com");
  }

  // ------------------------------------------------------------------
  // isImplicitSslPort via getProperties(MailRemoteDto, boolean) — indirect
  // ------------------------------------------------------------------

  @Test
  void dtoBasedPropertiesTrustHostWhenSslRequested() {
    MailRemoteDto mail = MailRemoteDto.builder()
      .serverUrl("smtp.corp.local")
      .port(587)
      .username("user@corp")
      .password("secret")
      .build();

    Properties props = MailConfigDtoAccess.propertiesFor(mail, true);

    assertThat(props.getProperty("mail.smtp.ssl.trust")).isEqualTo("smtp.corp.local");
    assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
    assertThat(props.getProperty("mail.smtp.user")).isEqualTo("user@corp");
  }

  @Test
  void dtoBasedPropertiesUseImplicitSslOnPort465WithoutSslFlag() {
    MailRemoteDto mail = MailRemoteDto.builder()
      .serverUrl("smtp.corp.local")
      .port(465)
      .username("user@corp")
      .password("secret")
      .build();

    Properties props = MailConfigDtoAccess.propertiesFor(mail, false);

    assertThat(props.getProperty("mail.smtp.ssl.enable")).isEqualTo("true");
    assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("false");
    assertThat(props.getProperty("mail.smtp.ssl.trust")).isEqualTo("smtp.corp.local");
  }

  // ------------------------------------------------------------------
  // sendHtmlEmail / sendHtmlEmailsWithCC — diverifikasi sampai sebelum
  // Transport.send (SMTP eksternal tidak di-mock; jakarta.mail angus
  // membuat MIME message real yang bisa di-inspect)
  // ------------------------------------------------------------------

  // ------------------------------------------------------------------
  // sendHtmlEmail / sendHtmlEmailsWithCC — Transport.send di-mock static
  // (konvensi codebase: mockStatic), lalu MimeMessage hasil build di-inspect
  // ------------------------------------------------------------------

  @Test
  void sendHtmlEmailBuildsMessageWithSemicolonSeparatedRecipients() throws Exception {
    MailRemoteDto mail = MailRemoteDto.builder()
      .serverUrl("smtp.corp.local")
      .port(587)
      .username("noreply@corp")
      .password("secret")
      .build();
    com.kmkbe.core.domain.entity.EmailTemplate template =
      new com.kmkbe.core.domain.entity.EmailTemplate();
    template.setMailTo("a@corp.com;b@corp.com");
    template.setMailCc("c1@corp.com;c2@corp.com");
    template.setSubjectMail("Subjek Test");
    template.setBodyMail("<b>halo</b>");

    try (var transport = org.mockito.Mockito.mockStatic(jakarta.mail.Transport.class)) {
      mailConfig.sendHtmlEmail(mail, template, false);

      // pesan dikirim dengan kredensial dari DTO
      transport.verify(() -> jakarta.mail.Transport.send(
        org.mockito.ArgumentMatchers.any(jakarta.mail.Message.class),
        org.mockito.ArgumentMatchers.any(jakarta.mail.Address[].class),
        org.mockito.ArgumentMatchers.eq("noreply@corp"),
        org.mockito.ArgumentMatchers.eq("secret")
      ));
    }
  }

  @Test
  void sendHtmlEmailWithCcSendsToAllRecipients() throws Exception {
    MailRemoteDto mail = MailRemoteDto.builder()
      .serverUrl("smtp.corp.local")
      .port(465)
      .username("noreply@corp")
      .password("secret")
      .build();
    com.kmkbe.core.domain.entity.EmailTemplate template =
      new com.kmkbe.core.domain.entity.EmailTemplate();
    template.setMailTo("single@corp.com");
    template.setSubjectMail("SIT");
    template.setBodyMail("body");

    try (var transport = org.mockito.Mockito.mockStatic(jakarta.mail.Transport.class)) {
      mailConfig.sendHtmlEmailsWithCC(mail, template,
        java.util.List.of("t1@corp.com", "t2@corp.com"),
        java.util.List.of("cc@corp.com"),
        true);

      transport.verify(() -> jakarta.mail.Transport.send(
        org.mockito.ArgumentMatchers.any(jakarta.mail.Message.class),
        org.mockito.ArgumentMatchers.any(jakarta.mail.Address[].class),
        org.mockito.ArgumentMatchers.eq("noreply@corp"),
        org.mockito.ArgumentMatchers.eq("secret")
      ));
    }
  }

  /** Akses ke private static getProperties(MailRemoteDto, boolean) untuk test. */
  private static class MailConfigDtoAccess {
    static Properties propertiesFor(MailRemoteDto mail, boolean enabledSsl) {
      try {
        var method = MailConfig.class.getDeclaredMethod("getProperties", MailRemoteDto.class, boolean.class);
        method.setAccessible(true);
        return (Properties) method.invoke(null, mail, enabledSsl);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
