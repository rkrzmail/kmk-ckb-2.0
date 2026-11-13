package com.kmkbe.modules.branch_admin.service;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTemplateDateTest {
  @Test
  void compiledBundleAndTransferLetterContainAgreementDateParameter() throws Exception {
    Path output = Path.of("target", "compiled-reports");
    Files.createDirectories(output);
    for (String name : new String[]{"main_report", "surat2"}) {
      Path source = Path.of("src", "main", "resources", "Reports", name + ".jrxml");
      Path compiled = output.resolve(name + ".jasper");
      JasperCompileManager.compileReportToFile(source.toString(), compiled.toString());
      JasperReport report = (JasperReport) JRLoader.loadObject(compiled.toFile());
      assertThat(report.getParameters()).extracting(JRParameter::getName)
        .contains("TglPengalihanPiutang");
    }
  }

  @Test
  void runtimeTemplatesKeepOtherDocumentDatesUnchanged() throws Exception {
    JasperReport main;
    JasperReport letter;
    try (var mainStream = getClass().getResourceAsStream("/Reports/main_report.jasper");
         var letterStream = getClass().getResourceAsStream("/Reports/surat2.jasper")) {
      main = (JasperReport) JRLoader.loadObject(mainStream);
      letter = (JasperReport) JRLoader.loadObject(letterStream);
    }
    JRSubreport first = (JRSubreport) main.getDetailSection().getBands()[0].getElements()[0];
    JRSubreport second = (JRSubreport) main.getDetailSection().getBands()[1].getElements()[0];
    JRSubreport third = (JRSubreport) main.getDetailSection().getBands()[2].getElements()[0];

    assertThat(parameterExpression(first, "TglDokumen")).isEqualTo("$P{TglPengalihanPiutang}");
    assertThat(parameterExpression(second, "TglPengalihanPiutang")).isEqualTo("$P{TglPengalihanPiutang}");
    assertThat(parameterExpression(second, "TglDokumen")).isEqualTo("$P{TglDokumen}");
    assertThat(parameterExpression(third, "TglDokumen")).isEqualTo("$P{TglDokumen}");
    assertThat(Arrays.stream(letter.getPageHeader().getElements())
      .filter(JRTextField.class::isInstance).map(JRTextField.class::cast)
      .map(field -> field.getExpression().getText()))
      .contains("$P{TglPengalihanPiutang}");
  }

  private String parameterExpression(JRSubreport report, String name) {
    return Arrays.stream(report.getParameters())
      .filter(parameter -> name.equals(parameter.getName()))
      .findFirst().orElseThrow().getExpression().getText();
  }
}
