package com.kmkbe.modules.branch_admin.service;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public class JasperReportRenderer {

    public byte[] renderToPdf(String templatePath, Map<String, Object> params) throws JRException, IOException {
        try (InputStream reportStream = getClass().getResourceAsStream(templatePath)) {
            if (reportStream == null) {
                throw new FileNotFoundException(templatePath + " tidak ditemukan");
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
            JRDataSource dataSource = new JREmptyDataSource();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }
}
