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
 * Base application.yaml memakai default aman; dev/local/brahma boleh lebih
 * verbose karena non-prod, namun env tidak boleh kembali ke prod.
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
  void baseConfigDefaultsToSafeEndpoints() throws Exception {
    String include = includeOf(loadYaml("src/main/resources/application.yaml"));

    // default env-var harus aman: tanpa env, loggers, logfile
    assertThat(include).isEqualTo("${MANAGEMENT_ENDPOINTS_INCLUDE:info,health}");
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
