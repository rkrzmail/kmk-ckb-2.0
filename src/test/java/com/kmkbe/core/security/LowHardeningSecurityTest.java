package com.kmkbe.core.security;

import com.kmkbe.modules.branch_admin.service.JasperReportRenderer;
import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LowHardeningSecurityTest {

    @Test
    @DisplayName("Hardening: JasperReportRenderer rejects path traversal in templatePath")
    void jasperReportRendererRejectsUntrustedTemplatePath() {
        JasperReportRenderer renderer = new JasperReportRenderer();

        // 1. Path traversal attempt
        assertThatThrownBy(() -> renderer.renderToPdf("../../../etc/passwd", new HashMap<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path traversal detected");

        // 2. Null template path
        assertThatThrownBy(() -> renderer.renderToPdf(null, new HashMap<>()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
