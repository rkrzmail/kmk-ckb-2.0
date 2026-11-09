package com.kmkbe.modules.branch_admin.service;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SitJasperRenderTest {
  @Test
  void suppliedTemplateExportsPdfWithClientNameBinding() throws Exception {
    try (var stream = getClass().getResourceAsStream("/Reports/sit.jasper")) {
      JasperReport report = (JasperReport) JRLoader.loadObject(stream);
      assertThat(report.getParameters()).extracting(JRParameter::getName)
        .contains("companyDebitur", "namaDebitur");
    }

    byte[] pdf = new JasperReportRenderer().renderToPdf("/Reports/sit.jasper", new HashMap<>(Map.ofEntries(
      Map.entry("tanggalCwr", "20 Oktober 2026"),
      Map.entry("namaBouwheer", "Bouwheer Satu"),
      Map.entry("periodeBerlaku", "31 Desember 2026"),
      Map.entry("namaDebitur", "Direktur Debitur"),
      Map.entry("companyDebitur", "PT Debitur Satu"),
      Map.entry("namaRegionalManager", "Manajer Regional"),
      Map.entry("namaPicFinance", "PIC Satu"),
      Map.entry("bouwheer", "Bouwheer Satu"),
      Map.entry("legalAddress", "Jakarta"),
      Map.entry("picName", "PIC Satu"),
      Map.entry("tanggalPerjanjian", "26 Oktober 2026"),
      Map.entry("nomorPerjanjian", "AGR-1"))));

    assertThat(pdf).startsWith("%PDF-".getBytes());
    assertThat(pdf.length).isGreaterThan(1000);
  }
}
