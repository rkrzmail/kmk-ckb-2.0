package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.email.MailDataDto;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.service.ConfigRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SitTemplateService {
  private static final String TEMPLATE_PATH = "/Reports/sit.jasper";
  private static final DateTimeFormatter DATE_FORMAT =
    DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

  private final FinancingHdrRepository financingHdrRepository;
  private final AgreementRepository agreementRepository;
  private final ConfigRemoteService configRemoteService;
  private final JasperReportRenderer jasperReportRenderer;

  @Transactional(readOnly = true)
  public List<String> agreementCodes(UUID financingHdrCode) {
    if (financingHdrRepository.findByFinancingHdrCode(financingHdrCode).isEmpty()) {
      throw new BusinessException(HttpStatus.NOT_FOUND, 404, "Leads ID tidak ditemukan.");
    }
    return agreementRepository.findByFinancingHdr_FinancingHdrCode(financingHdrCode).stream()
      .map(Agreement::getAgreementCode)
      .filter(code -> code != null && !code.isBlank())
      .distinct()
      .sorted()
      .toList();
  }

  @Transactional
  public byte[] download(UUID financingHdrCode, String agreementCode) throws JRException, IOException {
    FinancingHdr financing = financingHdrRepository.findByFinancingHdrCode(financingHdrCode)
      .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 404, "Leads ID tidak ditemukan."));
    Agreement agreement = selectAgreement(financingHdrCode, agreementCode);
    if (financing.getBouwheer() == null || financing.getCustomer() == null) {
      throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, 422,
        "Data debitur atau pemberi kerja untuk dokumen SIT tidak tersedia.");
    }
    return jasperReportRenderer.renderToPdf(TEMPLATE_PATH, parameters(financing, agreement));
  }

  private Agreement selectAgreement(UUID financingHdrCode, String agreementCode) {
    List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(financingHdrCode);
    if (agreementCode != null && !agreementCode.isBlank()) {
      return agreements.stream().filter(item -> agreementCode.equals(item.getAgreementCode()))
        .findFirst().orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 404,
          "Nomor perjanjian tidak ditemukan untuk Leads ID ini."));
    }
    if (agreements.size() > 1) {
      throw new BusinessException(HttpStatus.CONFLICT, 409,
        "Leads ID memiliki lebih dari satu perjanjian. Sertakan agreementCode.");
    }
    if (agreements.isEmpty()) {
      throw new BusinessException(HttpStatus.NOT_FOUND, 404,
        "Perjanjian untuk Leads ID ini tidak ditemukan.");
    }
    return agreements.getFirst();
  }

  private Map<String, Object> parameters(FinancingHdr financing, Agreement agreement) {
    Bouwheer bouwheer = financing.getBouwheer();
    Customer customer = financing.getCustomer();
    Cwr cwr = agreement.getCwr();
    Map<String, Object> params = new HashMap<>();
    params.put("tanggalCwr", date(cwr == null ? null : cwr.getDtmCrt()));
    params.put("namaBouwheer", value(bouwheer.getBouwheerName()));
    params.put("periodeBerlaku", date(financing.getFinancingDueDate()));
    params.put("namaDebitur", value(customer.getCustName()));
    params.put("companyDebitur", value(customer.getCustName()));
    params.put("namaRegionalManager", regionalManager(financing));
    params.put("namaPicFinance", value(bouwheer.getPicName()));
    params.put("bouwheer", value(bouwheer.getBouwheerName()));
    params.put("legalAddress", value(bouwheer.getLegalAddress()));
    params.put("picName", value(bouwheer.getPicName()));
    params.put("tanggalPerjanjian", date(agreement.getDtmCrt()));
    params.put("nomorPerjanjian", value(agreement.getAgreementCode()));
    return params;
  }

  private String regionalManager(FinancingHdr financing) {
    if (financing.getMstBranch() == null) return "";
    String branchCode = financing.getMstBranch().getBranchCode();
    try {
      MailPositionDto response = configRemoteService.getEmailByPosition("", branchCode, "RM");
      if (response == null || response.getData() == null) return "";
      return response.getData().stream()
        .filter(person -> person.getBranchCode() == null
          || branchCode.equalsIgnoreCase(person.getBranchCode()))
        .map(MailDataDto::getEmployeeName)
        .filter(name -> name != null && !name.isBlank())
        .findFirst().orElse("");
    } catch (Exception e) {
      log.warn("Nama Regional Manager tidak tersedia untuk cabang {}: {}", branchCode, e.getMessage());
      return "";
    }
  }

  private static String value(String value) {
    return value == null ? "" : value;
  }

  private static String date(LocalDateTime value) {
    return value == null ? "" : DATE_FORMAT.format(value);
  }
}
