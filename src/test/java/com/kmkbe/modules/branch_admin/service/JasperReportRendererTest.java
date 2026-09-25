package com.kmkbe.modules.branch_admin.service;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class JasperReportRendererTest {

    private final JasperReportRenderer renderer = new JasperReportRenderer();

    @Test
    void renderToPdfLoadsTemplateFillsReportAndExportsPdf() throws Exception {
        String templatePath = "/application.yaml";
        Map<String, Object> params = Map.of("agreementCode", "AGR001");
        byte[] expectedPdf = "pdf".getBytes();
        JasperReport jasperReport = mock(JasperReport.class);
        JasperPrint jasperPrint = mock(JasperPrint.class);

        try (MockedStatic<JRLoader> jrLoader = mockStatic(JRLoader.class);
             MockedStatic<JasperFillManager> fillManager = mockStatic(JasperFillManager.class);
             MockedStatic<JasperExportManager> exportManager = mockStatic(JasperExportManager.class)) {
            jrLoader.when(() -> JRLoader.loadObject(any(InputStream.class))).thenReturn(jasperReport);
            fillManager.when(() -> JasperFillManager.fillReport(eq(jasperReport), eq(params), any(JREmptyDataSource.class)))
                    .thenReturn(jasperPrint);
            exportManager.when(() -> JasperExportManager.exportReportToPdf(jasperPrint)).thenReturn(expectedPdf);

            byte[] actual = renderer.renderToPdf(templatePath, params);

            assertThat(actual).isSameAs(expectedPdf);
        }
    }

    @Test
    void renderToPdfThrowsWhenTemplateDoesNotExist() {
        assertThatThrownBy(() -> renderer.renderToPdf("/missing-template.jasper", Map.of()))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("/missing-template.jasper tidak ditemukan");
    }
}
