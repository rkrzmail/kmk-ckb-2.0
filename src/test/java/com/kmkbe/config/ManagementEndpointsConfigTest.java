package com.kmkbe.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresi test untuk security issue #8 (SECURITY.md):
 * actuator endpoint sensitif (env, loggers) tidak boleh terekspos di prod.
 * Base application.yaml memakai profil local untuk diagnostik; profil prod
 * harus membatasi exposure meskipun base mengaktifkan endpoint tambahan.
 */
class ManagementEndpointsConfigTest {

  private Map<String, Object> loadYaml(String path) throws Exception {
    try (InputStream in = Files.newInputStream(Paths.get(path))) {
      return new Yaml().load(in);
    }
  }

  @SuppressWarnings("unchecked")
  private String includeOf(Map<String, Object> yaml) {
    Map<String, Object> management = (Map<String, Object>) yaml.get("management");
    Map<String, Object> endpoints = (Map<String, Object>) management.get("endpoints");
    Map<String, Object> web = (Map<String, Object>) endpoints.get("web");
    Map<String, Object> exposure = (Map<String, Object>) web.get("exposure");
    return String.valueOf(exposure.get("include"));
  }

  @Test
  void prodDoesNotExposeEnvOrLoggersOrLogfile() throws Exception {
    String include = includeOf(loadYaml("src/main/resources/application-prod.yaml"));

    assertThat(include).isEqualTo("info, health");
  }

  @Test
  @SuppressWarnings("unchecked")
  void baseConfigUsesNonProdProfileForDiagnosticEndpoints() throws Exception {
    Map<String, Object> yaml = loadYaml("src/main/resources/application.yaml");
    Map<String, Object> spring = (Map<String, Object>) yaml.get("spring");
    Map<String, Object> profiles = (Map<String, Object>) spring.get("profiles");

    assertThat(profiles.get("active")).isIn("local", "dev", "brahma");
    assertThat(includeOf(yaml)).contains("env", "loggers", "logfile");
  }

  @Test
  void brahmaDoesNotExposeEnvEndpoint() throws Exception {
    String include = includeOf(loadYaml("src/main/resources/application-brahma.yaml"));

    assertThat(include).doesNotContain("env");
  }

  @Test
  void devAndLocalKeepFullExposureForTroubleshooting() throws Exception {
    assertThat(includeOf(loadYaml("src/main/resources/application-dev.yaml"))).contains("env", "loggers");
    assertThat(includeOf(loadYaml("src/main/resources/application-local.yaml"))).contains("env", "loggers");
  }
}
