package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.email.MailDataDto;
import com.kmkbe.core.domain.dto.email.MailPositionDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.utils.CommonUtils;
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
    String[] removeRBU = {"1", "2", "3", "4", "5", "ABU"," "};
    Map<String, Object> params = new HashMap<>();
    params.put("tanggalCwr", CommonUtils.capitalize(CommonUtils.safeRemoveAll(financing.getMstBranch().getBranchName(),removeRBU)).concat(", ").concat(CommonUtils.toDate(agreement.getDtmCrt())));
    params.put("namaBouwheer", CommonUtils.nullValue(financing.getBouwheer().getBouwheerName()));
    params.put("periodeBerlaku", CommonUtils.toDate(financing.getFinancingDueDate()));
    params.put("namaDebitur", CommonUtils.nullValue(financing.getCustomer().getCustName()));
    params.put("companyDebitur", CommonUtils.nullValue(financing.getCustomer().getCustName()));
    params.put("namaRegionalManager", regionalManager(financing));
    params.put("namaPicFinance", CommonUtils.nullValue(financing.getBouwheer().getPicName()));
    params.put("bouwheer", CommonUtils.nullValue(financing.getBouwheer().getBouwheerName()));
    params.put("legalAddress", CommonUtils.nullValue(financing.getBouwheer().getLegalAddress()));
    params.put("picName", CommonUtils.nullValue(financing.getBouwheer().getPicName()));
    params.put("tanggalPerjanjian", CommonUtils.toDate(agreement.getCwr().getCwrStartDate()));
    params.put("nomorPerjanjian", CommonUtils.nullValue(agreement.getCwr().getCwrCode()));
    return params;
  }

  private String regionalManager(FinancingHdr financing) {
    if (financing.getMstBranch() == null) return "";
    String branchCode = financing.getMstBranch().getBranchCode();
    try {
      MailPositionDto response = configRemoteService.getEmailByPosition("", branchCode, "RM");
      if (response == null || response.getData() == null) return "";
      return response.getData().stream()
        .map(MailDataDto::getEmployeeName)
        .filter(name -> name != null && !name.isBlank())
        .findFirst().orElse("");
    } catch (Exception e) {
      log.warn("Nama Regional Manager tidak tersedia untuk cabang {}: {}", branchCode, e.getMessage());
      return "";
    }
  }
}
