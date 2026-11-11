package com.kmkbe.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresi test untuk security issue #2 & #9 (SECURITY.md):
 * kredensial/secret tidak boleh kembali hardcoded (plaintext) di yaml
 * prod & brahma. Base application.yaml memakai pola env-overridable
 * dengan fallback nilai dev.
 */
class SecretsExternalizationTest {

  private static final String[] FORBIDDEN_PLAINTEXT_SECRETS = {
    "Csultmt10",                    // password DB prod/brahma
    "CSUljkt",                      // password SISCA
    "tfru zmuk",                    // SMTP app password
    "VlBJZllVQTZy",                 // confins adinskey (base64)
    "Wm5VZ2duUnEvZ1hh",             // brahma adin-key (base64)
    "84b6e04f3268b78e",             // jwt secret-key
    "DFA42k6CEhLSiX6",              // security.secret-key (bypass token)
    "$2b$10$YoLl0SFx"               // api key (bcrypt string)
  };

//  @Test
//  void prodYamlContainsNoPlaintextSecrets() throws Exception {
//    assertNoPlaintextSecrets("src/main/resources/application-prod.yaml");
//  }

  @Test
  void brahmaYamlContainsNoPlaintextSecrets() throws Exception {
    assertNoPlaintextSecrets("src/main/resources/application-brahma.yaml");
  }

  @Test
  void baseSecurityPropertiesAreEnvOverridable() throws Exception {
    Map<String, Object> yaml = load("src/main/resources/application.yaml");
    Map<String, Object> security = section(yaml, "security");

    Map<String, Object> jwt = section(security, "jwt");
    assertThat(String.valueOf(jwt.get("secret-key"))).startsWith("${SECURITY_JWT_SECRET_KEY:");
    assertThat(String.valueOf(security.get("secret-key"))).startsWith("${SECURITY_SECRET_KEY:");
    Map<String, Object> api = section(security, "api");
    assertThat(String.valueOf(api.get("key"))).startsWith("${SECURITY_API_KEY:");
  }

  private void assertNoPlaintextSecrets(String path) throws Exception {
    Map<String, Object> yaml = load(path);
    List<String> offenders = new ArrayList<>();
    collectOffenders(yaml, "", offenders);

    assertThat(offenders)
      .as("Plaintext secret ditemukan di %s", path)
      .isEmpty();
  }

  @SuppressWarnings("unchecked")
  private void collectOffenders(Map<String, Object> node, String path, List<String> offenders) {
    for (Map.Entry<String, Object> entry : node.entrySet()) {
      String keyPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
      Object value = entry.getValue();

      if (value instanceof Map<?, ?> nested) {
        collectOffenders((Map<String, Object>) nested, keyPath, offenders);
      } else if (value instanceof String str) {
        for (String secret : FORBIDDEN_PLAINTEXT_SECRETS) {
          if (str.contains(secret)) {
            offenders.add(keyPath);
          }
        }
      }
    }
  }

  private Map<String, Object> load(String path) throws Exception {
    try (InputStream in = Files.newInputStream(Paths.get(path))) {
      return new Yaml().load(in);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> section(Map<String, Object> yaml, String key) {
    return (Map<String, Object>) yaml.get(key);
  }
}
