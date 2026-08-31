package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.SitDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgreementCodeService {

    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private FinancingHdrRepository financingHdrRepository;

    @Autowired
    private EmailAo emailAo;

    @Autowired
    private DebtorRepository debtorRepository;

    @Autowired
    private AuthRemoteService authRemoteService;

    private String jwtToken;

    private void ensureJwtToken() {
        jwtToken = authRemoteService.fetchAuthJwt().getData();
    }

    public CommonResult<SitDto> getAgreementsByFinancingHdrCode(UUID financingHdrCode) {
        ensureJwtToken();

        List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(financingHdrCode);

        if (agreements.isEmpty()) {
            Optional<FinancingHdr> financingHdrOptional = financingHdrRepository.findByFinancingHdrCode(financingHdrCode);

            if (!financingHdrOptional.isPresent()) {
                return new CommonResult<SitDto>().fail(404, "No data found for financingHdrCode: " + financingHdrCode);
            }

            FinancingHdr financingHdr = financingHdrOptional.get();

            Customer customer = financingHdr.getCustomer();
//            String directorOrCustomerName = "Company".equals(customer.getCustTypeCode())
//                    ? customer.getCustomerCompany().getDirectorName()
//                    : customer.getCustName();

            String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode);

            List<Debtor> signerList = debtorRepository.findActiveSignerByDebtorName(debtorName);

            String signerName = signerList.isEmpty() ? "-" : signerList.get(0).getKaryawanName();
            String jabatan = signerList.isEmpty() ? "-" : signerList.get(0).getJabatan();

//            String signerName = debtorRepository
//                    .findActiveSignerByDebtorName(debtorName)
//                    .map(Debtor::getKaryawanName)
//                    .orElse("-");
//
//            String jabatan = debtorRepository
//                    .findActiveSignerByDebtorName(debtorName)
//                    .map(Debtor::getJabatan)
//                    .orElse("-");

            String branchCode = financingHdrRepository.findBranchCodeByFinancingHdrCode(financingHdrCode);
            List<Map<String, String>> employeeList = emailAo.getEmailByPosition(branchCode, "RM", jwtToken);
            String employeeName = employeeList.isEmpty() ? "N/A" : toCamelCase(employeeList.get(0).get("employeeName"));

            SitDto sitDto = SitDto.builder()
                    .custName(customer.getCustName())
                    .DirectorName(signerName)
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
                    .bankName("Bank Mandiri")
                    .accountName("CHANDRA SAKTI UTAMA")
                    .accountNo("1270098142159")
                    .build();

            return new CommonResult<SitDto>().success(sitDto);
        }

        SitDto sitDto = agreements.stream().map(agreement -> {
            Bouwheer bouwheer = agreement.getFinancingHdr().getBouwheer();
            FinancingHdr financingHdr = agreement.getFinancingHdr();
            Customer customer = agreement.getFinancingHdr().getCustomer();

            String branchCode = financingHdrRepository.findBranchCodeByFinancingHdrCode(financingHdrCode);

            List<Map<String, String>> employeeList = emailAo.getEmailByPosition(branchCode, "RM", jwtToken);
            String employeeName = employeeList.isEmpty() ? "N/A" : toCamelCase(employeeList.get(0).get("employeeName"));

//            String directorOrCustomerName = "Company".equals(customer.getCustTypeCode())
//                    ? customer.getCustomerCompany().getDirectorName()
//                    : customer.getCustName();

            String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode);

            List<Debtor> signerList = debtorRepository.findActiveSignerByDebtorName(debtorName);

            String signerName = signerList.isEmpty() ? "-" : signerList.get(0).getKaryawanName();
            String jabatan = signerList.isEmpty() ? "-" : signerList.get(0).getJabatan();

//            String signerName = debtorRepository
//                    .findActiveSignerByDebtorName(debtorName)
//                    .map(Debtor::getKaryawanName)
//                    .orElse("-");
//
//            String jabatan = debtorRepository
//                    .findActiveSignerByDebtorName(debtorName)
//                    .map(Debtor::getJabatan)
//                    .orElse("-");

            String bankName = "Bank Mandiri";
            String accountName = "CHANDRA SAKTI UTAMA";
            String accountNo = "1270098142159";

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
