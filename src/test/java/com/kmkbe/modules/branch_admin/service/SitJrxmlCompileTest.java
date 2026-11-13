package com.kmkbe.modules.branch_admin.service;

import net.sf.jasperreports.engine.JasperCompileManager;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guard regresi: template Jasper SIT harus bisa dikompilasi.
 * Melindungi dari kesalahan sintaks ekspresi (mis. konkatenasi string
 * yang rusak pada nomor/tanggal perjanjian di sit.jrxml).
 */
class SitJrxmlCompileTest {

  @Test
  void sitTemplateCompilesWithoutErrors() throws Exception {
    try (InputStream in = getClass().getResourceAsStream("/Reports/sit.jrxml")) {
      assertThat(in).as("sit.jrxml tidak ditemukan di classpath").isNotNull();
      var report = JasperCompileManager.compileReport(in);
      assertThat(report.getName()).isNotBlank();
    }
  }
}
