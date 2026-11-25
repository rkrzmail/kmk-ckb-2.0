package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.SitDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgreementCodeService {

  private final AgreementRepository agreementRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final EmailAo emailAo;
  private final DebtorRepository debtorRepository;
  private final AuthRemoteService authRemoteService;

  private String fetchJwtToken() {
    return authRemoteService.fetchAuthJwt().getData();
  }

  public CommonResult<SitDto> getAgreementsByFinancingHdrCode(UUID financingHdrCode) {
    String jwtToken = fetchJwtToken();

    List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(financingHdrCode);

    if (agreements.isEmpty()) {
      Optional<FinancingHdr> financingHdrOptional = financingHdrRepository.findByFinancingHdrCode(financingHdrCode);

      if (financingHdrOptional.isEmpty()) {
        return new CommonResult<SitDto>().fail(404, "No data found for financingHdrCode: " + financingHdrCode);
      }

      FinancingHdr financingHdr = financingHdrOptional.get();

      Customer customer = financingHdr.getCustomer();
      String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode);
      List<Debtor> signerList = debtorRepository.findActiveSignerByDebtorName(debtorName);
      String jabatan = signerList.isEmpty() ? "-" : signerList.getFirst().getJabatan();

      String branchCode = financingHdrRepository.findBranchCodeByFinancingHdrCode(financingHdrCode);
      List<Map<String, String>> employeeList = emailAo.getEmailByPosition(branchCode, "RM", jwtToken);
      String employeeName = employeeList.isEmpty() ? "N/A" : toCamelCase(employeeList.getFirst().get("employeeName"));

      SitDto sitDto = SitDto.builder()
        .custName(customer.getCustName())
        .DirectorName(customer.getCustomerCompany().getDirectorName())
        .Jabatan(jabatan)
        .BranchCode(branchCode)
        .EmployeeName(employeeName)
        .agreementCode("-")
        .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
        .legalAddress(financingHdr.getBouwheer().getLegalAddress())
        .rt(financingHdr.getBouwheer().getRt())
        .rw(financingHdr.getBouwheer().getRw())
        .kelurahan(financingHdr.getBouwheer().getKelurahan())
        .kecamatan(financingHdr.getBouwheer().getKecamatan())
        .city(financingHdr.getBouwheer().getCity())
        .province(financingHdr.getBouwheer().getProvince())
        .zipcode(financingHdr.getBouwheer().getZipcode())
        .area(financingHdr.getBouwheer().getArea())
        .picName(financingHdr.getBouwheer().getPicName())
        .bankName(AppConstants.BANK_NAME)
        .accountName(AppConstants.ACCOUNT_NAME)
        .accountNo(AppConstants.ACCOUNT_NO)
        .build();

      return new CommonResult<SitDto>().success(sitDto);
    }

    SitDto sitDto = agreements.stream().map(agreement -> {
      Bouwheer bouwheer = agreement.getFinancingHdr().getBouwheer();
      FinancingHdr financingHdr = agreement.getFinancingHdr();
      Customer customer = agreement.getFinancingHdr().getCustomer();

      String branchCode = financingHdrRepository.findBranchCodeByFinancingHdrCode(financingHdrCode);

      List<Map<String, String>> employeeList = emailAo.getEmailByPosition(branchCode, "RM", jwtToken);
      String employeeName = employeeList.isEmpty() ? "N/A" : toCamelCase(employeeList.getFirst().get("employeeName"));
      String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode);

      List<Debtor> signerList = debtorRepository.findActiveSignerByDebtorName(debtorName);

      String signerName = signerList.isEmpty() ? "-" : signerList.getFirst().getKaryawanName();
      String jabatan = signerList.isEmpty() ? "-" : signerList.getFirst().getJabatan();
      String bankName = AppConstants.BANK_NAME;
      String accountName = AppConstants.ACCOUNT_NAME;
      String accountNo = AppConstants.ACCOUNT_NO;

      return SitDto.builder()
        .agreementCode(agreement.getAgreementCode())
        .bouwheerName(bouwheer.getBouwheerName())
        .legalAddress(bouwheer.getLegalAddress())
        .rt(bouwheer.getRt())
        .rw(bouwheer.getRw())
        .kelurahan(bouwheer.getKelurahan())
        .kecamatan(bouwheer.getKecamatan())
        .city(bouwheer.getCity())
        .province(bouwheer.getProvince())
        .zipcode(bouwheer.getZipcode())
        .area(bouwheer.getArea())
        .picName(bouwheer.getPicName())
        .fapDate(financingHdr.getFapDate())
        .fapStatus(financingHdr.getFapStatus())
        .totalInvoiceAmt(financingHdr.getTotalInvoiceAmt())
        .financingDueDate(financingHdr.getFinancingDueDate())
        .bankName(bankName)
        .accountName(accountName)
        .accountNo(accountNo)
        .custName(customer.getCustName())
        .DirectorName(signerName)
        .Jabatan(jabatan)
        .BranchCode(branchCode)
        .EmployeeName(employeeName)
        .build();
    }).findFirst().orElse(null);

    if (sitDto == null) {
      return new CommonResult<SitDto>().fail(404, "No valid SitDto found.");
    }

    return new CommonResult<SitDto>().success(sitDto);
  }

  private String toCamelCase(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    text = text.toLowerCase();
    String[] words = text.split(" ");
    StringBuilder camelCaseText = new StringBuilder();

    for (String word : words) {
      if (!word.isEmpty()) {
        camelCaseText.append(Character.toUpperCase(word.charAt(0)))
          .append(word.substring(1))
          .append(" ");
      }
    }

    return camelCaseText.toString().trim();
  }
}
