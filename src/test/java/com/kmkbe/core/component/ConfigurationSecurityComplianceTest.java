package com.kmkbe.core.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationSecurityComplianceTest {

    @Test
    @DisplayName("C4 Compliance: application-template.yaml exists and contains no hardcoded DB passwords")
    void applicationTemplateExistsWithoutHardcodedSecrets() throws IOException {
        Path templatePath = Path.of("src/main/resources/application-template.yaml");
        assertThat(Files.exists(templatePath)).isTrue();

        String content = Files.readString(templatePath);
        // Memastikan nilai password dan secret menggunakan placeholder ${...} dan bukan plaintext bocor
        assertThat(content).doesNotContain("Csultmt10");
        assertThat(content).doesNotContain("DArS!OP#csul6?6Uu");
        assertThat(content).doesNotContain("tfru zmuk gtfb mrsz");
        assertThat(content).contains("${SPRING_DATASOURCE_PASSWORD:");
        assertThat(content).contains("${SPRING_DATASOURCE_URL:");
    }

    @Test
    @DisplayName("C4 Compliance: .env.example exists as guide for secret management")
    void envExampleExists() {
        Path envExamplePath = Path.of(".env.example");
        assertThat(Files.exists(envExamplePath)).isTrue();
    }

    @Test
    @DisplayName("C4 Compliance: .gitignore protects environment specific configs (application-*.yaml)")
    void gitignoreProtectsEnvironmentSpecificConfigs() throws IOException {
        Path gitignorePath = Path.of(".gitignore");
        assertThat(Files.exists(gitignorePath)).isTrue();

        List<String> lines = Files.readAllLines(gitignorePath);
        assertThat(lines).anyMatch(line -> line.trim().equals("src/main/resources/application-*.yaml"));
        assertThat(lines).anyMatch(line -> line.trim().equals("!src/main/resources/application-template.yaml"));
        assertThat(lines).anyMatch(line -> line.trim().equals(".env"));
    }

    @Test
    @DisplayName("C2 Compliance: Production and Brahma configs must not contain hardcoded plaintext DB password")
    void prodAndBrahmaConfigsDoNotContainPlaintextDbPassword() throws IOException {
        Path prodPath = Path.of("src/main/resources/application-prod.yaml");
        if (Files.exists(prodPath)) {
            String prodContent = Files.readString(prodPath);
            assertThat(prodContent).doesNotContain("password: Csultmt10");
            assertThat(prodContent).contains("${SPRING_DATASOURCE_PASSWORD:");
        }
    }

    @Test
    @DisplayName("C1 Compliance: RSA keys are configurable via env vars and private keys are ignored by git")
    void rsaKeysConfigurableAndPrivateKeysIgnored() throws IOException {
        Path prodPath = Path.of("src/main/resources/application-prod.yaml");
        if (Files.exists(prodPath)) {
            String prodContent = Files.readString(prodPath);
            assertThat(prodContent).contains("${RSA_PRIVATE_KEY:");
            assertThat(prodContent).contains("${RSA_PUBLIC_KEY:");
        }

        Path gitignorePath = Path.of(".gitignore");
        List<String> lines = Files.readAllLines(gitignorePath);
        assertThat(lines).anyMatch(line -> line.trim().equals("src/main/resources/certs/*.pem"));
        assertThat(lines).anyMatch(line -> line.trim().equals("!src/main/resources/certs/public-key.pem"));
    }

    @Test
    @DisplayName("H2 Compliance: Sensitive actuator endpoints not exposed by default, Admin client secured")
    void actuatorExposureAndAdminClientSecured() throws IOException {
        Path appYaml = Path.of("src/main/resources/application.yaml");
        assertThat(Files.exists(appYaml)).isTrue();
        String content = Files.readString(appYaml);

        assertThat(content).contains("${MANAGEMENT_ENDPOINTS_INCLUDE:info,health}");
        assertThat(content).doesNotContain("include: info, health, logfile, loggers, env");
        assertThat(content).contains("show-components: when-authorized");
        assertThat(content).doesNotContain("show-components: always");

        assertThat(content).contains("${SPRING_BOOT_ADMIN_CLIENT_ENABLED:false}");
        assertThat(content).contains("${SPRING_BOOT_ADMIN_PASSWORD:}");
        assertThat(content).doesNotContain("password: '12345'");

        assertThat(content).contains("${SECURITY_ACTUATOR_API_KEY:${security.api.key}}");
    }

    @Test
    @DisplayName("H3 Compliance: Swagger/OpenAPI (SpringDoc) must be disabled by default in production and brahma")
    void swaggerDisabledInProductionAndBrahmaConfigs() throws IOException {
        Path prodPath = Path.of("src/main/resources/application-prod.yaml");
        if (Files.exists(prodPath)) {
            String prodContent = Files.readString(prodPath);
            assertThat(prodContent).contains("api-docs:");
            assertThat(prodContent).contains("${SPRINGDOC_API_DOCS_ENABLED:false}");
            assertThat(prodContent).contains("swagger-ui:");
            assertThat(prodContent).contains("${SPRINGDOC_SWAGGER_UI_ENABLED:false}");
        }

        Path brahmaPath = Path.of("src/main/resources/application-brahma.yaml");
        if (Files.exists(brahmaPath)) {
            String brahmaContent = Files.readString(brahmaPath);
            assertThat(brahmaContent).contains("${SPRINGDOC_API_DOCS_ENABLED:false}");
            assertThat(brahmaContent).contains("${SPRINGDOC_SWAGGER_UI_ENABLED:false}");
        }

        Path templatePath = Path.of("src/main/resources/application-template.yaml");
        if (Files.exists(templatePath)) {
            String templateContent = Files.readString(templatePath);
            assertThat(templateContent).contains("${SPRINGDOC_API_DOCS_ENABLED:false}");
            assertThat(templateContent).contains("${SPRINGDOC_SWAGGER_UI_ENABLED:false}");
        }
    }

    @Test
    @DisplayName("H5 Compliance: External integration credentials (SISCA, Confins, e-sign, SBA) support dynamic env vars")
    void externalIntegrationCredentialsSupportEnvVars() throws IOException {
        Path prodPath = Path.of("src/main/resources/application-prod.yaml");
        if (Files.exists(prodPath)) {
            String prodContent = Files.readString(prodPath);
            assertThat(prodContent).contains("${TEST_MAIL_PASSWORD:");
            assertThat(prodContent).contains("${CSUL_SISCA_PASSWORD:");
            assertThat(prodContent).contains("${FEIGN_CONFINS_ADINS_KEY:");
            assertThat(prodContent).contains("${APP_API_KEY:");
            assertThat(prodContent).contains("${ESIGN_API_KEY:");
            assertThat(prodContent).contains("${SPRING_BOOT_ADMIN_PASSWORD:}");
            assertThat(prodContent).contains("${REDIS_PASSWORD:}");
            assertThat(prodContent).contains("${MONGO_PASSWORD:}");
        }

        Path appYaml = Path.of("src/main/resources/application.yaml");
        if (Files.exists(appYaml)) {
            String appContent = Files.readString(appYaml);
            assertThat(appContent).contains("${APP_API_KEY:");
            assertThat(appContent).contains("${ESIGN_API_KEY:");
        }
    }
}
