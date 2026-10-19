package com.kmkbe.core.component;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class CiScriptSecurityComplianceTest {

    @Test
    void sonarScriptDoesNotContainHardcodedToken() throws IOException {
        Path sonarPath = Paths.get("sonar.sh");
        if (Files.exists(sonarPath)) {
            String content = Files.readString(sonarPath);
            assertThat(content)
                    .doesNotContain("sqp_3afb6dccf2551b250c532469e7801a876a4c0f59")
                    .contains("${SONAR_TOKEN}");
        }
    }

    @Test
    void snykScriptDoesNotContainHardcodedOrgId() throws IOException {
        Path snykPath = Paths.get("snyk.sh");
        if (Files.exists(snykPath)) {
            String content = Files.readString(snykPath);
            assertThat(content)
                    .doesNotContain("8fca34cc-424b-49e8-80c5-aa264cccd87b")
                    .contains("${SNYK_ORG}");
        }
    }
}
