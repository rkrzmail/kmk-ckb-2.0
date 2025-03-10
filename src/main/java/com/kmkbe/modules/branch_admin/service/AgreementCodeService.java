package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.SitDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Bouwheer;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    private AuthRemoteService authRemoteService;

    private String jwtToken;

    private void ensureJwtToken() {
        if (jwtToken == null) {
            jwtToken = authRemoteService.fetchAuthJwt().getData();
            System.out.println("JWT Token fetched: " + jwtToken);
        }
    }

    public CommonResult<SitDto> getAgreementsByFinancingHdrCode(UUID financingHdrCode) {
        ensureJwtToken();

        List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(financingHdrCode);
        if (agreements.isEmpty()) {
            return new CommonResult<SitDto>().fail(404, "No agreement found for financingHdrCode: " + financingHdrCode);
        }

        SitDto sitDto = agreements.stream().map(agreement -> {
            Bouwheer bouwheer = agreement.getFinancingHdr().getBouwheer();
            FinancingHdr financingHdr = agreement.getFinancingHdr();
            Customer customer = agreement.getFinancingHdr().getCustomer();

            String branchCode = financingHdrRepository.findBranchCodeByFinancingHdrCode(financingHdrCode);
            System.out.println("Branch Code: " + branchCode);

            List<Map<String, String>> employeeList = emailAo.getEmailByPosition(branchCode, "AO/AM", jwtToken);
//            String email = emailList.isEmpty() ? "N/A" : emailList.get(0).get("email");

            String employeeName = employeeList.isEmpty() ? "N/A" : toCamelCase(employeeList.get(0).get("employeeName"));

            String directorOrCustomerName = "Company".equals(customer.getCustTypeCode())
                    ? customer.getCustomerCompany().getDirectorName()
                    : customer.getCustName();

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
                    .totalInvoiceAmt(financingHdr.getTotalInvoiceAmt())
                    .financingDueDate(financingHdr.getFinancingDueDate())
                    .bankName(bankName)
                    .accountName(accountName)
                    .accountNo(accountNo)
                    .custName(customer.getCustName())
                    .DirectorName(directorOrCustomerName)
                    .BranchCode(branchCode)
//                    .Email(email)
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
