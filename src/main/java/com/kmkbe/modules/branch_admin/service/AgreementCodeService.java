package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.SitDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Bouwheer;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AgreementCodeService {

    @Autowired
    private AgreementRepository agreementRepository;

//    @Autowired
//    private GeneralSettingDtlRepository generalSettingDtlRepository;

    @Autowired
    private FinancingHdrRepository financingHdrRepository;

    public CommonResult<SitDto> getAgreementsByFinancingHdrCode(UUID financingHdrCode) {
        List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(financingHdrCode);
        if (agreements.isEmpty()) {
            return new CommonResult<SitDto>().fail(404  , "No agreement found for financingHdrCode: " + financingHdrCode);
        }

        // Mengambil elemen pertama dari hasil yang ada di list
        SitDto sitDto = agreements.stream().map(agreement -> {
            Bouwheer bouwheer = agreement.getFinancingHdr().getBouwheer();
            FinancingHdr financingHdr = agreement.getFinancingHdr();
            Customer customer = agreement.getFinancingHdr().getCustomer();

            String branchCode = financingHdrRepository.findBranchCodeByFinancingHdrCode(financingHdrCode);
            System.out.println("Branch Code: " + branchCode);

            String directorOrCustomerName = "Company".equals(customer.getCustTypeCode())
                    ? customer.getCustomerCompany().getDirectorName() // Ambil director_name dari CustomerCompany
                    : customer.getCustName();

            String gsDtlCode = "BANK001";

            String bankName = "Bank Mandiri";
            String accountName = "CHANDRA SAKTI UTAMA";
            String accountNo = "1270098142159";

            String findBranchCodeByFinancingHdrCode = branchCode;
            System.out.println("Branch Code ke 2: " + findBranchCodeByFinancingHdrCode);

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
                    .build();
        }).findFirst().orElse(null); // Mengambil elemen pertama, jika ada

        // Jika tidak ada hasil, kembalikan null
        if (sitDto == null) {
            return new CommonResult<SitDto>().fail(404, "No valid SitDto found.");
        }

        return new CommonResult<SitDto>().success(sitDto); // Mengirimkan objek SitDto langsung tanpa array
    }

}
